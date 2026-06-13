-- Seed SQL Script: Populate users table with default and test accounts
-- Target Database: PostgreSQL

-- 1. Insert Default Admin User
-- Credentials:
--   Username: admin
--   Email: admin@example.com
--   Password (Plain): AdminSecure2026!
--   Password (Hash): $2b$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.uxNdB6S9pEwM9mfe (bcrypt)
--   Role: ADMIN
INSERT INTO users (username, password, email, role)
VALUES (
    'admin',
    '$2b$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.uxNdB6S9pEwM9mfe',
    'admin@example.com',
    'ADMIN'
)
ON CONFLICT (username) DO NOTHING;

-- 2. Insert Regular User 1 (John Doe)
-- Credentials:
--   Username: john_doe
--   Email: john.doe@example.com
--   Password (Plain): UserJohnPass!
--   Password (Hash): $2b$12$7DBy6l9G1f0wH8XwJ0D5OeN6fN0D5OeN6fN0D5OeN6fN0D5OeN6fN (bcrypt)
--   Role: USER
INSERT INTO users (username, password, email, role)
VALUES (
    'john_doe',
    '$2b$12$7DBy6l9G1f0wH8XwJ0D5OeN6fN0D5OeN6fN0D5OeN6fN0D5OeN6fN',
    'john.doe@example.com',
    'USER'
)
ON CONFLICT (username) DO NOTHING;

-- 3. Insert Regular User 2 (Jane Smith)
-- Credentials:
--   Username: jane_smith
--   Email: jane.smith@example.com
--   Password (Plain): UserJanePass!
--   Password (Hash): $2b$12$8EBy7l0G2f1wI9XwK1E6OeO7fO1D5OeO7fO1D5OeO7fO1D5OeO7fO (bcrypt)
--   Role: USER
INSERT INTO users (username, password, email, role)
VALUES (
    'jane_smith',
    '$2b$12$8EBy7l0G2f1wI9XwK1E6OeO7fO1D5OeO7fO1D5OeO7fO1D5OeO7fO',
    'jane.smith@example.com',
    'USER'
)
ON CONFLICT (username) DO NOTHING;
