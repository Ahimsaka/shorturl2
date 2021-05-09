CREATE TABLE IF NOT EXISTS verification_token (
    token_id SERIAL PRIMARY KEY,
    token VARCHAR(250) UNIQUE,
    expiry_date TIMESTAMP,
    user_id INT REFERENCES users (user_id) ON UPDATE CASCADE ON DELETE CASCADE UNIQUE
);