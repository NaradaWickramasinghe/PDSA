CREATE TABLE IF NOT EXISTS resource_options (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(32) NOT NULL,
    cost NUMERIC(10, 2) NOT NULL CHECK (cost >= 0),
    duration_hours NUMERIC(6, 2) NOT NULL CHECK (duration_hours >= 0),
    weight_kg NUMERIC(6, 2) NOT NULL CHECK (weight_kg >= 0),
    usefulness NUMERIC(6, 2) NOT NULL CHECK (usefulness >= 0),
    available BOOLEAN NOT NULL DEFAULT TRUE,
    transport_type VARCHAR(32),
    capacity INT CHECK (capacity IS NULL OR capacity >= 0),
    location_node_id BIGINT REFERENCES nodes(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_resource_options_available_category ON resource_options(available, category);

CREATE TABLE IF NOT EXISTS allocation_history (
    id BIGSERIAL PRIMARY KEY,
    algorithm_used VARCHAR(32) NOT NULL,
    feasible BOOLEAN NOT NULL,
    total_budget NUMERIC(10, 2) NOT NULL,
    emergency_reserve NUMERIC(10, 2) NOT NULL,
    max_available_hours NUMERIC(6, 2) NOT NULL,
    max_capacity_kg NUMERIC(6, 2) NOT NULL,
    traveller_count INT,
    total_cost NUMERIC(10, 2) NOT NULL,
    remaining_budget NUMERIC(10, 2) NOT NULL,
    total_time_used NUMERIC(6, 2) NOT NULL,
    remaining_time NUMERIC(6, 2) NOT NULL,
    total_weight NUMERIC(6, 2) NOT NULL,
    remaining_capacity NUMERIC(6, 2) NOT NULL,
    overall_score NUMERIC(8, 2) NOT NULL,
    execution_time_ms BIGINT NOT NULL,
    status_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_allocation_history_created_at ON allocation_history(created_at DESC);

CREATE TABLE IF NOT EXISTS allocation_history_items (
    allocation_id BIGINT NOT NULL REFERENCES allocation_history(id) ON DELETE CASCADE,
    resource_id VARCHAR(64) NOT NULL REFERENCES resource_options(id),
    PRIMARY KEY (allocation_id, resource_id)
);

CREATE INDEX IF NOT EXISTS idx_history_items_resource_id ON allocation_history_items(resource_id);
