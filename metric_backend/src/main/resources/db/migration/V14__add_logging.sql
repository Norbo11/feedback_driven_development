CREATE TABLE requests.logging_lines (
  id SERIAL PRIMARY KEY,
  profile_start_timestamp TIMESTAMP,
  line_number INT,
  filename TEXT,
  logger TEXT,
  level TEXT,
  message TEXT
);

-- Foreign keys into hypertable not supported...
-- ALTER TABLE requests.logging_lines ADD CONSTRAINT logging_lines_profile_start_timestamp_fkey FOREIGN KEY (profile_start_timestamp) REFERENCES
--     requests.profile(start_timestamp);
