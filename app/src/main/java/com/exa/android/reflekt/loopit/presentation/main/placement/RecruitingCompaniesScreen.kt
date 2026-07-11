package com.exa.android.reflekt.loopit.presentation.main.placement

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.data.feature.main.link_data.icon.viewmodel.BrandftechViewModel
import com.exa.android.reflekt.loopit.presentation.main.placement.components.*
import com.exa.android.reflekt.loopit.presentation.main.placement.model.CompanyDetail
import com.exa.android.reflekt.loopit.presentation.main.placement.model.DifficultyLevel

// ── Domain models ─────────────────────────────────────────────────────────────
// Filters moved to bottom sheet

// ── Screen ─────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RecruitingCompaniesScreen(
    companies: List<CompanyDetail> = emptyList(),
    isAdmin: Boolean = false,
    onBack: () -> Unit = {},
    onCompanyClick: (CompanyDetail) -> Unit = {},
    onAddCompanyClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Applied Filter States
    var minCgpaFilter by remember { mutableStateOf("") }
    var hasBacklogFilter by remember { mutableStateOf(false) }
    var minPackageFilter by remember { mutableStateOf("") }
    var difficultyFilter by remember { mutableStateOf<DifficultyLevel?>(null) }
    
    var showFilterSheet by remember { mutableStateOf(false) }

    val filtered = remember(searchQuery, minCgpaFilter, hasBacklogFilter, minPackageFilter, difficultyFilter, companies) {
        companies.filter { c ->
            val matchSearch = searchQuery.isBlank() || c.name.contains(searchQuery, true) || c.tagline.contains(searchQuery, true) || c.jobRole.contains(searchQuery, true)
            
            val reqCgpaStr = c.cgpaCutoff.replace(Regex("[^0-9.]"), "").trim()
            val reqCgpa = reqCgpaStr.toFloatOrNull() ?: 0f
            val filterCgpa = minCgpaFilter.toFloatOrNull()
            val matchCgpa = if (filterCgpa != null) reqCgpa <= filterCgpa else true
            
            val matchBacklogs = if (hasBacklogFilter) c.allowedBacklogs > 0 else true
            
            val maxStr = c.packageLpa?.split(Regex("[,\\-]"))?.lastOrNull()?.replace(Regex("[^0-9.]"), "")?.trim()
            val packageVal = maxStr?.toFloatOrNull() ?: 0f
            val filterPackage = minPackageFilter.toFloatOrNull()
            val matchPackage = if (filterPackage != null) packageVal >= filterPackage else true
            
            val matchDifficulty = if (difficultyFilter != null) c.level == difficultyFilter else true
            
            matchSearch && matchCgpa && matchBacklogs && matchPackage && matchDifficulty
        }
    }

    val liveCompanies = filtered.filter { it.isLiveProcess }
    val otherCompanies  = filtered.filter { !it.isLiveProcess }

    Box(modifier = Modifier.fillMaxSize().background(LiquidColors.BgBase)) {
        
        Column(modifier = Modifier.fillMaxSize()) {

            RCHeader(onBack = onBack)

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SearchBar(
                    query = searchQuery, 
                    onQueryChange = { searchQuery = it },
                    onFilterClick = { showFilterSheet = true }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (liveCompanies.isNotEmpty()) {
                    item { SectionHeader(title = "Live Processes", badge = "${liveCompanies.size} Live") }
                    items(liveCompanies, key = { it.id }) { company ->
                        AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically { it / 4 }) {
                            CompanyDetailCard(company = company, onClick = { onCompanyClick(company) })
                        }
                    }
                }

                if (otherCompanies.isNotEmpty()) {
                    item { Spacer(Modifier.height(8.dp)) }
                    item { SectionHeader(title = "All Visited", badge = null) }
                    items(otherCompanies, key = { it.id }) { company ->
                        CompanyDetailCard(company = company, onClick = { onCompanyClick(company) })
                    }
                }
            }
        }

        // Bookmark FAB (Floating Glass Pill)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 24.dp, bottom = if (isAdmin) 150.dp else 80.dp)
                .size(60.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(LiquidColors.Primary, LiquidColors.Primary.copy(alpha=0.6f))))
                .border(2.dp, LiquidColors.Primary.copy(alpha=0.3f), CircleShape)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Bookmark, "Saved", tint = LiquidColors.TextPrimary, modifier = Modifier.size(28.dp))
        }

        // Add Company FAB (Admin Only)
        if (isAdmin) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 24.dp, bottom = 80.dp)
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Color(0xFF7C3AED), Color(0xFF7C3AED).copy(alpha=0.6f))))
                    .border(2.dp, Color(0xFF7C3AED).copy(alpha=0.3f), CircleShape)
                    .clickable { onAddCompanyClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, "Add Company", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
        
        if (showFilterSheet) {
            CompanyFilterSheet(
                onDismiss = { showFilterSheet = false },
                initialCgpa = minCgpaFilter,
                initialHasBacklog = hasBacklogFilter,
                initialPackage = minPackageFilter,
                initialDifficulty = difficultyFilter,
                onApply = { cgpa, hasBacklog, pkg, diff ->
                    minCgpaFilter = cgpa
                    hasBacklogFilter = hasBacklog
                    minPackageFilter = pkg
                    difficultyFilter = diff
                    showFilterSheet = false
                },
                onClearAll = {
                    minCgpaFilter = ""
                    hasBacklogFilter = false
                    minPackageFilter = ""
                    difficultyFilter = null
                    showFilterSheet = false
                }
            )
        }
    }
}

