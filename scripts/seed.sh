#!/usr/bin/env bash
# Wipes the local database and seeds a deterministic workforce (10,000 employees by default).
#
#   scripts/seed.sh             # 10,000 employees
#   scripts/seed.sh 50000       # any other size
#   SEED=7 scripts/seed.sh      # a different (but still reproducible) workforce
set -euo pipefail

COUNT="${1:-10000}"
SEED="${SEED:-20261113}"
cd "$(dirname "$0")/../backend"

./mvnw -q spring-boot:run -Dspring-boot.run.arguments="\
--spring.main.web-application-type=none \
--app.seed.reset=true \
--app.seed.employee-count=${COUNT} \
--app.seed.random-seed=${SEED}"
