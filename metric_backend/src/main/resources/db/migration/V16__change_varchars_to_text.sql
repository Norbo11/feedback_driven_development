ALTER TABLE requests.exception ALTER COLUMN exception_type TYPE TEXT;
ALTER TABLE requests.exception_frames ALTER COLUMN filename TYPE TEXT;
ALTER TABLE requests.exception_frames ALTER COLUMN function_name TYPE TEXT;
ALTER TABLE requests.application ALTER COLUMN name TYPE TEXT;
ALTER TABLE requests.profile_lines ALTER COLUMN file_name TYPE TEXT;