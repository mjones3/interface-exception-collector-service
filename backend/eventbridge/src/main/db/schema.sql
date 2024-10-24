CREATE SCHEMA eventbridge;

CREATE TABLE eventbridge.lk_event (
    id                BIGSERIAL                NOT NULL CONSTRAINT pk_lk_event PRIMARY KEY,
    event_type        VARCHAR(255)             NOT NULL,
    order_number      INTEGER DEFAULT 1        NOT NULL,
    active            BOOLEAN                  NOT NULL
);

CREATE UNIQUE INDEX uq_idx_lk_event_type ON eventbridge.lk_event (event_type);


