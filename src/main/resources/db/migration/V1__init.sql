CREATE TABLE apod
(
    date            DATE         NOT NULL,
    title           VARCHAR(255) NOT NULL,
    explanation     TEXT         NOT NULL,
    media_type      VARCHAR(50)  NOT NULL,
    url             VARCHAR(255),
    hd_url          VARCHAR(255),
    thumbnail_url   VARCHAR(255),
    copyright       VARCHAR(255),
    service_version VARCHAR(50)  NOT NULL,
    PRIMARY KEY (date)
);
