CREATE TABLE IF NOT EXISTS insurance_policies (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  policy_number VARCHAR(50) NOT NULL,
  customer_id BIGINT NOT NULL,
  insurance_company_id BIGINT NOT NULL,
  policy_type_id BIGINT NOT NULL,
  policy_name VARCHAR(200) NOT NULL,
  category VARCHAR(30) NOT NULL,
  start_date DATE NOT NULL,
  expiry_date DATE NOT NULL,
  premium_amount DECIMAL(15, 2) NOT NULL,
  premium_frequency VARCHAR(30) NULL,
  coverage_amount DECIMAL(15, 2) NULL,
  status VARCHAR(30) NOT NULL,
  nominee_name VARCHAR(255) NULL,
  nominee_contact VARCHAR(100) NULL,
  notes TEXT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  CONSTRAINT uq_policies_number UNIQUE (policy_number),
  CONSTRAINT fk_policies_customer FOREIGN KEY (customer_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_policies_company FOREIGN KEY (insurance_company_id) REFERENCES insurance_companies (id),
  CONSTRAINT fk_policies_type FOREIGN KEY (policy_type_id) REFERENCES policy_types (id)
);

CREATE INDEX idx_policies_number ON insurance_policies (policy_number);
CREATE INDEX idx_policies_customer ON insurance_policies (customer_id);
CREATE INDEX idx_policies_company ON insurance_policies (insurance_company_id);
CREATE INDEX idx_policies_type ON insurance_policies (policy_type_id);
CREATE INDEX idx_policies_status ON insurance_policies (status);
CREATE INDEX idx_policies_expiry ON insurance_policies (expiry_date);
CREATE INDEX idx_policies_customer_status ON insurance_policies (customer_id, status);
