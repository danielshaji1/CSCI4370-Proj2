#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
container="${MYSQL_CONTAINER:-mysql-server-x370}"
read -r -s -p "MySQL root password: " db_password
printf '\n'
# Password travels over stdin, not as a command-line argument or a file.
export_file="$(mktemp ./database_export.XXXXXX)"
trap 'rm -f "$export_file"' EXIT
if printf '%s\n' "$db_password" | docker exec -i "$container" sh -c '
    IFS= read -r MYSQL_PWD
    export MYSQL_PWD
    exec mysqldump -u root --single-transaction --complete-insert --skip-extended-insert --hex-blob --no-tablespaces --set-gtid-purged=OFF --databases csx370_mb_platform
' > "$export_file"; then
    test -s "$export_file"
    cp database_setup.sql database_setup.previous.sql
    mv "$export_file" database_setup.sql
    printf 'Exported all tables and current rows to database_setup.sql. Previous file saved as database_setup.previous.sql.\n'
else
    printf 'Export failed; database_setup.sql was not changed.\n' >&2
    exit 1
fi
