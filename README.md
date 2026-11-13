# ACME People & Pay

Salary management for an HR manager at a 10,000-person company in 8 countries. It replaces the Excel workbooks with:

- **one place to maintain salaries**, where every change keeps its effective date and reason;
- **answers to "how do we pay people?" in two clicks**: min, median, average and max by country and by job title, payroll in USD, and each person's position against their peers.

**Live demo:** https://acme-salary-management-6h46.onrender.com (synthetic data; on Render's free tier the first request after idle can take ~30–60 s while the service wakes, and the database reseeds itself after a restart)

**Stack:** Spring Boot 4.1 (Java 21) · SQLite + Flyway · Angular 21 + PrimeNG 21 · Docker / Fly.io

## Start here

| Read | For |
|---|---|
| [docs/01-requirements.md](docs/01-requirements.md) | The one-page requirements: goal, scope, and what is deliberately left out and why |
| [docs/02-clarifying-questions.md](docs/02-clarifying-questions.md) | Questions for the product owner, and the assumption used until they are answered |
| [docs/03-architecture.md](docs/03-architecture.md) | Diagrams, data model, API, and the trade-off behind each technical decision |
| [docs/04-testing.md](docs/04-testing.md) | Testing strategy, 94 tests across both layers |
| [docs/05-performance.md](docs/05-performance.md) | Measured timings at 10k employees, and what changes at 100k and 1M |
| [docs/06-demo-script.md](docs/06-demo-script.md) | The path the demo video follows |
| [docs/07-ai-workflow.md](docs/07-ai-workflow.md) | How AI tools were used, and the mistakes they made that tests and review caught |
| [PRODUCT.md](PRODUCT.md) | Who the UI is for, and the product principles it follows |

The commit history is meant to be read in order. It goes requirements → design → failing tests → implementation → UI → packaging.

## Run it locally

Prerequisites: Java 21, Node 22.12+.

```bash
# 1. API on :8080. Seeds 10,000 employees on first start.
cd backend && ./mvnw spring-boot:run
```

```bash
# 2. UI on :4200, proxying /api to :8080
cd frontend && npm ci && npx ng serve
```

Open http://localhost:4200.

### Reseed the database

```bash
scripts/seed.sh            # wipe and seed 10,000 employees (deterministic)
scripts/seed.sh 50000      # any size
SEED=7 scripts/seed.sh     # a different, still reproducible, workforce
```

### Tests

```bash
cd backend && ./mvnw test                   # 61 tests: domain + full-stack API on SQLite
cd frontend && npx ng test --watch=false    # 33 tests: formatting, URL state, form behaviour
```

## Run it as one container

The image builds the UI, bundles it into the Spring Boot jar, and stores SQLite on `/data`.

```bash
docker build -t acme-pay .
docker run -p 8080:8080 -v acme-pay-data:/data acme-pay     # http://localhost:8080
```

### Deploy free on Render

`render.yaml` defines a free Docker web service. On render.com choose **New → Blueprint**, pick this repository, and apply. The first build takes a few minutes.

Free-plan trade-offs: the service sleeps after about 15 minutes idle, so the first request takes 30–60 s. The disk is not persistent, so the database returns to the seeded 10,000 employees after each restart; edits made in the demo do not survive it.

### Deploy on Fly.io (persistent volume, paid)

`fly.toml` is included:

```bash
fly launch --copy-config --no-deploy && fly volumes create salary_data --size 1 && fly deploy
```

## What it does

| Screen | Answers |
|---|---|
| **Pay overview** | Headcount, payroll and median in USD. Every country's pay range on one USD scale. Payroll by department. |
| **Country** | Min, median, average and max in local currency (₹ in lakh/crore), per job title, filterable by department |
| **People** | Search by name, email or ID. Filter, sort (salary compares USD equivalents), page and export CSV. All state lives in the URL. |
| **Profile drawer** | Current pay, compa-ratio and position in the peer band, salary history with % changes, details |
| **Add / edit** | Validation inline. A change to pay or country asks for an effective date and reason, and is appended to history. |

## API

`GET /api/employees` · `GET /api/employees/export` · `GET|PUT|DELETE /api/employees/{id}` · `POST /api/employees` · `GET /api/reference` · `GET /api/insights/overview` · `GET /api/insights/countries` · `GET /api/insights/countries/{code}/job-titles`. Errors are RFC 9457 `problem+json` with field-level `errors`. Details are in [the architecture doc](docs/03-architecture.md#rest-api).
