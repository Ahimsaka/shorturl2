CREATE TABLE IF NOT EXISTS url_record (
    extension CHAR(8) PRIMARY KEY,
    url VARCHAR(250) UNIQUE,
    hits INT DEFAULT 0 NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(64),
    enabled BOOLEAN DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS roles (
    role_id INT PRIMARY KEY,
    name VARCHAR(64) NOT NULL
);

INSERT INTO roles (role_id, name) VALUES (0, 'ADMIN'), (1, 'USER');

CREATE TABLE IF NOT EXISTS users_roles (
    user_id INT REFERENCES users (user_id) ON UPDATE CASCADE ON DELETE CASCADE,
    role_id INT REFERENCES roles (role_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT user_role_pkey PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS users_url_records (
    user_id INT REFERENCES users (user_id) ON UPDATE CASCADE ON DELETE CASCADE,
    extension CHAR(8) REFERENCES url_record (extension) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT user_url_record_pkey PRIMARY KEY (user_id, extension)
);

