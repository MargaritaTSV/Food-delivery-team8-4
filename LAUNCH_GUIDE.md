# 🚀 ИНСТРУКЦИЯ ПО ЗАПУСКУ

## 1. Предварительные требования

### Установленное ПО:
- ✅ Java JDK 11+ (проверка: `java -version`)
- ✅ Maven 3.6+ (проверка: `mvn -version`)
- ✅ PostgreSQL 12+ (проверка: `psql --version`)
- ✅ Tomcat 10.1+ (Apache Tomcat)

### Переменные окружения:
```bash
# Установите переменные окружения
export JAVA_HOME=/path/to/java
export M2_HOME=/path/to/maven
export TOMCAT_HOME=/path/to/tomcat
export PATH=$JAVA_HOME/bin:$M2_HOME/bin:$TOMCAT_HOME/bin:$PATH
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

## 3. Компиляция и сборка проекта

### Перейдите в директорию проекта:
```bash
cd /Users/smolevanataliia/Desktop/Food-delivery-team8-main
```

### Очистка старых артефактов:
```bash
mvn clean
```

### Компиляция:
```bash
mvn compile
```

### Сборка WAR архива (пропуск тестов):
```bash
mvn package -DskipTests
```

### Результат:
```
✅ Создан файл: target/food-delivery.war
```

---

## 4. Развертывание на Tomcat

### Способ 1: Копирование WAR файла

```bash
# Копируем WAR на Tomcat
cp /Users/smolevanataliia/Desktop/Food-delivery-team8-main/target/food-delivery.war \
   $TOMCAT_HOME/webapps/

# Tomcat автоматически распакует WAR при запуске
```

### Способ 2: Используя Tomcat Manager (веб-интерфейс)

1. Откройте http://localhost:8080/manager
2. Введите логин/пароль администратора Tomcat
3. Загрузите файл `target/food-delivery.war`

### Способ 3: Конфигурация через context.xml (для production)

```xml
<!-- $TOMCAT_HOME/conf/Catalina/localhost/food-delivery.xml -->
<Context path="/food-delivery" docBase="/path/to/food-delivery.war">
    <Resource name="jdbc/FoodDeliveryDB"
              auth="Container"
              type="javax.sql.DataSource"
              driverClassName="org.postgresql.Driver"
              url="jdbc:postgresql://localhost:5432/food_delivery"
              username="fooddelivery_user"
              password="fooddelivery_pass"
              maxActive="20"
              maxIdle="10"
              maxWait="-1"/>
</Context>
```

---

## 5. Запуск Tomcat

### На macOS/Linux:
```bash
# Запуск в foreground (с логами)
$TOMCAT_HOME/bin/catalina.sh run

# Запуск в background (как сервис)
$TOMCAT_HOME/bin/catalina.sh start

# Остановка
$TOMCAT_HOME/bin/catalina.sh stop
```

### На Windows:
```cmd
# Запуск
%TOMCAT_HOME%\bin\catalina.bat run

# Остановка (Ctrl+C)
```

---

## 6. Проверка развертывания

### Проверьте логи Tomcat:
```bash
tail -f $TOMCAT_HOME/logs/catalina.out
```

### Должны увидеть:
```
INFO: Server startup in XXX ms
```

### Откройте приложение в браузере:
```
http://localhost:8080/food-delivery/
```

Должны увидеть:
- ✅ Главная страница Food Delivery
- ✅ Кнопки входа (Клиент, Магазин, Курьер)

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
# Основные логи Tomcat
tail -f $TOMCAT_HOME/logs/catalina.out

# Логи приложения (если настроены)
tail -f $TOMCAT_HOME/logs/food-delivery.log
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

# Или измените порт в $TOMCAT_HOME/conf/server.xml
# Найдите строку: <Connector port="8080"
# Измените на: <Connector port="8081"
```

### Ошибка: "Database connection refused"
```bash
# Проверьте что PostgreSQL запущена
psql -U postgres

# Проверьте конфиги БД в коде
# src/main/java/com/team8/fooddelivery/util/DatabaseInitializer.java
```

### Ошибка: "Cannot find WAR file"
```bash
# Убедитесь что сборка прошла успешно
mvn package -DskipTests

# Проверьте наличие файла
ls -la target/food-delivery.war

# Скопируйте заново
cp target/food-delivery.war $TOMCAT_HOME/webapps/
```

### 404 при открытии страницы
```bash
# Проверьте что Tomcat распаковал WAR
ls -la $TOMCAT_HOME/webapps/food-delivery/

# Проверьте логи
tail -f $TOMCAT_HOME/logs/catalina.out | grep "ERROR\|WARN"
```

---

## 10. Production развертывание

### Рекомендации:

1. **Используйте SSL/TLS:**
   ```xml
   <!-- server.xml -->
   <Connector port="8443" 
              protocol="org.apache.coyote.http11.Http11NioProtocol"
              scheme="https" 
              secure="true"
              sslProtocol="TLS"
              keystoreFile="path/to/keystore.jks"
              keystorePass="password"/>
   ```

2. **Настройте memory Tomcat:**
   ```bash
   # setenv.sh
   export CATALINA_OPTS="-Xms512M -Xmx1024M"
   ```

3. **Используйте reverse proxy (nginx):**
   ```nginx
   upstream tomcat {
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

### Очистка логов:
```bash
# Очистите старые логи
rm $TOMCAT_HOME/logs/catalina.*.log
rm $TOMCAT_HOME/logs/localhost.*.log
```

### Перезагрузка приложения:
```bash
# Без перезагрузки Tomcat
rm -rf $TOMCAT_HOME/webapps/food-delivery*
cp target/food-delivery.war $TOMCAT_HOME/webapps/

# Tomcat автоматически распакует новый WAR
```

---

## 12. Контакты и поддержка

**Для вопросов и проблем:**
- 📧 Отправьте issue в GitHub
- 💬 Свяжитесь с командой разработки
- 📝 Изучите документацию в README_IMPLEMENTATION.md

---

✅ **Приложение готово к запуску!**

