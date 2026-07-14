CREATE TABLE attendance_correction_requests (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    attendance_record_id BIGINT NOT NULL REFERENCES attendance_records(id),
    requested_clock_in TIMESTAMP,
    requested_clock_out TIMESTAMP,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approver_id BIGINT REFERENCES employees(id),
    approved_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_correction_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE INDEX idx_correction_requests_status ON attendance_correction_requests (status);
CREATE INDEX idx_correction_requests_employee ON attendance_correction_requests (employee_id);
