CREATE TABLE IF NOT EXISTS policy_renewals (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  policy_id BIGINT NOT NULL,
  previous_expiry_date DATE NULL,
  new_expiry_date DATE NULL,
  renewal_premium DECIMAL(15, 2) NULL,
  status VARCHAR(30) NOT NULL,
  requested_at DATETIME NULL,
  processed_at DATETIME NULL,
  remarks TEXT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  CONSTRAINT fk_renewals_policy FOREIGN KEY (policy_id) REFERENCES insurance_policies (id) ON DELETE CASCADE
);

CREATE INDEX idx_renewals_policy ON policy_renewals (policy_id);
CREATE INDEX idx_renewals_status ON policy_renewals (status);
