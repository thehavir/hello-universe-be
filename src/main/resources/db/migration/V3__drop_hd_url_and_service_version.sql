-- Correct the separate YouTube entry.
UPDATE public.apod
SET media_type = 'VIDEO'
WHERE date = DATE '2020-06-16'
  AND media_type = 'IMAGE'
  AND url LIKE 'https://www.youtube.com/embed/bhJ0MotUblo%';

-- Prefer HD URLs only when they point to image files.
-- The nine movie-file entries retain their current image URLs
-- and remain classified as IMAGE.
UPDATE public.apod
SET url = hd_url
WHERE media_type = 'IMAGE'
  AND hd_url ~* '[.](jpe?g|png|gif|webp|tiff?|bmp|avif|svg)([?#].*)?$';

-- Prevent losing unexpected HD links.
-- The nine deferred movie links are intentionally excluded:
-- both original URLs are saved in the media analysis file.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.apod
        WHERE NULLIF(btrim(hd_url), '') IS NOT NULL
          AND hd_url IS DISTINCT FROM url
          AND date NOT IN (
              DATE '2006-12-04',
              DATE '2007-01-20',
              DATE '2007-03-03',
              DATE '2007-03-12',
              DATE '2007-05-14',
              DATE '2007-08-29',
              DATE '2007-11-06',
              DATE '2007-12-04',
              DATE '2008-02-28'
          )
    ) THEN
        RAISE EXCEPTION
            'Unexpected hd_url would be lost. Review before dropping the column.';
    END IF;
END;
$$;

ALTER TABLE public.apod
    DROP COLUMN hd_url,
    DROP COLUMN service_version;
