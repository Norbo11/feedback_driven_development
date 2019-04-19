
CREATE TABLE requests.exception (
  id SERIAL PRIMARY KEY,
  profile_id INT REFERENCES requests.profile(id),
  exception_type VARCHAR(100)
);

CREATE TABLE requests.exception_frames (
  id SERIAL PRIMARY KEY,
  exception_id INT REFERENCES requests.exception(id),
  filename VARCHAR(100),
  line_number INT,
  function_name VARCHAR(100),
  parent_id INT REFERENCES requests.exception_frames(id)
);
