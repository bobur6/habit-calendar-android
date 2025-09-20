# Habit Calendar Checker

Android приложение для отслеживания привычек с календарем, разработанное на Kotlin с использованием Jetpack Compose.

## 🚀 Особенности

- **Современный UI**: Jetpack Compose с Material Design 3
- **Аутентификация**: Регистрация и вход пользователей
- **База данных**: Room для локального хранения данных
- **Архитектура**: MVVM с использованием ViewModel и LiveData
- **Навигация**: Navigation Compose для управления экранами

## 🛠 Технологический стек

- **Язык**: Kotlin
- **UI**: Jetpack Compose
- **База данных**: Room
- **Архитектура**: MVVM
- **DI**: Hilt (если используется)
- **Навигация**: Navigation Compose
- **Сеть**: Retrofit (для будущих API запросов)

## 📱 Функциональность

- Создание и управление привычками
- Календарь для отметки выполнения привычек
- Система аутентификации пользователей
- Профиль пользователя
- Статистика и отслеживание прогресса

## 🔧 Требования

- Android Studio Arctic Fox или новее
- Android SDK 25+
- Kotlin 1.9.0+

## 📦 Установка и запуск

1. Клонируйте репозиторий:
```bash
git clone https://github.com/[your-username]/habit-calendar-android.git
```

2. Откройте проект в Android Studio

3. Синхронизируйте Gradle файлы

4. Запустите приложение на эмуляторе или устройстве

## 📁 Структура проекта

```
app/src/main/java/com/bobur/habitcalendarchecker/
├── data/
│   ├── auth/          # Аутентификация
│   ├── database/      # Room база данных
│   ├── model/         # Модели данных
│   └── repository/    # Репозитории
├── ui/
│   ├── components/    # Переиспользуемые компоненты
│   ├── navigation/    # Навигация
│   ├── screens/       # Экраны приложения
│   ├── theme/         # Тема и стили
│   └── viewmodels/    # ViewModels
└── MainActivity.kt
```

## 🤝 Вклад в проект

Если вы хотите внести вклад в проект:

1. Сделайте Fork репозитория
2. Создайте ветку для новой функции (`git checkout -b feature/AmazingFeature`)
3. Зафиксируйте изменения (`git commit -m 'Add some AmazingFeature'`)
4. Отправьте в ветку (`git push origin feature/AmazingFeature`)
5. Откройте Pull Request

## 📄 Лицензия

Этот проект создан в образовательных целях.

## 👨‍💻 Автор

Bobur - [GitHub Profile](https://github.com/[your-username])