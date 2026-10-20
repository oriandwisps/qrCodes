package com.neonoxus.qrcodes

enum class Lang(val label: String) { EN("English"), RU("Русский") }

private val en = mapOf(
    "create" to "Create", "scan" to "Scan", "settings" to "Settings",
    "data_hint" to "Text or link", "preview" to "Preview",
    "empty_hint" to "Enter data to see the preview",
    "too_big" to "Data is too long for a QR code",
    "ecc" to "Error correction", "size" to "Size", "margin" to "Margin",
    "shape" to "Module shape", "square" to "Square", "rounded" to "Rounded", "circle" to "Dots",
    "fg" to "Foreground color", "bg" to "Background color", "transparent" to "Transparent background",
    "download" to "Download PNG", "saved" to "Saved to Pictures/qrCodes",
    "save_failed" to "Could not save the image",
    "upload" to "Upload photo", "camera" to "Camera", "stop" to "Stop camera",
    "scan_hint" to "Upload a photo or film a QR code",
    "no_qr" to "No QR code found", "result" to "Scan result",
    "open_link" to "Open link", "copy" to "Copy", "copied" to "Copied", "close" to "Close",
    "camera_perm" to "Camera permission is required",
    "theme" to "Theme", "dark" to "Dark", "light" to "Light",
    "accent" to "Accent color", "default" to "Default", "red" to "Red", "yellow" to "Yellow",
    "green" to "Green", "blue" to "Blue", "purple" to "Purple", "language" to "Language"
)

private val ru = mapOf(
    "create" to "Создать", "scan" to "Скан", "settings" to "Настройки",
    "data_hint" to "Текст или ссылка", "preview" to "Предпросмотр",
    "empty_hint" to "Введите данные для предпросмотра",
    "too_big" to "Слишком много данных для QR-кода",
    "ecc" to "Коррекция ошибок", "size" to "Размер", "margin" to "Отступ",
    "shape" to "Форма модулей", "square" to "Квадраты", "rounded" to "Скруглённые", "circle" to "Точки",
    "fg" to "Цвет кода", "bg" to "Цвет фона", "transparent" to "Прозрачный фон",
    "download" to "Скачать PNG", "saved" to "Сохранено в Pictures/qrCodes",
    "save_failed" to "Не удалось сохранить изображение",
    "upload" to "Загрузить фото", "camera" to "Камера", "stop" to "Остановить камеру",
    "scan_hint" to "Загрузите фото или наведите камеру на QR-код",
    "no_qr" to "QR-код не найден", "result" to "Результат",
    "open_link" to "Открыть ссылку", "copy" to "Копировать", "copied" to "Скопировано", "close" to "Закрыть",
    "camera_perm" to "Нужно разрешение на камеру",
    "theme" to "Тема", "dark" to "Тёмная", "light" to "Светлая",
    "accent" to "Цвет акцента", "default" to "По умолчанию", "red" to "Красный", "yellow" to "Жёлтый",
    "green" to "Зелёный", "blue" to "Синий", "purple" to "Фиолетовый", "language" to "Язык"
)

fun tr(lang: Lang, key: String): String =
    (if (lang == Lang.RU) ru else en)[key] ?: key
	