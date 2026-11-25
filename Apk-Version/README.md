# Apk-Version

Нативная Android-версия мини-панели поиска изображений на Jetpack Compose.

## Возможности

- Поиск по встроенному mock-набору и Unsplash API.
- Ключ Unsplash вводится прямо в приложении через настройки.
- Фильтры: Все / Фото / Иллюстрации / Текстуры.
- Копирование изображения в буфер обмена нативно.
- Тёмная тема в стиле liquid glass.
- Перетаскивание карточки за шапку.

## Сборка

Сборка выполняется через GitHub Actions: `.github/workflows/build-apk.yml`.

Локально:

```bash
cd Apk-Version
gradle assembleDebug
```
