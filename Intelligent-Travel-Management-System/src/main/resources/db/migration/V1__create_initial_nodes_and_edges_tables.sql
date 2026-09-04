CREATE TABLE IF NOT EXISTS nodes (
    id BIGSERIAL PRIMARY KEY,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS edges (
    id BIGSERIAL PRIMARY KEY,
    source_node_id BIGINT,
    target_node_id BIGINT,
    distance DOUBLE PRECISION,
    travel_time DOUBLE PRECISION
);