// ── Header ─────────────────────────────────────────────────────────────────────
@Composable
private fun RCHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(44.dp).clip(CircleShape).background(LiquidColors.GlassSurface).border(1.dp, LiquidColors.GlassBorder, CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = LiquidColors.TextPrimary)
        }
        
        Text("Visited Companies", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)
        
        Box(modifier = Modifier.size(44.dp))
    }
}

// ── Search Bar ─────────────────────────────────────────────────────────────────
@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, onFilterClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(LiquidColors.GlassSurface)
            .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(50.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, null, tint = LiquidColors.TextSecondary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, color = LiquidColors.TextPrimary),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(LiquidColors.Primary),
                singleLine = true,
                decorationBox = @Composable { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) Text("Search companies, roles...", fontSize = 15.sp, color = LiquidColors.TextSecondary)
                        innerTextField()
                    }
                }
            )
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, null, tint = LiquidColors.TextSecondary, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(8.dp))
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(LiquidColors.Primary.copy(alpha=0.15f))
                    .clickable { onFilterClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Tune, "Filters", tint = LiquidColors.Primary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompanyFilterSheet(
    onDismiss: () -> Unit,
    initialCgpa: String,
    initialHasBacklog: Boolean,
    initialPackage: String,
    initialDifficulty: DifficultyLevel?,
    onApply: (String, Boolean, String, DifficultyLevel?) -> Unit,
    onClearAll: () -> Unit
) {
    var minCgpa by remember { mutableStateOf(initialCgpa) }
    var hasBacklog by remember { mutableStateOf(initialHasBacklog) }
    var minPackage by remember { mutableStateOf(initialPackage) }
    var difficulty by remember { mutableStateOf(initialDifficulty) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = LiquidColors.BgBase,
        dragHandle = { BottomSheetDefaults.DragHandle(color = LiquidColors.GlassBorder) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Filter Companies", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)
                TextButton(onClick = onClearAll) {
                    Text("Clear All", color = LiquidColors.Primary, fontWeight = FontWeight.SemiBold)
                }
            }

            // CGPA
            Column {
                Text("My CGPA (Min Required)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextSecondary)
                Spacer(Modifier.height(8.dp))
                BasicTextField(
                    value = minCgpa,
                    onValueChange = { minCgpa = it },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(LiquidColors.GlassSurface).border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(12.dp)).padding(16.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = LiquidColors.TextPrimary),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(LiquidColors.Primary),
                    singleLine = true,
                    decorationBox = @Composable { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (minCgpa.isEmpty()) Text("e.g. 8.0", color = LiquidColors.TextMuted)
                            innerTextField()
                        }
                    }
                )
            }

            // No Backlogs
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("I have a Backlog", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)
                    Text("Show companies that allow backlogs", fontSize = 13.sp, color = LiquidColors.TextSecondary)
                }
                Switch(
                    checked = hasBacklog,
                    onCheckedChange = { hasBacklog = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = LiquidColors.Primary)
                )
            }

            // Min Package
            Column {
                Text("Minimum Package (LPA)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextSecondary)
                Spacer(Modifier.height(8.dp))
                BasicTextField(
                    value = minPackage,
                    onValueChange = { minPackage = it },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(LiquidColors.GlassSurface).border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(12.dp)).padding(16.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = LiquidColors.TextPrimary),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(LiquidColors.Primary),
                    singleLine = true,
                    decorationBox = @Composable { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (minPackage.isEmpty()) Text("e.g. 10", color = LiquidColors.TextMuted)
                            innerTextField()
                        }
                    }
                )
            }

            // Difficulty
            Column {
                Text("Difficulty", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextSecondary)
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(null to "All", DifficultyLevel.EASY to "Easy", DifficultyLevel.MEDIUM to "Medium", DifficultyLevel.HARD to "Hard").forEach { (level, label) ->
                        val isSelected = difficulty == level
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) LiquidColors.Primary.copy(alpha=0.15f) else LiquidColors.GlassSurface)
                                .border(1.dp, if (isSelected) LiquidColors.Primary else LiquidColors.GlassBorder, RoundedCornerShape(12.dp))
                                .clickable { difficulty = level }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) LiquidColors.Primary else LiquidColors.TextSecondary)
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Apply Button
            Button(
                onClick = { onApply(minCgpa, hasBacklog, minPackage, difficulty) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.Primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Apply Filters", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// ── Section Header ─────────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String, badge: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextSecondary, letterSpacing = 1.sp, modifier = Modifier.weight(1f))
        badge?.let {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(50.dp)).background(LiquidColors.Primary.copy(alpha=0.15f)).padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(it, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LiquidColors.Primary)
            }
        }
    }
}

