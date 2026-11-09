CREATE SCHEMA IF NOT EXISTS extensions;

CREATE TABLE currency (
    code CHAR(3) PRIMARY KEY,
    name TEXT    NOT NULL
);

INSERT INTO currency (code, name) VALUES
    ('USD', 'US Dollar'),
    ('PHP', 'Philippine Peso');