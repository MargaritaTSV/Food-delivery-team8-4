# ⚡️ Быстрый старт (JAR + embedded Tomcat)

Этот гайд показывает, как развернуть фронтенд и сервер без дополнительных скриптов и без внешнего Tomcat.

## 1. Требования
- Java 17 (`java -version`)
- Maven 3.9+ (`mvn -version`)
- PostgreSQL 12+ (`psql --version`)
- Доступ к Bash/Terminal

## 2. Подготовьте базу данных
```bash
psql -U postgres -c "CREATE USER fooddelivery_user WITH PASSWORD 'fooddelivery_pass';" || true
psql -U postgres -c "CREATE DATABASE food_delivery OWNER fooddelivery_user;" || true
PGPASSWORD=fooddelivery_pass psql -U fooddelivery_user -d food_delivery -f src/main/resources/sql/007_main_schema.sql
```
*(Опционально добавьте тестовые данные файлами из `src/main/resources/sql/test_data/` в указанном порядке).* 

## 3. Соберите исполняемый JAR
```bash
mvn clean package -DskipTests
# Результат: target/food-delivery.jar
```

## 4. Запустите приложение
```bash
java -jar target/food-delivery.jar
# или другой порт
PORT=9090 java -jar target/food-delivery.jar
```
Откройте в браузере `http://localhost:8080/` и используйте ссылки Клиент/Магазин/Курьер.

## 5. Частые проблемы
- **Порт занят:** `lsof -i :8080` → `kill -9 <PID>` или `PORT=8081 java -jar ...`.
- **Нет JAR:** повторите `mvn clean package -DskipTests` и проверьте `target/food-delivery.jar`.
- **Схема не накатывается:** выполните `src/main/resources/sql/000_drop_tables.sql` под владельцем таблиц, затем снова запустите `007_main_schema.sql` от имени `fooddelivery_user`.

## 6. Остановка и перезапуск
```bash
pkill -f "food-delivery.jar" || true
java -jar target/food-delivery.jar
```

## 7. Минимальные проверки
- Регистрация клиента: `/client/register`
- Кабинет магазина: `/shop/login`
- Кабинет курьера: `/courier/login`

Удачной работы! 
