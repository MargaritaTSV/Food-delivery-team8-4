#!/bin/bash

# Универсальная проверка подключения к PostgreSQL с учетом env/JVM настроек

DB_URL=${DB_URL:-${1:-jdbc:postgresql://localhost:5432/food_delivery}}
DB_USER=${DB_USER:-fooddelivery_user}
DB_PASSWORD=${DB_PASSWORD:-fooddelivery_pass}

# Разбор JDBC URL
url_body=${DB_URL#jdbc:postgresql://}
host_port_db=${url_body}
host_port=${host_port_db%%/*}
DB_NAME=${host_port_db#*/}
DB_HOST=${host_port%%:*}
DB_PORT=${host_port#*:}
[ "$DB_PORT" = "$DB_HOST" ] && DB_PORT=5432

echo "🔍 Проверка подключения к PostgreSQL"
echo "URL: $DB_URL"
echo "Host: $DB_HOST  Port: $DB_PORT  DB: $DB_NAME"
echo "User: $DB_USER"

if command -v pg_isready &> /dev/null; then
  if pg_isready -h "$DB_HOST" -p "$DB_PORT" &> /dev/null; then
    echo "✅ PostgreSQL отвечает"
  else
    echo "❌ PostgreSQL недоступен по $DB_HOST:$DB_PORT"
    exit 1
  fi
else
  echo "⚠️  pg_isready не найден, пропускаем проверку доступности"
fi

export PGPASSWORD="$DB_PASSWORD"

echo "🔍 Проверка наличия базы $DB_NAME..."
if psql -U "$DB_USER" -h "$DB_HOST" -p "$DB_PORT" -lqt 2>/dev/null | cut -d '|' -f1 | grep -qw "$DB_NAME"; then
  echo "✅ База найдена"
else
  echo "❌ База не найдена. Создайте её перед запуском тестов"
  exit 1
fi

echo "🔍 Проверка ключевых таблиц..."
KEY_TABLES=("clients" "shops" "products" "couriers" "addresses" "carts" "cart_items" "orders" "order_items" "payments")
missing=()
for tbl in "${KEY_TABLES[@]}"; do
  exists=$(psql -U "$DB_USER" -h "$DB_HOST" -p "$DB_PORT" -d "$DB_NAME" -tAc "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_schema='public' AND table_name='$tbl');")
  if [ "$exists" = "t" ]; then
    echo "   ✅ $tbl"
  else
    echo "   ❌ $tbl"
    missing+=("$tbl")
  fi
done

if [ ${#missing[@]} -gt 0 ]; then
  echo "⚠️  Отсутствуют таблицы: ${missing[*]}"
  echo "   Выполните схему: psql -U $DB_USER -h $DB_HOST -p $DB_PORT -d $DB_NAME -f src/main/resources/sql/007_main_schema.sql"
  exit 1
fi

echo "✅ Проверка завершена успешно"
