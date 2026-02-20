package com.homepantry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homepantry.data.dao.FolderDao
import com.homepantry.data.entity.Folder
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 收藏夹列表 ViewModel
 */
class FolderViewModel(private val folderDao: FolderDao) : ViewModel() {

    private val _uiState = MutableStateFlow(FolderUiState())
    val uiState: StateFlow<FolderUiState> = _uiState.asStateFlow()

    init {
        loadFolders()
    }

    /**
     * 加载所有收藏夹
     */
    private fun loadFolders() {
        viewModelScope.launch {
            folderDao.getAllFoldersWithCount()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { folders ->
                    _uiState.update { it.copy(folders = folders, isLoading = false) }
                }
        }
    }

    /**
     * 创建收藏夹
     */
    fun createFolder(name: String, icon: String?, color: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 验证
            if (name.length < 2) {
                _uiState.update { it.copy(isLoading = false, error = "名称不能少于2个字符") }
                return@launch
            }
            if (name.length > 20) {
                _uiState.update { it.copy(isLoading = false, error = "名称不能超过20个字符") }
                return@launch
            }

            try {
                val maxSortOrder = folderDao.getMaxSortOrder() ?: -1
                val folder = Folder(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    icon = icon,
                    color = color,
                    sortOrder = maxSortOrder + 1
                )
                folderDao.insert(folder)
                _uiState.update { it.copy(isLoading = false, successMessage = "收藏夹创建成功") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "创建失败：${e.message}") }
            }
        }
    }

    /**
     * 更新收藏夹
     */
    fun updateFolder(folder: Folder) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                folderDao.update(folder.copy(updatedAt = System.currentTimeMillis()))
                _uiState.update { it.copy(isLoading = false, successMessage = "收藏夹更新成功") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "更新失败：${e.message}") }
            }
        }
    }

    /**
     * 删除收藏夹
     */
    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                folderDao.deleteById(folderId)
                _uiState.update { it.copy(isLoading = false, successMessage = "收藏夹删除成功") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "删除失败：${e.message}") }
            }
        }
    }

    /**
     * 重新排序收藏夹
     */
    fun reorderFolders(folderIds: List<String>) {
        viewModelScope.launch {
            try {
                folderIds.forEachIndexed { index, folderId ->
                    folderDao.updateSortOrder(folderId, index)
                }
                _uiState.update { it.copy(successMessage = "排序更新成功") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "排序失败：${e.message}") }
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
     * 收藏夹 UI 状态
     */
    data class FolderUiState(
        val isLoading: Boolean = true,
        val folders: List<FolderDao.FolderWithCount> = emptyList(),
        val error: String? = null,
        val successMessage: String? = null
    )
}
