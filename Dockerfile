# 1. Используем официальный JDK/Maven образ для сборки
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Копируем проект, включая pom.xml
COPY . .

# Копируем скрипт и даем права на выполнение
COPY run_scheme.sh /app/run_scheme.sh
RUN chmod +x /app/run_scheme.sh

# Собираем jar
RUN mvn -q -e -DskipTests package

# 2. Lightweight образ для запуска
FROM eclipse-temurin:17-jre

WORKDIR /app

# Копируем собранный jar
COPY --from=build /app/target/*.jar app.jar

# Копируем скрипт из stage сборки
COPY --from=build /app/run_scheme.sh /app/run_scheme.sh
RUN chmod +x /app/run_scheme.sh

# Устанавливаем postgresql-client для использования psql и pg_isready
RUN apt-get update && apt-get install -y postgresql-client && rm -rf /var/lib/apt/lists/*

# Порт приложения
EXPOSE 8080

# Запуск
ENTRYPOINT ["java", "-jar", "app.jar"]