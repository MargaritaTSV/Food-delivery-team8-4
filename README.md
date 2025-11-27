# Food Delivery Service

Минимальная инструкция по подготовке БД и запуску тестов без вспомогательных скриптов.

## Требования
- Java 17
- PostgreSQL 12+
- Maven 3.9+
- Проект без Spring (plain JDBC + Lombok)

## Подготовка базы данных (однократно)
1. Создайте пользователя и базу (пример с дефолтными учетными данными):
   ```bash
   psql -U postgres -c "CREATE USER fooddelivery_user WITH PASSWORD 'fooddelivery_pass';"
   psql -U postgres -c "CREATE DATABASE food_delivery OWNER fooddelivery_user;"
   ```
2. Примените основную схему (все таблицы и индексы теперь в **одном** файле):
   ```bash
   PGPASSWORD=fooddelivery_pass psql -U fooddelivery_user -d food_delivery -f src/main/resources/sql/007_main_schema.sql
   ```
   После выполнения появятся минимум 11 таблиц: `addresses, working_hours, clients, shops, products, couriers, orders, order_items, carts, cart_items, payments`.
3. (Опционально) загрузите тестовые данные по категориям (можно повторно, используется `ON CONFLICT DO NOTHING` и фиксированные идентификаторы):
   ```bash
   PGPASSWORD=fooddelivery_pass psql -U fooddelivery_user -d food_delivery -f src/main/resources/sql/test_data/000_insert_addresses.sql
   PGPASSWORD=fooddelivery_pass psql -U fooddelivery_user -d food_delivery -f src/main/resources/sql/test_data/001_insert_working_hours.sql
   PGPASSWORD=fooddelivery_pass psql -U fooddelivery_user -d food_delivery -f src/main/resources/sql/test_data/002_insert_clients.sql
   PGPASSWORD=fooddelivery_pass psql -U fooddelivery_user -d food_delivery -f src/main/resources/sql/test_data/003_insert_shops.sql
   PGPASSWORD=fooddelivery_pass psql -U fooddelivery_user -d food_delivery -f src/main/resources/sql/test_data/004_insert_products.sql
   PGPASSWORD=fooddelivery_pass psql -U fooddelivery_user -d food_delivery -f src/main/resources/sql/test_data/005_insert_couriers.sql
   PGPASSWORD=fooddelivery_pass psql -U fooddelivery_user -d food_delivery -f src/main/resources/sql/test_data/006_insert_orders.sql
   PGPASSWORD=fooddelivery_pass psql -U fooddelivery_user -d food_delivery -f src/main/resources/sql/test_data/007_insert_order_items.sql
   PGPASSWORD=fooddelivery_pass psql -U fooddelivery_user -d food_delivery -f src/main/resources/sql/test_data/008_insert_carts.sql
   PGPASSWORD=fooddelivery_pass psql -U fooddelivery_user -d food_delivery -f src/main/resources/sql/test_data/009_insert_cart_items.sql
   PGPASSWORD=fooddelivery_pass psql -U fooddelivery_user -d food_delivery -f src/main/resources/sql/test_data/010_insert_payments.sql
   ```
   При необходимости выполните отдельные файлы из `src/main/resources/sql/004_create_order_tables/` (payments, адрес, delivery_time).

## Настройка подключения
По умолчанию используется `jdbc:postgresql://localhost:5432/food_delivery`, пользователь `fooddelivery_user`, пароль `fooddelivery_pass`.
Переопределение:
- Переменные окружения: `DB_URL`, `DB_USER`, `DB_PASSWORD`
- JVM-параметры: `-Ddb.url=... -Ddb.user=... -Ddb.password=...`

## Запуск тестов вручную
1. Собрать проект без тестов (поможет заранее выявить проблемы компиляции):
   ```bash
   mvn -DskipTests compile
   ```
