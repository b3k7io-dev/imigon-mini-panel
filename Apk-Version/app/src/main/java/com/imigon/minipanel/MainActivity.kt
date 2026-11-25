package com.imigon.minipanel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

data class ImageItem(
    val id: String,
    val title: String,
    val category: String,
    val tags: List<String>,
    val url: String,
    val thumb: String
)

enum class ImageCategory(val key: String, val label: String) {
    ALL("all", "Все"),
    PHOTOS("photos", "Фото"),
    ILLUSTRATIONS("illustrations", "Иллюстрации"),
    TEXTURES("textures", "Текстуры")
}

private val MOCK_ITEMS = listOf(
    ImageItem("m1", "Горный пейзаж", "photos", listOf("горы", "природа", "пейзаж", "фото"), "https://picsum.photos/seed/mountain/900/600", "https://picsum.photos/seed/mountain/420/280"),
    ImageItem("m2", "Городская неоновая улица", "photos", listOf("город", "неон", "ночь", "улица", "фото"), "https://picsum.photos/seed/neoncity/900/600", "https://picsum.photos/seed/neoncity/420/280"),
    ImageItem("m3", "Спокойное озеро", "photos", listOf("озеро", "вода", "природа", "пейзаж", "фото"), "https://picsum.photos/seed/lake/900/600", "https://picsum.photos/seed/lake/420/280"),
    ImageItem("m4", "Абстрактные формы", "illustrations", listOf("абстракция", "формы", "цвет", "иллюстрация"), "https://picsum.photos/seed/abstract/900/600", "https://picsum.photos/seed/abstract/420/280"),
    ImageItem("m5", "Иллюстрация космоса", "illustrations", listOf("космос", "звезды", "галактика", "иллюстрация"), "https://picsum.photos/seed/space/900/600", "https://picsum.photos/seed/space/420/280"),
    ImageItem("m6", "Цветочные узоры", "illustrations", listOf("цветы", "узор", "растения", "иллюстрация"), "https://picsum.photos/seed/floral/900/600", "https://picsum.photos/seed/floral/420/280"),
    ImageItem("m7", "Бетонная текстура", "textures", listOf("бетон", "текстура", "материал", "серый"), "https://picsum.photos/seed/concrete/900/600", "https://picsum.photos/seed/concrete/420/280"),
    ImageItem("m8", "Деревянная поверхность", "textures", listOf("дерево", "текстура", "материал", "коричневый"), "https://picsum.photos/seed/wood/900/600", "https://picsum.photos/seed/wood/420/280"),
    ImageItem("m9", "Тканевое плетение", "textures", listOf("ткань", "плетение", "текстура", "материал"), "https://picsum.photos/seed/fabric/900/600", "https://picsum.photos/seed/fabric/420/280"),
    ImageItem("m10", "Морской закат", "photos", listOf("море", "закат", "небо", "пейзаж", "фото"), "https://picsum.photos/seed/sunset/900/600", "https://picsum.photos/seed/sunset/420/280"),
    ImageItem("m11", "Геометрический паттерн", "illustrations", listOf("геометрия", "паттерн", "формы", "иллюстрация"), "https://picsum.photos/seed/geometry/900/600", "https://picsum.photos/seed/geometry/420/280"),
    ImageItem("m12", "Мраморная текстура", "textures", listOf("мрамор", "текстура", "камень", "материал"), "https://picsum.photos/seed/marble/900/600", "https://picsum.photos/seed/marble/420/280"),
    ImageItem("m13", "Лесная тропа", "photos", listOf("лес", "тропа", "природа", "фото"), "https://picsum.photos/seed/forest/900/600", "https://picsum.photos/seed/forest/420/280"),
    ImageItem("m14", "Снежные вершины", "photos", listOf("снег", "горы", "зима", "пейзаж", "фото"), "https://picsum.photos/seed/snowpeaks/900/600", "https://picsum.photos/seed/snowpeaks/420/280"),
    ImageItem("m15", "Акварельный градиент", "illustrations", listOf("акварель", "градиент", "цвет", "иллюстрация"), "https://picsum.photos/seed/watercolor/900/600", "https://picsum.photos/seed/watercolor/420/280"),
    ImageItem("m16", "Металлическая поверхность", "textures", listOf("металл", "текстура", "материал", "блеск"), "https://picsum.photos/seed/metal/900/600", "https://picsum.photos/seed/metal/420/280")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                MiniPanelScreen()
            }
        }
    }
}

