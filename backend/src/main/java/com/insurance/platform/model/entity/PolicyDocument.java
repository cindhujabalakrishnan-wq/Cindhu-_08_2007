package com.insurance.platform.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.insurance.platform.model.enums.DocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A file stored against an {@link InsurancePolicy} (upload + optional PDF text extraction).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "policy_documents", indexes = {
    @Index(name = "idx_documents_policy", columnList = "policy_id"),
    @Index(name = "idx_documents_type", columnList = "document_type"),
    @Index(name = "idx_documents_uploaded_by", columnList = "uploaded_by_id")
})
public class PolicyDocument {

    /** Surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Policy this document belongs to. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private InsurancePolicy policy;

    /** User who uploaded the file. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id")
    private User uploadedBy;

    /** Classification of the document. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "document_type", nullable = false, length = 30)
    private DocumentType documentType;

    /** Stored file name on disk (unique per upload). */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /** Original client-side file name. */
    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    /** Absolute storage path of the file. */
    @Column(name = "file_path", nullable = false, length = 1024)
    private String filePath;

    /** MIME type reported at upload time. */
    @Column(name = "content_type", length = 100)
    private String contentType;

    /** File size in bytes. */
    @Column(name = "file_size")
    private Long fileSize;

    /** Text extracted from PDFs (nullable, potentially large). */
    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    /** Row creation timestamp. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Row last-update timestamp. */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Initialises audit timestamps on insert. */
    @PrePersist
    public void onPrePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    /** Refreshes the update timestamp on modification. */
    @PreUpdate
    public void onPreUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
