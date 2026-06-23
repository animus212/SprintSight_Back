-- ===========================================================================
-- extract_sprint_features(sprint_id)
--
-- Returns a JSONB document matching the FastAPI /predict request body:
--   { "sprint":      { plan_duration, no_issue, no_teammember },
--     "issues":      [ { no_component, no_comments, no_change_description,
--                        no_change_priority, no_change_fix, type, priority, text } ],
--     "developers":  [ { no_distinct_action, developer_activeness, most_prefer_type } ] }
--
-- Predicts AT SPRINT START: every history-based feature is cut off at the
-- sprint's start_date, so nothing that happens after the sprint begins leaks in.
-- Raw category names (type / priority / most_prefer_type) are returned as-is;
-- the Python service maps them onto the frozen training one-hot vocabulary.
--
-- Java usage (JDBC):
--   SELECT extract_sprint_features(?::uuid);   -- read the single jsonb/text column
--   then POST that JSON straight to the model API.
-- ===========================================================================

CREATE OR REPLACE FUNCTION extract_sprint_features(p_sprint_id uuid)
RETURNS jsonb
LANGUAGE plpgsql
STABLE
AS $$
DECLARE
    v_start_date date;
    v_end_date   date;
    v_cutoff     timestamptz;
    v_result     jsonb;
BEGIN
    SELECT s.start_date, s.end_date
      INTO v_start_date, v_end_date
      FROM sprints s
     WHERE s.id = p_sprint_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'sprint % not found', p_sprint_id;
    END IF;

    -- Prediction happens at the start of the sprint. Cut off all history at the
    -- start_date (fallback to now() if the sprint has no start_date set yet).
    v_cutoff := COALESCE(v_start_date::timestamptz, now());

    WITH scope AS (   -- issues planned into the sprint and not removed, as of cutoff
        SELECT si.issue_id
          FROM sprint_issues si
         WHERE si.sprint_id = p_sprint_id
           AND si.added_at <= v_cutoff
           AND (si.removed_at IS NULL OR si.removed_at > v_cutoff)
    ),
    issue_feat AS (
        SELECT
            i.id,
            (SELECT count(*) FROM issue_components ic WHERE ic.issue_id = i.id) AS no_component,
            (SELECT count(*) FROM comments c
              WHERE c.issue_id = i.id AND c.created_at <= v_cutoff)              AS no_comments,
            (SELECT count(*) FROM issue_events e
              WHERE e.issue_id = i.id AND e.changed_at < v_cutoff
                AND e.field_name = 'DESCRIPTION')                               AS no_change_description,
            (SELECT count(*) FROM issue_events e
              WHERE e.issue_id = i.id AND e.changed_at < v_cutoff
                AND e.field_name = 'PRIORITY')                                  AS no_change_priority,
            (SELECT count(*) FROM issue_events e
              WHERE e.issue_id = i.id AND e.changed_at < v_cutoff
                AND e.field_name = 'FIX_VERSION')                              AS no_change_fix,
            tc.name AS type,
            pc.name AS priority,
            trim(coalesce(i.title, '') || ' ' || coalesce(i.description, '')) AS text
          FROM issues i
          JOIN scope sc ON sc.issue_id = i.id
          LEFT JOIN issue_type_configs     tc ON tc.id = i.type_id
          LEFT JOIN issue_priority_configs pc ON pc.id = i.priority_id
    ),
    -- A developer "action" set, restricted to in-scope issues and the cutoff:
    --   field changes (issue_events) + comments + issue creation.
    -- (Assignment is captured via issue_events field_name='ASSIGNEE'.)
    dev_action AS (
        SELECT e.changed_by AS uid, e.issue_id, e.field_name AS action
          FROM issue_events e
          JOIN scope sc ON sc.issue_id = e.issue_id
         WHERE e.changed_at < v_cutoff AND e.changed_by IS NOT NULL
        UNION ALL
        SELECT c.user_id, c.issue_id, 'COMMENT'
          FROM comments c
          JOIN scope sc ON sc.issue_id = c.issue_id
         WHERE c.created_at <= v_cutoff AND c.user_id IS NOT NULL
        UNION ALL
        SELECT i.created_by, i.id, 'CREATE'
          FROM issues i
          JOIN scope sc ON sc.issue_id = i.id
         WHERE i.created_by IS NOT NULL AND i.created_at < v_cutoff
    ),
    dev_feat AS (
        SELECT uid,
               count(DISTINCT action)   AS no_distinct_action,
               count(DISTINCT issue_id) AS developer_activeness
          FROM dev_action
         GROUP BY uid
    ),
    dev_type_count AS (
        SELECT da.uid, tc.name AS type_name, count(*) AS c
          FROM dev_action da
          JOIN issues i               ON i.id = da.issue_id
          LEFT JOIN issue_type_configs tc ON tc.id = i.type_id
         GROUP BY da.uid, tc.name
    ),
    dev_pref AS (   -- modal issue type per developer (ties broken by name)
        SELECT DISTINCT ON (uid) uid, type_name AS most_prefer_type
          FROM dev_type_count
         ORDER BY uid, c DESC, type_name
    )
    SELECT jsonb_build_object(
        'sprint', jsonb_build_object(
            'plan_duration', GREATEST(COALESCE(v_end_date - v_start_date, 0), 0),
            'no_issue',      (SELECT count(*) FROM scope),
            'no_teammember', (SELECT count(*) FROM dev_feat)
        ),
        'issues', COALESCE((
            SELECT jsonb_agg(jsonb_build_object(
                'no_component',          no_component,
                'no_comments',           no_comments,
                'no_change_description', no_change_description,
                'no_change_priority',    no_change_priority,
                'no_change_fix',         no_change_fix,
                'type',                  type,
                'priority',              priority,
                'text',                  text
            )) FROM issue_feat), '[]'::jsonb),
        'developers', COALESCE((
            SELECT jsonb_agg(jsonb_build_object(
                'no_distinct_action',   f.no_distinct_action,
                'developer_activeness', f.developer_activeness,
                'most_prefer_type',     p.most_prefer_type
            ))
            FROM dev_feat f
            LEFT JOIN dev_pref p ON p.uid = f.uid), '[]'::jsonb)
    )
    INTO v_result;

    RETURN v_result;
END;
$$;
