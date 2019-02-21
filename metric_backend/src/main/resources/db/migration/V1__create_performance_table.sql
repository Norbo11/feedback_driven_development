CREATE TABLE metrics.performance (
  file_name VARCHAR(100) NOT NULL,
  average_performance FLOAT NOT NULL,

  CONSTRAINT pk_performance PRIMARY KEY (file_name)
);