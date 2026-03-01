package com.campuswave.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campuswave.app.data.models.Marquee
import com.campuswave.app.ui.components.MarqueeComponent
import com.campuswave.app.ui.theme.*
import com.campuswave.app.ui.viewmodels.AdminViewModel
import com.campuswave.app.utils.ApiResult

private val PRESET_COLORS = listOf(
    Color(0xFF6366F1), Color(0xFFE91E63), Color(0xFF4CAF50), Color(0xFFFF9800),
    Color(0xFF2196F3), Color(0xFF9C27B0), Color(0xFF00BCD4), Color(0xFFFF5722),
    Color(0xFF1A1A2E), Color.White
)

private fun Color.toHexString(): String {
    val r = (red * 255).toInt()
    val g = (green * 255).toInt()
    val b = (blue * 255).toInt()
    return String.format("#%02X%02X%02X", r, g, b)
}

private fun parseHexColor(hex: String): Color? {
    return try {
        Color(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"))
    } catch (_: Exception) { null }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageMarqueeScreen(
    viewModel: AdminViewModel,
    onBackClick: () -> Unit
) {
    val marqueeListState by viewModel.marqueeList.collectAsState()
    val actionState by viewModel.marqueeActionState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showEditor by remember { mutableStateOf(false) }
    var editingMarquee by remember { mutableStateOf<Marquee?>(null) }

    // Editor state
    var messageText by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(false) }
    var textColor by remember { mutableStateOf(PRESET_COLORS[0]) }
    var textColorHex by remember { mutableStateOf("#6366F1") }
    var fontStyleOpt by remember { mutableStateOf("Bold") }
    var fontSizeOpt by remember { mutableStateOf("Medium") }
    var bgColor by remember { mutableStateOf(PRESET_COLORS[0]) }
    var bgColorHex by remember { mutableStateOf("#6366F1") }
    var enableGradient by remember { mutableStateOf(false) }
    var gradientEndColor by remember { mutableStateOf(PRESET_COLORS[1]) }
    var gradientEndHex by remember { mutableStateOf("#E91E63") }
    var scrollSpeedOpt by remember { mutableStateOf("Normal") }
    var textAlignOpt by remember { mutableStateOf("Left") }

    fun resetEditor(marquee: Marquee? = null) {
        editingMarquee = marquee
        messageText = marquee?.message ?: ""
        isActive = marquee?.is_active ?: true
        textColorHex = marquee?.text_color ?: "#6366F1"
        textColor = parseHexColor(textColorHex) ?: PRESET_COLORS[0]
        fontStyleOpt = marquee?.font_style ?: "Bold"
        fontSizeOpt = marquee?.font_size ?: "Medium"
        bgColorHex = marquee?.bg_color ?: "#6366F1"
        bgColor = parseHexColor(bgColorHex) ?: PRESET_COLORS[0]
        enableGradient = marquee?.bg_gradient_end != null
        gradientEndHex = marquee?.bg_gradient_end ?: "#E91E63"
        gradientEndColor = parseHexColor(gradientEndHex) ?: PRESET_COLORS[1]
        scrollSpeedOpt = marquee?.scroll_speed ?: "Normal"
        textAlignOpt = marquee?.text_alignment ?: "Left"
    }

    val isDark = LocalIsDarkTheme.current
    val backgroundColor = if (isDark) Color(0xFF0D1117) else Color(0xFFF8F9FC)
    val surfaceColor = if (isDark) Color(0xFF161B22) else Color.White
    val onSurfaceColor = if (isDark) Color.White else Color(0xFF1E293B)
    val secondaryTextColor = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF64748B)

    LaunchedEffect(actionState) {
        if (actionState is ApiResult.Success) {
            android.widget.Toast.makeText(context, "Marquee saved & applied!", android.widget.Toast.LENGTH_SHORT).show()
            showEditor = false
            editingMarquee = null
            viewModel.resetMarqueeActionState()
        }
    }

    if (showEditor) {
        // Full screen editor
        MarqueeEditorScreen(
            messageText = messageText,
            onMessageChange = { if (it.length <= 200) messageText = it },
            isActive = isActive,
            onActiveChange = { isActive = it },
            textColor = textColor,
            textColorHex = textColorHex,
            onTextColorChange = { color, hex -> textColor = color; textColorHex = hex },
            fontStyleOpt = fontStyleOpt,
            onFontStyleChange = { fontStyleOpt = it },
            fontSizeOpt = fontSizeOpt,
            onFontSizeChange = { fontSizeOpt = it },
            bgColor = bgColor,
            bgColorHex = bgColorHex,
            onBgColorChange = { color, hex -> bgColor = color; bgColorHex = hex },
            enableGradient = enableGradient,
            onEnableGradientChange = { enableGradient = it },
            gradientEndColor = gradientEndColor,
            gradientEndHex = gradientEndHex,
            onGradientEndChange = { color, hex -> gradientEndColor = color; gradientEndHex = hex },
            scrollSpeedOpt = scrollSpeedOpt,
            onScrollSpeedChange = { scrollSpeedOpt = it },
            textAlignOpt = textAlignOpt,
            onTextAlignChange = { textAlignOpt = it },
            isLoading = actionState is ApiResult.Loading,
            isEditing = editingMarquee != null,
            onSave = {
                viewModel.saveMarquee(Marquee(
                    id = editingMarquee?.id,
                    message = messageText,
                    is_active = isActive,
                    text_color = textColorHex,
                    font_style = fontStyleOpt,
                    font_size = fontSizeOpt,
                    bg_color = bgColorHex,
                    bg_gradient_end = if (enableGradient) gradientEndHex else null,
                    scroll_speed = scrollSpeedOpt,
                    text_alignment = textAlignOpt
                ))
            },
            onBack = { showEditor = false },
            isDark = isDark,
            surfaceColor = surfaceColor,
            onSurfaceColor = onSurfaceColor,
            secondaryTextColor = secondaryTextColor
        )
    } else {
        // Marquee list screen
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Manage Marquee", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            resetEditor(null)
                            showEditor = true
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Marquee")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = surfaceColor,
                        titleContentColor = onSurfaceColor
                    )
                )
            },
            containerColor = backgroundColor
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                when (val state = marqueeListState) {
                    is ApiResult.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = accentPurple)
                    }
                    is ApiResult.Success -> {
                        val marquees = state.data
                        if (marquees.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Announcement, contentDescription = null, modifier = Modifier.size(64.dp), tint = CampusGrey)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No marquee messages found", color = CampusGrey)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(marquees) { marquee ->
                                    MarqueeItem(
                                        marquee = marquee,
                                        onToggle = { viewModel.toggleMarquee(marquee.id!!) },
                                        onEdit = {
                                            resetEditor(marquee)
                                            showEditor = true
                                        },
                                        onDelete = { viewModel.deleteMarquee(marquee.id!!) }
                                    )
                                }
                            }
                        }
                    }
                    is ApiResult.Error -> {
                        Text(
                            text = "Error: ${state.message}",
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center).padding(16.dp)
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

// ==================== Full-Screen Marquee Editor ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarqueeEditorScreen(
    messageText: String,
    onMessageChange: (String) -> Unit,
    isActive: Boolean,
    onActiveChange: (Boolean) -> Unit,
    textColor: Color,
    textColorHex: String,
    onTextColorChange: (Color, String) -> Unit,
    fontStyleOpt: String,
    onFontStyleChange: (String) -> Unit,
    fontSizeOpt: String,
    onFontSizeChange: (String) -> Unit,
    bgColor: Color,
    bgColorHex: String,
    onBgColorChange: (Color, String) -> Unit,
    enableGradient: Boolean,
    onEnableGradientChange: (Boolean) -> Unit,
    gradientEndColor: Color,
    gradientEndHex: String,
    onGradientEndChange: (Color, String) -> Unit,
    scrollSpeedOpt: String,
    onScrollSpeedChange: (String) -> Unit,
    textAlignOpt: String,
    onTextAlignChange: (String) -> Unit,
    isLoading: Boolean,
    isEditing: Boolean,
    onSave: () -> Unit,
    onBack: () -> Unit,
    isDark: Boolean,
    surfaceColor: Color,
    onSurfaceColor: Color,
    secondaryTextColor: Color
) {
    // Computed preview values
    val previewFontWeight = when (fontStyleOpt) {
        "Regular" -> FontWeight.Normal
        "Italic" -> FontWeight.Normal
        "Semi-Bold" -> FontWeight.SemiBold
        else -> FontWeight.Bold
    }
    val previewFontStyle = if (fontStyleOpt == "Italic") FontStyle.Italic else FontStyle.Normal
    val previewFontSize = when (fontSizeOpt) { "Small" -> 10f; "Large" -> 16f; else -> 12f }
    val previewVelocity = when (scrollSpeedOpt) { "Slow" -> 50; "Fast" -> 180; else -> 100 }
    val previewTextAlign = if (textAlignOpt == "Center") TextAlign.Center else TextAlign.Left

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Marquee" else "New Marquee", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceColor,
                    titleContentColor = onSurfaceColor
                )
            )
        },
        bottomBar = {
            // Save & Apply Button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = surfaceColor,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = onSave,
                    enabled = messageText.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentPurple)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save & Apply", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        },
        containerColor = if (isDark) Color(0xFF0D1117) else Color(0xFFF8F9FC)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Live Preview ──
            item {
                SectionLabel("✨ Live Preview", onSurfaceColor)
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = surfaceColor,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        MarqueeComponent(
                            message = messageText.ifBlank { "Preview text will appear here..." },
                            backgroundColor = bgColor.copy(alpha = 0.15f),
                            textColor = textColor,
                            velocity = previewVelocity,
                            fontWeight = previewFontWeight,
                            fontStyle = previewFontStyle,
                            fontSize = previewFontSize,
                            gradientEndColor = if (enableGradient) gradientEndColor.copy(alpha = 0.15f) else null,
                            textAlignment = previewTextAlign
                        )
                    }
                }
            }

            // ── Message ──
            item {
                SectionLabel("📝 Message", onSurfaceColor)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = messageText,
                    onValueChange = onMessageChange,
                    label = { Text("Announcement text") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    supportingText = { Text("${messageText.length}/200", color = secondaryTextColor) },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // ── Active Toggle ──
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = surfaceColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Active", fontWeight = FontWeight.SemiBold, color = onSurfaceColor)
                            Text("Will deactivate all others", fontSize = 11.sp, color = secondaryTextColor)
                        }
                        Switch(
                            checked = isActive,
                            onCheckedChange = onActiveChange,
                            colors = SwitchDefaults.colors(checkedTrackColor = accentPurple)
                        )
                    }
                }
            }

            // ── Text Color ──
            item {
                SectionLabel("🎨 Text Color", onSurfaceColor)
                Spacer(modifier = Modifier.height(8.dp))
                ColorPickerRow(
                    selectedColor = textColor,
                    hexValue = textColorHex,
                    onColorChange = onTextColorChange,
                    surfaceColor = surfaceColor,
                    onSurfaceColor = onSurfaceColor,
                    secondaryTextColor = secondaryTextColor,
                    isDark = isDark
                )
            }

            // ── Font Style ──
            item {
                SectionLabel("✍️ Font Style", onSurfaceColor)
                Spacer(modifier = Modifier.height(8.dp))
                SegmentedSelector(
                    options = listOf("Regular", "Bold", "Italic", "Semi-Bold"),
                    selected = fontStyleOpt,
                    onSelect = onFontStyleChange,
                    surfaceColor = surfaceColor,
                    onSurfaceColor = onSurfaceColor,
                    isDark = isDark
                )
            }

            // ── Font Size ──
            item {
                SectionLabel("🔤 Font Size", onSurfaceColor)
                Spacer(modifier = Modifier.height(8.dp))
                SegmentedSelector(
                    options = listOf("Small", "Medium", "Large"),
                    selected = fontSizeOpt,
                    onSelect = onFontSizeChange,
                    surfaceColor = surfaceColor,
                    onSurfaceColor = onSurfaceColor,
                    isDark = isDark
                )
            }

            // ── Background Color ──
            item {
                SectionLabel("🖌️ Background Color", onSurfaceColor)
                Spacer(modifier = Modifier.height(8.dp))
                ColorPickerRow(
                    selectedColor = bgColor,
                    hexValue = bgColorHex,
                    onColorChange = onBgColorChange,
                    surfaceColor = surfaceColor,
                    onSurfaceColor = onSurfaceColor,
                    secondaryTextColor = secondaryTextColor,
                    isDark = isDark
                )
            }

            // ── Gradient Option ──
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = surfaceColor
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Enable Gradient", fontWeight = FontWeight.SemiBold, color = onSurfaceColor)
                                Text("Horizontal gradient background", fontSize = 11.sp, color = secondaryTextColor)
                            }
                            Switch(
                                checked = enableGradient,
                                onCheckedChange = onEnableGradientChange,
                                colors = SwitchDefaults.colors(checkedTrackColor = accentPurple)
                            )
                        }
                        if (enableGradient) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Gradient End Color", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = secondaryTextColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            ColorPickerRow(
                                selectedColor = gradientEndColor,
                                hexValue = gradientEndHex,
                                onColorChange = onGradientEndChange,
                                surfaceColor = surfaceColor,
                                onSurfaceColor = onSurfaceColor,
                                secondaryTextColor = secondaryTextColor,
                                isDark = isDark
                            )
                        }
                    }
                }
            }

            // ── Scroll Speed ──
            item {
                SectionLabel("⚡ Scroll Speed", onSurfaceColor)
                Spacer(modifier = Modifier.height(8.dp))
                SegmentedSelector(
                    options = listOf("Slow", "Normal", "Fast"),
                    selected = scrollSpeedOpt,
                    onSelect = onScrollSpeedChange,
                    surfaceColor = surfaceColor,
                    onSurfaceColor = onSurfaceColor,
                    isDark = isDark
                )
            }

            // ── Text Alignment ──
            item {
                SectionLabel("↔️ Text Alignment", onSurfaceColor)
                Spacer(modifier = Modifier.height(8.dp))
                SegmentedSelector(
                    options = listOf("Left", "Center"),
                    selected = textAlignOpt,
                    onSelect = onTextAlignChange,
                    surfaceColor = surfaceColor,
                    onSurfaceColor = onSurfaceColor,
                    isDark = isDark
                )
            }

            // Bottom spacer for save button
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// ==================== Reusable Components ====================

