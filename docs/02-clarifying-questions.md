# Clarifying questions and working assumptions

The brief asks us not to assume, so these are the questions I would put to the product owner before building. To avoid blocking, each one has the assumption the v1 build uses. Every assumption is cheap to change: most live in one place in the code, noted in the last column.

| # | Question | Working assumption | Where it lives |
|---|---|---|---|
| 1 | What does "salary" mean: annual or monthly, gross or net, base only or including allowances? | Annual gross base salary | `employee.annual_salary` |
| 2 | Is salary always paid in the employee's country currency? | Yes. One currency per country | `country.currency_code` |
| 3 | Does HR need cross-country totals in a single currency? Which one, and which exchange rates? | Yes, in USD, using fixed dated reference rates stored in the database | `country.units_per_usd` |
| 4 | Which pay questions matter most? | Min, max, average and median per country; average per job title within a country; headcount and payroll by location; peer comparison | Insights API |
| 5 | Must every salary change be auditable (old value, new value, effective date, reason)? | Yes | `salary_change` table |
| 6 | When someone leaves, should the record be deleted or kept with a status? | Delete is only for data-entry mistakes. Terminations are out of scope for v1 | `DELETE /api/employees/{id}` |
| 7 | Who uses the system? Only the HR manager, or also HRBPs, finance and managers with restricted views? | Only the HR manager, so v1 has no authentication | Out of scope (see requirements) |
| 8 | Should job titles be a controlled list (job architecture with levels) or free text? | Free text, with suggestions from existing titles | Employee form |
| 9 | Which countries are in scope? | 8 countries with 8 currencies, used by the seed data | `V2__reference_countries.sql` |
| 10 | Is there a sample of the current Excel sheet? Which columns does HR rely on? | Name, email, job title, department, country, salary, hire date | `V1__create_schema.sql` |
| 11 | What growth do we expect beyond 10,000 employees? | Design for 10k–100k. See [performance notes](05-performance.md) | n/a |
| 12 | Any hosting or data-residency constraints for the deployed demo? | No. Synthetic data only, reseeded when the database is empty | `fly.toml` |
