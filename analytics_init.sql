-- 可视化统计库初始化脚本（PostgreSQL）
-- 用途：存放聚合统计/看板数据，避免影响业务库

-- 1) 创建统计数据库
CREATE DATABASE hospital_analytics;

-- 2) 进入统计数据库后执行以下结构
-- \c hospital_analytics

-- 每日班次统计（全院 + 可选科室）
-- 约定：department_id = 0 表示全院汇总
CREATE TABLE IF NOT EXISTS shift_stats_daily (
    id BIGSERIAL PRIMARY KEY,
    stat_date DATE NOT NULL,
    department_id BIGINT,
    department_name VARCHAR(160),
    total_shifts INT NOT NULL DEFAULT 0,
    assigned_shifts INT NOT NULL DEFAULT 0,
    unassigned_shifts INT NOT NULL DEFAULT 0,
    night_shifts INT NOT NULL DEFAULT 0,
    day_shifts INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (stat_date, department_id)
);

-- 人员分布（按月）
CREATE TABLE IF NOT EXISTS assignee_stats_monthly (
    id BIGSERIAL PRIMARY KEY,
    stat_month DATE NOT NULL, -- 该月第一天
    assignee_user_id BIGINT NOT NULL,
    assignee_name VARCHAR(160),
    shift_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (stat_month, assignee_user_id)
);

-- 科室分布（按月）
CREATE TABLE IF NOT EXISTS department_stats_monthly (
    id BIGSERIAL PRIMARY KEY,
    stat_month DATE NOT NULL, -- 该月第一天
    department_id BIGINT NOT NULL,
    department_name VARCHAR(160),
    shift_count INT NOT NULL DEFAULT 0,
    night_shift_count INT NOT NULL DEFAULT 0,
    day_shift_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (stat_month, department_id)
);

-- 智能体任务统计（按日）
CREATE TABLE IF NOT EXISTS agent_task_stats_daily (
    id BIGSERIAL PRIMARY KEY,
    stat_date DATE NOT NULL,
    task_type VARCHAR(60) NOT NULL,
    status VARCHAR(40) NOT NULL,
    task_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (stat_date, task_type, status)
);

-- 常用索引
CREATE INDEX IF NOT EXISTS idx_shift_stats_daily_date ON shift_stats_daily(stat_date);
CREATE INDEX IF NOT EXISTS idx_shift_stats_daily_dept ON shift_stats_daily(department_id);
CREATE INDEX IF NOT EXISTS idx_assignee_stats_monthly_month ON assignee_stats_monthly(stat_month);
CREATE INDEX IF NOT EXISTS idx_department_stats_monthly_month ON department_stats_monthly(stat_month);
CREATE INDEX IF NOT EXISTS idx_agent_task_stats_daily_date ON agent_task_stats_daily(stat_date);
