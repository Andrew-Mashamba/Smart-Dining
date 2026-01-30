package com.seacliff.pos.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seacliff.pos.data.local.entity.MenuItemEntity
import com.seacliff.pos.data.repository.MenuRepository
import com.seacliff.pos.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val menuRepository: MenuRepository
) : ViewModel() {

    private val _menuItems = MutableLiveData<Resource<List<MenuItemEntity>>>()
    val menuItems: LiveData<Resource<List<MenuItemEntity>>> = _menuItems

    private val _selectedCategory = MutableLiveData<String>("all")
    val selectedCategory: LiveData<String> = _selectedCategory

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery

    init {
        loadMenuItems()
    }

    fun loadMenuItems(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            menuRepository.getMenuItems(forceRefresh).collect { resource ->
                _menuItems.value = resource
            }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        if (category == "all") {
            loadMenuItems()
        } else {
            viewModelScope.launch {
                menuRepository.getMenuItemsByCategory(category).collect { items ->
                    _menuItems.value = Resource.Success(items)
                }
            }
        }
    }

    fun searchMenu(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            loadMenuItems()
        } else {
            viewModelScope.launch {
                menuRepository.searchMenuItems(query).collect { items ->
                    _menuItems.value = Resource.Success(items)
                }
            }
        }
    }

    fun updateMenuItemAvailability(itemId: Long, isAvailable: Boolean) {
        viewModelScope.launch {
            menuRepository.updateMenuItemAvailability(itemId, isAvailable)
            loadMenuItems(true)
        }
    }

    fun refresh() {
        loadMenuItems(true)
    }
}
