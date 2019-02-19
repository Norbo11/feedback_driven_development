CREATE SCHEMA requests;

CREATE TABLE requests.profile (
  id INT NOT NULL,
  duration VARCHAR(100) NOT NULL,
  start_timestamp VARCHAR(100) NOT NULL,
  end_timestamp FLOAT NOT NULL,
  CONSTRAINT pk_profile PRIMARY KEY (id)
);

CREATE TABLE requests.profile_lines (
  id INT NOT NULL,
  profile_id INT REFERENCES requests.profile(id) NOT NULL,
  file_name VARCHAR(100) NOT NULL,
  samples INT NOT NULL,
  CONSTRAINT pk_profile_lines PRIMARY KEY (id)
);
