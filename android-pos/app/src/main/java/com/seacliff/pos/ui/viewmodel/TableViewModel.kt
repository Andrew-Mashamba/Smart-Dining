package com.seacliff.pos.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seacliff.pos.data.local.entity.TableEntity
import com.seacliff.pos.data.repository.TableRepository
import com.seacliff.pos.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TableViewModel @Inject constructor(
    private val tableRepository: TableRepository
) : ViewModel() {

    private val _tables = MutableLiveData<Resource<List<TableEntity>>>()
    val tables: LiveData<Resource<List<TableEntity>>> = _tables

    private val _selectedTable = MutableLiveData<TableEntity?>()
    val selectedTable: LiveData<TableEntity?> = _selectedTable

    private val _statusFilter = MutableLiveData<String>("all")
    val statusFilter: LiveData<String> = _statusFilter

    init {
        loadTables()
    }

    fun loadTables(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            tableRepository.getTables(forceRefresh).collect { resource ->
                _tables.value = resource
            }
        }
    }

    fun filterByStatus(status: String) {
        _statusFilter.value = status
        if (status == "all") {
            loadTables()
        } else {
            viewModelScope.launch {
                tableRepository.getTablesByStatus(status).collect { tables ->
                    _tables.value = Resource.Success(tables)
                }
            }
        }
    }

    fun selectTable(table: TableEntity) {
        _selectedTable.value = table
    }

    fun updateTableStatus(tableId: Long, status: String) {
        viewModelScope.launch {
            tableRepository.updateTableStatus(tableId, status)
            loadTables(true)
        }
    }

    fun clearSelectedTable() {
        _selectedTable.value = null
    }

    fun refresh() {
        loadTables(true)
    }
}
