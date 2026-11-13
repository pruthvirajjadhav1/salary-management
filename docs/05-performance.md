# Performance notes

Measured on a laptop (Apple Silicon, Java 21) against the seeded workforce: 10,000 employees and 44,139 salary-history rows in SQLite.

| Operation | Time | How |
|---|---|---|
| Seed 10k employees and their history | ~0.45 s | One transaction, JDBC batch inserts of 1,000 rows |
| Directory page: search + filter + sort + count | ~8 ms | Indexed filters; `LIKE` scan on 10k names |
| Employee profile with peer comparison | ~12 ms | Indexed `(country_code, job_title)` for peers |
| Org overview (all statistics, USD conversion) | ~70 ms cold, faster warm | One 4-column scan; grouping and percentiles in Java |
| CSV export of all 10k rows | ~120 ms | Streamed row by row; never fully held in memory |

## Design choices that keep it fast

- **Current salary is stored on the employee row** and also appended to `salary_change`. The directory and insights read one table, with no "latest row per employee" subqueries.
- **Filters only add the clauses that are set**, so SQLite can use `idx_employee_country_job_title` and `idx_employee_department`.
- **Sorting is whitelisted SQL** with a stable tiebreaker (`id`), so paging never shows a row twice.
- **Statistics in Java.** Sorting 10k `long`s takes about a millisecond. The query returns 4 small columns, not whole rows.
- **The UI pages on the server** (25 rows by default) and keeps state in the URL, so it never downloads the workforce.

## What changes at 100k and at 1M employees

| Pressure | At 100k | At 1M+ |
|---|---|---|
| Text search (`LIKE '%…%'`) | Still fine (tens of ms) | SQLite FTS5 or Postgres trigram index |
| `OFFSET` paging deep pages | Acceptable | Keyset pagination on `(sort value, id)` |
| Insights scan + Java percentiles | ~0.5 s; cache per data version | Pre-aggregate on write, or `percentile_cont` in Postgres |
| Single SQLite writer | Fine for one HR team | Postgres; the SQL here is portable apart from `COLLATE NOCASE` |
