CREATE SEQUENCE requests.profile_id_seq OWNED BY requests.profile.id;

CREATE SEQUENCE requests.profile_lines_id_seq OWNED BY requests.profile_lines.id;

ALTER TABLE requests.profile
ALTER COLUMN id SET DEFAULT nextval('requests.profile_id_seq');

ALTER TABLE requests.profile_lines
ALTER COLUMN id SET DEFAULT nextval('requests.profile_lines_id_seq');
