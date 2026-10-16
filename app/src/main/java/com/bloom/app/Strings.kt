package com.bloom.app

val languageNames: Map<String, String> = linkedMapOf(
    "ru" to "русский",
    "tt" to "татарский"
)

private val ru = mapOf(
    "emptyText" to "добавьте первую привычку",
    "days" to "дни",
    "hours" to "часы",
    "minutes" to "минуты",
    "seconds" to "секунды",
    "history" to "история",
    "created" to "привычка создана:",
    "addHabit" to "добавить привычку",
    "theme" to "тема",
    "language" to "язык",
    "habitName" to "название привычки",
    "enterName" to "введите название",
    "charLimit" to "можно ввести лишь 25 символов!",
    "color" to "цвет",
    "add" to "добавить",
    "infoTitle" to "Информация",
    "strongDep" to "сильная зависимость",
    "mediumDep" to "средняя зависимость",
    "lightDep" to "лёгкая зависимость",
    "deleteConfirm" to "вы точно хотите удалить привычку?",
    "yes" to "да",
    "no" to "нет"
)

private val tt = mapOf(
    "emptyText" to "беренче гадәтегезне өстәгез",
    "days" to "көннәр",
    "hours" to "сәгатьләр",
    "minutes" to "минутлар",
    "seconds" to "секундлар",
    "history" to "тарих",
    "created" to "гадәт булдырылган:",
    "addHabit" to "гадәт өстәү",
    "theme" to "тема",
    "language" to "тел",
    "habitName" to "гадәт исеме",
    "enterName" to "исемне керетегез",
    "charLimit" to "бары 25 символ гына мөмкин!",
    "color" to "төс",
    "add" to "өстәү",
    "infoTitle" to "Мәгълүмат",
    "strongDep" to "көчле бәйлелек",
    "mediumDep" to "уртача бәйлелек",
    "lightDep" to "җиңел бәйлелек",
    "deleteConfirm" to "гадәтне чыннан да бетерергә телисезме?",
    "yes" to "әйе",
    "no" to "юк"
)

private val translations = mapOf("ru" to ru, "tt" to tt)

fun tr(lang: String, key: String): String =
    translations[lang]?.get(key) ?: ru[key] ?: key