@Composable
fun MiniPanelScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val prefs = remember { context.getSharedPreferences("minipanel", Context.MODE_PRIVATE) }

    var apiKey by remember { mutableStateOf(prefs.getString("unsplash_key", "") ?: "") }
    var showSettings by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf(prefs.getString("query", "") ?: "") }
    var selectedCategory by remember { mutableStateOf(ImageCategory.ALL) }
    var items by remember { mutableStateOf(MOCK_ITEMS) }
    var loading by remember { mutableStateOf(false) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var entered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val savedCategory = prefs.getString("category", ImageCategory.ALL.key)
        selectedCategory = ImageCategory.values().firstOrNull { it.key == savedCategory } ?: ImageCategory.ALL
        delay(80)
        entered = true
        loadResults(context, scope, apiKey, query, selectedCategory, items, { newItems -> items = newItems }, { newLoading -> loading = newLoading }, { message -> scope.launch { snackbarHostState.showSnackbar(message) } })
    }

    fun persistState() {
        prefs.edit().putString("query", query).putString("category", selectedCategory.key).apply()
    }

    fun runSearch() {
        persistState()
        scope.launch {
            loadResults(context, scope, apiKey, query, selectedCategory, items, { newItems -> items = newItems }, { newLoading -> loading = newLoading }, { message -> scope.launch { snackbarHostState.showSnackbar(message) } })
        }
    }

    val accentBrush = Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6), Color(0xFFEC4899)))

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF111218), Color(0xFF050507), Color(0xFF000000))
                    )
                )
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = entered,
                enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + scaleIn(initialScale = 0.92f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
                exit = fadeOut() + scaleOut()
            ) {
                Surface(
                    modifier = Modifier
                        .widthIn(max = 380.dp)
                        .fillMaxWidth(0.92f)
                        .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                        .shadow(32.dp, RoundedCornerShape(30.dp), ambientColor = Color.Black, spotColor = Color(0x668B5CF6))
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color(0xCC12131A))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(30.dp)),
                    color = Color(0xCC12131A),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Drag handle and header.
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectDragGestures { _, dragAmount ->
                                        offset += dragAmount
                                    }
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(52.dp)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(accentBrush)
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = query,
                                    onValueChange = { query = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Найти изображения") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    trailingIcon = {
                                        if (query.isNotBlank()) {
                                            IconButton(onClick = { query = ""; runSearch() }) {
                                                Icon(Icons.Default.Close, contentDescription = "Очистить поиск")
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(999.dp)
                                )
                                IconButton(onClick = { showSettings = true }) {
                                    Icon(Icons.Default.Settings, contentDescription = "Настройки")
                                }
                            }
                        }

                        // Filter chips.
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ImageCategory.values().forEach { category ->
                                val selected = selectedCategory == category
                                Surface(
                                    modifier = Modifier
                                        .height(44.dp)
                                        .clickable {
                                            selectedCategory = category
                                            runSearch()
                                        },
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (selected) Color.Transparent else Color.White.copy(alpha = 0.08f),
                                    border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .then(if (selected) Modifier.background(accentBrush, RoundedCornerShape(999.dp)) else Modifier)
                                            .padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = category.label,
                                            color = Color.White,
                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        // Results.
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 220.dp, max = 420.dp)
                        ) {
                            if (loading) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    repeat(4) {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(4f / 3f),
                                            shape = RoundedCornerShape(18.dp),
                                            color = Color.White.copy(alpha = 0.06f)
                                        ) {}
                                    }
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    contentPadding = PaddingValues(0.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(items, key = { it.id }) { item ->
                                        ImageCard(
                                            item = item,
                                            onCopy = {
                                                copyImage(context, item, scope, snackbarHostState)
                                            }
                                        )
                                    }
                                }
                            }

                            if (!loading && items.isEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("Ничего не найдено.", color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Попробуйте другой запрос, тег или фильтр.", color = Color.White.copy(alpha = 0.72f), fontSize = 13.sp)
                                    Spacer(Modifier.height(12.dp))
                                    Button(onClick = {
                                        query = ""
                                        selectedCategory = ImageCategory.ALL
                                        runSearch()
                                    }) {
                                        Text("Показать всё")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            confirmButton = {
                TextButton(onClick = {
                    prefs.edit().putString("unsplash_key", apiKey).apply()
                    showSettings = false
                    runSearch()
                }) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { showSettings = false }) { Text("Закрыть") }
            },
            title = { Text("Настройки") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ключ Unsplash API", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        singleLine = true,
                        placeholder = { Text("Оставьте пустым для локальных данных") }
                    )
                }
            }
        )
    }
}

