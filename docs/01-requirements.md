# ACME Salary Management: Requirements (v1)

**Persona:** HR Manager at ACME. 10,000 employees across several countries. Salary data lives in Excel workbooks today.

**Goal:** One trustworthy place to **(1) maintain employee salary records** and **(2) answer "how do we pay people?"** in seconds instead of rebuilding pivot tables.

## Why the spreadsheets hurt

- **Finding and changing records is slow and unvalidated.** Typos in job titles, wrong currencies and duplicate rows creep in.
- **There is no history.** Overwriting a cell loses the previous salary and the reason it changed.
- **Every pay question is a manual pivot table.** Cross-country totals silently add rupees to dollars.

## Success criteria

- Find any employee in under 2 seconds. Record a salary change in under 30 seconds.
- Answer the core pay questions in at most 2 clicks, with statistics that match what Excel would give (`MEDIAN`, `PERCENTILE.INC`).
- Every salary change is kept: old value, new value, effective date and reason.

## Questions the product must answer

1. What are the minimum, maximum, average and median salaries in a country?
2. What is the average salary for a job title in a country?
3. How many people do we employ where, and what does that cost in a single currency?
4. Is this person paid in line with their peers (same job title and country)?

## In scope

| Feature | What it does |
|---|---|
| Employee directory | Server-side search by name or email, filters (country, department, job title), sorting and pagination over 10k rows |
| Add, edit and remove employees | Validation: required fields, valid and unique email, positive salary, known country |
| Salary history | Every salary change stores an effective date and reason, shown as a timeline on the employee |
| Org pay overview | Headcount, annual payroll and median salary normalised to USD, plus payroll by department |
| Country pay breakdown | Min, P25, median, average, P75 and max in **local currency**, per country and per job title within a country |
| Peer comparison | Employee's compa-ratio (salary ÷ peer median) and position within the peer pay band |
| CSV export | Export the filtered directory, because HR will still use Excel for ad-hoc work |
| Seed script | Deterministic generator for 10,000 realistic employees |

## Deliberately out of scope

| Left out | Reasoning |
|---|---|
| Authentication and roles | There is one persona and the data is synthetic. This is the first thing to add before real data goes in (SSO, plus who-changed-what). |
| Bulk import from Excel | The most valuable next feature. A safe import needs a row-level validation and error-report UX; a rushed one corrupts the data this product exists to protect. |
| Payroll, tax and payslips | A different, regulated product. This tool manages salary data, not payments. |
| Bonus, equity and benefits | Base salary first. Total compensation needs its own model. |
| Live FX rates | Fixed, dated reference rates keep reports reproducible and avoid a runtime dependency. Every converted figure is labelled "≈ USD". |
| Pay-equity (gender) analysis | Needs protected attributes and an agreed methodology. That calls for HR, legal and data-protection input, not an assumption. |
| Org chart, performance reviews, approval workflows | Not needed to maintain or analyse salaries. |
| Employment status and terminations | "Remove" is a hard delete for data-entry mistakes. Offboarding needs status, retention rules and reporting by status. |

## Key assumptions

Each one is listed with the question behind it in [02-clarifying-questions.md](02-clarifying-questions.md).

- Salary means **annual gross base salary**, in whole units of the employee's country currency. Each country has one currency.
- One user role (the HR manager), working on a desktop browser.
- Job titles and departments are free text, with suggestions from existing values to keep them consistent.
