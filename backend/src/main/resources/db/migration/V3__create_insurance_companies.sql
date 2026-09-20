CREATE TABLE IF NOT EXISTS insurance_companies (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  code VARCHAR(50) NOT NULL,
  contact_email VARCHAR(255) NULL,
  contact_phone VARCHAR(30) NULL,
  address VARCHAR(500) NULL,
  website VARCHAR(255) NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  CONSTRAINT uq_insurance_companies_code UNIQUE (code)
);

CREATE INDEX idx_insurance_companies_code ON insurance_companies (code);
CREATE INDEX idx_insurance_companies_name ON insurance_companies (name);
CREATE INDEX idx_insurance_companies_active ON insurance_companies (active);
