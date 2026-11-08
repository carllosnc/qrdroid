package com.example.qrdroid.ui.create

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQrScreen(
    viewModel: CreateQrViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var showDarkPicker by remember { mutableStateOf(false) }
    var showLightPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    if (showDarkPicker) {
        HsvColorPickerDialog(
            title = "Cor do Código",
            initialColor = uiState.darkColor,
            onColorSelected = { viewModel.onDarkColorChange(it) },
            onDismiss = { showDarkPicker = false }
        )
    }

    if (showLightPicker) {
        HsvColorPickerDialog(
            title = "Cor do Fundo",
            initialColor = uiState.lightColor,
            onColorSelected = { viewModel.onLightColorChange(it) },
            onDismiss = { showLightPicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Criar QR Code",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Types selector (Chips)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QrCreateType.entries.forEach { type ->
                    val isSelected = uiState.selectedType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectType(type) },
                        label = {
                            Text(
                                text = type.label,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        leadingIcon = {
                            val icon = when (type) {
                                QrCreateType.TEXT -> Icons.Default.TextFields
                                QrCreateType.URL -> Icons.Default.Language
                                QrCreateType.WIFI -> Icons.Default.Wifi
                                QrCreateType.PIX -> Icons.Default.Payments
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 0.8.dp
            )

            // Input Fields based on Type
            when (uiState.selectedType) {
                QrCreateType.TEXT -> {
                    OutlinedTextField(
                        value = uiState.textContent,
                        onValueChange = { viewModel.onTextChange(it) },
                        label = { Text("Texto ou Mensagem") },
                        placeholder = { Text("Digite qualquer texto...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                QrCreateType.URL -> {
                    OutlinedTextField(
                        value = uiState.urlContent,
                        onValueChange = { viewModel.onUrlChange(it) },
                        label = { Text("Endereço do Site / Link") },
                        placeholder = { Text("https://exemplo.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                QrCreateType.WIFI -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = uiState.wifiSsid,
                            onValueChange = { viewModel.onWifiSsidChange(it) },
                            label = { Text("Nome da Rede (SSID)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = uiState.wifiPassword,
                            onValueChange = { viewModel.onWifiPasswordChange(it) },
                            label = { Text("Senha da Rede") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                QrCreateType.PIX -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = uiState.pixKey,
                            onValueChange = { viewModel.onPixKeyChange(it) },
                            label = { Text("Chave PIX *") },
                            placeholder = { Text("CPF, CNPJ, E-mail, Celular ou Chave Aleatória") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = uiState.pixMerchantName,
                            onValueChange = { viewModel.onPixMerchantNameChange(it) },
                            label = { Text("Nome do Beneficiário") },
                            placeholder = { Text("Ex: JOAO DA SILVA") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.pixMerchantCity,
                                onValueChange = { viewModel.onPixMerchantCityChange(it) },
                                label = { Text("Cidade") },
                                placeholder = { Text("Ex: SAO PAULO") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = uiState.pixAmount,
                                onValueChange = { viewModel.onPixAmountChange(it) },
                                label = { Text("Valor R$ (Opcional)") },
                                placeholder = { Text("0,00") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        OutlinedTextField(
                            value = uiState.pixTxId,
                            onValueChange = { viewModel.onPixTxIdChange(it) },
                            label = { Text("Identificador / TxID (Opcional)") },
                            placeholder = { Text("***") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Fina Barra de Cores (Dois Color Pickers)
            SlimColorBar(
                darkColor = uiState.darkColor,
                lightColor = uiState.lightColor,
                onDarkClick = { showDarkPicker = true },
                onLightClick = { showLightPicker = true }
            )

            // QR Code Preview Area
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val bitmap = uiState.qrBitmap
                    if (bitmap != null) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(uiState.lightColor),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "QR Code Gerado",
                                    modifier = Modifier
                                        .size(220.dp)
                                        .padding(14.dp)
                                )
                                if (uiState.isGenerating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(36.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Action Buttons: Share, Save, Copy (Icon Buttons)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            QrActionButton(
                                icon = Icons.Default.Share,
                                label = "Compartilhar",
                                onClick = { viewModel.shareQrCode(context) }
                            )
                            QrActionButton(
                                icon = Icons.Default.Download,
                                label = "Salvar",
                                onClick = { viewModel.saveQrToGallery(context) }
                            )
                            QrActionButton(
                                icon = Icons.Default.ContentCopy,
                                label = "Copiar",
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("QR Code", uiState.generatedPayload))
                                    Toast.makeText(context, "Conteúdo copiado!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    } else {
                        // Empty Preview State
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp, horizontal = 16.dp)
                        ) {
                            if (uiState.isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(36.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Gerando QR Code...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    text = "Preencha as informações acima para gerar o QR Code",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Fina barra compacta com dois seletores de cor (Código e Fundo).
 */
@Composable
private fun SlimColorBar(
    darkColor: Int,
    lightColor: Int,
    onDarkClick: () -> Unit,
    onLightClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Seletor: Cor do Código
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onDarkClick() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(darkColor))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                )
                Column {
                    Text(
                        text = "Código",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = String.format("#%06X", 0xFFFFFF and darkColor),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Divisor vertical fino
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            )

            // Seletor: Cor do Fundo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onLightClick() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(lightColor))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                )
                Column {
                    Text(
                        text = "Fundo",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = String.format("#%06X", 0xFFFFFF and lightColor),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Color picker visual clássico HSV:
 * - Painel 2D de Saturação e Brilho (Value) com cursor interativo
 * - Barra horizontal de Matiz (Hue / Arco-Íris) com cursor interativo
 */
@Composable
private fun HsvColorPickerDialog(
    title: String,
    initialColor: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val hsv = remember(initialColor) {
        val array = FloatArray(3)
        android.graphics.Color.colorToHSV(initialColor, array)
        array
    }

    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var saturation by remember { mutableFloatStateOf(hsv[1]) }
    var value by remember { mutableFloatStateOf(hsv[2]) }

    val currentColorInt = remember(hue, saturation, value) {
        android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))
    }
    val hexString = remember(currentColorInt) {
        String.format("#%06X", 0xFFFFFF and currentColorInt)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Painel 2D de Saturação e Brilho (SV Box)
                SaturationValueBox(
                    hue = hue,
                    saturation = saturation,
                    value = value,
                    onPositionChanged = { newSat, newVal ->
                        saturation = newSat
                        value = newVal
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.2f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Barra horizontal de Matiz (Hue Slider)
                HueSlider(
                    hue = hue,
                    onHueChanged = { hue = it },
                    modifier = Modifier.fillMaxWidth()
                )

                // 3. Prévia da cor e código Hex
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(currentColorInt))
                                .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        )
                        Text(
                            text = hexString,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Atalhos rápidos Branco / Preto
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.Black)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                .clickable {
                                    saturation = 0f
                                    value = 0f
                                }
                        )
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                .clickable {
                                    saturation = 0f
                                    value = 1f
                                }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onColorSelected(currentColorInt)
                    onDismiss()
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Painel 2D: Eixo X = Saturação, Eixo Y = Brilho (Value).
 */
@Composable
private fun SaturationValueBox(
    hue: Float,
    saturation: Float,
    value: Float,
    onPositionChanged: (saturation: Float, value: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val pureHueColor = remember(hue) {
        Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f)))
    }
    val selectedColor = remember(hue, saturation, value) {
        Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value)))
    }

    val density = LocalDensity.current
    val thumbRadiusDp = 13.dp
    val insetPx = with(density) { thumbRadiusDp.toPx() }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val trackWidth = (size.width - 2 * insetPx).coerceAtLeast(1f)
                    val trackHeight = (size.height - 2 * insetPx).coerceAtLeast(1f)

                    val s = ((down.position.x - insetPx) / trackWidth).coerceIn(0f, 1f)
                    val v = (1f - ((down.position.y - insetPx) / trackHeight)).coerceIn(0f, 1f)
                    onPositionChanged(s, v)
                    down.consume()

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        val currentS = ((change.position.x - insetPx) / trackWidth).coerceIn(0f, 1f)
                        val currentV = (1f - ((change.position.y - insetPx) / trackHeight)).coerceIn(0f, 1f)
                        onPositionChanged(currentS, currentV)
                        change.consume()
                    }
                }
            }
    ) {
        // 1. Fundo do painel 2D clipado com cantos arredondados, recuado pelo raio da bolinha
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(thumbRadiusDp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Cor pura da matiz (base)
                drawRect(color = pureHueColor)

                // Gradiente horizontal (Branco -> Transparente para Saturação)
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.White, Color.Transparent)
                    )
                )

                // Gradiente vertical (Transparente -> Preto para Brilho / Value)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black)
                    )
                )
            }
        }

        // 2. Cursor / Seletor circular desenhado SOBRE o painel
        Canvas(modifier = Modifier.fillMaxSize()) {
            val trackWidth = (size.width - 2 * insetPx).coerceAtLeast(1f)
            val trackHeight = (size.height - 2 * insetPx).coerceAtLeast(1f)

            val thumbX = insetPx + saturation * trackWidth
            val thumbY = insetPx + (1f - value) * trackHeight

            // 1. Sombra suave para contraste sobre qualquer fundo
            drawCircle(
                color = Color.Black.copy(alpha = 0.35f),
                radius = 13.5.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
            // 2. Disco branco sólido (borda totalmente opaca)
            drawCircle(
                color = Color.White,
                radius = 12.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
            // 3. Miolo totalmente preenchido com a cor selecionada (sólido, sem vazamento)
            drawCircle(
                color = selectedColor,
                radius = 8.5.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
        }
    }
}

/**
 * Barra horizontal de Matiz (Hue Slider) com gradiente do arco-íris.
 */
@Composable
private fun HueSlider(
    hue: Float,
    onHueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val rainbowColors = remember {
        listOf(
            Color(0xFFFF0000), // Vermelho (0°)
            Color(0xFFFFFF00), // Amarelo (60°)
            Color(0xFF00FF00), // Verde (120°)
            Color(0xFF00FFFF), // Ciano (180°)
            Color(0xFF0000FF), // Azul (240°)
            Color(0xFFFF00FF), // Magenta (300°)
            Color(0xFFFF0000)  // Vermelho (360°)
        )
    }
    val hueColor = remember(hue) {
        Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f)))
    }

    val density = LocalDensity.current
    val thumbRadiusDp = 13.dp
    val insetPx = with(density) { thumbRadiusDp.toPx() }

    Box(
        modifier = modifier
            .height(36.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val trackWidth = (size.width - 2 * insetPx).coerceAtLeast(1f)
                    val newHue = (((down.position.x - insetPx) / trackWidth).coerceIn(0f, 1f) * 360f)
                    onHueChanged(newHue)
                    down.consume()

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        val currentHue = (((change.position.x - insetPx) / trackWidth).coerceIn(0f, 1f) * 360f)
                        onHueChanged(currentHue)
                        change.consume()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // 1. Barra de arco-íris clipada em formato de pílula com inset horizontal para a bolinha
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = thumbRadiusDp)
                .height(26.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.horizontalGradient(rainbowColors)
                )
            }
        }

        // 2. Cursor / Seletor circular desenhado SOBRE a barra
        Canvas(modifier = Modifier.fillMaxSize()) {
            val trackWidth = (size.width - 2 * insetPx).coerceAtLeast(1f)
            val thumbX = insetPx + (hue / 360f) * trackWidth
            val thumbY = size.height / 2f

            // 1. Sombra suave para contraste
            drawCircle(
                color = Color.Black.copy(alpha = 0.35f),
                radius = 13.5.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
            // 2. Disco branco sólido (borda totalmente opaca)
            drawCircle(
                color = Color.White,
                radius = 12.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
            // 3. Miolo totalmente preenchido com a cor da matiz (sólido, sem vazamento)
            drawCircle(
                color = hueColor,
                radius = 8.5.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
        }
    }
}

/**
 * Botão de ação outlined com ícone e rótulo abaixo (Share, Salvar, Copiar).
 */
@Composable
private fun QrActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        OutlinedIconButton(
            onClick = onClick,
            modifier = Modifier.size(52.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = IconButtonDefaults.outlinedIconButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
