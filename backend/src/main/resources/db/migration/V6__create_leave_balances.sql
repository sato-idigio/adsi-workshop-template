CREATE TABLE leave_balances (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    fiscal_year INTEGER NOT NULL,
    total_days DECIMAL(4,1) NOT NULL DEFAULT 20.0,
    used_days DECIMAL(4,1) NOT NULL DEFAULT 0.0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(employee_id, fiscal_year)
);
