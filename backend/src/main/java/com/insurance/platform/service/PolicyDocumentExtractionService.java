package com.insurance.platform.service;

import com.insurance.platform.dto.document.ExtractionPreviewResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Extracts structured hints from uploaded policy documents.
 */
public interface PolicyDocumentExtractionService {

    /** Extracts a preview from an uploaded file. */
    ExtractionPreviewResponse extract(MultipartFile file);

    /** Extracts a preview from raw bytes with a filename hint. */
    ExtractionPreviewResponse extractFromBytes(byte[] content, String filename);
}
