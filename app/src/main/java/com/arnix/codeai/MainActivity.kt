package com.arnix.codeai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ArnixApp() }
    }
}

private data class ChatLine(val role: String, val text: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArnixApp() {
    val bg = Color(0xFF0B1020)
    val panel = Color(0xFF151D31)
    val accent = Color(0xFF70A7FF)
    var baseUrl by remember { mutableStateOf("http://127.0.0.1:3001/v1") }
    var apiKey by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("auto") }
    var prompt by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Подключите FreeLLMAPI или другой OpenAI-compatible API") }
    val messages = remember { mutableStateListOf(ChatLine("assistant", "Здравствуйте! Я ARNIX Code AI. Опишите, какой сайт, приложение или программу хотите создать. Сначала составлю план, затем будем добавлять инструменты работы с проектом.")) }
    val scope = rememberCoroutineScope()

    MaterialTheme(colorScheme = darkColorScheme(
        primary = accent, background = bg, surface = panel,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Scaffold(containerColor = bg, topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("ARNIX Code AI", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("AI DEVELOPMENT WORKSPACE", fontSize = 10.sp, color = Color.LightGray)
                    }
                },
                actions = { TextButton(onClick = { showSettings = true }) { Text("Настройки") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bg, titleContentColor = Color.White)
            )
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusCard("МОДЕЛЬ", model, Modifier.weight(1f), panel)
                    StatusCard("РЕЖИМ", "AI CHAT", Modifier.weight(1f), panel)
                    StatusCard("ПРОЕКТ", "Локальный", Modifier.weight(1f), panel)
                }
                Spacer(Modifier.height(12.dp))
                Text("Рабочая область", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text("Задачи • код • анализ • исправления", color = Color.Gray, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth()
                        .background(panel, RoundedCornerShape(18.dp)).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { line ->
                        val user = line.role == "user"
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = if (user) Alignment.End else Alignment.Start
                        ) {
                            Text(if (user) "ВЫ" else "ARNIX AI", color = if (user) accent else Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(3.dp))
                            Text(
                                line.text,
                                color = Color.White,
                                modifier = Modifier.widthIn(max = 310.dp)
                                    .background(if (user) Color(0xFF203B63) else Color(0xFF202A40), RoundedCornerShape(12.dp))
                                    .padding(11.dp)
                            )
                        }
                    }
                    if (busy) item { Text("ИИ думает…", color = accent) }
                }
                Spacer(Modifier.height(10.dp))
                Text(status, color = Color.Gray, fontSize = 11.sp)
                Spacer(Modifier.height(7.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f).background(panel, RoundedCornerShape(14.dp)).padding(12.dp)) {
                        if (prompt.isEmpty()) Text("Опишите задачу или вставьте код…", color = Color.Gray)
                        BasicTextField(
                            value = prompt, onValueChange = { prompt = it },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 42.dp, max = 110.dp),
                            textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 14.sp)
                        )
                    }
                    Button(
                        enabled = !busy && prompt.isNotBlank(),
                        onClick = {
                            val question = prompt.trim()
                            prompt = ""
                            messages.add(ChatLine("user", question))
                            busy = true
                            scope.launch {
                                val answer = withContext(Dispatchers.IO) {
                                    runCatching { callChat(baseUrl, apiKey, model, messages.toList()) }
                                        .getOrElse { "Ошибка API: ${it.message ?: "не удалось выполнить запрос"}" }
                                }
                                messages.add(ChatLine("assistant", answer))
                                status = if (answer.startsWith("Ошибка API:")) "Проверьте URL, ключ и доступность сервера" else "Ответ получен"
                                busy = false
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 15.dp)
                    ) { Text("➤") }
                }
            }
        }

        if (showSettings) {
            AlertDialog(
                onDismissRequest = { showSettings = false },
                title = { Text("Подключение ИИ") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("OpenAI-compatible Base URL")
                        OutlinedTextField(value = baseUrl, onValueChange = { baseUrl = it }, singleLine = true, placeholder = { Text("https://server/v1") })
                        Text("API key")
                        OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, singleLine = true, placeholder = { Text("Ваш ключ") })
                        Text("Модель (auto поддерживается FreeLLMAPI)")
                        OutlinedTextField(value = model, onValueChange = { model = it }, singleLine = true)
                        Text("Ключ хранится только в памяти экрана и не записывается в репозиторий.", fontSize = 11.sp, color = Color.Gray)
                    }
                },
                confirmButton = { TextButton(onClick = { showSettings = false }) { Text("Готово") } },
                dismissButton = { TextButton(onClick = {
                    scope.launch {
                        status = "Проверка подключения…"
                        val result = withContext(Dispatchers.IO) {
                            runCatching { fetchModels(baseUrl, apiKey) }.fold({ "Подключено: ${it.take(4).joinToString()}" }, { "Ошибка: ${it.message}" })
                        }
                        status = result
                    }
                }) { Text("Проверить") } }
            )
        }
    }
}

@Composable
private fun StatusCard(label: String, value: String, modifier: Modifier, panel: Color) {
    Column(modifier.background(panel, RoundedCornerShape(12.dp)).padding(10.dp)) {
        Text(label, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

private fun fetchModels(base: String, key: String): List<String> {
    val connection = (URL(base.trimEnd('/') + "/models").openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 15000
        readTimeout = 15000
        if (key.isNotBlank()) setRequestProperty("Authorization", "Bearer $key")
    }
    try {
        val body = connection.inputStream.bufferedReader().use { it.readText() }
        val data = JSONObject(body).optJSONArray("data") ?: JSONArray()
        return (0 until data.length()).mapNotNull { data.optJSONObject(it)?.optString("id") }
    } finally { connection.disconnect() }
}

private fun callChat(base: String, key: String, model: String, history: List<ChatLine>): String {
    require(base.startsWith("http://") || base.startsWith("https://")) { "Base URL должен начинаться с http:// или https://" }
    require(key.isNotBlank()) { "Введите API key в настройках" }
    val url = URL(base.trimEnd('/') + "/chat/completions")
    val connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 20000
        readTimeout = 120000
        doOutput = true
        setRequestProperty("Content-Type", "application/json")
        setRequestProperty("Authorization", "Bearer $key")
    }
    try {
        val msgs = JSONArray()
        history.forEach { msgs.put(JSONObject().put("role", it.role).put("content", it.text)) }
        val payload = JSONObject().put("model", model.ifBlank { "auto" }).put("messages", msgs)
            .put("temperature", 0.3)
        connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val body = stream.bufferedReader().use { it.readText() }
        if (code !in 200..299) error("HTTP $code: ${body.take(500)}")
        val root = JSONObject(body)
        return root.getJSONArray("choices").getJSONObject(0).getJSONObject("message").optString("content", "Пустой ответ")
    } finally { connection.disconnect() }
}
