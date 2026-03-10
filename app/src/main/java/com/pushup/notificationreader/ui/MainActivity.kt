package com.pushup.notificationreader.ui

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.pushup.notificationreader.R
import com.pushup.notificationreader.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        adapter = NotificationAdapter()

        binding.recyclerNotifications.layoutManager = LinearLayoutManager(this)
        binding.recyclerNotifications.adapter = adapter

        setupButtons()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun setupButtons() {
        binding.btnTogglePermission.setOnClickListener {
            if (isNotificationListenerEnabled()) {
                Toast.makeText(this, R.string.permission_already_granted, Toast.LENGTH_SHORT).show()
            } else {
                showPermissionDialog()
            }
        }

        binding.btnClear.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.dialog_clear_title)
                .setMessage(R.string.dialog_clear_message)
                .setPositiveButton(R.string.yes) { _, _ ->
                    viewModel.clearAll()
                    Toast.makeText(this, R.string.notifications_cleared, Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        binding.btnExport.setOnClickListener {
            viewModel.exportToTextFile()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.notifications.collect { list ->
                        adapter.submitList(list)
                        binding.tvEmptyState.visibility =
                            if (list.isEmpty()) View.VISIBLE else View.GONE
                        binding.recyclerNotifications.visibility =
                            if (list.isEmpty()) View.GONE else View.VISIBLE
                        binding.tvNotificationCount.text =
                            getString(R.string.captured_notifications_count, list.size)
                    }
                }
                launch {
                    viewModel.error.collect { errorMsg ->
                        errorMsg?.let {
                            Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                            viewModel.clearError()
                        }
                    }
                }
                launch {
                    viewModel.exportResult.collect { result ->
                        result?.let {
                            if (it.success) {
                                AlertDialog.Builder(this@MainActivity)
                                    .setTitle(R.string.export_success_title)
                                    .setMessage(getString(R.string.export_success_message, it.path))
                                    .setPositiveButton("OK", null)
                                    .show()
                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    getString(R.string.export_error, it.errorMessage ?: ""),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            viewModel.clearExportResult()
                        }
                    }
                }
            }
        }
    }

    private fun updateStatus() {
        if (isNotificationListenerEnabled()) {
            binding.tvStatus.text = getString(R.string.status_active)
            binding.tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))
            binding.btnTogglePermission.text = getString(R.string.permission_granted)
        } else {
            binding.tvStatus.text = getString(R.string.status_inactive)
            binding.tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
            binding.btnTogglePermission.text = getString(R.string.btn_grant_permission)
        }
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_permission_title)
            .setMessage(R.string.dialog_permission_message)
            .setPositiveButton(R.string.open_settings) { _, _ ->
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":")
            for (name in names) {
                val cn = ComponentName.unflattenFromString(name)
                if (cn != null && cn.packageName == packageName) {
                    return true
                }
            }
        }
        return false
    }
}
