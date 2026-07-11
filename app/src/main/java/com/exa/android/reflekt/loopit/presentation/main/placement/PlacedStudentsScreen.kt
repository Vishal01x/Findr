@file:OptIn(ExperimentalLayoutApi::class)

package com.exa.android.reflekt.loopit.presentation.main.placement

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.presentation.main.placement.components.*
import com.exa.android.reflekt.loopit.presentation.main.placement.model.PlacedStudent
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacedStudentEvent
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacedStudentSideEffect
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacedStudentState
import com.exa.android.reflekt.loopit.presentation.main.placement.vm.PlacedStudentViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.absoluteValue

// ─── Entry Point ──────────────────────────────────────────────────────────────
@Composable
fun PlacedStudentsScreen(
    onBack: () -> Unit,
    preFilterCompany: String? = null,
    preFilterBatch: Int? = null,
    preFilterPackageMin: Float? = null,
    preFilterPackageMax: Float? = null,
    viewModel: PlacedStudentViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onEvent(PlacedStudentEvent.Load(preFilterCompany, preFilterBatch))
        if (preFilterPackageMin != null && preFilterPackageMax != null) {
            viewModel.onEvent(PlacedStudentEvent.PackageRangeChanged(preFilterPackageMin..preFilterPackageMax))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PlacedStudentSideEffect.OpenUrl -> {
                    try {
                        val url = if (effect.url.startsWith("http")) effect.url else "https://${effect.url}"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
                    }
                }
                is PlacedStudentSideEffect.ShowToast ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    PlacedStudentsContent(
        state = state,
        preFilterCompany = preFilterCompany,
        onBack = onBack,
        onEvent = viewModel::onEvent
    )
}

// ─── Main Content ─────────────────────────────────────────────────────────────
@Composable
private fun PlacedStudentsContent(
    state: PlacedStudentState,
    preFilterCompany: String?,
    onBack: () -> Unit,
    onEvent: (PlacedStudentEvent) -> Unit
) {
    val listState = rememberLazyListState()
    val groupedStudents = remember(state.students) {
        state.students.groupBy { it.company.ifBlank { "Other Company" } }
    }
    val sortedCompanyNames = remember(groupedStudents) {
        groupedStudents.keys.sortedWith(
            Comparator<String> { c1, c2 ->
                val max1 = groupedStudents[c1]?.maxOfOrNull { it.packageLpa } ?: 0.0
                val max2 = groupedStudents[c2]?.maxOfOrNull { it.packageLpa } ?: 0.0
                val pkgDiff = max2.compareTo(max1)
                if (pkgDiff != 0) pkgDiff
                else {
                    (groupedStudents[c2]?.size ?: 0).compareTo(groupedStudents[c1]?.size ?: 0)
                }
            }
        )
    }
    var expandedCompanies by remember(sortedCompanyNames) {
        mutableStateOf(if (sortedCompanyNames.size == 1) sortedCompanyNames.toSet() else emptySet())
    }
    var selectedPackageByCompany by remember {
        mutableStateOf<Map<String, String?>>(emptyMap())
    }

    // Trigger load-more when within 3 items of the bottom
    LaunchedEffect(listState, state.canLoadMore) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = layoutInfo.totalItemsCount
            lastVisible to total
        }
            .distinctUntilChanged()
            .collect { (lastVisible, total) ->
                if (state.canLoadMore && !state.isLoadingMore && total > 0 && lastVisible >= total - 3) {
                    onEvent(PlacedStudentEvent.LoadMore)
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize().background(LiquidColors.BgBase)) {

        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            PlacedStudentsHeader(
                title = if (!preFilterCompany.isNullOrBlank()) "Alumni at $preFilterCompany" else "Placed Students",
                hasActiveFilters = state.selectedBatch != null
                    || state.selectedCompanies.isNotEmpty()
                    || state.packageRange != 0f..100f,
                onBack = onBack,
                onFilter = { onEvent(PlacedStudentEvent.ToggleFilterSheet) }
            )

            if (state.isLoading && state.students.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LiquidColors.Primary)
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Stats banner
                    item {
                        StatsBanner(state)
                        Spacer(Modifier.height(4.dp))
                    }

                    // Batch chips
                    item {
                        BatchChipsRow(
                            batches = state.availableBatches,
                            selected = state.selectedBatch,
                            onSelect = { onEvent(PlacedStudentEvent.BatchSelected(it)) }
                        )
                    }



                    // Empty state
                    if (state.students.isEmpty() && !state.isLoading) {
                        item {
                            Box(
                                Modifier.fillMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("🎓", fontSize = 48.sp)
                                    Text("No records found", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)
                                    Text("Try changing the batch or filter", fontSize = 13.sp, color = LiquidColors.TextSecondary)
                                }
                            }
                        }
                    }

                    // Company placement group cards
                    items(sortedCompanyNames, key = { it }) { companyName ->
                        val students = groupedStudents[companyName] ?: emptyList()
                        val isExpanded = expandedCompanies.contains(companyName)
                        val selectedPkg = selectedPackageByCompany[companyName]
                        CompanyPlacementGroupCard(
                            companyName = companyName,
                            students = students,
                            isExpanded = isExpanded,
                            onToggleExpand = {
                                expandedCompanies = if (isExpanded) {
                                    expandedCompanies - companyName
                                } else {
                                    expandedCompanies + companyName
                                }
                            },
                            selectedPackage = selectedPkg,
                            onSelectPackage = { pkg ->
                                selectedPackageByCompany = if (pkg == null) {
                                    selectedPackageByCompany - companyName
                                } else {
                                    selectedPackageByCompany + (companyName to pkg)
                                }
                            },
                            isAdmin = state.isAdmin,
                            onLinkedIn = { url -> onEvent(PlacedStudentEvent.OpenLinkedIn(url)) }
                        )
                    }

                    // Shimmer placeholders while loading next page
                    if (state.isLoadingMore) {
                        items(4, key = { "shimmer_$it" }) {
                            ShimmerStudentCard()
                        }
                    }

                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }

        // Filter Sheet
        if (state.showFilterSheet) {
            FilterBottomSheet(
                state = state,
                onEvent = onEvent
            )
        }
    }
}

