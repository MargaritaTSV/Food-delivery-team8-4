#!/bin/bash

# Food Delivery Database Setup Script
# Автоматически создает всю схему БД

set -e  # Выход при ошибке

echo "🚀 Настройка базы данных Food Delivery..."
echo ""

# Параметры подключения (для Docker контейнера)
DB_HOST="db"
DB_PORT="5432"
DB_USER="fooddelivery_user"
DB_PASSWORD="fooddelivery_pass"
DB_NAME="food_delivery"

# Путь к SQL файлам в контейнере
SQL_DIR="/app/src/main/resources/sql"

echo "📊 База данных: $DB_NAME"
echo "🌐 Хост: $DB_HOST:$DB_PORT"
echo "👤 Пользователь: $DB_USER"
echo "📁 SQL файлы: $SQL_DIR"
echo ""

# Функция для ожидания готовности БД
wait_for_db() {
    echo "🔍 Ожидание запуска PostgreSQL..."
    for i in {1..30}; do
        if PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT 1;" &>/dev/null; then
            echo "✅ PostgreSQL готов и принимает подключения"
            return 0
        else
            echo "⌛ Попытка $i/30: PostgreSQL еще не готов, ждем..."
            sleep 2
        fi
    done
    echo "❌ PostgreSQL не запустился за 60 секунд"
    return 1
}

# Ждем готовности БД
wait_for_db

# Проверяем существование SQL директории
if [ ! -d "$SQL_DIR" ]; then
    echo "❌ SQL директория не найдена: $SQL_DIR"
    echo "📋 Содержимое текущей директории:"
    ls -la /app/
    exit 1
fi

# Переходим в папку с SQL файлами
cd "$SQL_DIR"
echo "📁 Текущая директория: $(pwd)"
echo "📋 Файлы в директории:"
ls -la
echo ""

# Массив SQL файлов в правильном порядке
SQL_FILES=(
    "000_drop_tables.sql"
    "001_create_base_tables/001_create_addresses.sql"
    "001_create_base_tables/002_create_working_hours.sql"
    "001_create_base_tables/003_create_clients.sql"
    "002_create_shop_tables/004_create_shops.sql"
    "002_create_shop_tables/005_create_products.sql"
    "002_create_shop_tables/006_add_shop_foreign_keys.sql"
    "003_create_courier_tables/003_create_courier_tables.sql"
    "004_create_order_tables/008_create_orders.sql"
    "004_create_order_tables/009_create_order_items.sql"
    "004_create_order_tables/010_create_carts.sql"
    "005_create_cart_tables/011_create_cart_items.sql"
    "005_create_cart_tables/012_add_cart_foreign_keys.sql"
    "006_create_indexes/013_create_indexes.sql"
)

# Выполняем все SQL файлы
echo "📊 Выполнение SQL схемы..."
echo ""

for sql_file in "${SQL_FILES[@]}"; do
    if [ -f "$sql_file" ]; then
        echo "✅ Выполняется: $sql_file"
        if PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$sql_file"; then
            echo "   ✅ Успешно: $sql_file"
        else
            echo "   ❌ Ошибка в файле: $sql_file"
            exit 1
        fi
    else
        echo "❌ Файл не найден: $sql_file"
        echo "📋 Доступные файлы:"
        find . -name "*.sql" | sort
        exit 1
    fi
    echo ""
done

# Проверяем созданные таблицы
echo "🔍 Проверка созданных таблиц..."
PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "
SELECT
    COUNT(*) as total_tables,
    string_agg(table_name, ', ' ORDER BY table_name) as tables_list
FROM information_schema.tables
WHERE table_schema = 'public';"

echo ""
echo "🎉 База данных успешно настроена!"
echo "📊 Схема Food Delivery готова к использованию"

echo ""
echo "🚀 Для запуска тестов выполните:"
echo "   mvn test -Ddb.user=fooddelivery_user -Ddb.password='fooddelivery_pass'"
echo "   или"
echo "   ./RUN_TESTS.sh"