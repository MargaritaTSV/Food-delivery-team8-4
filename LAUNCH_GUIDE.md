# 🚀 ИНСТРУКЦИЯ ПО ЗАПУСКУ

## 1. Предварительные требования

### Установленное ПО:
- ✅ Java JDK 17 (проверка: `java -version`)
- ✅ Maven 3.9+ (проверка: `mvn -version`)
- ✅ PostgreSQL 12+ (проверка: `psql --version`)

### Переменные окружения:
```bash
# Установите переменные окружения
export JAVA_HOME=/path/to/java
export M2_HOME=/path/to/maven
export PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH
```

---

## 2. Подготовка БД

### Создание БД и пользователя:
```sql
-- Подключитесь к PostgreSQL
psql -U postgres

-- Создайте БД
CREATE DATABASE food_delivery;

CREATE USER fooddelivery_user WITH PASSWORD 'fooddelivery_pass';

-- Выдайте права
GRANT ALL PRIVILEGES ON DATABASE food_delivery TO fooddelivery_user;
GRANT ALL ON SCHEMA public TO fooddelivery_user;

-- Выход
\q
```

### Запуск миграций:
```bash
cd /Users/smolevanataliia/Desktop/Food-delivery-team8-main

# Запустите SQL скрипты
psql -U fooddelivery_user -d food_delivery -f src/main/resources/sql/000_drop_tables.sql
psql -U fooddelivery_user -d food_delivery -f src/main/resources/sql/001_create_base_tables/001_create_addresses.sql
psql -U fooddelivery_user -d food_delivery -f src/main/resources/sql/001_create_base_tables/002_create_working_hours.sql
psql -U fooddelivery_user -d food_delivery -f src/main/resources/sql/001_create_base_tables/003_create_clients.sql
# ... остальные скрипты
```

---

## 3. Компиляция, сборка и запуск JAR

### Перейдите в директорию проекта:
```bash
cd /Users/smolevanataliia/Desktop/Food-delivery-team8-main
```

### Очистка и сборка без тестов:
```bash
mvn clean package -DskipTests
```

### Результат:
```
✅ Создан исполняемый файл: target/food-delivery.jar
```

### Запуск (embedded Tomcat):
```bash
java -jar target/food-delivery.jar
# при необходимости: PORT=9090 java -jar target/food-delivery.jar
```

### Проверка в браузере:
```
http://localhost:8080/
```

Должны увидеть стартовую страницу приложения и входные ссылки (Клиент, Магазин, Курьер).

---

## 7. Тестирование функций

### Тест 1: Регистрация клиента
```
1. Нажмите "Клиент" на главной
2. Введите:
   - Имя: Иван Иванов
   - Email: ivan@example.com
   - Телефон: 89991112233
   - Город: Москва
   - Пароль: Password123!
3. Нажмите "Зарегистрироваться"
4. Должны попасть на главную клиента (/client/home)
```

### Тест 2: Логин курьера
```
1. Нажмите "Курьер"
2. Введите:
   - Телефон: 89998889900
   - Пароль: CourierPass123!
3. Нажмите "Войти"
4. Должны попасть на dashboard курьера (/courier/dashboard)
```

### Тест 3: Управление товарами магазина
```
1. Нажмите "Магазин"
2. Введите:
   - Email: shop@example.com
   - Пароль: ShopPass123!
3. Нажмите "Войти"
4. Перейдите на /products/list
5. Нажмите "+ Добавить товар"
6. Заполните форму и добавьте товар
```

---

## 8. Отладка и логирование

### Проверьте логи приложения:
```bash
# Логи пишутся в stdout
java -jar target/food-delivery.jar | tee food-delivery.log

# Если уже запущено в фоне
tail -f food-delivery.log
```

### Включение debug режима

В файле `src/main/resources/log4j.properties`:
```properties
log4j.rootLogger=DEBUG, console
log4j.logger.com.team8.fooddelivery=DEBUG
```

Затем пересоберите проект:
```bash
mvn clean package -DskipTests
```

---

## 9. Решение типичных проблем

### Ошибка: "Port 8080 already in use"
```bash
# Найдите процесс на порту 8080
lsof -i :8080

# Завершите процесс
kill -9 <PID>

# Или запустите приложение на другом порту
PORT=8081 java -jar target/food-delivery.jar
```

### Ошибка: "Database connection refused"
```bash
# Проверьте что PostgreSQL запущена
psql -U postgres

# Проверьте конфиги БД в коде
# src/main/java/com/team8/fooddelivery/util/DatabaseInitializer.java
```

### Ошибка: "Cannot find JAR file"
```bash
# Убедитесь что сборка прошла успешно
mvn package -DskipTests

# Проверьте наличие файла
ls -la target/food-delivery.jar
```

### 404 при открытии страницы
```bash
# Проверьте консоль, где запущен `java -jar ...` (ошибки выводятся туда)
# Убедитесь, что JAR запущен и слушает указанный порт
curl -I http://localhost:8080/ | head -n 1
```

---

## 10. Production развертывание

### Рекомендации:

1. **Запуск как сервис:**
   - оформите systemd unit с командой `java -jar /opt/food-delivery/food-delivery.jar` и переменными `PORT`, `DB_URL`, `DB_USER`, `DB_PASSWORD`.

2. **Используйте SSL/TLS через reverse proxy (nginx):**
   ```nginx
   upstream food_delivery {
       server localhost:8080;
   }

   server {
       listen 80;
       server_name food-delivery.com;

       location / {
           proxy_pass http://tomcat;
       }
   }
   ```

4. **Регулярная архивация БД:**
   ```bash
   # Backup БД еженедельно
   pg_dump food_delivery | gzip > backup_$(date +%Y%m%d).sql.gz
   ```

---

## 11. Мониторинг и обслуживание

### Проверка здоровья приложения:
```bash
# Проверьте основную страницу
curl http://localhost:8080/food-delivery/

# Проверьте логин
curl -X POST http://localhost:8080/food-delivery/client/login \
     -d "email=test@example.com&password=test123"
```

### Логи приложения:
```bash
# Логи выводятся в stdout, используйте tee для сохранения
java -jar target/food-delivery.jar | tee food-delivery.log
```

### Перезагрузка приложения:
```bash
# Остановите текущий процесс и запустите JAR заново
pkill -f "food-delivery.jar" || true
java -jar target/food-delivery.jar
```

---

## 12. Контакты и поддержка

**Для вопросов и проблем:**
- 📧 Отправьте issue в GitHub
- 💬 Свяжитесь с командой разработки
- 📝 Изучите документацию в README_IMPLEMENTATION.md

---

✅ **Приложение готово к запуску!**

