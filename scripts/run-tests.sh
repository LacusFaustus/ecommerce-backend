#!/bin/bash

# Скрипт для запуска тестов
echo "Запуск тестов E-commerce приложения..."

# Проверка наличия Maven
if ! command -v mvn &> /dev/null; then
    echo "Ошибка: Maven не установлен"
    exit 1
fi

# Запуск тестов
echo "Запуск unit тестов..."
mvn test

if [ $? -eq 0 ]; then
    echo "Unit тесты прошли успешно"
else
    echo "Unit тесты завершились с ошибками"
    exit 1
fi

echo "Запуск интеграционных тестов..."
mvn verify -DskipUnitTests=false

if [ $? -eq 0 ]; then
    echo "Все тесты прошли успешно!"
else
    echo "Интеграционные тесты завершились с ошибками"
    exit 1
fi

# Генерация отчета о покрытии
echo "Генерация отчета о покрытии..."
mvn jacoco:report

echo "Отчет о покрытии доступен в target/site/jacoco/index.html"