@Composable
private fun ImageCard(item: ImageItem, onCopy: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )
    Box(
        modifier = Modifier
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.06f))
    ) {
        AsyncImage(
            model = item.thumb,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                    )
                )
        )
        Text(
            text = item.title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        )
        IconButton(
            onClick = onCopy,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(44.dp)
                .shadow(12.dp, RoundedCornerShape(999.dp))
                .background(
                    Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6), Color(0xFFEC4899))),
                    RoundedCornerShape(999.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Скопировать",
                tint = Color.White
            )
        }
    }
}

private suspend fun loadResults(
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    apiKey: String,
    query: String,
    category: ImageCategory,
    currentItems: List<ImageItem>,
    setItems: (List<ImageItem>) -> Unit,
    setLoading: (Boolean) -> Unit,
    showToast: (String) -> Unit
) {
    setLoading(true)
    try {
        if (apiKey.isNotBlank()) {
            val result = withContext(Dispatchers.IO) { fetchUnsplash(apiKey, query, category) }
            setItems(result)
        } else {
            delay(180)
            setItems(filterMock(query, category))
        }
    } catch (error: Exception) {
        if (apiKey.isNotBlank()) {
            if (currentItems.isNotEmpty()) {
                showToast("Не удалось обновить результаты. Показаны предыдущие.")
            } else {
                setItems(filterMock(query, category))
                showToast("Unsplash недоступен, показаны локальные результаты")
            }
        } else {
            setItems(emptyList())
        }
    } finally {
        setLoading(false)
    }
}

private fun filterMock(query: String, category: ImageCategory): List<ImageItem> {
    val normalized = query.trim().lowercase()
    return MOCK_ITEMS.filter { item ->
        val categoryMatch = category == ImageCategory.ALL || item.category == category.key
        val haystack = listOf(item.title, item.category) + item.tags
        val queryMatch = normalized.isBlank() || haystack.any { it.lowercase().contains(normalized) }
        categoryMatch && queryMatch
    }
}

private fun fetchUnsplash(apiKey: String, query: String, category: ImageCategory): List<ImageItem> {
    val categoryWord = when (category) {
        ImageCategory.PHOTOS -> "photo"
        ImageCategory.ILLUSTRATIONS -> "illustration"
        ImageCategory.TEXTURES -> "texture"
        ImageCategory.ALL -> ""
    }
    val searchQuery = listOf(query.trim(), categoryWord).filter { it.isNotBlank() }.joinToString(" ").ifBlank { "wallpaper" }
    val connection = URL("https://api.unsplash.com/search/photos?query=${java.net.URLEncoder.encode(searchQuery, "UTF-8")}&per_page=18&orientation=landscape").openConnection() as HttpURLConnection
    connection.setRequestProperty("Authorization", "Client-ID $apiKey")
    connection.connectTimeout = 10000
    connection.readTimeout = 10000
    val responseCode = connection.responseCode
    if (responseCode !in 200..299) {
        throw Exception("HTTP $responseCode")
    }
    val text = connection.inputStream.bufferedReader().readText()
    val json = JSONObject(text)
    val results = json.optJSONArray("results") ?: return emptyList()
    val items = mutableListOf<ImageItem>()
    for (index in 0 until results.length()) {
        val photo = results.getJSONObject(index)
        val urls = photo.optJSONObject("urls")
        items += ImageItem(
            id = photo.optString("id", index.toString()),
            title = photo.optString("alt_description").ifBlank { photo.optString("description").ifBlank { "Изображение Unsplash" } },
            category = if (category == ImageCategory.ALL) "photos" else category.key,
            tags = emptyList(),
            url = urls?.optString("regular").orEmpty().ifBlank { urls?.optString("small").orEmpty() },
            thumb = urls?.optString("small").orEmpty().ifBlank { urls?.optString("thumb").orEmpty() }
        )
    }
    return items
}

private fun copyImage(context: Context, item: ImageItem, scope: kotlinx.coroutines.CoroutineScope, snackbarHostState: SnackbarHostState) {
    scope.launch {
        val copied = withContext(Dispatchers.IO) {
            try {
                val directory = File(context.cacheDir, "images").apply { mkdirs() }
                val file = File(directory, "${item.id}.jpg")
                val connection = URL(item.url).openConnection() as HttpURLConnection
                connection.inputStream.use { input ->
                    FileOutputStream(file).use { output -> input.copyTo(output) }
                }
                val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newUri(context.contentResolver, "image", uri))
                true
            } catch (error: Exception) {
                false
            }
        }

        if (copied) {
            snackbarHostState.showSnackbar("Скопировано в буфер")
        } else {
            try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("url", item.url))
                snackbarHostState.showSnackbar("Ссылка скопирована")
            } catch (error: Exception) {
                snackbarHostState.showSnackbar("Не удалось скопировать")
            }
        }
    }
}
