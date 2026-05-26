# ConsultantApp

Мобильное приложение для консультантов с интеграцией базы данных CCMC и REST API.

## О проекте

Приложение предназначено для работы консультантов и администраторов. Включает авторизацию, раздельные роли (консультант/администратор) и взаимодействие с серверной частью через API.

## Технологии

- Android (Kotlin)
- Backend API (VS Code)
- База данных CCMC

## Функционал

- Авторизация (LoginActivity)
- Роль "Консультант" (ConsultantActivity)
- Роль "Администратор" (AdminActivity)
- Главный экран (MainActivity)

## Установка и запуск

Требования:
- Android Studio Hedgehog | 2023.1.1 или новее
- Android SDK API 24+
- JDK 17

## Структура проекта

ConsultantApp/
├── app/
│   ├── src/main/java/com/example/consultantapp/
│   │   ├── AdminActivity.kt
│   │   ├── ConsultantActivity.kt
│   │   ├── LoginActivity.kt
│   │   └── MainActivity.kt
│   └── src/main/res/
│       ├── layout/
│       ├── drawable/
│       └── values/
├── gradle/
├── build.gradle.kts
└── settings.gradle.kts

## Контакты

Автор: Alllixy
GitHub: https://github.com/Alllixy

Примечание: Для работы приложения необходим запущенный бэкенд-сервер. Убедитесь, что API запущен перед использованием приложения.
