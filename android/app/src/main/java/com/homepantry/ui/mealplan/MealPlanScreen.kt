package com.homepantry.ui.mealplan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homepantry.data.entity.MealType
import com.homepantry.ui.theme.*
import com.homepantry.viewmodel.MealPlanViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    onBackClick: () -> Unit,
    viewModel: MealPlanViewModel = viewModel()
) {
    var selectedDate by remember { mutableStateOf(Date()) }
    val mealPlans by viewModel.mealPlans.collectAsState()
    val availableRecipes by viewModel.availableRecipes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(selectedDate) {
        viewModel.selectDate(selectedDate)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("餐食计划", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = OnPrimary,
                    navigationIconContentColor = OnPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
        ) {
            // Error message
            error?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Week Calendar
            WeekCalendar(
                selectedDate = selectedDate,
                onDateSelected = {
                    selectedDate = it
                    viewModel.selectDate(it)
                }
            )

            // Meal Plan for Selected Day
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else {
                DayMealPlan(
                    date = selectedDate,
                    mealPlans = mealPlans[selectedDate] ?: emptyList(),
                    availableRecipes = availableRecipes,
                    onMealPlanClick = { mealType ->
                        // TODO: Show meal plan editor
                    }
                )
            }
        }
    }
}

@Composable
fun WeekCalendar(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit
) {
    val calendar = Calendar.getInstance()
    val daysOfWeek = mutableListOf<Date>()

    // Get current week dates (starting from Monday)
    calendar.firstDayOfWeek = Calendar.MONDAY
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    repeat(7) {
        daysOfWeek.add(calendar.time)
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface)
            .padding(16.dp)
    ) {
        Text(
            text = "${calendar.get(Calendar.YEAR)}年${calendar.get(Calendar.MONTH) + 1}月",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = OnBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            daysOfWeek.forEach { date ->
                DaySelector(
                    date = date,
                    isSelected = isSameDay(date, selectedDate),
                    onClick = { onDateSelected(date) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun DaySelector(
    date: Date,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val calendar = Calendar.getInstance().apply { time = date }
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    val dayName = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "一"
        Calendar.TUESDAY -> "二"
        Calendar.WEDNESDAY -> "三"
        Calendar.THURSDAY -> "四"
        Calendar.FRIDAY -> "五"
        Calendar.SATURDAY -> "六"
        Calendar.SUNDAY -> "日"
        else -> ""
    }

    Surface(
        onClick = onClick,
        modifier = modifier.padding(2.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Primary else SurfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayName,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) OnPrimary else OnSurfaceVariant
            )
            Text(
                text = dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) OnPrimary else OnSurface
            )
        }
    }
}

@Composable
fun DayMealPlan(
    date: Date,
    mealPlans: List<com.homepantry.viewmodel.MealPlanUi>,
    availableRecipes: List<com.homepantry.data.entity.Recipe>,
    onMealPlanClick: (MealType) -> Unit
) {
    val dateFormat = SimpleDateFormat("MM月dd日 EEEE", Locale.CHINA)
    val formattedDate = dateFormat.format(date)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )
        }

        // Breakfast
        item {
            val breakfastPlan = mealPlans.find { it.mealType == MealType.BREAKFAST }
            MealTypeSection(
                mealType = MealType.BREAKFAST,
                title = "早餐",
                icon = "🌅",
                hasPlan = breakfastPlan != null,
                recipeName = breakfastPlan?.recipeName,
                onClick = { onMealPlanClick(MealType.BREAKFAST) }
            )
        }

        // Lunch
        item {
            val lunchPlan = mealPlans.find { it.mealType == MealType.LUNCH }
            MealTypeSection(
                mealType = MealType.LUNCH,
                title = "午餐",
                icon = "☀️",
                hasPlan = lunchPlan != null,
                recipeName = lunchPlan?.recipeName,
                onClick = { onMealPlanClick(MealType.LUNCH) }
            )
        }

        // Dinner
        item {
            val dinnerPlan = mealPlans.find { it.mealType == MealType.DINNER }
            MealTypeSection(
                mealType = MealType.DINNER,
                title = "晚餐",
                icon = "🌙",
                hasPlan = dinnerPlan != null,
                recipeName = dinnerPlan?.recipeName,
                onClick = { onMealPlanClick(MealType.DINNER) }
            )
        }

        // Snack
        item {
            val snackPlan = mealPlans.find { it.mealType == MealType.SNACK }
            MealTypeSection(
                mealType = MealType.SNACK,
                title = "加餐",
                icon = "🍎",
                hasPlan = snackPlan != null,
                recipeName = snackPlan?.recipeName,
                onClick = { onMealPlanClick(MealType.SNACK) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealTypeSection(
    mealType: MealType,
    title: String,
    icon: String,
    hasPlan: Boolean,
    recipeName: String? = null,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasPlan) Surface else SurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (hasPlan) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = OnSurfaceVariant
                )
                if (hasPlan && recipeName != null) {
                    Text(
                        text = recipeName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurface
                    )
                } else {
                    Text(
                        text = "点击添加计划",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
            Text(
                text = ">",
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurfaceVariant
            )
        }
    }
}

fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
