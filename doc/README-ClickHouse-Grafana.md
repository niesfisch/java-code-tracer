# JCT with ClickHouse + Vector + Grafana

This is an alternative to ELK for collecting, mining, and visualizing JCT stack events.

## Architecture

```
JCT agent  -->  /tmp/stacks/jct_*.log
                      |
                  Vector (tail + rename field)
                      |
            ClickHouse: default.jct_raw        (raw ingest table)
                      |
            ClickHouse: default.jct_events_mv  (materialized view)
                      |
            ClickHouse: default.jct_events     (parsed, queryable)
                      |
                  Grafana dashboards
```

JSON parsing happens entirely in ClickHouse via a materialized view — Vector only forwards raw lines.
This avoids VRL type-system complexity and keeps the Vector config minimal.

## Start the stack

Before bootstrapping with Docker Compose, create the host stack directory yourself. This prevents Docker from creating it as `root`.

```zsh
mkdir -p /tmp/stacks
chmod 775 /tmp/stacks
```

If you prefer a different path, set `JCT_STACKS_DIR` and use the same path in your JCT file processor config (`processor.stackFolderName`):

```zsh
export JCT_STACKS_DIR=/path/you/can/write
mkdir -p "$JCT_STACKS_DIR"
chmod 775 "$JCT_STACKS_DIR"
```

```zsh
cd /home/msauer/dev/workspace-private/java-code-tracer
docker compose -f docker-compose-clickhouse.yml up -d
```

Grafana:

- URL: `http://localhost:5601`
- User: `admin`
- Password: `admin`

DB viewer (ClickLens):

- URL: `http://localhost:3000`
- Login user: `default`
- Login password: *(empty)*
- These credentials are validated against ClickHouse.
- ClickHouse host: `clickhouse`
- ClickHouse port: `8123`
- Use this to run ad-hoc SQL against ClickHouse without curl.
- Hint: if `http://localhost:3000` does not open, verify the container is up with `docker compose -f docker-compose-clickhouse.yml ps`.

Browse tables in ClickLens:

1. Open `http://localhost:3000` and log in with `default` and an empty password.
2. Open the ClickHouse connection and select database `default`.
3. Expand `Tables` to see `jct_raw` (raw ingest) and `jct_events` (parsed events).
4. Open a table and use the data preview, or run SQL in the query editor, for example:

```sql
SELECT *
FROM default.jct_events
ORDER BY ingest_time DESC
LIMIT 100;
```

Dashboard provisioning:

- Folder: `JCT`
- Dashboard: `JCT Overview`
- Panels:
  - Throughput by Minute
  - Top Classes
  - Top Methods

If Grafana was already running before these files were added, restart the stack once:

```zsh
cd /home/msauer/dev/workspace-private/java-code-tracer
docker compose -f docker-compose-clickhouse.yml down
docker compose -f docker-compose-clickhouse.yml up -d
```

## Stop and reset

```zsh
cd /home/msauer/dev/workspace-private/java-code-tracer
docker compose -f docker-compose-clickhouse.yml down
docker volume rm java-code-tracer_clickhouse-data java-code-tracer_grafana-data
```

## Run your app with JCT (file processor)

Use a file-based config such as `doc/config-sample-application-file.yaml` and write to the same host path mounted into Vector (default: `/tmp/stacks`, or `JCT_STACKS_DIR` if set).

Important: make sure `processor.fullQualifiedClass` is set to `de.marcelsauer.profiler.processor.file.AsyncFileWritingStackProcessor`. This ClickHouse setup polls/tails log files from `/tmp/stacks`; UDP/TCP processors will not produce files for Vector to ingest.

## Verify ingestion quickly

```zsh
curl -s "http://localhost:8123/?query=SELECT%20count(*)%20FROM%20default.jct_events"
```

Verify dashboard data:

1. Open `http://localhost:5601`.
2. Go to `Dashboards` and open `JCT / JCT Overview`.
3. Select time range `Last 15 minutes` and confirm panels are populated.

## Useful ClickHouse queries

Run queries directly:

```zsh
curl -s "http://localhost:8123/?query=<URL-encoded SQL>"
```

Or open the ClickHouse Play UI at `http://localhost:8123/play`.

All stacks that contain a class or method with name "someMethodA" (case-insensitive)

```sql
SELECT *
FROM default.jct_events
WHERE arrayExists(frame -> lowerUTF8(frame) LIKE '%someMethodA%', stack);
```

Top classes in the last 15 minutes:

```sql
SELECT class_name, count(*) AS hits
FROM default.jct_events
WHERE ingest_time >= now() - INTERVAL 15 MINUTE
GROUP BY class_name
ORDER BY hits DESC
LIMIT 30;
```

Top methods in the last 15 minutes:

```sql
SELECT method_name, count(*) AS hits
FROM default.jct_events
WHERE ingest_time >= now() - INTERVAL 15 MINUTE
GROUP BY method_name
ORDER BY hits DESC
LIMIT 30;
```

Call frequency over time (1-minute buckets):

```sql
SELECT toStartOfMinute(ingest_time) AS minute, count(*) AS hits
FROM default.jct_events
WHERE ingest_time >= now() - INTERVAL 60 MINUTE
GROUP BY minute
ORDER BY minute;
```

Filter by class prefix:

```sql
SELECT ingest_time, class_name, method_name, stack_depth, stack
FROM default.jct_events
WHERE class_name LIKE 'de.marcelsauer.%'
ORDER BY ingest_time DESC
LIMIT 200;
```

Explode frames to count hot frames across all stacks:

```sql
SELECT frame, count(*) AS hits
FROM default.jct_events
ARRAY JOIN stack AS frame
WHERE ingest_time >= now() - INTERVAL 15 MINUTE
GROUP BY frame
ORDER BY hits DESC
LIMIT 50;
```

Check raw ingestion count:

```sql
SELECT count(*) FROM default.jct_raw;
SELECT count(*) FROM default.jct_events;
```

## Troubleshooting

**Dashboard is empty:**
- Check the Grafana time picker — set it to `Last 15 minutes` or wider.
- Verify events exist: `SELECT count(*) FROM default.jct_events WHERE ingest_time >= now() - INTERVAL 15 MINUTE`
- Confirm Vector is running: `docker compose -f docker-compose-clickhouse.yml ps`

**Vector exits on startup:**
- Check logs: `docker compose -f docker-compose-clickhouse.yml logs vector`
- Confirm the mounted stack directory exists and contains `jct_*.log` files (`/tmp/stacks` by default, or `JCT_STACKS_DIR`).
- If Compose reports bind-mount errors, create the host folder manually and ensure your app user can write there.

**ClickHouse authentication errors (403/516):**
- Ensure `doc/clickhouse/config/users.d/default-user.xml` is present and mounted.
- Recreate the stack: `docker compose -f docker-compose-clickhouse.yml down -v && docker compose -f docker-compose-clickhouse.yml up -d`

