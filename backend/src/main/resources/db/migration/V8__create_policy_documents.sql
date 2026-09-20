CREATE TABLE IF NOT EXISTS policy_documents (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  policy_id BIGINT NOT NULL,
  uploaded_by_id BIGINT NULL,
  document_type VARCHAR(30) NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  original_file_name VARCHAR(255) NULL,
  file_path VARCHAR(1024) NOT NULL,
  content_type VARCHAR(100) NULL,
  file_size BIGINT NULL,
  extracted_text TEXT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  CONSTRAINT fk_documents_policy FOREIGN KEY (policy_id) REFERENCES insurance_policies (id) ON DELETE CASCADE,
  CONSTRAINT fk_documents_uploader FOREIGN KEY (uploaded_by_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_documents_policy ON policy_documents (policy_id);
CREATE INDEX idx_documents_type ON policy_documents (document_type);
CREATE INDEX idx_documents_uploaded_by ON policy_documents (uploaded_by_id);
