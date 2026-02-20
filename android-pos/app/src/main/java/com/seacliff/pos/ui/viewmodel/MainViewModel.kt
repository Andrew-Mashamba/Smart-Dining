package com.seacliff.pos.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.seacliff.pos.data.local.dao.MenuItemDao
import com.seacliff.pos.data.local.dao.StaffDao
import com.seacliff.pos.data.local.dao.TableDao
import com.seacliff.pos.worker.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tableDao: TableDao,
    private val menuItemDao: MenuItemDao,
    private val staffDao: StaffDao,
    private val syncManager: SyncManager
) : ViewModel() {

    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Deletes all locally synced data (tables, menu, staff) and triggers an immediate
     * sync to reload from the backend.
     */
    fun refreshAllDataFromBackend() {
        scope.launch {
            tableDao.deleteAllTables()
            menuItemDao.deleteAllMenuItems()
            staffDao.deleteAllStaff()
            syncManager.triggerImmediateSync()
        }
    }
}
