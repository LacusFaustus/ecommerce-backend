#!/bin/bash

# Скрипт для запуска MySQL в Docker
echo "Запуск MySQL в Docker..."

docker run --name ecommerce-mysql \
  -e MYSQL_ROOT_PASSWORD=password \
  -e MYSQL_DATABASE=online_store \
  -e MYSQL_USER=app_user \
  -e MYSQL_PASSWORD=app_password \
  -p 3306:3306 \
  -d mysql:8.0 \
  --default-authentication-plugin=mysql_native_password

echo "MySQL запущен на порту 3306"
echo "Подключение: mysql -h 127.0.0.1 -P 3306 -u root -p"