2. Запустить нужные наборы (указывайте свои креды при необходимости):
   ```bash
   mvn test -Ddb.user=fooddelivery_user -Ddb.password=fooddelivery_pass -Dtest=ClientRepositoryTest,CartRepositoryTest
   mvn test -Ddb.user=fooddelivery_user -Ddb.password=fooddelivery_pass -Dtest=ShopProductIntegrationTest
   mvn test -Ddb.user=fooddelivery_user -Ddb.password=fooddelivery_pass -Dtest=OrderCourierIntegrationTest
   mvn test -Ddb.user=fooddelivery_user -Ddb.password=fooddelivery_pass -Dtest=OrderInteractionIntegrationTest
   ```
   Тестовые данные не очищаются, поэтому прогоны можно повторять поочередно или выборочно.

## Запуск в Docker
- Поднять базу данных (postgres запустится в контейнере `db` и примонтирует volume `postgres_data`). Если вы запускаете вспомогательные скрипты **с хоста**, убедитесь, что `DB_HOST=localhost` (по умолчанию так и есть), потому что hostname `db` виден только внутри docker-сети:
  ```bash
  docker compose up -d db
  ```
- Применить схему и прогнать тесты внутри контейнера Maven (Java/Maven локально не нужны):
  ```bash
  docker compose run --rm tests
  ```
  Контейнер `tests` ждет готовности PostgreSQL, накатывает схему через `run_scheme.sh` (берет файлы из примонтированной папки проекта), затем запускает `mvn test` с параметрами из `docker-compose.yml`.

- Проверить количество таблиц после подготовки схемы в контейнере:
  ```bash
  docker exec -it food-delivery-db psql -U fooddelivery_user -d food_delivery -c "\dt"
  ```
  Норма — 11 таблиц (`addresses, cart_items, carts, clients, couriers, order_items, orders, payments, products, shops, working_hours`).

## Полезное
- Проверка подключения: `./check_db_connection.sh`
- Запуск схемы целиком: `./run_scheme.sh` (если нужна полная переинициализация)

## Очистка/обновление БД перед повторным запуском
Если нужно сбросить данные и применить схему заново:
1. Остановите приложение/тесты, чтобы соединения не держали блокировки.
2. Выполните полный скрипт:
   ```bash
   PGPASSWORD=fooddelivery_pass psql -U fooddelivery_user -d food_delivery -f src/main/resources/sql/000_drop_tables.sql
   PGPASSWORD=fooddelivery_pass psql -U fooddelivery_user -d food_delivery -f src/main/resources/sql/007_main_schema.sql
   ```
   При необходимости повторно прогоните файлы с тестовыми данными из `src/main/resources/sql/test_data/` в нужном порядке.
3. Запустите приложение или нужные тесты с теми же параметрами подключения.

### Другие способы создать/удалить таблицы
- **Скрипт запуска схемы:** `./run_scheme.sh` — обёртка над `psql`, последовательно вызывает `000_drop_tables.sql` и единый файл `007_main_schema.sql` из каталога `src/main/resources/sql/`.
  - Если запускаете **на хосте**, оставьте `DB_HOST=localhost` (значение по умолчанию).
  - Если запускаете **внутри Docker/Compose**, передайте `DB_HOST=db` (в `docker-compose.yml` это уже сделано через переменные окружения).
- **Через psql в интерактивном режиме:**
  ```bash
  PGPASSWORD=fooddelivery_pass psql -U fooddelivery_user -d food_delivery
  food_delivery=> \i src/main/resources/sql/000_drop_tables.sql
  food_delivery=> \i src/main/resources/sql/007_main_schema.sql
  ```
  Команда `\i` подключает файлы относительно текущего каталога.
- **Добавление тестовых данных:** любые скрипты из `src/main/resources/sql/test_data/` можно запускать выборочно после основного файла схемы.
- **Docker Compose БД:** если база запущена в контейнере, подключитесь в него и выполните те же команды (SQL файлы доступны по пути `/app/src/main/resources/sql` благодаря volume):
  ```bash
  docker exec -it food-delivery-db psql -U fooddelivery_user -d food_delivery -f /app/src/main/resources/sql/000_drop_tables.sql
  docker exec -it food-delivery-db psql -U fooddelivery_user -d food_delivery -f /app/src/main/resources/sql/007_main_schema.sql
  ```
