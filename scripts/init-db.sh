#!/bin/bash

# Скрипт инициализации базы данных
echo "Инициализация базы данных..."

# Ожидание запуска MySQL
until mysql -h 127.0.0.1 -P 3306 -u root -ppassword -e "SELECT 1" > /dev/null 2>&1; do
    echo "Ожидание запуска MySQL..."
    sleep 2
done

# Создание базы данных и пользователя
mysql -h 127.0.0.1 -P 3306 -u root -ppassword << EOF
CREATE DATABASE IF NOT EXISTS online_store;
CREATE USER IF NOT EXISTS 'app_user'@'%' IDENTIFIED BY 'app_password';
GRANT ALL PRIVILEGES ON online_store.* TO 'app_user'@'%';
FLUSH PRIVILEGES;
EOF

echo "База данных инициализирована успешно"
