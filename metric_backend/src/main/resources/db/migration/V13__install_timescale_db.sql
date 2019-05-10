-- To be run as super user:
-- psql postgres
-- \c feedback_driven_development
-- CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;

ALTER TABLE requests.profile RENAME TO profile_old;

CREATE TABLE requests.profile (LIKE requests.profile_old INCLUDING ALL EXCLUDING INDEXES);
ALTER TABLE requests.profile ADD CONSTRAINT profile_id_timestamp_unique UNIQUE (id, start_timestamp);
DROP TABLE requests.profile_old CASCADE;

-- ALTER TABLE requests.profile_lines DROP CONSTRAINT profile_lines_profile_id_fkey;
-- ALTER TABLE requests.profile_lines ADD CONSTRAINT profile_lines_profile_id_fkey FOREIGN KEY (profile_id) REFERENCES requests.profile(id) ON DELETE CASCADE ON
-- UPDATE CASCADE;
--
-- ALTER TABLE requests.exception DROP CONSTRAINT exception_profile_id_fkey;
-- ALTER TABLE requests.exception ADD CONSTRAINT exception_profile_id_fkey FOREIGN KEY (profile_id) REFERENCES requests.profile(id) ON DELETE CASCADE ON
--     UPDATE CASCADE;

CREATE SEQUENCE requests.profile_id_seq;
ALTER TABLE requests.profile ALTER COLUMN id SET DEFAULT nextval('requests.profile_id_seq'::regclass);

SELECT create_hypertable('requests.profile', 'start_timestamp', migrate_data => true)