// ── Company Detail Card ───────────────────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompanyDetailCard(company: CompanyDetail, onClick: () -> Unit) {
    val isLive = company.isLiveProcess
    val packageText = company.packageLpa?.let { lpaString ->
        if (lpaString.contains("LPA", ignoreCase = true) || lpaString.contains("₹")) {
            lpaString.trim()
        } else {
            "${lpaString.trim()} LPA"
        }
    } ?: company.stipendPerMonth?.let { "$it/mo" } ?: "TBA"

    val cgpaText = if (company.cgpaCutoff.isNotBlank()) "${company.cgpaCutoff}+" else "7.5+"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(LiquidColors.GlassSurface)
            .border(1.dp, if (isLive) LiquidColors.Primary.copy(alpha = 0.2f) else LiquidColors.GlassBorder, RoundedCornerShape(20.dp))
            .clickableWithNoIndication(onClick = onClick)
    ) {
        if (isLive) {
            Box(modifier = Modifier.matchParentSize().background(Brush.radialGradient(listOf(LiquidColors.Primary.copy(alpha=0.08f), Color.Transparent), radius = 800f, center = Offset(0f, 0f))))
        }

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp)) {
            // Top Section
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                // Logo
                CompanyLogo(logoUrl = company.logoUrl, companyName = company.name, initials = company.name.take(2).uppercase())
                Spacer(Modifier.width(16.dp))
                
                // Details
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = company.name, 
                            fontSize = 20.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = LiquidColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isLive) {
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(LiquidColors.Primary.copy(alpha = 0.15f))
                                    .border(1.dp, LiquidColors.Primary.copy(alpha = 0.3f), RoundedCornerShape(50.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("LIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LiquidColors.Primary, letterSpacing = 0.5.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${company.jobRole.ifBlank { "Software Engineer" }} • ${company.tagline.ifBlank { "Bangalore" }}", 
                        fontSize = 14.sp, 
                        color = LiquidColors.TextSecondary,
                        maxLines = 2,
                        lineHeight = 18.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = LiquidColors.GlassBorder.copy(alpha=0.3f), thickness = 1.dp)
            Spacer(Modifier.height(20.dp))

            // Bottom Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // PACKAGE
                Column(modifier = Modifier.weight(1f)) {
                    Text("PACKAGE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextMuted, letterSpacing = 1.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = packageText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiquidColors.Primary,
                        maxLines = 2,
                        lineHeight = 22.sp
                    )
                }
                
                // REQ. CGPA
                Column(modifier = Modifier.weight(1f)) {
                    Text("REQ. CGPA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextMuted, letterSpacing = 1.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(cgpaText, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = "Valid", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                    }
                }
                
                // DIFFICULTY
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("DIFFICULTY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextMuted, letterSpacing = 1.sp)
                    Spacer(Modifier.height(8.dp))
                    
                    val levelText = when (company.level) {
                        DifficultyLevel.HARD -> "HARD"
                        DifficultyLevel.MEDIUM -> "MEDIUM"
                        DifficultyLevel.EASY -> "EASY"
                    }
                    val levelColor = when (company.level) {
                        DifficultyLevel.HARD -> LiquidColors.Red
                        DifficultyLevel.MEDIUM -> LiquidColors.Amber
                        DifficultyLevel.EASY -> LiquidColors.Green
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(levelColor.copy(alpha = 0.15f))
                            .border(1.dp, levelColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(levelText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = levelColor)
                    }
                }
            }
        }
    }
}

// ── Company Logo (Glowing Glass) ───────────────────────────────────────────────
@Composable
fun CompanyLogo(
    logoUrl: String = "",
    companyName: String = "",
    initials: String,
    containerSize: androidx.compose.ui.unit.Dp = 72.dp,
    imageSize: androidx.compose.ui.unit.Dp = 48.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 16.dp,
    viewModel: BrandftechViewModel = hiltViewModel()
) {
    var effectiveUrl by remember(logoUrl, companyName) { mutableStateOf(logoUrl) }

    LaunchedEffect(logoUrl, companyName) {
        if (logoUrl.isBlank() && companyName.isNotBlank()) {
            effectiveUrl = viewModel.resolveLogoUrl(companyName) ?: ""
        }
    }

    Box(
        modifier = Modifier
            .size(containerSize)
            .clip(RoundedCornerShape(cornerRadius))
            .background(if (effectiveUrl.isNotBlank()) Color.Transparent else androidx.compose.ui.graphics.Color(0xFF0F172A))
            .border(1.dp, LiquidColors.GlassBorder.copy(alpha = 0.3f), RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        if (effectiveUrl.isNotBlank()) {
            AsyncImage(
                model = effectiveUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onError = { effectiveUrl = "" }
            )
        } else {
            Text(initials, fontSize = (containerSize.value * 0.33f).sp, fontWeight = FontWeight.Black, color = LiquidColors.TextPrimary)
            Box(modifier = Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.2f), Color.Transparent))))
        }
    }
}

@Preview(name = "Liquid UI – Recruiting", device = "spec:width=412dp,height=892dp,dpi=560,isRound=false,chinSize=0dp,orientation=portrait")
@Composable
private fun RecruitingCompaniesScreenLiquidPreview() {
    MaterialTheme {
        RecruitingCompaniesScreen()
    }
}
