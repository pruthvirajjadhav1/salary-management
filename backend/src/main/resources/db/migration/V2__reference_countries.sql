-- Illustrative fixed reference rates (local currency units per 1 USD).
-- They are used only to compare and total pay across countries, and are always shown as "≈ USD".
INSERT INTO country (code, name, currency_code, units_per_usd, rate_as_of) VALUES
    ('US', 'United States',  'USD',  1.00, '2026-11-01'),
    ('GB', 'United Kingdom', 'GBP',  0.79, '2026-11-01'),
    ('DE', 'Germany',        'EUR',  0.92, '2026-11-01'),
    ('IN', 'India',          'INR', 84.00, '2026-11-01'),
    ('CA', 'Canada',         'CAD',  1.38, '2026-11-01'),
    ('AU', 'Australia',      'AUD',  1.53, '2026-11-01'),
    ('SG', 'Singapore',      'SGD',  1.34, '2026-11-01'),
    ('BR', 'Brazil',         'BRL',  5.70, '2026-11-01');
