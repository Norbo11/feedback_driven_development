ALTER TABLE requests.profile
DROP COLUMN duration,
DROP COLUMN start_timestamp,
DROP COLUMN end_timestamp,
ADD COLUMN duration float NOT NULL,
ADD COLUMN start_timestamp timestamp NOT NULL,
ADD COLUMN end_timestamp timestamp NOT NULL