-- Raw ingest table: Vector writes here.
-- Keeping this simple avoids any JSON parsing in Vector.
CREATE TABLE IF NOT EXISTS default.jct_raw
(
    ingest_time DateTime DEFAULT now(),
    raw_json    String
)
ENGINE = MergeTree
PARTITION BY toDate(ingest_time)
ORDER BY ingest_time
SETTINGS index_granularity = 8192;

-- Parsed events table: populated by the materialized view below.
-- All JSON extraction happens in ClickHouse at insert time.
CREATE TABLE IF NOT EXISTS default.jct_events
(
    ingest_time      DateTime,
    timestamp_millis UInt64,
    stack            Array(String),
    stack_depth      UInt16,
    top_frame        String,
    class_name       String,
    method_name      String,
    raw_json         String
)
ENGINE = MergeTree
PARTITION BY toDate(ingest_time)
ORDER BY (ingest_time, class_name, method_name)
SETTINGS index_granularity = 8192;

-- Materialized view: transforms raw JSON into typed columns on every insert.
CREATE MATERIALIZED VIEW IF NOT EXISTS default.jct_events_mv
TO default.jct_events
AS SELECT
    ingest_time,
    toUInt64OrDefault(JSONExtractString(raw_json, 'timestampMillis'), toUInt64(0)) AS timestamp_millis,
    JSONExtract(raw_json, 'stack', 'Array(String)')                                AS stack,
    toUInt16(length(JSONExtract(raw_json, 'stack', 'Array(String)')))              AS stack_depth,
    arrayElement(JSONExtract(raw_json, 'stack', 'Array(String)'), 1)        AS top_frame,
    replaceRegexpOne(
        arrayElement(JSONExtract(raw_json, 'stack', 'Array(String)'), 1),
        '^(.+)\\.[^.(]+\\(.*\\)$', '\\1')                                  AS class_name,
    replaceRegexpOne(
        arrayElement(JSONExtract(raw_json, 'stack', 'Array(String)'), 1),
        '^.+\\.([^.(]+\\(.*\\))$', '\\1')                                  AS method_name,
    raw_json
FROM default.jct_raw;

