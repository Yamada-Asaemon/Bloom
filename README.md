<div align="center">

# 🌸 Bloom

**Трекер отказа от плохих привычек**

Следите за тем, сколько времени вы держитесь, и растите своё «цветение» день за днём.

[

![Build APK](https://github.com/Yamada-Asaemon/Bloom/actions/workflows/build.yml/badge.svg)

](https://github.com/Yamada-Asaemon/Bloom/actions/workflows/build.yml)


![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-7F52FF?logo=kotlin&logoColor=white)




![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.06-4285F4?logo=jetpackcompose&logoColor=white)




![Android](https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&logoColor=white)



</div>

---

## ✨ Возможности

- ⏱ **Живой таймер** отказа: дни, часы, минуты, секунды
- 🟣 **Кольцо прогресса** с вехами 1 · 7 · 14 · 30 · 60 · 90 · 120 · 150 … 365 дней, дальше каждые 30
- 🎨 **Цвет = сила зависимости**: 🔴 сильная, 🟡 средняя, 🟢 лёгкая
- 👆 **Свайп** по кольцу переключает привычки, снизу точки-индикатор
- 📜 **История**: дата и время создания привычки
- 🗑 **Удаление** с подтверждением
- 🌗 **Тёмная и светлая тема**
- 🌍 **Два языка**: русский и татарский
- 💾 **Автосохранение**: данные остаются после перезапуска

## 🧰 Технологии

| | |
|---|---|
| Язык | Kotlin 1.9.24 |
| UI | Jetpack Compose (BOM 2024.06.00) |
| Сборка | Gradle 8.9 · Android Gradle Plugin 8.5.2 |
| Android | minSdk 24 · targetSdk 34 |
| Хранение | SharedPreferences (JSON) |
| CI | GitHub Actions |

## 📁 Структура проекта

```
Bloom/
├── .github/workflows/build.yml     # сборка APK в GitHub Actions
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/bloom/app/
│       │   ├── MainActivity.kt     # точка входа
│       │   ├── BloomApp.kt         # главный экран, меню, модальные окна
│       │   ├── Screens.kt          # пустой экран и экран привычки
│       │   ├── Components.kt       # тема и общие компоненты
│       │   ├── Data.kt             # модель, логика вех, хранение
│       │   └── Strings.kt          # переводы (ru / tt)
│       └── res/
│           ├── drawable/           # векторные иконки
│           ├── drawable-nodpi/     # app_icon.png, empty.png
│           └── values/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## 🖼 Картинки

Положите свои файлы в `app/src/main/res/drawable-nodpi/`:

| Файл | Назначение |
|---|---|
| `app_icon.png` | иконка приложения и картинка внутри кольца |
| `empty.png` | картинка на пустом экране |

> Имена файлов должны остаться прежними, иначе сборка не найдёт ресурсы.

## 🚀 Сборка APK через GitHub Actions

1. Загрузите проект в репозиторий (ветка `main`).
2. Откройте вкладку **Actions** → **Build APK**.
   Сборка запускается сама после каждого коммита, или вручную кнопкой **Run workflow**.
3. Когда появится зелёная галочка, откройте запуск и внизу страницы в **Artifacts** скачайте `Bloom-debug-apk`.
4. Распакуйте архив и установите `.apk` на телефон (разрешите установку из неизвестных источников).

> Gradle wrapper в проекте не нужен: workflow сам ставит Gradle 8.9.

## 🛠 Сборка локально

Нужны JDK 17 и Android SDK 34.

```bash
gradle assembleDebug
```

Готовый файл: `app/build/outputs/apk/debug/app-debug.apk`

## ⚙️ Настройка

**Сменить имя пакета.** Поменяйте `namespace` и `applicationId` в `app/build.gradle.kts`, переименуйте папки `com/bloom/app` в `app/src/main/java` и строку `package` в начале каждого `.kt` файла.

**Добавить язык.** В `Strings.kt` создайте новый словарь по образцу `ru` и добавьте его в `translations` и `languageNames`.

**Изменить вехи прогресса.** Список `milestones` в `Data.kt`.

---

<div align="center">
Сделано с 💜 на Kotlin
</div>
