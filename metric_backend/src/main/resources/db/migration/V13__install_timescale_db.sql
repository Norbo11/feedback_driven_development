-- To be run as super user:
-- psql postgres
-- \c feedback_driven_development
-- CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;

DROP TABLE requests.profile CASCADE;
CREATE TABLE requests.profile (
    start_timestamp TIMESTAMP PRIMARY KEY,
    end_timestamp TIMESTAMP NOT NULL,
    duration BIGINT NOT NULL,
    version TEXT NOT NULL,
    application_name TEXT REFERENCES requests.application(name)
);

ALTER TABLE requests.profile_lines DROP COLUMN profile_id;
ALTER TABLE requests.profile_lines ADD COLUMN profile_start_timestamp TIMESTAMP WITHOUT TIME ZONE;
-- ALTER TABLE requests.profile_lines ADD CONSTRAINT profile_lines_profile_start_timestamp_fkey FOREIGN KEY (profile_start_timestamp) REFERENCES
--     requests.profile(start_timestamp);

ALTER TABLE requests.exception DROP COLUMN profile_id;
ALTER TABLE requests.exception ADD COLUMN profile_start_timestamp TIMESTAMP WITHOUT TIME ZONE;
-- ALTER TABLE requests.exception ADD CONSTRAINT exception_profile_start_timestamp_fkey FOREIGN KEY (profile_start_timestamp) REFERENCES
--     requests.profile(start_timestamp);

SELECT create_hypertable('requests.profile', 'start_timestamp')