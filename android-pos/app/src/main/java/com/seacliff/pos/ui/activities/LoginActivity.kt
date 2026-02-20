package com.seacliff.pos.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.seacliff.pos.R
import com.seacliff.pos.databinding.ActivityLoginBinding
import com.seacliff.pos.data.local.prefs.PreferencesManager
import com.seacliff.pos.data.remote.api.ApiService
import com.seacliff.pos.data.remote.dto.StaffSummaryDto
import com.seacliff.pos.service.TokenManager
import com.seacliff.pos.ui.viewmodel.AuthViewModel
import com.seacliff.pos.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private var staffList: List<StaffSummaryDto> = emptyList()
    private var selectedStaff: StaffSummaryDto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (authViewModel.isLoggedIn()) {
            navigateToMain()
            return
        }

        setupUI()
        observeViewModel()

        authViewModel.loadStaffForPinLogin()
    }

    private fun setupUI() {
        binding.actvStaff.setOnItemClickListener { _, _, position, _ ->
            if (position in staffList.indices) {
                selectedStaff = staffList[position]
                binding.etPin.isEnabled = true
                binding.etPin.requestFocus()
            }
        }

        binding.etPin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.tilPin.error = null
                if (s?.length == 4 && selectedStaff != null) {
                    val pin = s.toString()
                    if (validatePinInput(pin)) {
                        selectedStaff?.let { staff ->
                            authViewModel.loginWithPin(staff.id, pin)
                        }
                    }
                }
            }
        })

        binding.btnRetry.setOnClickListener {
            binding.cardError.visibility = View.GONE
            authViewModel.loadStaffForPinLogin()
        }

        binding.etPin.isEnabled = false
    }

    private fun validatePinInput(pin: String): Boolean {
        if (selectedStaff == null) {
            Toast.makeText(this, "Please select your name", Toast.LENGTH_SHORT).show()
            return false
        }

        if (pin.isEmpty()) {
            binding.tilPin.error = "PIN is required"
            return false
        }

        if (pin.length != 4) {
            binding.tilPin.error = "PIN must be 4 digits"
            return false
        }

        if (!pin.all { it.isDigit() }) {
            binding.tilPin.error = "PIN must contain only digits"
            return false
        }

        binding.tilPin.error = null
        return true
    }

    private fun observeViewModel() {
        authViewModel.staffList.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading("Loading staff…")
                is Resource.Success -> {
                    hideLoading()
                    staffList = resource.data ?: emptyList()

                    if (staffList.isEmpty()) {
                        showError("No staff available. Please contact your manager.")
                        binding.pinLoginSection.visibility = View.GONE
                    } else {
                        binding.cardError.visibility = View.GONE
                        binding.pinLoginSection.visibility = View.VISIBLE
                        setupStaffDropdown()
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    showError(resource.message ?: "Failed to load staff list")
                    binding.pinLoginSection.visibility = View.GONE
                }
                null -> {}
            }
        }

        authViewModel.loginState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading("Signing in…")
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                    // Register FCM token after successful login
                    val authToken = preferencesManager.getToken()
                    if (authToken != null && authToken.isNotEmpty()) {
                        TokenManager.registerToken(apiService, authToken)
                    }

                    navigateToMain()
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(this, resource.message ?: "Invalid PIN", Toast.LENGTH_LONG).show()
                    binding.etPin.text?.clear()
                }
                null -> {}
            }
        }
    }

    private fun showLoading(message: String) {
        binding.tvLoadingMessage.text = message
        binding.loadingOverlay.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingOverlay.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.cardError.visibility = View.VISIBLE
    }

    private fun setupStaffDropdown() {
        val displayNames = staffList.map { "${it.name} (${it.role.replaceFirstChar { c -> c.uppercase() }})" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, displayNames)
        binding.actvStaff.setAdapter(adapter)
        binding.actvStaff.setText("", false)
        binding.tilStaff.hint = getString(R.string.select_your_name)
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
