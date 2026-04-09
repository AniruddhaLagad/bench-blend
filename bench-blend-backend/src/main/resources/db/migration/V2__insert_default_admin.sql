-- Default admin user (password: admin@2003)
INSERT INTO admin_users (username, password, email, created_at, updated_at)
VALUES (
    'admin',
    '$2b$12$0t85nAWuKxwGFcl06rHt6Oup5CpJThNCXlZV9gu9BeIGmQYu0fWMa',
    'admin@benchblend.com',
    NOW(),
    NOW()
) ON CONFLICT (username) DO NOTHING;