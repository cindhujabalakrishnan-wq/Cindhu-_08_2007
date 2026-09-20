package com.insurance.platform.controller;

import com.insurance.platform.dto.common.ApiResponse;
import com.insurance.platform.dto.document.DocumentResponse;
import com.insurance.platform.dto.document.ExtractionPreviewResponse;
import com.insurance.platform.service.PolicyDocumentExtractionService;
import com.insurance.platform.service.PolicyDocumentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Policy document uploads, listing, download and deletion.
 */
@RestController
@RequestMapping("/api/v1/policies/{policyId}/documents")
public class PolicyDocumentController {

    private final PolicyDocumentService documentService;
    private final PolicyDocumentExtractionService extractionService;

    public PolicyDocumentController(PolicyDocumentService documentService,
                                    PolicyDocumentExtractionService extractionService) {
        this.documentService = documentService;
        this.extractionService = extractionService;
    }

    /** Lists documents for an owned policy. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> list(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long policyId) {
        return ResponseEntity.ok(ApiResponse.ok(documentService.list(principal.getUsername(), policyId)));
    }

    /** Uploads a document to an owned policy. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentResponse>> upload(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long policyId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "OTHER") String documentType) {
        return ResponseEntity.ok(ApiResponse.ok("Document uploaded",
                documentService.upload(principal.getUsername(), policyId, file, documentType)));
    }

    /** Returns a heuristic extraction preview without persisting anything. */
    @PostMapping(value = "/extract-preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ExtractionPreviewResponse>> extractPreview(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok(extractionService.extract(file)));
    }

    /**
     * Downloads a document file. Mapped under /api/v1/documents/{id}/download for
     * compatibility: this handler serves /api/v1/policies/{policyId}/documents/{id}/download.
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@AuthenticationPrincipal UserDetails principal,
                                             @PathVariable Long policyId,
                                             @PathVariable("id") Long documentId) {
        Resource resource = documentService.download(principal.getUsername(), documentId);
        DocumentResponse meta = documentService.get(principal.getUsername(), documentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + meta.getFileName() + "\"")
                .body(resource);
    }

    /** Deletes a document. */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal UserDetails principal,
                                                    @PathVariable Long policyId,
                                                    @PathVariable("id") Long documentId) {
        documentService.delete(principal.getUsername(), documentId);
        return ResponseEntity.ok(ApiResponse.ok("Document deleted"));
    }
}
