CREATE TABLE requests.application (
  name VARCHAR(60) PRIMARY KEY
);

ALTER TABLE requests.profile
ADD application_name VARCHAR(60) REFERENCES requests.application(name) NOT NULL default ''
