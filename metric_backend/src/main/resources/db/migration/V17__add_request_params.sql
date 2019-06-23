CREATE TABLE requests.request_params (
    id SERIAL PRIMARY KEY,
    request_start_timestamp TIMESTAMP WITHOUT TIME ZONE,
    name TEXT,
    value TEXT,
    type TEXT
)
