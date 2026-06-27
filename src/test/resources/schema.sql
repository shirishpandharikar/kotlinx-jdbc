CREATE TABLE IF NOT EXISTS users
(
    id     SERIAL PRIMARY KEY,
    name   VARCHAR(100),
    status VARCHAR(20),
    age    INT,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_status ON users (status);

CREATE TABLE IF NOT EXISTS time_entries
(
    id                  SERIAL PRIMARY KEY,
    instant_col         TIMESTAMPTZ,
    local_date_col      DATE,
    local_time_col      TIME,
    local_datetime_col  TIMESTAMP,
    offset_datetime_col TIMESTAMPTZ,
    zoned_datetime_col  TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS departments
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);


-- Added for testing various procedure/function signatures with CallableStatement

-- No parameters (void procedure)
CREATE OR REPLACE PROCEDURE refresh_user_count() AS $$
BEGIN
    -- simple side effect procedure
UPDATE users SET updated_at = NOW() WHERE id = 1;
END;
$$ LANGUAGE plpgsql;

-- IN parameters only
CREATE OR REPLACE PROCEDURE update_user_status(p_id INT, p_status TEXT) AS $$
BEGIN
UPDATE users SET status = p_status WHERE id = p_id;
END;
$$ LANGUAGE plpgsql;

-- OUT parameter only
CREATE OR REPLACE FUNCTION get_user_count(OUT total INT) AS $$
BEGIN
SELECT COUNT(*) INTO total FROM users;
END;
$$ LANGUAGE plpgsql;

-- IN + OUT parameters
CREATE OR REPLACE FUNCTION find_user_status(p_id INT, OUT p_status VARCHAR) AS $$
BEGIN
SELECT status INTO p_status FROM users WHERE id = p_id;
END;
$$ LANGUAGE plpgsql;

-- Multiple OUT parameters
CREATE OR REPLACE FUNCTION add_numbers(a INT, b INT, OUT sum INT, OUT product INT) AS $$
BEGIN
sum := a + b;
    product := a * b;
END;
$$ LANGUAGE plpgsql;

-- INOUT parameter
CREATE OR REPLACE FUNCTION increment_counter(INOUT counter INT) AS $$
BEGIN
    counter := counter + 1;
END;
$$ LANGUAGE plpgsql;

-- Returns result set (TABLE)
CREATE OR REPLACE FUNCTION get_active_users()
RETURNS TABLE(id INT, name VARCHAR(255), status VARCHAR(255), age INT) AS $$
BEGIN
RETURN QUERY SELECT u.id, u.name, u.status, u.age FROM users u WHERE u.status = 'ACTIVE';
END;
$$ LANGUAGE plpgsql;

-- Returns result set with IN parameter
CREATE OR REPLACE FUNCTION get_users_by_status(p_status TEXT)
RETURNS TABLE(id INT, name VARCHAR(255), status VARCHAR(255), age INT) AS $$
BEGIN
RETURN QUERY SELECT u.id, u.name, u.status, u.age FROM users u WHERE u.status = p_status;
END;
$$ LANGUAGE plpgsql;