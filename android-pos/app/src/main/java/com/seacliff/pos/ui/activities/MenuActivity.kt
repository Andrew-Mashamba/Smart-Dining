package com.seacliff.pos.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.seacliff.pos.databinding.ActivityMenuBinding
import com.seacliff.pos.ui.adapters.MenuAdapter
import com.seacliff.pos.ui.viewmodel.MenuViewModel
import com.seacliff.pos.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding
    private val menuViewModel: MenuViewModel by viewModels()
    private lateinit var menuAdapter: MenuAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupTabs()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Menu"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        menuAdapter = MenuAdapter { menuItem ->
            // Toggle availability
            menuViewModel.updateMenuItemAvailability(menuItem.id, !menuItem.isAvailable)
        }

        binding.rvMenu.apply {
            layoutManager = LinearLayoutManager(this@MenuActivity)
            adapter = menuAdapter
        }

        binding.swipeRefresh.setOnRefreshListener {
            menuViewModel.refresh()
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { menuViewModel.searchMenu(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { menuViewModel.searchMenu(it) }
                return true
            }
        })
    }

    private fun setupTabs() {
        val categories = listOf("All", "Appetizer", "Main", "Dessert", "Drink")

        categories.forEach { category ->
            binding.tabCategories.addTab(
                binding.tabCategories.newTab().setText(category)
            )
        }

        binding.tabCategories.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val category = tab?.text.toString().lowercase()
                menuViewModel.selectCategory(if (category == "all") "all" else category)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeViewModel() {
        menuViewModel.menuItems.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    resource.data?.let { items ->
                        menuAdapter.submitList(items)
                        binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
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
