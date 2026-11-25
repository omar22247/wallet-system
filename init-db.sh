#!/bin/bash

run_sql() {
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres "$@"
}

create_service_db() {
  run_sql -v db="$1" -v usr="$2" -v pass="$3" <<'EOSQL'
CREATE USER :"usr" WITH PASSWORD :'pass';
CREATE DATABASE :"db" OWNER :"usr"; 
REVOKE ALL ON DATABASE :"db" FROM PUBLIC;
EOSQL
}

create_service_db auth_db         auth_user         "$AUTH_DB_PASSWORD"
create_service_db wallet_db       wallet_user       "$WALLET_DB_PASSWORD"
create_service_db notification_db notification_user "$NOTIFICATION_DB_PASSWORD"
