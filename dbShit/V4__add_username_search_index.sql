-- ════════════════════════════════════════════════════════════════════════════
-- V4__add_username_search_index.sql
--
-- Place in src/main/resources/db/migration/
--
-- The search query filters with  LOWER(username) LIKE LOWER(:prefix || '%').
-- Without a matching index, every search is a full table scan over `users`.
--
-- This functional index on LOWER(username) lets PostgreSQL use the index for
-- a case-insensitive PREFIX match (LIKE 'joh%'). The text_pattern_ops operator
-- class is what makes LIKE-prefix queries index-usable — a plain index would
-- NOT be used for LIKE because of how PostgreSQL collation works.
--
-- NOTE: prefix matching ('joh%') uses this index. A "contains" match
-- ('%joh%') canNOT use it — another reason we use prefix matching, not
-- substring matching, in the search query.
-- ════════════════════════════════════════════════════════════════════════════

CREATE INDEX IF NOT EXISTS users_username_lower_pattern_idx
    ON users (LOWER(username) text_pattern_ops);
