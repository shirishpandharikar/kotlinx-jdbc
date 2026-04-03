CREATE TABLE IF NOT EXISTS users
(
    id     SERIAL PRIMARY KEY,
    name   VARCHAR(100),
    status VARCHAR(20),
    age    INT
);

CREATE INDEX IF NOT EXISTS idx_users_status ON users (status);

CREATE TABLE IF NOT EXISTS departments
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);