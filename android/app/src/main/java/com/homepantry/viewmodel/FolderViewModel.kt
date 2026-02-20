package com.homepantry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homepantry.data.entity.Folder
import com.homepantry.data.repository.FolderRepository
import com.homepantry.utils.Logger
import com.homepantry.utils.PerformanceMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 收藏夹视图模型
 */
class FolderViewModel(
    private val folderRepository: FolderRepository
) : ViewModel() {

    companion object {
        private const val TAG = "FolderViewModel"
    }

    private val _uiState = MutableStateFlow(FolderUiState())
    val uiState: StateFlow<FolderUiState> = _uiState.asStateFlow()

    init {
        Logger.d(TAG, "FolderViewModel init")
        loadFolders()
    }

    /**
     * 加载所有收藏夹
     */
    private fun loadFolders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("loadFolders") {
                folderRepository.getAllFolders()
                    .collect { folderWithCountList ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                folders = folderWithCountList.map { folderWithCount ->
                                    FolderUiModel(
                                        id = folderWithCount.folder.id,
                                        name = folderWithCount.folder.name,
                                        icon = folderWithCount.folder.icon,
                                        color = folderWithCount.folder.color,
                                        sortOrder = folderWithCount.folder.sortOrder,
                                        recipeCount = folderWithCount.recipe_count,
                                        isSystem = folderWithCount.folder.isSystem
                                    )
                                },
                                error = null
                            )
                        }
                    }
            }
        }
    }

    /**
     * 创建收藏夹
     */
    fun createFolder(
        name: String,
        icon: String? = null,
        color: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            PerformanceMonitor.recordMethodPerformance("createFolder") {
                Logger.enter("FolderViewModel.createFolder", name, icon, color)

                folderRepository.createFolder(name, icon, color)
                    .onSuccess { folder ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                folders = it.folders + folder,
                                successMessage = "文件夹创建成功"
                            )
                        }
                        Logger.d(TAG, "文件夹创建成功：${folder.name}")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "创建失败：${e.message}"
                            )
                        }
                        Logger.e(TAG, "文件夹创建失败", e)
                    }

                Logger.exit("FolderViewModel.createFolder")
            }
        }
    }

    /**
     * 更新收藏夹
     */
    fun updateFolder(folder: Folder) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            PerformanceMonitor.recordMethodPerformance("updateFolder") {
                Logger.enter("FolderViewModel.updateFolder", folder.id)

                folderRepository.updateFolder(folder)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                folders = it.folders.map { if (it.id == folder.id) folder else it },
                                successMessage = "文件夹更新成功"
                            )
                        }
                        Logger.d(TAG, "文件夹更新成功：${folder.name}")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "更新失败：${e.message}"
                            )
                        }
                        Logger.e(TAG, "文件夹更新失败", e)
                    }

                Logger.exit("FolderViewModel.updateFolder")
            }
        }
    }

    /**
     * 删除收藏夹
     */
    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            PerformanceMonitor.recordMethodPerformance("deleteFolder") {
                Logger.enter("FolderViewModel.deleteFolder", folderId)

                folderRepository.deleteFolder(folderId)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                folders = it.folders.filter { it.id != folderId },
                                successMessage = "文件夹删除成功"
                            )
                        }
                        Logger.d(TAG, "文件夹删除成功：$folderId")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "删除失败：${e.message}"
                            )
                        }
                        Logger.e(TAG, "文件夹删除失败", e)
                    }

                Logger.exit("FolderViewModel.deleteFolder")
            }
        }
    }

    /**
     * 重新排序收藏夹
     */
    fun reorderFolders(folderIds: List<String>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            PerformanceMonitor.recordMethodPerformance("reorderFolders") {
                Logger.enter("FolderViewModel.reorderFolders", folderIds.size)

                folderRepository.reorderFolders(folderIds)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                folders = folderIds.mapNotNull { folderId ->
                                    it.folders.find { folder -> folder.id == folderId }
                                },
                                successMessage = "文件夹重新排序成功"
                            )
                        }
                        Logger.d(TAG, "文件夹重新排序成功：${folderIds.size} 个")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "重新排序失败：${e.message}"
                            )
                        }
                        Logger.e(TAG, "文件夹重新排序失败", e)
                    }

                Logger.exit("FolderViewModel.reorderFolders")
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
        Logger.d(TAG, "FolderViewModel onCleared")
    }
}

/**
 * 收藏夹 UI 状态
 */
data class FolderUiState(
    val isLoading: Boolean = false,
    val folders: List<FolderUiModel> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * 收藏夹 UI 模型
 */
data class FolderUiModel(
    val id: String,
    val name: String,
    val icon: String?,
    val color: String?,
    val sortOrder: Int,
    val recipeCount: Int,
    val isSystem: Boolean
)
