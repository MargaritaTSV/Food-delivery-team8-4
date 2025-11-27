#!/bin/bash

# Скрипт для исправления прав доступа к базе данных
# Используйте этот скрипт, если получили ошибку "permission denied"

echo "🔧 Исправление прав доступа к базе данных"
echo ""

# Определяем суперпользователя PostgreSQL и базу данных для подключения
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS (обычно используется имя пользователя системы)
    SUPERUSER=$(whoami)
    # Пробуем подключиться к разным базам данных по порядку
    ADMIN_DB=""
    for db in postgres template1 "$SUPERUSER"; do
        if psql -U "$SUPERUSER" -d "$db" -c "SELECT 1;" >/dev/null 2>&1; then
            ADMIN_DB="$db"
            break
        fi
    done
    if [ -z "$ADMIN_DB" ]; then
        echo "❌ Ошибка: Не удалось подключиться к PostgreSQL"
        echo "   Убедитесь, что PostgreSQL запущен и доступен"
        exit 1
    fi
else
    # Linux (обычно postgres)
    SUPERUSER="postgres"
    ADMIN_DB="postgres"
fi

echo "📍 Используем суперпользователя: $SUPERUSER"
echo "📍 Подключаемся к базе данных: $ADMIN_DB"
echo ""

# Проверяем, существует ли база данных
if ! psql -U "$SUPERUSER" -d "$ADMIN_DB" -lqt | cut -d \| -f 1 | grep -qw food_delivery; then
    echo "❌ База данных food_delivery не найдена"
    echo "   Запустите сначала: ./src/main/resources/setup_database.sh"
    exit 1
fi

# Предоставление прав на все существующие таблицы и последовательности
echo "🔐 Предоставление прав на все таблицы и последовательности..."
psql -U "$SUPERUSER" -d food_delivery <<EOF
-- Предоставление прав на схему
GRANT ALL ON SCHEMA public TO fooddelivery_user;
ALTER SCHEMA public OWNER TO fooddelivery_user;

-- Предоставление прав на все таблицы
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO fooddelivery_user;

-- Предоставление прав на все последовательности
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO fooddelivery_user;

-- Установка прав по умолчанию для будущих объектов
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO fooddelivery_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO fooddelivery_user;
EOF

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Права доступа успешно исправлены!"
    echo ""
    echo "Теперь вы можете запускать userstory:"
    echo "  ./run_userstory.sh client"
    echo "  ./run_userstory.sh cart"
    echo "  ./run_userstory.sh courier"
    echo "  ./run_userstory.sh notifications"
else
    echo ""
    echo "❌ Ошибка при исправлении прав доступа"
    exit 1
fi