// ─── Header ───────────────────────────────────────────────────────────────────
@Composable
private fun PlacedStudentsHeader(
    title: String,
    hasActiveFilters: Boolean,
    onBack: () -> Unit,
    onFilter: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(42.dp).clip(CircleShape)
                .background(LiquidColors.GlassSurface)
                .border(1.dp, LiquidColors.GlassBorder, CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = LiquidColors.TextPrimary)
        }
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
        Box {
            IconButton(
                onClick = onFilter,
                modifier = Modifier.size(42.dp).clip(CircleShape)
                    .background(if (hasActiveFilters) LiquidColors.Primary.copy(0.15f) else LiquidColors.GlassSurface)
                    .border(1.dp, if (hasActiveFilters) LiquidColors.Primary else LiquidColors.GlassBorder, CircleShape)
            ) {
                Icon(Icons.Default.FilterList, "Filter", tint = if (hasActiveFilters) LiquidColors.Primary else LiquidColors.TextPrimary)
            }
            if (hasActiveFilters) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(LiquidColors.Primary).align(Alignment.TopEnd))
            }
        }
    }
}

// ─── Stats Banner ─────────────────────────────────────────────────────────────
@Composable
private fun StatsBanner(state: PlacedStudentState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard("PLACED", state.totalPlaced.toString(), Color(0xFF6366F1), modifier = Modifier.weight(1f))
        StatCard(
            "AVG PACKAGE",
            if (state.avgPackageLpa > 0) "${String.format("%.1f", state.avgPackageLpa)} LPA" else "—",
            Color(0xFF10B981),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            "TOP PACKAGE",
            if (state.topPackageLpa > 0) "${String.format("%.1f", state.topPackageLpa)} LPA" else "—",
            Color(0xFFF59E0B),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(0.08f))
            .border(1.dp, color.copy(0.2f), RoundedCornerShape(14.dp))
            .padding(vertical = 14.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color, maxLines = 1)
            Text(label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = color.copy(0.7f), letterSpacing = 0.3.sp)
        }
    }
}

