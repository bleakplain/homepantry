package com.homepantry.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homepantry.data.dao.FolderDao
import com.homepantry.data.dao.RecipeDao
import com.homepantry.data.dao.RecipeFolderDao
import com.homepantry.data.entity.Folder
import com.homepantry.data.entity.Recipe
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 收藏夹详情 ViewModel
 */
class FolderDetailViewModel(
    private val folderDao: FolderDao,
    private val recipeDao: RecipeDao,
    private val recipeFolderDao: RecipeFolderDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val folderId: String = checkNotNull(savedStateHandle["folderId"])

    private val _uiState = MutableStateFlow(FolderDetailUiState())
    val uiState: StateFlow<FolderDetailUiState> = _uiState.asStateFlow()

    init {
        loadFolder()
        loadRecipes()
    }

    /**
     * 加载收藏夹详情
     */
    private fun loadFolder() {
        viewModelScope.launch {
            folderDao.getFolderByIdFlow(folderId)
                .filterNotNull()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { folder ->
                    _uiState.update { it.copy(folder = folder) }
                }
        }
    }

    /**
     * 加载收藏夹中的菜谱
     */
    private fun loadRecipes() {
        viewModelScope.launch {
            recipeFolderDao.getRecipeIdsByFolderIdFlow(folderId)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { recipeIds ->
                    if (recipeIds.isEmpty()) {
                        _uiState.update { it.copy(recipes = emptyList()) }
                        return@collect
                    }

                    try {
                        val recipes = recipeIds.mapNotNull { recipeId ->
                            recipeDao.getRecipeById(recipeId)
                        }
                        _uiState.update { it.copy(recipes = recipes, isLoading = false) }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
        }
    }

    /**
     * 收藏菜谱到收藏夹
     */
    fun addToFolder(recipeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val exists = recipeFolderDao.exists(recipeId, folderId) > 0
                if (exists) {
                    _uiState.update { it.copy(isLoading = false, error = "菜谱已在此收藏夹中") }
                    return@launch
                }

                val recipeFolder = com.homepantry.data.entity.RecipeFolder(
                    id = java.util.UUID.randomUUID().toString(),
                    recipeId = recipeId,
                    folderId = folderId
                )
                recipeFolderDao.insert(recipeFolder)
                _uiState.update { it.copy(isLoading = false, successMessage = "收藏成功") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "收藏失败：${e.message}") }
            }
        }
    }

    /**
     * 从收藏夹移除菜谱
     */
    fun removeFromFolder(recipeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                recipeFolderDao.delete(recipeId, folderId)
                _uiState.update { it.copy(isLoading = false, successMessage = "移除成功") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "移除失败：${e.message}") }
            }
        }
    }

    /**
     * 清除错误和成功消息
     */
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    /**
     * 收藏夹详情 UI 状态
     */
    data class FolderDetailUiState(
        val isLoading: Boolean = true,
        val folder: Folder? = null,
        val recipes: List<Recipe> = emptyList(),
        val error: String? = null,
        val successMessage: String? = null
    )
}
