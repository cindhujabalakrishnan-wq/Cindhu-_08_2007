package com.insurance.platform.service;

import com.insurance.platform.dto.document.DocumentResponse;
import com.insurance.platform.exception.BadRequestException;
import com.insurance.platform.exception.ForbiddenException;
import com.insurance.platform.exception.ResourceNotFoundException;
import com.insurance.platform.model.entity.InsurancePolicy;
import com.insurance.platform.model.entity.PolicyDocument;
import com.insurance.platform.model.entity.User;
import com.insurance.platform.model.enums.DocumentType;
import com.insurance.platform.model.enums.Role;
import com.insurance.platform.repository.PolicyDocumentRepository;
import com.insurance.platform.repository.UserRepository;
import com.insurance.platform.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Policy document uploads, downloads and deletion with ownership checks.
 */
@Service
public class PolicyDocumentService {

    private static final Logger log = LoggerFactory.getLogger(PolicyDocumentService.class);

    private final PolicyDocumentRepository documentRepository;
    private final PolicyService policyService;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public PolicyDocumentService(PolicyDocumentRepository documentRepository,
                                 PolicyService policyService,
                                 FileStorageService fileStorageService,
                                 UserRepository userRepository,
                                 AuditLogService auditLogService) {
        this.documentRepository = documentRepository;
        this.policyService = policyService;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    /** Uploads a document to an owned policy. */
    @Transactional
    public DocumentResponse upload(String email, Long policyId, MultipartFile file, String documentType) {
        InsurancePolicy policy = policyService.requireOwned(email, policyId);
        String storedName = fileStorageService.store(file);
        User uploader = userRepository.findByEmail(email).orElse(null);
        PolicyDocument document = new PolicyDocument();
        document.setPolicy(policy);
        document.setUploadedBy(uploader);
        document.setFileName(storedName);
        document.setOriginalFileName(file.getOriginalFilename());
        document.setContentType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setFilePath(fileStorageService.storagePath(storedName));
        document.setDocumentType(parseType(documentType));
        PolicyDocument saved = documentRepository.save(document);
        auditLogService.record(policy.getCustomer(), "UPLOAD", "PolicyDocument",
                String.valueOf(saved.getId()), "Document uploaded: " + saved.getFileName(), null);
        log.info("Document {} uploaded for policy {}", saved.getFileName(), policy.getPolicyNumber());
        return toResponse(saved);
    }

    /** Lists documents for an owned policy. */
    @Transactional(readOnly = true)
    public List<DocumentResponse> list(String email, Long policyId) {
        InsurancePolicy policy = policyService.requireOwned(email, policyId);
        return documentRepository.findByPolicyId(policy.getId()).stream().map(this::toResponse).toList();
    }

    /** Loads the file resource for download after an ownership check. */
    @Transactional(readOnly = true)
    public Resource download(String email, Long documentId) {
        PolicyDocument document = requireOwned(email, documentId);
        String storedName = java.nio.file.Paths.get(document.getFilePath()).getFileName().toString();
        return fileStorageService.load(storedName);
    }

    /** Fetches document metadata after an ownership check. */
    @Transactional(readOnly = true)
    public DocumentResponse get(String email, Long documentId) {
        return toResponse(requireOwned(email, documentId));
    }

    /** Deletes a document and its stored file. */
    @Transactional
    public void delete(String email, Long documentId) {
        PolicyDocument document = requireOwned(email, documentId);
        String storedName = java.nio.file.Paths.get(document.getFilePath()).getFileName().toString();
        fileStorageService.delete(storedName);
        documentRepository.delete(document);
        auditLogService.record(document.getPolicy().getCustomer(), "DELETE", "PolicyDocument",
                String.valueOf(documentId), "Document deleted: " + document.getFileName(), null);
    }

    private PolicyDocument requireOwned(String email, Long documentId) {
        PolicyDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));
        boolean admin = userRepository.findByEmail(email)
                .map(u -> u.getRole() == Role.ROLE_ADMIN).orElse(false);
        if (!admin) {
            policyService.requireOwned(email, document.getPolicy().getId());
        }
        return document;
    }

    private DocumentType parseType(String raw) {
        if (raw == null || raw.isBlank()) {
            return DocumentType.OTHER;
        }
        try {
            return DocumentType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid document type: " + raw);
        }
    }

    private DocumentResponse toResponse(PolicyDocument document) {
        DocumentResponse dto = new DocumentResponse();
        dto.setId(document.getId());
        if (document.getPolicy() != null) {
            dto.setPolicyId(document.getPolicy().getId());
        }
        dto.setFileName(document.getOriginalFileName() != null
                ? document.getOriginalFileName() : document.getFileName());
        dto.setFileType(document.getContentType());
        dto.setFileSize(document.getFileSize() != null ? document.getFileSize() : 0L);
        dto.setDocumentType(document.getDocumentType() != null ? document.getDocumentType().name() : null);
        dto.setUploadedAt(document.getCreatedAt());
        return dto;
    }
}
