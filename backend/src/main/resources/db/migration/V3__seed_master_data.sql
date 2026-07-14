-- 部署マスタ
INSERT INTO departments (id, name, parent_id, level) VALUES (1, '本社', NULL, 1);
INSERT INTO departments (id, name, parent_id, level) VALUES (2, '管理部', 1, 2);
INSERT INTO departments (id, name, parent_id, level) VALUES (3, '開発部', 1, 2);

-- 管理者アカウント (password: password123)
INSERT INTO employees (id, employee_number, last_name, first_name, email, password, department_id, position, role, hire_date)
VALUES (1, 'EMP001', '管理者', '太郎', 'admin@example.com',
        '$2a$10$6QhLnpNSjYasSakRHJ4hd.Wb8G9kspgtaGBZj2pPFF2nlKFokma16',
        2, '部長', 'ADMIN', '2020-04-01');

-- IDENTITYシーケンスを次の値にリセット
ALTER TABLE departments ALTER COLUMN id RESTART WITH 100;
ALTER TABLE employees ALTER COLUMN id RESTART WITH 100;
