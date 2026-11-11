ALTER TABLE apod
    ADD COLUMN credit TEXT,
    ADD COLUMN source_url TEXT;

ALTER TABLE apod
    ALTER COLUMN copyright TYPE TEXT;

ALTER TABLE apod
    ALTER COLUMN service_version DROP NOT NULL;
