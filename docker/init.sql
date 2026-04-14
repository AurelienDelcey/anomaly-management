USE anomaly;

CREATE TABLE IF NOT EXISTS anomalies (
  id CHAR(36) NOT NULL,
  parent_id CHAR(36) DEFAULT NULL,
  child_id CHAR(36) DEFAULT NULL,
  anomaly_state VARCHAR(10) NOT NULL,
  description TEXT NOT NULL,
  sector VARCHAR(100) NOT NULL,
  impacted_quantity INT DEFAULT NULL,
  production_order INT DEFAULT NULL,
  machine VARCHAR(50) DEFAULT NULL,
  corrective_action_id VARCHAR(20) DEFAULT NULL,
  quality_decision VARCHAR(10) NOT NULL,
  proving_document_id VARCHAR(20) DEFAULT NULL,
  created_by VARCHAR(20) NOT NULL,
  created_name VARCHAR(20) DEFAULT NULL,
  created_at DATETIME NOT NULL,
  corrected_by VARCHAR(20) DEFAULT NULL,
  corrected_name VARCHAR(20) DEFAULT NULL,
  corrected_at DATETIME DEFAULT NULL,
  resolved_by VARCHAR(20) DEFAULT NULL,
  resolved_name VARCHAR(20) DEFAULT NULL,
  resolved_at DATETIME DEFAULT NULL,
  archived_by VARCHAR(20) DEFAULT NULL,
  archived_name VARCHAR(20) DEFAULT NULL,
  archived_at DATETIME DEFAULT NULL,
  prolongation_comment VARCHAR(100) DEFAULT NULL,
  year INT DEFAULT NULL,
  sequence INT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY anomalies_business_id_unique (year, sequence)
);

CREATE TABLE IF NOT EXISTS anomalies_test LIKE anomalies;