@Composable
private fun SectionLabel(text: String, color: Color) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = color
    )
}

@Composable
private fun ColorPickerRow(
    selectedColor: Color,
    hexValue: String,
    onColorChange: (Color, String) -> Unit,
    surfaceColor: Color,
    onSurfaceColor: Color,
    secondaryTextColor: Color,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = surfaceColor
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Preset colors
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PRESET_COLORS.forEach { color ->
                    val isSelected = color.toHexString().equals(hexValue, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (isSelected) Modifier.border(3.dp, accentPurple, CircleShape)
                                else if (color == Color.White) Modifier.border(1.dp, Color.Gray, CircleShape)
                                else Modifier
                            )
                            .clickable {
                                onColorChange(color, color.toHexString())
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = if (color == Color.White || color.red > 0.7f && color.green > 0.7f && color.blue > 0.7f) Color.Black else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // HEX input
            var tempHex by remember(hexValue) { mutableStateOf(hexValue) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(selectedColor)
                        .then(
                            if (selectedColor == Color.White) Modifier.border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                            else Modifier
                        )
                )
                OutlinedTextField(
                    value = tempHex,
                    onValueChange = { value ->
                        tempHex = value
                        val parsed = parseHexColor(value)
                        if (parsed != null) {
                            onColorChange(parsed, if (value.startsWith("#")) value else "#$value")
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    placeholder = { Text("#HEX", fontSize = 12.sp) },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
                )
            }
        }
    }
}

@Composable
private fun SegmentedSelector(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    surfaceColor: Color,
    onSurfaceColor: Color,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = surfaceColor
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selected
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clickable { onSelect(option) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) accentPurple else Color.Transparent
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = option,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else (if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B))
                        )
                    }
                }
            }
        }
    }
}

