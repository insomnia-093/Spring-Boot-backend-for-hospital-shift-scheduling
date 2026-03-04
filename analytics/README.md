# Analytics Database

This project uses a separate PostgreSQL database to store aggregated analytics for dashboards.

## Create the analytics database

```sql
\i D:/hospital/hospital/analytics_init.sql
```

## Configure connection

Set environment variables (or override in `application.yml`):

- `ANALYTICS_DB_HOST` (default: `localhost`)
- `ANALYTICS_DB_PORT` (default: `5432`)
- `ANALYTICS_DB_NAME` (default: `hospital_analytics`)
- `ANALYTICS_DB_USER` (default: `postgres`)
- `ANALYTICS_DB_PASSWORD` (default: `123456`)

## Refresh analytics

Admin users can trigger a refresh manually:

```
POST /api/analytics/refresh?date=2026-03-04&month=2026-03
```
