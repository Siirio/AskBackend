#!/bin/bash
set -e

DB_USER="${ASK_DB_USERNAME:?ASK_DB_USERNAME must be set}"
DB_PASSWORD="${ASK_DB_PASSWORD:?ASK_DB_PASSWORD must be set}"

sudo -u postgres psql -c "DO \$\$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '$DB_USER') THEN
      CREATE ROLE $DB_USER LOGIN PASSWORD '$DB_PASSWORD';
   END IF;
END
\$\$;"
sudo -u postgres psql -tc "SELECT 1 FROM pg_database WHERE datname = 'ask'" | grep -q 1 || sudo -u postgres psql -c "CREATE DATABASE ask OWNER $DB_USER;"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE ask TO $DB_USER;"
echo "POSTGRES_SETUP_DONE"
