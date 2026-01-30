package com.seacliff.pos.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.seacliff.pos.databinding.ActivityTablesBinding
import com.seacliff.pos.ui.adapters.TableAdapter
import com.seacliff.pos.ui.viewmodel.TableViewModel
import com.seacliff.pos.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TablesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTablesBinding
    private val tableViewModel: TableViewModel by viewModels()
    private lateinit var tableAdapter: TableAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTablesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupFilters()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Tables"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        tableAdapter = TableAdapter { table ->
            // Navigate to order creation with selected table
            tableViewModel.selectTable(table)
            val intent = Intent(this, OrderActivity::class.java)
            intent.putExtra("table_id", table.id)
            intent.putExtra("table_name", table.name)
            startActivity(intent)
        }

        binding.rvTables.apply {
            layoutManager = GridLayoutManager(this@TablesActivity, 3)
            adapter = tableAdapter
        }

        binding.swipeRefresh.setOnRefreshListener {
            tableViewModel.refresh()
        }
    }

    private fun setupFilters() {
        binding.chipAll.setOnClickListener {
            tableViewModel.filterByStatus("all")
        }

        binding.chipAvailable.setOnClickListener {
            tableViewModel.filterByStatus("available")
        }

        binding.chipOccupied.setOnClickListener {
            tableViewModel.filterByStatus("occupied")
        }
    }

    private fun observeViewModel() {
        tableViewModel.tables.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    resource.data?.let { tables ->
                        tableAdapter.submitList(tables)
                        binding.tvEmpty.visibility = if (tables.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
