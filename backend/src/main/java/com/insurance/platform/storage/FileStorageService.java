package com.insurance.platform.storage;

import com.insurance.platform.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Local file storage with UUID filenames, type validation (PDF/JPG/JPEG/PNG) and max-size checks.
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private static final List<String> ALLOWED_TYPES =
            List.of("application/pdf", "image/jpeg", "image/jpg", "image/png");
    private static final List<String> ALLOWED_EXTENSIONS = List.of("pdf", "jpg", "jpeg", "png");

    private final Path rootLocation;
    private final long maxSizeBytes;

    public FileStorageService(
            @Value("${file.storage.location:${upload.path:./uploads}}") String location,
            @Value("${file.storage.max-size:${upload.max-size:10485760}}") long maxSizeBytes) {
        this.rootLocation = Paths.get(location).toAbsolutePath().normalize();
        this.maxSizeBytes = maxSizeBytes;
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not create upload directory: " + location, ex);
        }
    }

    /**
     * Stores a file after validating size and type; returns the generated safe filename.
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new BadRequestException("File exceeds maximum size of " + maxSizeBytes + " bytes");
        }
        String contentType = file.getContentType();
        String extension = StringUtils.getFilenameExtension(Objects.requireNonNull(file.getOriginalFilename(), "filename"));
        extension = extension != null ? extension.toLowerCase() : "";
        if ((contentType != null && !ALLOWED_TYPES.contains(contentType.toLowerCase()))
                || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Only PDF, JPG, JPEG and PNG files are allowed");
        }
        String safeName = UUID.randomUUID() + "." + extension;
        try (InputStream input = file.getInputStream()) {
            Files.copy(input, rootLocation.resolve(safeName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store file", ex);
        }
        log.info("Stored file {}", safeName);
        return safeName;
    }

    /** Loads a stored file as a resource. */
    public Resource load(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BadRequestException("File not found: " + filename);
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new BadRequestException("File not found: " + filename);
        }
    }

    /** Deletes a stored file if present. */
    public void delete(String filename) {
        try {
            Files.deleteIfExists(rootLocation.resolve(filename).normalize());
        } catch (IOException ex) {
            log.warn("Could not delete file {}: {}", filename, ex.getMessage());
        }
    }

    /** Returns the absolute storage path for a filename. */
    public String storagePath(String filename) {
        return rootLocation.resolve(filename).normalize().toString();
    }
}
