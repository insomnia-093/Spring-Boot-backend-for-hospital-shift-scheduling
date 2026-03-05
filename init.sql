-- 统一初始化脚本（PostgreSQL）
-- 用途：创建 hospital_analytics 库的核心表结构 + 可视化测试数据
-- 说明：请先连接 postgres 库执行 CREATE DATABASE，再切换到 hospital_analytics 执行本文件其余内容。

-- CREATE DATABASE hospital_analytics;
-- \c hospital_analytics

BEGIN;

-- 部门表
CREATE TABLE IF NOT EXISTS departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL UNIQUE,
    description TEXT
);

-- 角色表
CREATE TABLE IF NOT EXISTS app_roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(160) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(160) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    department_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_users_department
        FOREIGN KEY (department_id)
        REFERENCES departments(id)
        ON DELETE SET NULL
);

-- 用户-角色关联表
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id)
        REFERENCES app_roles(id)
        ON DELETE CASCADE
);

-- 智能体沟通记录
CREATE TABLE IF NOT EXISTS agent_chat_messages (
    id BIGSERIAL PRIMARY KEY,
    sender VARCHAR(120) NOT NULL,
    role VARCHAR(40) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 排班表（与现有 users 直接关联，支持“已指派/待指派”统计）
CREATE TABLE IF NOT EXISTS shifts (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    required_role VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    department_id BIGINT NOT NULL,
    assigned_user_id BIGINT,
    notes VARCHAR(512),
    CONSTRAINT fk_shifts_department
        FOREIGN KEY (department_id)
        REFERENCES departments(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_shifts_assignee
        FOREIGN KEY (assigned_user_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);

-- 值班日历表
CREATE TABLE IF NOT EXISTS duty_calendar_entries (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,
    department_id BIGINT,
    summary VARCHAR(256) NOT NULL,
    headcount INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_duty_calendar_department
        FOREIGN KEY (department_id)
        REFERENCES departments(id)
        ON DELETE SET NULL
);

-- 常用索引
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_department ON users(department_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON user_roles(role_id);
CREATE INDEX IF NOT EXISTS idx_agent_chat_messages_time ON agent_chat_messages(timestamp);
CREATE INDEX IF NOT EXISTS idx_shifts_department ON shifts(department_id);
CREATE INDEX IF NOT EXISTS idx_shifts_assignee ON shifts(assigned_user_id);
CREATE INDEX IF NOT EXISTS idx_shifts_start_time ON shifts(start_time);
CREATE INDEX IF NOT EXISTS idx_shifts_status ON shifts(status);
CREATE INDEX IF NOT EXISTS idx_duty_calendar_date ON duty_calendar_entries(date);
CREATE INDEX IF NOT EXISTS idx_duty_calendar_dept ON duty_calendar_entries(department_id);

COMMIT;

-- =========================
-- 可视化测试数据（可重复执行）
-- =========================
BEGIN;

DELETE FROM duty_calendar_entries WHERE summary LIKE 'DEMO-%';
DELETE FROM shifts WHERE notes LIKE 'DEMO-%';
DELETE FROM user_roles WHERE user_id IN (
    SELECT id FROM users WHERE email LIKE 'demo_%@hospital.local'
);
DELETE FROM users WHERE email LIKE 'demo_%@hospital.local';
DELETE FROM app_roles WHERE name IN ('ADMIN', 'DOCTOR', 'NURSE');
DELETE FROM departments WHERE name IN ('呼吸内科', '心血管内科', '普通外科', '急诊科');

INSERT INTO departments (name, description) VALUES
('呼吸内科', 'DEMO-内科'),
('心血管内科', 'DEMO-内科'),
('普通外科', 'DEMO-外科'),
('急诊科', 'DEMO-急诊');

INSERT INTO app_roles (name) VALUES ('ADMIN'), ('DOCTOR'), ('NURSE');

-- password 为占位 bcrypt 串；若需要登录，请改成系统可识别的真实 hash
INSERT INTO users (email, password, full_name, enabled, department_id)
SELECT 'demo_admin@hospital.local', '$2a$10$demo.hash.placeholder', '演示管理员', TRUE, d.id
FROM departments d WHERE d.name = '急诊科';

INSERT INTO users (email, password, full_name, enabled, department_id)
SELECT 'demo_doc1@hospital.local', '$2a$10$demo.hash.placeholder', '张医生', TRUE, d.id
FROM departments d WHERE d.name = '呼吸内科';

INSERT INTO users (email, password, full_name, enabled, department_id)
SELECT 'demo_doc2@hospital.local', '$2a$10$demo.hash.placeholder', '李医生', TRUE, d.id
FROM departments d WHERE d.name = '呼吸内科';

INSERT INTO users (email, password, full_name, enabled, department_id)
SELECT 'demo_doc3@hospital.local', '$2a$10$demo.hash.placeholder', '王医生', TRUE, d.id
FROM departments d WHERE d.name = '心血管内科';

INSERT INTO users (email, password, full_name, enabled, department_id)
SELECT 'demo_doc4@hospital.local', '$2a$10$demo.hash.placeholder', '赵医生', TRUE, d.id
FROM departments d WHERE d.name = '普通外科';

INSERT INTO users (email, password, full_name, enabled, department_id)
SELECT 'demo_nurse1@hospital.local', '$2a$10$demo.hash.placeholder', '陈护士', TRUE, d.id
FROM departments d WHERE d.name = '急诊科';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, app_roles r
WHERE u.email = 'demo_admin@hospital.local' AND r.name = 'ADMIN';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, app_roles r
WHERE u.email IN (
    'demo_doc1@hospital.local',
    'demo_doc2@hospital.local',
    'demo_doc3@hospital.local',
    'demo_doc4@hospital.local'
) AND r.name = 'DOCTOR';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, app_roles r
WHERE u.email = 'demo_nurse1@hospital.local' AND r.name = 'NURSE';

-- 8条班次：白班4 + 夜班4；已指派6 + 待指派2
INSERT INTO shifts (version, start_time, end_time, required_role, status, department_id, assigned_user_id, notes)
SELECT 0, '2026-03-05 08:00:00', '2026-03-05 16:00:00', 'DOCTOR', 'ASSIGNED', d.id, u.id, 'DEMO-呼吸白班'
FROM departments d, users u WHERE d.name = '呼吸内科' AND u.email = 'demo_doc1@hospital.local';

INSERT INTO shifts (version, start_time, end_time, required_role, status, department_id, assigned_user_id, notes)
SELECT 0, '2026-03-05 16:00:00', '2026-03-05 23:59:00', 'DOCTOR', 'ASSIGNED', d.id, u.id, 'DEMO-呼吸夜班'
FROM departments d, users u WHERE d.name = '呼吸内科' AND u.email = 'demo_doc2@hospital.local';

INSERT INTO shifts (version, start_time, end_time, required_role, status, department_id, assigned_user_id, notes)
SELECT 0, '2026-03-06 08:00:00', '2026-03-06 16:00:00', 'DOCTOR', 'ASSIGNED', d.id, u.id, 'DEMO-心内白班'
FROM departments d, users u WHERE d.name = '心血管内科' AND u.email = 'demo_doc3@hospital.local';

INSERT INTO shifts (version, start_time, end_time, required_role, status, department_id, assigned_user_id, notes)
SELECT 0, '2026-03-06 16:00:00', '2026-03-06 23:59:00', 'DOCTOR', 'OPEN', d.id, NULL, 'DEMO-心内夜班待派'
FROM departments d WHERE d.name = '心血管内科';

INSERT INTO shifts (version, start_time, end_time, required_role, status, department_id, assigned_user_id, notes)
SELECT 0, '2026-03-07 08:00:00', '2026-03-07 16:00:00', 'DOCTOR', 'ASSIGNED', d.id, u.id, 'DEMO-外科白班'
FROM departments d, users u WHERE d.name = '普通外科' AND u.email = 'demo_doc4@hospital.local';

INSERT INTO shifts (version, start_time, end_time, required_role, status, department_id, assigned_user_id, notes)
SELECT 0, '2026-03-07 16:00:00', '2026-03-07 23:59:00', 'DOCTOR', 'ASSIGNED', d.id, u.id, 'DEMO-外科夜班'
FROM departments d, users u WHERE d.name = '普通外科' AND u.email = 'demo_doc4@hospital.local';

INSERT INTO shifts (version, start_time, end_time, required_role, status, department_id, assigned_user_id, notes)
SELECT 0, '2026-03-08 08:00:00', '2026-03-08 16:00:00', 'NURSE', 'ASSIGNED', d.id, u.id, 'DEMO-急诊白班'
FROM departments d, users u WHERE d.name = '急诊科' AND u.email = 'demo_nurse1@hospital.local';

INSERT INTO shifts (version, start_time, end_time, required_role, status, department_id, assigned_user_id, notes)
SELECT 0, '2026-03-08 16:00:00', '2026-03-08 23:59:00', 'NURSE', 'OPEN', d.id, NULL, 'DEMO-急诊夜班待派'
FROM departments d WHERE d.name = '急诊科';

INSERT INTO duty_calendar_entries (date, department_id, summary, headcount)
SELECT DATE '2026-03-05', d.id, 'DEMO-呼吸内科值班', 2 FROM departments d WHERE d.name = '呼吸内科';
INSERT INTO duty_calendar_entries (date, department_id, summary, headcount)
SELECT DATE '2026-03-06', d.id, 'DEMO-心内科值班', 1 FROM departments d WHERE d.name = '心血管内科';
INSERT INTO duty_calendar_entries (date, department_id, summary, headcount)
SELECT DATE '2026-03-07', d.id, 'DEMO-外科值班', 2 FROM departments d WHERE d.name = '普通外科';
INSERT INTO duty_calendar_entries (date, department_id, summary, headcount)
SELECT DATE '2026-03-08', d.id, 'DEMO-急诊值班', 1 FROM departments d WHERE d.name = '急诊科';

COMMIT;

-- =========================
-- 可视化校验查询
-- =========================
-- 总班次/已指派/待指派/夜班
SELECT
    COUNT(*) AS total_shifts,
    COUNT(*) FILTER (WHERE status = 'ASSIGNED') AS assigned_shifts,
    COUNT(*) FILTER (WHERE status = 'OPEN') AS pending_shifts,
    COUNT(*) FILTER (WHERE EXTRACT(HOUR FROM start_time) >= 16) AS night_shifts
FROM shifts
WHERE start_time >= '2026-03-01' AND start_time < '2026-04-01';

-- 班次类型占比（按白/夜）
SELECT
    CASE WHEN EXTRACT(HOUR FROM start_time) >= 16 THEN 'NIGHT' ELSE 'DAY' END AS shift_type,
    COUNT(*) AS cnt
FROM shifts
WHERE start_time >= '2026-03-01' AND start_time < '2026-04-01'
GROUP BY shift_type
ORDER BY shift_type;

-- 人员分布（本月）
SELECT u.full_name, COUNT(*) AS assigned_count
FROM shifts s
JOIN users u ON u.id = s.assigned_user_id
WHERE s.start_time >= '2026-03-01' AND s.start_time < '2026-04-01'
GROUP BY u.full_name
ORDER BY assigned_count DESC, u.full_name;

-- 科室分布（本月）
SELECT d.name AS department_name, COUNT(*) AS shift_count
FROM shifts s
JOIN departments d ON d.id = s.department_id
WHERE s.start_time >= '2026-03-01' AND s.start_time < '2026-04-01'
GROUP BY d.name
ORDER BY shift_count DESC, d.name;