// ─── Batch Chips ──────────────────────────────────────────────────────────────
@Composable
private fun BatchChipsRow(
    batches: List<Int>,
    selected: Int?,
    onSelect: (Int?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(label = "All Batches", isSelected = selected == null, onClick = { onSelect(null) })
        }
        items(batches) { batch ->
            FilterChip(label = "$batch Batch", isSelected = selected == batch, onClick = { onSelect(batch) })
        }
    }
}



@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color = LiquidColors.Primary
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (isSelected) color.copy(0.18f) else LiquidColors.GlassSurface)
            .border(1.dp, if (isSelected) color else LiquidColors.GlassBorder, RoundedCornerShape(50.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            label, fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) color else LiquidColors.TextSecondary
        )
    }
}

// ─── Shimmer Card ─────────────────────────────────────────────────────────────
@Composable
private fun ShimmerStudentCard() {
    val shimmerColors = listOf(
        LiquidColors.GlassBorder.copy(alpha = 0.6f),
        LiquidColors.GlassBorder.copy(alpha = 0.2f),
        LiquidColors.GlassBorder.copy(alpha = 0.6f),
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 300f, translateAnim - 300f),
        end = Offset(translateAnim, translateAnim)
    )

    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Package placeholder
            Box(
                Modifier.width(56.dp).height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.fillMaxWidth(0.55f).height(14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                Box(Modifier.fillMaxWidth(0.75f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                Box(Modifier.fillMaxWidth(0.40f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(brush))
            }
            Box(Modifier.width(48.dp).height(20.dp).clip(RoundedCornerShape(6.dp)).background(brush))
        }
    }
}

// ─── Company Group Card ───────────────────────────────────────────────────────
@Composable
private fun CompanyPlacementGroupCard(
    companyName: String,
    students: List<PlacedStudent>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    selectedPackage: String?,
    onSelectPackage: (String?) -> Unit,
    isAdmin: Boolean,
    onLinkedIn: (String) -> Unit
) {
    val packageGroups = remember(students) {
        students.groupBy { student ->
            if (student.packageDisplay.isNotBlank()) student.packageDisplay.trim()
            else if (student.packageLpa > 0) {
                val lpa = student.packageLpa
                val lpaStr = if (lpa % 1.0 == 0.0) lpa.toInt().toString() else lpa.toString()
                "$lpaStr LPA"
            } else "Specified"
        }
    }
    val sortedPackages = remember(packageGroups) {
        packageGroups.keys.sortedByDescending { pkg ->
            packageGroups[pkg]?.maxOfOrNull { it.packageLpa } ?: 0.0
        }
    }
    
    val filteredStudents = remember(students, selectedPackage) {
        if (selectedPackage == null) students
        else packageGroups[selectedPackage] ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LiquidColors.Surface)
            .border(1.dp, if (isExpanded) LiquidColors.Primary.copy(alpha = 0.4f) else LiquidColors.GlassBorder, RoundedCornerShape(16.dp))
            .animateContentSize()
    ) {
        // Company Summary Header (Clickable to expand/collapse)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f).padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = companyName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = LiquidColors.TextPrimary
                )
                
                // Package pills summary
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sortedPackages.forEach { pkg ->
                        val count = packageGroups[pkg]?.size ?: 0
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(LiquidColors.Primary.copy(alpha = 0.12f))
                                .border(1.dp, LiquidColors.Primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$pkg ($count)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidColors.Primary
                            )
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Placed count badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(LiquidColors.Primary.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${students.size} Placed",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiquidColors.Primary
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = LiquidColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Expanded Content: Quick Package Filters + Students List
        if (isExpanded) {
            HorizontalDivider(color = LiquidColors.GlassBorder, thickness = 1.dp)
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quick package filters (only show if company has more than 1 package!)
                if (sortedPackages.size > 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Filter by Package",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LiquidColors.TextMuted
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // All chip
                            FilterChip(
                                label = "All (${students.size})",
                                isSelected = selectedPackage == null,
                                onClick = { onSelectPackage(null) }
                            )
                            // Individual package chips
                            sortedPackages.forEach { pkg ->
                                val count = packageGroups[pkg]?.size ?: 0
                                FilterChip(
                                    label = "$pkg ($count)",
                                    isSelected = selectedPackage == pkg,
                                    onClick = { onSelectPackage(if (selectedPackage == pkg) null else pkg) }
                                )
                            }
                        }
                    }
                }

                // Students list for this company
                if (filteredStudents.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No students match this package filter", fontSize = 13.sp, color = LiquidColors.TextMuted)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        filteredStudents.forEach { student ->
                            PlacedStudentCard(
                                student = student,
                                isAdmin = isAdmin,
                                onLinkedIn = { onLinkedIn(student.linkedinUrl) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Student Card ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlacedStudentCard(
    student: PlacedStudent,
    isAdmin: Boolean,
    onLinkedIn: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val avatarColor = avatarColor(student.name)

    GlassCard(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Row(
            modifier = Modifier.padding(if (compact) 12.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Left Column: Package (LPA)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.width(56.dp)
            ) {
                val packageNum = if (student.packageLpa > 0) {
                    val formatted = String.format("%.2f", student.packageLpa)
                    if (formatted.endsWith(".00")) formatted.dropLast(3) else formatted
                } else "-"
                Text(packageNum, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("LPA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextMuted)
            }

            // Middle Column: Info block
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        student.name,
                        fontSize = if (compact) 14.sp else 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiquidColors.TextPrimary,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false).basicMarquee()
                    )
                    if (student.linkedinUrl.isNotBlank()) {
                        Icon(
                            painter = painterResource(id = R.drawable.linedin),
                            contentDescription = "LinkedIn",
                            tint = Color(0xFF0A66C2),
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onLinkedIn() }
                        )
                    }
                }
                Text(student.company, fontSize = 13.sp, color = LiquidColors.TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (student.batchYear > 0) {
                        Text("${student.batchYear}", fontSize = 11.sp, color = LiquidColors.TextMuted)
                    }
                    if (student.branch.isNotBlank()) {
                        Text("· ${student.branch}", fontSize = 11.sp, color = LiquidColors.TextMuted)
                    }
                }
            }

            // Right Column: Badges & Actions
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                if (student.type.equals("Off-Campus", ignoreCase = true) || student.type.equals("Off Campus", ignoreCase = true)) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFFF59E0B).copy(0.12f)).padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Off-Campus", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isAdmin && student.phone.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, null, tint = LiquidColors.TextMuted, modifier = Modifier.size(12.dp))
                        Text(student.phone, fontSize = 11.sp, color = LiquidColors.TextSecondary)
                    }
                } else if (!isAdmin && student.hasPhone) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                android.widget.Toast.makeText(context, "Contact request sent to admins.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 2.dp, horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.ContactSupport, null, tint = LiquidColors.Primary, modifier = Modifier.size(12.dp))
                        Text("Request Contact", fontSize = 11.sp, color = LiquidColors.Primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ─── Filter Bottom Sheet ──────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FilterBottomSheet(
    state: PlacedStudentState,
    onEvent: (PlacedStudentEvent) -> Unit
) {
    // Draft filter state — local to the sheet, initialized from applied state on each open.
    // Changes here do NOT trigger any reload until the user taps Apply.
    var draftRange by remember(state.showFilterSheet) { mutableStateOf(state.packageRange) }
    var draftCompanies by remember(state.showFilterSheet) { mutableStateOf(state.selectedCompanies) }
    var showCompanySearch by remember { mutableStateOf(false) }

    if (showCompanySearch) {
        CompanySearchSheet(
            allLoadedCompanies = state.students.map { it.company }.distinct().take(30),
            draftCompanies = draftCompanies,
            onToggle = { company ->
                draftCompanies = if (draftCompanies.contains(company)) {
                    draftCompanies - company
                } else {
                    draftCompanies + company
                }
            },
            onDismiss = { showCompanySearch = false }
        )
        return
    }

    ModalBottomSheet(
        onDismissRequest = { onEvent(PlacedStudentEvent.ToggleFilterSheet) },
        containerColor = LiquidColors.BgBase,
        dragHandle = {
            Box(Modifier.padding(vertical = 12.dp)) {
                Box(Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(50.dp)).background(LiquidColors.GlassBorder))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Filters", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)

            // Package Range Slider
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Package Range", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LiquidColors.TextPrimary)
                    Text(
                        "${draftRange.start.toInt()} - ${if (draftRange.endInclusive >= 100f) "100+" else draftRange.endInclusive.toInt()} LPA",
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LiquidColors.Primary
                    )
                }
                RangeSlider(
                    value = draftRange,
                    onValueChange = { draftRange = it },
                    valueRange = 0f..100f,
                    steps = 100,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Companies
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Companies", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LiquidColors.TextPrimary)
                if (draftCompanies.isEmpty()) {
                    Text("No companies selected", fontSize = 12.sp, color = LiquidColors.TextMuted)
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        draftCompanies.forEach { company ->
                            FilterChip(
                                label = company,
                                isSelected = true,
                                onClick = { draftCompanies = draftCompanies - company }
                            )
                        }
                    }
                }
                OutlinedButton(
                    onClick = { showCompanySearch = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LiquidColors.Primary.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Add, null, tint = LiquidColors.Primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add Company Filter", color = LiquidColors.Primary)
                }
            }

            // Clear | Apply — half-and-half
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        draftRange = 0f..100f
                        draftCompanies = emptyList()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LiquidColors.GlassBorder)
                ) {
                    Icon(Icons.Default.Clear, null, tint = LiquidColors.TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Clear", color = LiquidColors.TextSecondary)
                }
                Button(
                    onClick = {
                        onEvent(
                            PlacedStudentEvent.ApplyFilters(
                                type = state.selectedType,
                                packageRange = draftRange,
                                companies = draftCompanies
                            )
                        )
                        onEvent(PlacedStudentEvent.ToggleFilterSheet)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.Primary)
                ) {
                    Text("Apply", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── Company Search Sheet ─────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompanySearchSheet(
    allLoadedCompanies: List<String>,
    draftCompanies: List<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = LiquidColors.BgBase,
        modifier = Modifier.fillMaxHeight(0.85f),
        dragHandle = {
            Box(Modifier.padding(vertical = 12.dp)) {
                Box(Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(50.dp)).background(LiquidColors.GlassBorder))
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Search Companies", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LiquidColors.TextPrimary)

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter company name (e.g. TCS)", color = LiquidColors.TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LiquidColors.Primary,
                    unfocusedBorderColor = LiquidColors.GlassBorder
                ),
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = {
                            onToggle(searchQuery.trim())
                            searchQuery = ""
                            onDismiss()
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = LiquidColors.Primary)
                        }
                    }
                }
            )

            if (searchQuery.isNotBlank()) {
                Text("Press + to add '${searchQuery.trim()}'", fontSize = 13.sp, color = LiquidColors.Primary)
            }

            Text("Recently Searched / Top Loaded", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LiquidColors.TextSecondary)

            androidx.compose.foundation.lazy.LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(allLoadedCompanies.filter { it.contains(searchQuery, ignoreCase = true) }) { company ->
                    val isSelected = draftCompanies.contains(company)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onToggle(company) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(company, fontSize = 15.sp, color = LiquidColors.TextPrimary)
                        if (isSelected) {
                            Icon(Icons.Default.Check, null, tint = LiquidColors.Primary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun avatarColor(name: String): Color {
    val palette = listOf(
        Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFF3B82F6),
        Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFFEF4444),
        Color(0xFFEC4899), Color(0xFF14B8A6)
    )
    return palette[name.hashCode().absoluteValue % palette.size]
}

private fun String.initials(): String {
    val parts = trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0][0]}${parts[1][0]}".uppercase()
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "?"
    }
}
