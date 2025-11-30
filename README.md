# Food Delivery Service

Минимальная инструкция по подготовке БД и запуску тестов без вспомогательных скриптов.

## Требования
- Java 17
- PostgreSQL 12+
- Maven 3.9+
- Проект без Spring (plain JDBC + Lombok)

## Быстрый запуск для полноценного UI (локально)
1. **Поднимите PostgreSQL**:
   - Самый быстрый путь — `docker compose up -d db` (минимальный compose-файл разворачивает только PostgreSQL с volume, база доступна на `localhost:5432`).
   - Или создайте базу вручную по инструкции ниже.
2. **Накатите схему и (по желанию) тестовые данные** командами из раздела «Подготовка базы данных» (без вспомогательных скриптов).
3. **Соберите приложение**: `mvn clean package -DskipTests`.
   - Получится исполняемый JAR `target/food-delivery.jar` с embedded Tomcat и JSP.
4. **Запустите JAR**:
   - `java -jar target/food-delivery.jar` (по умолчанию порт `8080`, можно переопределить через `PORT`).
   - Приложение доступно на `http://localhost:8080/`.
5. **Проверьте основные сценарии в браузере** (без JavaScript, все формы серверные):
   - Клиент: `…/client/register` → регистрация с полным адресом; `…/client/login` → вход и переход в профиль/корзину.
   - Магазин: `…/shop/register` → анкета владельца; `…/shop/login` → кабинет, товары и заказы.
   - Курьер: `…/courier/login` → дашборд смены и заказов.

Если нужно работать только с БД (без UI), можно ограничиться шагами 1–2.

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

### Частые проблемы и решения при локальном разворачивании
- **Роль/БД уже существуют.** Сообщения вида `role "fooddelivery_user" already exists` или `database "food_delivery" already exists` безопасны: просто переходите к следующему шагу.
- **Конфликт имени контейнера БД.** Compose теперь использует имя `postgres-db`; при сообщении `... is already in use` удалите старый контейнер (`docker rm -f postgres-db`) и повторите команду.
- **Ошибки “must be owner of table …” при накатывании схемы.** Это значит, что таблицы созданы под другим пользователем. Решения на выбор:
  1) Полностью сбросить таблицы: выполнить `src/main/resources/sql/000_drop_tables.sql` под владельцем таблиц (обычно `postgres`), затем снова применить `src/main/resources/sql/007_main_schema.sql` от имени `fooddelivery_user`.
  2) Или сменить владельца существующих таблиц: в `psql` под `postgres` выполнить `REASSIGN OWNED BY postgres TO fooddelivery_user;` (или указать вашего владельца) и повторить команду со схемой.
- **Maven падает с `TypeTag :: UNKNOWN` или jstl-warning.** Используйте Java 17 и системный Maven: `JAVA_HOME` должен указывать на JDK 17, а `mvn -version` в консоли обязан показывать 17. Если сборка запускалась из IDE с другим JDK (например, 21+), выберите JDK 17 в настройках Maven Runner или запустите `mvn clean package -DskipTests` из терминала.

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
- Поднять базу данных (PostgreSQL в контейнере `postgres-db` с volume `postgres_data`). Если вы запускаете вспомогательные скрипты **с хоста**, оставьте `DB_HOST=localhost` (по умолчанию так и есть):
  ```bash
  docker compose up -d db
  ```
- Применить схему удобнее всего с хоста (контейнер уже слушает на `localhost:5432`):
  ```bash
  PGPASSWORD=fooddelivery_pass psql -h localhost -p 5432 -U fooddelivery_user -d food_delivery -f src/main/resources/sql/007_main_schema.sql
  ```

- Проверить количество таблиц после подготовки схемы в контейнере:
  ```bash
  docker exec -it postgres-db psql -U fooddelivery_user -d food_delivery -c "\dt"
  ```
  Норма — 11 таблиц (`addresses, cart_items, carts, clients, couriers, order_items, orders, payments, products, shops, working_hours`).

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
- **Через psql в интерактивном режиме:**
  ```bash
  PGPASSWORD=fooddelivery_pass psql -U fooddelivery_user -d food_delivery
  food_delivery=> \i src/main/resources/sql/000_drop_tables.sql
  food_delivery=> \i src/main/resources/sql/007_main_schema.sql
  ```
  Команда `\i` подключает файлы относительно текущего каталога.
- **Добавление тестовых данных:** любые скрипты из `src/main/resources/sql/test_data/` можно запускать выборочно после основного файла схемы.
- **Docker Compose БД:** если база запущена в контейнере, подключитесь в него и выполните те же команды (SQL файлы можно передать через stdin):
  ```bash
  docker exec -i postgres-db psql -U fooddelivery_user -d food_delivery < src/main/resources/sql/000_drop_tables.sql
  docker exec -i postgres-db psql -U fooddelivery_user -d food_delivery < src/main/resources/sql/007_main_schema.sql
  ```
