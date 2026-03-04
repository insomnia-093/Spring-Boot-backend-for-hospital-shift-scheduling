-- 请在 PostgreSQL 中执行以下命令创建数据库
-- 命令行: psql -U postgres -c "CREATE DATABASE my_pg_
CREATE DATABASE my_pg_db;
CREATE DATABASE hospital_analytics;
-- 进入数据库后执行以下表结构
-- \c my_pg_db

-- 部门表 (可选)
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

-- 排班表
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
CREATE INDEX IF NOT EXISTS idx_duty_calendar_date ON duty_calendar_entries(date);
CREATE INDEX IF NOT EXISTS idx_duty_calendar_dept ON duty_calendar_entries(department_id);
