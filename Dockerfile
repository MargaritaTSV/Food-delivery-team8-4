# 1. Используем официальный JDK/Maven образ для сборки
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Копируем ВСЕ файлы проекта
COPY . .

# Даем права на выполнение скриптов
RUN chmod +x run_scheme.sh

# Собираем jar
RUN mvn -q -e -DskipTests package

# 2. Lightweight образ для запуска
FROM eclipse-temurin:17-jre

WORKDIR /app

# Копируем собранный jar
COPY --from=build /app/target/*.jar app.jar

# Копируем скрипты из stage сборки
COPY --from=build /app/run_scheme.sh /app/run_scheme.sh
RUN chmod +x /app/run_scheme.sh

# Устанавливаем postgresql-client для использования psql и pg_isready
RUN apt-get update && apt-get install -y postgresql-client && rm -rf /var/lib/apt/lists/*

# Порт приложения
EXPOSE 8080

# Запуск по умолчанию (может быть переопределен в docker-compose)
ENTRYPOINT ["/bin/sh", "-c"]
CMD ["java -jar app.jar"]