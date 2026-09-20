package com.insurance.platform.controller;

import com.insurance.platform.dto.common.ApiResponse;
import com.insurance.platform.dto.document.DocumentResponse;
import com.insurance.platform.service.PolicyDocumentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Flat document endpoints: download and delete by document id.
 */
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentDownloadController {

    private final PolicyDocumentService documentService;

    public DocumentDownloadController(PolicyDocumentService documentService) {
        this.documentService = documentService;
    }

    /** Downloads a document file after an ownership check. */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@AuthenticationPrincipal UserDetails principal,
                                             @PathVariable Long id) {
        Resource resource = documentService.download(principal.getUsername(), id);
        DocumentResponse meta = documentService.get(principal.getUsername(), id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + meta.getFileName() + "\"")
                .body(resource);
    }

    /** Deletes a document after an ownership check. */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal UserDetails principal,
                                                    @PathVariable Long id) {
        documentService.delete(principal.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.ok("Document deleted"));
    }
}
