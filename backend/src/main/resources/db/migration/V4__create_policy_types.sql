CREATE TABLE IF NOT EXISTS policy_types (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  code VARCHAR(50) NOT NULL,
  category VARCHAR(30) NOT NULL,
  description TEXT NULL,
  min_coverage DECIMAL(15, 2) NULL,
  max_coverage DECIMAL(15, 2) NULL,
  base_premium DECIMAL(15, 2) NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  CONSTRAINT uq_policy_types_code UNIQUE (code)
);

CREATE INDEX idx_policy_types_code ON policy_types (code);
CREATE INDEX idx_policy_types_category ON policy_types (category);
CREATE INDEX idx_policy_types_active ON policy_types (active);
