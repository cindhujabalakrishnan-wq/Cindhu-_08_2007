CREATE TABLE IF NOT EXISTS premium_payments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  policy_id BIGINT NOT NULL,
  amount DECIMAL(15, 2) NOT NULL,
  payment_date DATE NULL,
  due_date DATE NULL,
  payment_method VARCHAR(30) NULL,
  transaction_reference VARCHAR(100) NULL,
  status VARCHAR(30) NOT NULL,
  notes TEXT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  CONSTRAINT fk_payments_policy FOREIGN KEY (policy_id) REFERENCES insurance_policies (id) ON DELETE CASCADE
);

CREATE INDEX idx_payments_policy ON premium_payments (policy_id);
CREATE INDEX idx_payments_status ON premium_payments (status);
CREATE INDEX idx_payments_due_date ON premium_payments (due_date);
CREATE INDEX idx_payments_txn_ref ON premium_payments (transaction_reference);
