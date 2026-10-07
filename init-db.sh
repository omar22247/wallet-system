#!/bin/bash
# بيتنفذ مرة واحدة بس، أول ما الـ postgres volume يتعمل.
# Database per service: كل service ليها DB و user خاص بيها.
# الباسوردات جاية من الـ environment (.env) مش مكتوبة هنا.
set -e

run_sql() {
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres "$@"
}

create_service_db() {
  # :"x" = identifier و :'x' = string literal (psql بيعمل escape آمن)
  run_sql -v db="$1" -v usr="$2" -v pass="$3" <<'EOSQL'
CREATE USER :"usr" WITH PASSWORD :'pass';
CREATE DATABASE :"db" OWNER :"usr";
-- by default أي user يقدر يعمل CONNECT لأي database (PUBLIC)
-- بنقفلها علشان كل service توصل للـ DB بتاعتها بس
REVOKE ALL ON DATABASE :"db" FROM PUBLIC;
EOSQL
}

create_service_db auth_db         auth_user         "$AUTH_DB_PASSWORD"
create_service_db wallet_db       wallet_user       "$WALLET_DB_PASSWORD"
create_service_db notification_db notification_user "$NOTIFICATION_DB_PASSWORD"

# الـ audit: owner للـ migrations، و app user هياخد INSERT + SELECT بس (Phase 5)
create_service_db audit_db audit_owner "$AUDIT_OWNER_PASSWORD"
run_sql -v pass="$AUDIT_APP_PASSWORD" <<'EOSQL'
CREATE USER audit_app WITH PASSWORD :'pass';
GRANT CONNECT ON DATABASE audit_db TO audit_app;
EOSQL