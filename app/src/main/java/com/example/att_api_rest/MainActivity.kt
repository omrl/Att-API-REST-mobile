package com.example.att_api_rest

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.att_api_rest.controller.JokeController
import com.example.att_api_rest.controller.JokeData
import com.example.att_api_rest.ui.theme.AttAPIRESTTheme
import kotlinx.coroutines.launch

// High Contrast / Safe Light Mode Palette
val AppleLightBg = Color(0xFFF5F5F7)
val AppleWhite = Color(0xFFFFFFFF)
val AppleText = Color(0xFF1D1D1F)
val ProBlue = Color(0xFF007AFF) // Apple Blue
val ProGreen = Color(0xFF34C759) // Apple Green
val ProRed = Color(0xFFFF3B30) // Apple Red

class MainActivity : ComponentActivity() {
    private val jokeController = JokeController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AttAPIRESTTheme {
                var currentScreen by remember { mutableStateOf("Jokes") }
                
                Surface(color = AppleLightBg) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent,
                        bottomBar = { SafeNavBar(currentScreen) { currentScreen = it } }
                    ) { innerPadding ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Subtle background accents for depth
                            Box(modifier = Modifier.size(300.dp).offset(x = 150.dp, y = (-50).dp).blur(100.dp).background(ProBlue.copy(alpha = 0.05f), RoundedCornerShape(150.dp)))
                            
                            if (currentScreen == "Jokes") {
                                JokeScreen(jokeController, Modifier.padding(innerPadding))
                            } else {
                                AdminScreen(jokeController, Modifier.padding(innerPadding))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SafeNavBar(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(AppleWhite.copy(alpha = 0.8f))
            .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SafeNavButton("Piadas", selected == "Jokes") { onSelect("Jokes") }
        SafeNavButton("Admin", selected == "Admin") { onSelect("Admin") }
    }
}

@Composable
fun SafeNavButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (isSelected) ProBlue else Color.Gray
        )
    ) {
        Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
fun JokeScreen(controller: JokeController, modifier: Modifier = Modifier) {
    var joke by remember { mutableStateOf<JokeData?>(null) }
    var showAnswer by remember { mutableStateOf(false) }
    var lang by remember { mutableStateOf("pt") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Função para tocar o som (Atualizada para R.raw.risada)
    fun playAnswerSound() {
        try {
            val mediaPlayer = MediaPlayer.create(context, R.raw.risada)
            mediaPlayer.setOnCompletionListener { it.release() }
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Professional Lang Toggle
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black.copy(alpha = 0.03f))
                .padding(4.dp)
        ) {
            SafeLangTab("Português", lang == "pt") { lang = "pt" }
            SafeLangTab("English", lang == "en") { lang = "en" }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // High Contrast Glass Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 250.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(AppleWhite)
                .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(28.dp))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (joke == null) {
                    Text("PRONTO PARA INICIAR", color = ProBlue, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                } else {
                    Text(
                        text = joke?.question ?: "",
                        color = AppleText,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 28.sp
                    )
                    
                    if (showAnswer) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = joke?.answer ?: "...",
                            color = ProGreen,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        if (!showAnswer && joke != null) {
            SafeButton("Ver Resposta", ProGreen) { 
                showAnswer = true 
                playAnswerSound() // Toca o som ao clicar em ver resposta
            }
        } else {
            SafeButton(if (joke == null) "Carregar Piada" else "Próxima Piada", ProBlue) {
                scope.launch {
                    showAnswer = false
                    joke = controller.getJoke(lang)
                }
            }
        }
        
        if (joke != null && !showAnswer) {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { scope.launch { joke = controller.getJoke(lang) } }) {
                Text("Pular", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun SafeLangTab(label: String, active: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (active) AppleWhite else Color.Transparent,
            contentColor = if (active) ProBlue else Color.Gray
        ),
        shape = RoundedCornerShape(10.dp),
        elevation = if (active) ButtonDefaults.buttonElevation(2.dp) else null,
        modifier = Modifier.height(36.dp).padding(horizontal = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SafeButton(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(2.dp)
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun AdminScreen(controller: JokeController, modifier: Modifier = Modifier) {
    var q by remember { mutableStateOf("") }
    var a by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Aguardando entrada...") }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("Gerenciamento", color = AppleText, fontWeight = FontWeight.Bold, fontSize = 28.sp)
        Text("Adicione novas piadas ao sistema", color = Color.Gray, fontSize = 16.sp)
        
        Spacer(modifier = Modifier.height(32.dp))

        SafeInput("Pergunta", q) { q = it }
        Spacer(modifier = Modifier.height(16.dp))
        SafeInput("Resposta", a) { a = it }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        SafeButton("Salvar Piada", ProBlue) {
            scope.launch {
                status = "Salvando..."
                status = controller.submitJoke(1, "User", q, a, "pt")
                q = ""; a = ""
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppleWhite),
            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Status do Sistema", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(status, color = ProBlue, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun SafeInput(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.Black.copy(alpha = 0.1f),
            focusedBorderColor = ProBlue,
            cursorColor = ProBlue
        ),
        shape = RoundedCornerShape(14.dp)
    )
}
