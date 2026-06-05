#!/bin/sh
set -eu

MYSQL_PWD="${MYSQL_ROOT_PASSWORD}" mysql -uroot <<SQL
CREATE DATABASE IF NOT EXISTS \`${MYSQL_DATABASE}\`;
SQL

MYSQL_PWD="${MYSQL_ROOT_PASSWORD}" mysql -uroot "${MYSQL_DATABASE}" < "/seed/onlineparkingreservation_adminaccount.sql"
MYSQL_PWD="${MYSQL_ROOT_PASSWORD}" mysql -uroot "${MYSQL_DATABASE}" < "/seed/onlineparkingreservation_useraccount.sql"
MYSQL_PWD="${MYSQL_ROOT_PASSWORD}" mysql -uroot "${MYSQL_DATABASE}" < "/seed/onlineparkingreservation_inventory.sql"
MYSQL_PWD="${MYSQL_ROOT_PASSWORD}" mysql -uroot "${MYSQL_DATABASE}" < "/seed/onlineparkingreservation_routines.sql"
MYSQL_PWD="${MYSQL_ROOT_PASSWORD}" mysql -uroot "${MYSQL_DATABASE}" < "/seed/onlineparkingreservation_enhancements.sql"
