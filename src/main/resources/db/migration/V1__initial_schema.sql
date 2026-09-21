CREATE TABLE IF NOT EXISTS blocks (
    id BIGSERIAL PRIMARY KEY,
    block_number BIGINT NOT NULL UNIQUE,
    block_hash VARCHAR(255) NOT NULL UNIQUE,
    parent_hash VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    gas_limit NUMERIC(50,0),
    gas_used NUMERIC(50,0),
    base_fee NUMERIC(50,0),
    transaction_count INTEGER
);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_hash VARCHAR(255) NOT NULL UNIQUE,
    from_address VARCHAR(42) NOT NULL,
    to_address VARCHAR(42),
    value_wei NUMERIC(80,0),
    gas_price_wei NUMERIC(80,0),
    gas NUMERIC(50,0),
    nonce NUMERIC(50,0),
    block_number BIGINT NOT NULL,
    block_hash VARCHAR(255) NOT NULL,
    transaction_index INTEGER,
    input_data TEXT,
    transaction_type VARCHAR(50),
    timestamp TIMESTAMP NOT NULL,
    status VARCHAR(50),
    gas_used NUMERIC(50,0),
    eth_value DECIMAL(38,18),
    block_id BIGINT,
    CONSTRAINT fk_transactions_block FOREIGN KEY (block_id) REFERENCES blocks(id)
);

CREATE TABLE IF NOT EXISTS address_activity (
    id BIGSERIAL PRIMARY KEY,
    address VARCHAR(42) NOT NULL,
    transaction_count INTEGER,
    sent_transactions INTEGER,
    received_transactions INTEGER,
    unique_counterparties INTEGER,
    eth_sent DECIMAL(38,18),
    eth_received DECIMAL(38,18),
    first_seen TIMESTAMP NOT NULL,
    last_seen TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS observation_sessions (
    id BIGSERIAL PRIMARY KEY,
    session_start TIMESTAMP NOT NULL,
    last_updated TIMESTAMP,
    latest_block BIGINT,
    blocks_observed BIGINT,
    transactions_observed BIGINT,
    unique_wallets BIGINT,
    eth_volume_wei NUMERIC(80,0),
    repeated_addresses BIGINT,
    average_rpc_latency_ms DOUBLE PRECISION,
    average_block_interval_ms DOUBLE PRECISION
);

CREATE INDEX IF NOT EXISTS idx_blocks_number ON blocks(block_number);
CREATE INDEX IF NOT EXISTS idx_blocks_timestamp ON blocks(timestamp);
CREATE INDEX IF NOT EXISTS idx_tx_hash ON transactions(transaction_hash);
CREATE INDEX IF NOT EXISTS idx_tx_block_number ON transactions(block_number);
CREATE INDEX IF NOT EXISTS idx_tx_from ON transactions(from_address);
CREATE INDEX IF NOT EXISTS idx_tx_to ON transactions(to_address);
CREATE INDEX IF NOT EXISTS idx_tx_timestamp ON transactions(timestamp);
CREATE INDEX IF NOT EXISTS idx_address_activity_address ON address_activity(address);
CREATE INDEX IF NOT EXISTS idx_address_activity_last_seen ON address_activity(last_seen);
