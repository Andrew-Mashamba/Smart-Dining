package com.seacliff.pos.ui.activities

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.seacliff.pos.databinding.ActivityTipsBinding
import com.seacliff.pos.ui.adapter.TipAdapter
import com.seacliff.pos.ui.viewmodel.TipViewModel
import com.seacliff.pos.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class TipsActivity : AppCompatActivity() {

    private var _binding: ActivityTipsBinding? = null
    private val binding get() = _binding!!

    private val tipViewModel: TipViewModel by viewModels()
    private lateinit var tipAdapter: TipAdapter

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("sw", "TZ")).apply {
        currency = Currency.getInstance("TZS")
    }

    private var currentPeriod: Period = Period.TODAY

    enum class Period {
        TODAY, WEEK, MONTH
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityTipsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        loadTips()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "My Tips"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        tipAdapter = TipAdapter()
        binding.rvTips.apply {
            layoutManager = LinearLayoutManager(this@TipsActivity)
            adapter = tipAdapter
        }
    }

    private fun setupListeners() {
        binding.tabPeriod.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentPeriod = when (tab?.position) {
                    0 -> Period.TODAY
                    1 -> Period.WEEK
                    2 -> Period.MONTH
                    else -> Period.TODAY
                }
                loadTips()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadTips() {
        binding.progressBar.visibility = View.VISIBLE

        val (startDate, endDate) = getDateRange(currentPeriod)

        // Load tips for the selected period
        tipViewModel.loadTipsForPeriod(startDate, endDate)

        // Observe tips data
        tipViewModel.tipsForPeriod.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    resource.data?.let { tips ->
                        if (tips.isEmpty()) {
                            showEmptyState()
                        } else {
                            hideEmptyState()
                            tipAdapter.submitList(tips)
                            calculateStats(tips)
                        }
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showEmptyState()
                }
            }
        }

        // Load summary statistics
        tipViewModel.loadTipSummary(startDate, endDate)

        tipViewModel.tipSummary.observe(this) { summary ->
            summary?.let {
                binding.tvTodayTotal.text = currencyFormatter.format(it.total)
                binding.tvTodayCount.text = "From ${it.count} orders"
            }
        }
    }

    private fun getDateRange(period: Period): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis

        when (period) {
            Period.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            Period.WEEK -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
            }
            Period.MONTH -> {
                calendar.add(Calendar.MONTH, -1)
            }
        }

        return Pair(calendar.timeInMillis, endDate)
    }

    private fun calculateStats(tips: List<com.seacliff.pos.data.local.entity.TipEntity>) {
        if (tips.isEmpty()) {
            binding.tvAverageTip.text = currencyFormatter.format(0.0)
            binding.tvHighestTip.text = currencyFormatter.format(0.0)
            return
        }

        val average = tips.map { it.amount }.average()
        val highest = tips.maxOfOrNull { it.amount } ?: 0.0

        binding.tvAverageTip.text = currencyFormatter.format(average)
        binding.tvHighestTip.text = currencyFormatter.format(highest)
    }

    private fun showEmptyState() {
        binding.emptyState.visibility = View.VISIBLE
        binding.rvTips.visibility = View.GONE
        binding.tvTodayTotal.text = currencyFormatter.format(0.0)
        binding.tvTodayCount.text = "From 0 orders"
        binding.tvAverageTip.text = currencyFormatter.format(0.0)
        binding.tvHighestTip.text = currencyFormatter.format(0.0)
    }

    private fun hideEmptyState() {
        binding.emptyState.visibility = View.GONE
        binding.rvTips.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
