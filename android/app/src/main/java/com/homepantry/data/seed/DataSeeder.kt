package com.homepantry.data.seed

import com.homepantry.data.dao.CategoryDao
import com.homepantry.data.dao.IngredientDao
import com.homepantry.data.dao.RecipeDao
import com.homepantry.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataSeeder(
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao,
    private val categoryDao: CategoryDao
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    fun seedData() {
        scope.launch {
            // Check if data already exists
            if (recipeDao.getAllRecipes().emitToList()) {
                return@launch // Data already seeded
            }

            seedCategories()
            seedIngredients()
            seedRecipes()
        }
    }

    private suspend fun seedCategories() {
        val categories = listOf(
            Category(
                name = "家常菜",
                icon = "🍲",
                color = "#FF6B35",
                sortOrder = 1
            ),
            Category(
                name = "汤品",
                icon = "🍵",
                color = "#2A9D8F",
                sortOrder = 2
            ),
            Category(
                name = "主食",
                icon = "🍚",
                color = "#E9C46A",
                sortOrder = 3
            ),
            Category(
                name = "甜点",
                icon = "🍰",
                color = "#F4A261",
                sortOrder = 4
            ),
            Category(
                name = "凉菜",
                icon = "🥗",
                color = "#A8DADC",
                sortOrder = 5
            )
        )

        categories.forEach { categoryDao.insertCategory(it) }
    }

    private suspend fun seedIngredients() {
        val ingredients = listOf(
            // 蔬菜
            Ingredient(name = "番茄", unit = "个", category = IngredientCategory.VEGETABLE),
            Ingredient(name = "土豆", unit = "个", category = IngredientCategory.VEGETABLE),
            Ingredient(name = "黄瓜", unit = "根", category = IngredientCategory.VEGETABLE),
            Ingredient(name = "白菜", unit = "颗", category = IngredientCategory.VEGETABLE),
            Ingredient(name = "胡萝卜", unit = "根", category = IngredientCategory.VEGETABLE),
            Ingredient(name = "青椒", unit = "个", category = IngredientCategory.VEGETABLE),
            Ingredient(name = "洋葱", unit = "个", category = IngredientCategory.VEGETABLE),
            Ingredient(name = "大蒜", unit = "瓣", category = IngredientCategory.VEGETABLE),
            Ingredient(name = "生姜", unit = "块", category = IngredientCategory.VEGETABLE),
            Ingredient(name = "韭菜", unit = "把", category = IngredientCategory.VEGETABLE),

            // 肉类
            Ingredient(name = "猪肉", unit = "克", category = IngredientCategory.MEAT),
            Ingredient(name = "牛肉", unit = "克", category = IngredientCategory.MEAT),
            Ingredient(name = "鸡肉", unit = "克", category = IngredientCategory.MEAT),
            Ingredient(name = "鸡蛋", unit = "个", category = IngredientCategory.MEAT),

            // 海鲜
            Ingredient(name = "鱼", unit = "条", category = IngredientCategory.SEAFOOD),
            Ingredient(name = "虾", unit = "只", category = IngredientCategory.SEAFOOD),

            // 调料
            Ingredient(name = "盐", unit = "克", category = IngredientCategory.SPICE),
            Ingredient(name = "糖", unit = "克", category = IngredientCategory.SPICE),
            Ingredient(name = "酱油", unit = "勺", category = IngredientCategory.SAUCE),
            Ingredient(name = "醋", unit = "勺", category = IngredientCategory.SAUCE),
            Ingredient(name = "料酒", unit = "勺", category = IngredientCategory.SAUCE),
            Ingredient(name = "食用油", unit = "勺", category = IngredientCategory.SAUCE),
            Ingredient(name = "香油", unit = "勺", category = IngredientCategory.SAUCE),

            // 其他
            Ingredient(name = "葱", unit = "根", category = IngredientCategory.OTHER),
            Ingredient(name = "香菜", unit = "把", category = IngredientCategory.OTHER),
            Ingredient(name = "淀粉", unit = "克", category = IngredientCategory.GRAIN)
        )

        ingredients.forEach { ingredientDao.insertIngredient(it) }
    }

    private suspend fun seedRecipes() {
        // 获取分类ID
        val categories = categoryDao.getAllCategories().emitToList() ?: emptyList()
        val homeCookingCategory = categories.find { it.name == "家常菜" }?.id

        // 番茄炒蛋
        val tomatoEggs = Recipe(
            name = "番茄炒蛋",
            description = "经典的家常菜，酸甜可口，营养丰富",
            cookingTime = 15,
            servings = 2,
            difficulty = DifficultyLevel.EASY,
            categoryId = homeCookingCategory,
            tags = "["经典", "家常", "快手菜"]"
        )
        recipeDao.insertRecipe(tomatoEggs)

        val tomatoEggsIngredients = listOf(
            RecipeIngredient(tomatoEggs.id, getIngredientId("番茄") ?: "", 2.0),
            RecipeIngredient(tomatoEggs.id, getIngredientId("鸡蛋") ?: "", 3.0),
            RecipeIngredient(tomatoEggs.id, getIngredientId("食用油") ?: "", 2.0),
            RecipeIngredient(tomatoEggs.id, getIngredientId("盐") ?: "", 2.0),
            RecipeIngredient(tomatoEggs.id, getIngredientId("葱") ?: "", 1.0)
        )
        tomatoEggsIngredients.forEach { recipeDao.insertRecipeIngredient(it) }

        val tomatoEggsInstructions = listOf(
            RecipeInstruction(tomatoEggs.id, 1, "番茄洗净切块，鸡蛋打散备用"),
            RecipeInstruction(tomatoEggs.id, 2, "热锅下油，倒入鸡蛋液炒熟盛起"),
            RecipeInstruction(tomatoEggs.id, 3, "锅中留底油，下番茄块炒出汁水"),
            RecipeInstruction(tomatoEggs.id, 4, "倒入炒蛋，加盐调味，翻炒均匀"),
            RecipeInstruction(tomatoEggs.id, 5, "撒上葱花，出锅装盘")
        )
        tomatoEggsInstructions.forEach { recipeDao.insertRecipeInstruction(it) }

        // 土豆丝
        val potatoShreds = Recipe(
            name = "酸辣土豆丝",
            description = "爽脆开胃，酸辣可口的家常素菜",
            cookingTime = 20,
            servings = 2,
            difficulty = DifficultyLevel.EASY,
            categoryId = homeCookingCategory,
            tags = "["素菜", "开胃", "下饭"]"
        )
        recipeDao.insertRecipe(potatoShreds)

        val potatoShredsIngredients = listOf(
            RecipeIngredient(potatoShreds.id, getIngredientId("土豆") ?: "", 2.0),
            RecipeIngredient(potatoShreds.id, getIngredientId("青椒") ?: "", 1.0),
            RecipeIngredient(potatoShreds.id, getIngredientId("醋") ?: "", 2.0),
            RecipeIngredient(potatoShreds.id, getIngredientId("盐") ?: "", 2.0),
            RecipeIngredient(potatoShreds.id, getIngredientId("食用油") ?: "", 2.0)
        )
        potatoShredsIngredients.forEach { recipeDao.insertRecipeIngredient(it) }

        val potatoShredsInstructions = listOf(
            RecipeInstruction(potatoShreds.id, 1, "土豆去皮切丝，用水冲洗去淀粉"),
            RecipeInstruction(potatoShreds.id, 2, "青椒切丝备用"),
            RecipeInstruction(potatoShreds.id, 3, "热锅下油，先炒青椒丝盛起"),
            RecipeInstruction(potatoShreds.id, 4, "锅中再下油，炒土豆丝至半透明"),
            RecipeInstruction(potatoShreds.id, 5, "加入青椒丝，加盐醋调味，翻炒均匀出锅")
        )
        potatoShredsInstructions.forEach { recipeDao.insertRecipeInstruction(it) }

        // 红烧肉
        val braisedPork = Recipe(
            name = "红烧肉",
            description = "肥而不腻，入口即化的经典红烧肉",
            cookingTime = 90,
            servings = 4,
            difficulty = DifficultyLevel.MEDIUM,
            categoryId = homeCookingCategory,
            tags = "["经典", "硬菜", "下饭"]"
        )
        recipeDao.insertRecipe(braisedPork)

        val braisedPorkIngredients = listOf(
            RecipeIngredient(braisedPork.id, getIngredientId("猪肉") ?: "", 500.0),
            RecipeIngredient(braisedPork.id, getIngredientId("糖") ?: "", 30.0),
            RecipeIngredient(braisedPork.id, getIngredientId("酱油") ?: "", 3.0),
            RecipeIngredient(braisedPork.id, getIngredientId("料酒") ?: "", 2.0),
            RecipeIngredient(braisedPork.id, getIngredientId("生姜") ?: "", 3.0),
            RecipeIngredient(braisedPork.id, getIngredientId("大蒜") ?: "", 3.0)
        )
        braisedPorkIngredients.forEach { recipeDao.insertRecipeIngredient(it) }

        val braisedPorkInstructions = listOf(
            RecipeInstruction(braisedPork.id, 1, "五花肉切块，冷水下锅焯水去腥"),
            RecipeInstruction(braisedPork.id, 2, "锅中放少量油，下肉块小火煸炒出油"),
            RecipeInstruction(braisedPork.id, 3, "加入冰糖炒糖色，肉块上色"),
            RecipeInstruction(braisedPork.id, 4, "加入葱姜蒜、料酒、酱油翻炒"),
            RecipeInstruction(braisedPork.id, 5, "加开水没过肉块，大火烧开转小火炖1小时"),
            RecipeInstruction(braisedPork.id, 6, "大火收汁，撒上葱花出锅")
        )
        braisedPorkInstructions.forEach { recipeDao.insertRecipeInstruction(it) }
    }

    private suspend fun getIngredientId(name: String): String? {
        return ingredientDao.getAllIngredients().emitToList()?.find { it.name == name }?.id
    }

    private suspend fun <T> kotlinx.coroutines.flow.Flow<T>.emitToList(): T? {
        var result: T? = null
        this.collect { result = it }
        return result
    }
}