// ==================== Marquee List Item ====================

@Composable
fun MarqueeItem(
    marquee: Marquee,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val surfaceColor = if (isDark) Color(0xFF161B22) else Color.White

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = surfaceColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (marquee.is_active) Color.Green else Color.Gray, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (marquee.is_active) "Active" else "Inactive",
                        fontSize = 12.sp,
                        color = if (marquee.is_active) Color.Green else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp), tint = Color.Red)
                    }
                }
            }

            // Mini preview
            val previewTextColor = parseHexColor(marquee.text_color ?: "#6366F1") ?: Color(0xFF6366F1)
            val previewBgColor = parseHexColor(marquee.bg_color ?: "#6366F1") ?: Color(0xFF6366F1)

            MarqueeComponent(
                message = marquee.message ?: "",
                backgroundColor = previewBgColor.copy(alpha = 0.15f),
                textColor = previewTextColor,
                velocity = when (marquee.scroll_speed) { "Slow" -> 50; "Fast" -> 180; else -> 100 },
                fontWeight = when (marquee.font_style) { "Regular" -> FontWeight.Normal; "Italic" -> FontWeight.Normal; "Semi-Bold" -> FontWeight.SemiBold; else -> FontWeight.Bold },
                fontStyle = if (marquee.font_style == "Italic") FontStyle.Italic else FontStyle.Normal,
                fontSize = when (marquee.font_size) { "Small" -> 10f; "Large" -> 16f; else -> 12f },
                gradientEndColor = marquee.bg_gradient_end?.let { parseHexColor(it)?.copy(alpha = 0.15f) },
                textAlignment = if (marquee.text_alignment == "Center") TextAlign.Center else TextAlign.Left
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Style badges
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StyleBadge("${marquee.font_style ?: "Bold"}")
                StyleBadge("${marquee.font_size ?: "Medium"}")
                StyleBadge("${marquee.scroll_speed ?: "Normal"} speed")
                if (marquee.bg_gradient_end != null) StyleBadge("Gradient")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onToggle,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (marquee.is_active) Color.Gray.copy(alpha = 0.2f) else accentPurple.copy(alpha = 0.1f),
                    contentColor = if (marquee.is_active) (if (isDark) Color.White else Color.Black) else accentPurple
                ),
                elevation = null
            ) {
                Text(if (marquee.is_active) "Deactivate" else "Activate")
            }
        }
    }
}

@Composable
private fun StyleBadge(label: String) {
    val isDark = LocalIsDarkTheme.current
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F5F9)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B)
        )
    }
}
