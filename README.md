# Мини-панель поиска изображений

Компактное плавающее окно (HTML/CSS/JS, Tailwind через CDN) в стиле Gemini / Material You. Работает без сборки: откройте `index.html` в WebView или браузере.

## Файлы

- `index.html` — разметка панели, иконки, точки монтирования.
- `styles.css` — стеклянный стиль, анимации, адаптивность, reduced motion.
- `app.js` — данные, фильтрация, копирование, перетаскивание.
- `README.md` — эта инструкция.

## Локальный просмотр

Откройте `index.html` в Chrome/WebView. Панель появляется по центру, её можно перетаскивать за шапку. Поиск фильтрует встроенный mock-набор и работает как в браузере: ввод мгновенно фильтрует результаты, Enter запускает поиск, кнопка очистки сбрасывает запрос.

Ключ Unsplash можно указать двумя способами:

- для локального сервера: заполните `.env`;
- для режима `file://` или WebView assets: заполните `config.js`.

Если ключ пустой, используется встроенный mock-набор. Папка `design/` содержит описание визуальных слоёв и токенов.

## Встраивание в APK

### Вариант 1: Capacitor

1. Установите Capacitor и создайте проект:
   ```bash
   npm init -y
   npm install @capacitor/core @capacitor/cli @capacitor/android
   npx cap init ImagePanel com.example.imagepanel --web-dir .
   ```
2. Скопируйте `index.html`, `styles.css`, `app.js` в web-директорию проекта.
3. Добавьте Android:
   ```bash
   npx cap add android
   npx cap sync
   npx cap open android
   ```
4. В Android Studio соберите APK/AAB. Для офлайн-работы Tailwind CDN можно заменить локальным файлом Tailwind, если WebView без интернета.

### Вариант 2: Минимальная WebView Activity

Создайте Activity с прозрачным/плавающим окном и загрузите локальный HTML:

```kotlin
class MiniPanelActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.addJavascriptInterface(AndroidBridge(this), "AndroidBridge")
        webView.loadUrl("file:///android_asset/index.html")
        setContentView(webView)
    }
}
```

Поместите файлы в `app/src/main/assets/`. Зарегистрируйте Activity в `AndroidManifest.xml`:

```xml
<activity
    android:name=".MiniPanelActivity"
    android:theme="@style/Theme.MiniPanel.Floating"
    android:exported="false" />
```

## Плавающее окно поверх других приложений

Есть два основных подхода.

### A. WindowManager overlay (рекомендуется для настоящего overlay)

Используйте `TYPE_APPLICATION_OVERLAY`. Требуется разрешение `SYSTEM_ALERT_WINDOW`.

```kotlin
val params = WindowManager.LayoutParams(
    WindowManager.LayoutParams.WRAP_CONTENT,
    WindowManager.LayoutParams.WRAP_CONTENT,
    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
    PixelFormat.TRANSLUCENT
)
params.gravity = Gravity.CENTER
windowManager.addView(overlayView, params)
```

`overlayView` можно сделать через `WebView`, загрузив локальный HTML. Для перетаскивания окна используйте `WindowManager` drag или обработку касаний в WebView.

### B. Activity с `windowIsFloating=true`

Подходит для простого мини-окна внутри приложения. В `styles.xml`:

```xml
<style name="Theme.MiniPanel.Floating" parent="Theme.Material3.Dark.NoActionBar">
    <item name="android:windowIsFloating">true</item>
    <item name="android:windowBackground">@android:color/transparent</item>
    <item name="android:backgroundDimEnabled">true</item>
    <item name="android:windowCloseOnTouchOutside">true</item>
</style>
```

Activity запускается с небольшим размером и прозрачным фоном. HTML-панель при этом всё равно остаётся компактной, а не full-screen.

## Quick Settings Tile

Добавьте `TileService`, который запускает мини-окно или показывает overlay:

```kotlin
class MiniPanelTileService : TileService() {
    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MiniPanelActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivityAndCollapse(intent)
    }
}
```

Регистрация в манифесте:

```xml
<service
    android:name=".MiniPanelTileService"
    android:permission="android.permission.BIND_QUICK_SETTINGS_TILE"
    android:exported="true">
    <intent-filter>
        <action android:name="android.service.quicksettings.action.QS_TILE" />
    </intent-filter>
</service>
```

## Нативный мост

HTML вызывает нативный override через:

```js
window.AndroidBridge.copyImage(url)
```

Реализуйте его в Kotlin/Java:

```kotlin
class AndroidBridge(private val context: Context) {
    @JavascriptInterface
    fun copyImage(url: String) {
        // Скачайте файл, поместите в MediaStore или кэш,
        // затем скопируйте Uri/Bitmap в ClipboardManager.
    }
}
```

Плитка QS и clipboard должны реализовываться нативно на Kotlin/Java, как требует Android. Веб-часть остаётся только UI-оболочкой.

## Разрешения

Для overlay поверх других приложений:

```xml
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

Проверка разрешения:

```kotlin
if (!Settings.canDrawOverlays(context)) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${context.packageName}")
    )
    startActivity(intent)
}
```

## Замечания по доступности

- Все интерактивные элементы имеют размер не меньше 44 px.
- Поддерживается `prefers-reduced-motion`.
- Панель не full-screen и не блокирует весь экран.
- Фон затемнён и размыт, но сама панель — основная интерактивная поверхность.
