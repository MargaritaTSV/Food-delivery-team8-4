#!/bin/bash

# Food Delivery Database Setup Script
# Автоматически создает всю схему БД

set -e  # Выход при ошибке

echo "🚀 Настройка базы данных Food Delivery..."
echo ""

# Параметры подключения (по умолчанию дружелюбные к локальному запуску)
# Если скрипт запускается внутри Docker Compose, передайте DB_HOST=db через переменные окружения.
# При запуске на хосте контейнера «db» недоступен, поэтому по умолчанию используем localhost.
DEFAULT_DB_HOST="localhost"
if getent hosts db >/dev/null 2>&1; then
    DEFAULT_DB_HOST="db"
fi

DB_HOST=${DB_HOST:-"$DEFAULT_DB_HOST"}
DB_PORT=${DB_PORT:-"5432"}
DB_USER=${DB_USER:-"fooddelivery_user"}
DB_PASSWORD=${DB_PASSWORD:-"fooddelivery_pass"}
DB_NAME=${DB_NAME:-"food_delivery"}

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
SQL_DIR=${SQL_DIR:-"$SCRIPT_DIR/src/main/resources/sql"}

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
    ls -la "$SCRIPT_DIR"
    exit 1
fi

# Переходим в папку с SQL файлами
cd "$SQL_DIR"
echo "📁 Текущая директория: $(pwd)"
echo "📋 Файлы в директории:"
ls -la
echo ""

# Массив SQL файлов в правильном порядке (вся схема — один файл)
SQL_FILES=(
    "000_drop_tables.sql"
    "007_main_schema.sql"
)

# Выполняем все SQL файлы
echo "📊 Выполнение SQL схемы..."
echo ""

for sql_file in "${SQL_FILES[@]}"; do
    if [ -f "$sql_file" ]; then
        echo "✅ Выполняется: $sql_file"
        if PGPASSWORD="$DB_PASSWORD" psql -v ON_ERROR_STOP=1 -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$sql_file"; then
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
EXPECTED_TABLES=11
echo "🔍 Проверка созданных таблиц... (ожидаем минимум $EXPECTED_TABLES базовых таблиц, остальные части находятся в одном файле схемы)"

TABLE_COUNT=$(PGPASSWORD="$DB_PASSWORD" psql -At -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';")

PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;"

if [ "$TABLE_COUNT" -lt "$EXPECTED_TABLES" ]; then
    echo "⚠️ Найдено только $TABLE_COUNT таблиц. Проверьте вывод выше: возможно, не все SQL файлы выполнились."
else
    echo "✅ Создано $TABLE_COUNT таблиц (ожидаемые базовые таблицы созданы)."
fi

echo ""
echo "🎉 База данных успешно настроена!"
echo "📊 Схема Food Delivery готова к использованию"

echo ""
echo "🚀 Для запуска тестов выполните:"
echo "   mvn test -Ddb.user=fooddelivery_user -Ddb.password='fooddelivery_pass'"
echo "   или"
echo "   ./RUN_TESTS.sh"
