# Testing strategy

Tests are written as a specification an HR person could read: `answersTheAverageSalaryForAJobTitleInACountry`, `sortsSalariesByTheirUsdEquivalentNotTheirFaceValue`. They are fast and deterministic, with no network, no sleeps and a fixed clock.

| Layer | What | Tool | Count | Time |
|---|---|---|---|---|
| Domain (pure) | `SalaryStatistics` (Excel-compatible percentiles, rounding, overflow), `PayPosition` (compa-ratio bands), `EmployeeGenerator` (determinism, realism, unique emails), CSV escaping and formula-injection guard | JUnit 5, AssertJ | 31 | < 0.5 s |
| API (full stack) | Every endpoint through HTTP, against a real SQLite file built by the real Flyway migrations: validation, 404/409, salary-history rules, search, filters, sorting, paging, CSV, insights maths, SPA forwarding, seeding 10k rows | `@SpringBootTest` + `MockMvcTester` | 30 | ~ 3 s |
| UI logic | Money and date formatting (lakh grouping, "≈ USD"), pay-band geometry, URL state parsing, history deltas, HTTP params, suggestion matching | Vitest | 26 | < 1 s |
| UI components | Pay band rendering; employee form: validation, pay-change panel only when pay or country changes, emitted request, server field errors | Angular TestBed + Vitest | 7 | < 1 s |

**Why the API tests use a real database instead of mocks.** The interesting behaviour lives in SQL: LIKE escaping, case-insensitive unique email, sorting by a USD expression, cascade deletes. A mocked repository would test the mock. SQLite makes the real thing cheap: each Spring context gets its own throwaway file, every test runs in a rolled-back transaction, and all API test classes share one context that starts once.

**Test-first where it mattered.** The statistics and pay-position specs were committed failing before any implementation (`test: specify salary statistics and pay position`), then made to pass. Those two classes encode the numbers HR will check against Excel.

## Running

```bash
cd backend && ./mvnw test          # 61 tests
cd frontend && npx ng test --watch=false   # 33 tests
```

## Not covered (and why)

- **Browser end-to-end tests (Playwright).** The full flows (edit a salary, see history, delete) were checked by hand in a real browser during development. The next step would be one smoke test per screen. For a take-home, the cost of keeping those tests stable outweighed the value.
- **Load tests.** See [performance notes](05-performance.md) for measured timings at 10k rows.
