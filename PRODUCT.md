# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

The HR manager at ACME, which has 10,000 employees across 8 countries. Today they maintain salary data in Excel workbooks. They work at a desk, in long sessions, switching between this tool and spreadsheets. Their two jobs are keeping salary records correct and answering leadership's questions about how the company pays people. (Inferred from the assessment brief, not from interviews.)

## Product Purpose

One trustworthy place to maintain employee salary records, with history, and to answer pay questions in seconds: min, max, average and median by country, average by job title within a country, payroll in one currency, and how an individual is paid against their peers. Success means HR stops rebuilding pivot tables, and the numbers reconcile with Excel.

## Positioning

The tool keeps statistics within a country in local currency and labels every cross-country figure as "≈ USD". Each person's pay is shown against their peer band (same title and country) as a compa-ratio, the way compensation teams reason, not just as raw numbers.

## Operating Context

- Desktop browser, daylight office.
- The workforce is synthetic: 10,000 seeded employees.
- CSV export round-trips to Excel.
- Exchange rates are fixed reference rates (illustrative, dated 2026-11-01).

## Capabilities and Constraints

- Directory with search, filters, sorting and pagination. Add, edit and remove employees.
- Every salary change keeps its effective date and reason. Org, country and job-title statistics, peer comparison and CSV export are supported.
- No authentication, roles, payroll, bonuses, equity, Excel import or pay-equity analysis in v1. See `docs/01-requirements.md`.
- Built with Angular and PrimeNG, served by Spring Boot.

## Brand Commitments

- ACME is a fictional company name from the brief. There is no logo or brand asset.
- The user asked for the look and feel of established HR and compensation platforms (Rippling, Pave and Deel as the bar), explicitly not generic AI UI.

## Evidence on Hand

- Seeded synthetic data only.
- There are no real customers, testimonials or benchmarks, and none may be invented.

## Product Principles

1. Never mix currencies silently. Local statistics stay in local currency, and converted figures are always labelled "≈ USD".
2. History is never lost. A salary change appends, never overwrites.
3. The answer is at most two clicks away from any screen.
4. Familiar beats clever. Standard HR-tool affordances, dense and scannable.
