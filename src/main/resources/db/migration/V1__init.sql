CREATE TABLE apod
(
    date            DATE         NOT NULL,
    title           VARCHAR(255) NOT NULL,
    explanation     TEXT         NOT NULL,
    media_type      VARCHAR(50)  NOT NULL,
    url             TEXT,
    hd_url          TEXT,
    thumbnail_url   TEXT,
    copyright       TEXT,
    service_version VARCHAR(50)  NOT NULL,
    PRIMARY KEY (date)
);
