package com.homepantry.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homepantry.data.entity.Folder
import com.homepantry.data.repository.FolderRepository
import com.homepantry.data.repository.RecipeFolderRepository
import com.homepantry.data.entity.Recipe
import com.homepantry.utils.Logger
import com.homepantry.utils.PerformanceMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 收藏夹详情视图模型
 */
class FolderDetailViewModel(
    private val folderId: String,
    private val folderRepository: FolderRepository,
    private val recipeFolderRepository: RecipeFolderRepository
) : ViewModel() {

    companion object {
        private const val TAG = "FolderDetailViewModel"
    }

    private val _uiState = MutableStateFlow(FolderDetailUiState())
    val uiState: StateFlow<FolderDetailUiState> = _uiState.asStateFlow()

    init {
        Logger.d(TAG, "FolderDetailViewModel init for folder: $folderId")
        loadFolderDetail()
        loadRecipesInFolder()
    }

    /**
     * 加载收藏夹详情
     */
    private fun loadFolderDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("loadFolderDetail") {
                folderRepository.getFolderById(folderId)
                    .collect { folder ->
                        if (folder != null) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    folder = folder
                                )
                            }
                            Logger.d(TAG, "加载收藏夹详情成功：${folder.name}")
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "收藏夹不存在"
                                )
                            }
                            Logger.e(TAG, "收藏夹不存在：$folderId")
                        }
                    }
            }
        }
    }

    /**
     * 加载收藏夹中的菜谱
     */
    private fun loadRecipesInFolder() {
        viewModelScope.launch {
            PerformanceMonitor.recordMethodPerformance("loadRecipesInFolder") {
                recipeFolderRepository.getRecipesInFolder(folderId)
                    .collect { recipeIds ->
                        // TODO: 根据菜谱 ID 加载菜谱详情
                        _uiState.update {
                            it.copy(
                                recipes = emptyList()
                            )
                        }
                        Logger.d(TAG, "加载收藏夹中的菜谱：${recipeIds.size} 个")
                    }
            }
        }
    }

    /**
     * 添加菜谱到收藏夹
     */
    fun addRecipeToFolder(recipeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("addRecipeToFolder") {
                Logger.enter("FolderDetailViewModel.addRecipeToFolder", recipeId, folderId)

                recipeFolderRepository.addRecipeToFolder(recipeId, folderId)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "添加成功"
                            )
                        }
                        Logger.d(TAG, "添加菜谱到收藏夹成功：$recipeId → $folderId")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "添加失败：${e.message}"
                            )
                        }
                        Logger.e(TAG, "添加菜谱到收藏夹失败：$recipeId → $folderId", e)
                    }

                Logger.exit("FolderDetailViewModel.addRecipeToFolder")
            }
        }
    }

    /**
     * 从收藏夹移除菜谱
     */
    fun removeRecipeFromFolder(recipeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("removeRecipeFromFolder") {
                Logger.enter("FolderDetailViewModel.removeRecipeFromFolder", recipeId, folderId)

                recipeFolderRepository.removeRecipeFromFolder(recipeId, folderId)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "移除成功"
                            )
                        }
                        Logger.d(TAG, "从收藏夹移除菜谱成功：$recipeId ← $folderId")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "移除失败：${e.message}"
                            )
                        }
                        Logger.e(TAG, "从收藏夹移除菜谱失败：$recipeId ← $folderId", e)
                    }

                Logger.exit("FolderDetailViewModel.removeRecipeFromFolder")
            }
        }
    }

    /**
     * 删除收藏夹
     */
    fun deleteFolder() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("deleteFolder") {
                Logger.enter("FolderDetailViewModel.deleteFolder", folderId)

                folderRepository.deleteFolder(folderId)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isDeleted = true,
                                successMessage = "删除成功"
                            )
                        }
                        Logger.d(TAG, "删除收藏夹成功：$folderId")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "删除失败：${e.message}"
                            )
                        }
                        Logger.e(TAG, "删除收藏夹失败：$folderId", e)
                    }

                Logger.exit("FolderDetailViewModel.deleteFolder")
            }
        }
    }

    /**
     * 编辑收藏夹
     */
    fun editFolder(folder: Folder) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("editFolder") {
                Logger.enter("FolderDetailViewModel.editFolder", folder.id)

                folderRepository.updateFolder(folder)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                folder = folder,
                                successMessage = "编辑成功"
                            )
                        }
                        Logger.d(TAG, "编辑收藏夹成功：${folder.name}")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "编辑失败：${e.message}"
                            )
                        }
                        Logger.e(TAG, "编辑收藏夹失败：${folder.name}", e)
                    }

                Logger.exit("FolderDetailViewModel.editFolder")
            }
        }
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * 清除成功消息
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "FolderDetailViewModel onCleared")
    }
}

/**
 * 收藏夹详情 UI 状态
 */
data class FolderDetailUiState(
    val isLoading: Boolean = false,
    val folder: Folder? = null,
    val recipes: List<Recipe> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
    val isDeleted: Boolean = false
)
