-- Reference data: one currency per country, with a fixed reference rate used only for reporting.
CREATE TABLE country (
    code          TEXT PRIMARY KEY CHECK (length(code) = 2),
    name          TEXT NOT NULL UNIQUE,
    currency_code TEXT NOT NULL CHECK (length(currency_code) = 3),
    units_per_usd REAL NOT NULL CHECK (units_per_usd > 0),
    rate_as_of    TEXT NOT NULL
);

-- annual_salary is the current annual base salary in whole units of the country's currency.
-- It is denormalised from salary_change so that list and insight queries read a single table.
CREATE TABLE employee (
    id            INTEGER PRIMARY KEY,
    full_name     TEXT    NOT NULL,
    email         TEXT    NOT NULL UNIQUE COLLATE NOCASE,
    job_title     TEXT    NOT NULL,
    department    TEXT    NOT NULL,
    country_code  TEXT    NOT NULL REFERENCES country (code),
    annual_salary INTEGER NOT NULL CHECK (annual_salary > 0),
    hire_date     TEXT    NOT NULL,
    created_at    TEXT    NOT NULL,
    updated_at    TEXT    NOT NULL
);

CREATE INDEX idx_employee_country_job_title ON employee (country_code, job_title);
CREATE INDEX idx_employee_department ON employee (department);
CREATE INDEX idx_employee_full_name ON employee (full_name);

-- Append-only history. Currency is stored because an employee can relocate to another country.
CREATE TABLE salary_change (
    id             INTEGER PRIMARY KEY,
    employee_id    INTEGER NOT NULL REFERENCES employee (id) ON DELETE CASCADE,
    annual_salary  INTEGER NOT NULL CHECK (annual_salary > 0),
    currency_code  TEXT    NOT NULL,
    effective_date TEXT    NOT NULL,
    reason         TEXT,
    recorded_at    TEXT    NOT NULL
);

CREATE INDEX idx_salary_change_employee ON salary_change (employee_id, effective_date);
