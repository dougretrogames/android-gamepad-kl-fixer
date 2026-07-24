package com.dougretrogames.gamepadklfixer.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dougretrogames.gamepadklfixer.R
import com.dougretrogames.gamepadklfixer.databinding.ActivityMainBinding
import com.dougretrogames.gamepadklfixer.model.GamepadDevice
import com.dougretrogames.gamepadklfixer.root.RootManager
import com.dougretrogames.gamepadklfixer.ui.adapter.DeviceAdapter
import com.dougretrogames.gamepadklfixer.ui.viewmodel.DeviceListViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: DeviceListViewModel by viewModels()
    private lateinit var adapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupDrawerToggle()
        setupNavigation()

        adapter = DeviceAdapter { device -> openDeviceDetail(device) }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
            adapter = this@MainActivity.adapter
        }

        binding.fabRefresh.setOnClickListener { viewModel.refresh() }
        binding.btnRetryScan.setOnClickListener { viewModel.refresh() }

        viewModel.devices.observe(this) { devices ->
            adapter.submitList(devices)
            val empty = devices.isEmpty()
            binding.emptyStateContainer.visibility = if (empty) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (empty) View.GONE else View.VISIBLE
        }

        viewModel.refresh()
        checkRootAndInstructions()
    }

    private fun setupDrawerToggle() {
        val toggle = androidx.appcompat.app.ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.app_name,
            R.string.app_name
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun setupNavigation() {
        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_instructions -> showInstructionsDialog()
                R.id.nav_home -> { /* já estamos na home */ }
            }
            binding.drawerLayout.closeDrawers()
            true
        }
    }

    private fun checkRootAndInstructions() {
        lifecycleScope.launch {
            val rootResult = RootManager.checkRootAccess()
            val isRooted = rootResult.success && rootResult.output.contains("uid=0")

            if (!isRooted) {
                showRootWarningDialog()
            }
        }
    }

    private fun showRootWarningDialog() {
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        if (!prefs.getBoolean("show_root_warning", true)) return

        val content = getString(R.string.root_required_message)
        val styledContent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            android.text.Html.fromHtml(content, android.text.Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            android.text.Html.fromHtml(content)
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.root_required_title)
            .setMessage(styledContent)
            .setPositiveButton(R.string.root_check_button, null)
            .setNeutralButton(R.string.root_warning_dont_show) { _, _ ->
                prefs.edit().putBoolean("show_root_warning", false).apply()
            }
            .setCancelable(false)
            .show()
    }

    private fun showInstructionsDialog() {
        val content = getString(R.string.instructions_content)
        val styledContent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            android.text.Html.fromHtml(content, android.text.Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            android.text.Html.fromHtml(content)
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.instructions_title)
            .setMessage(styledContent)
            .setPositiveButton(R.string.instructions_button, null)
            .setCancelable(true)
            .show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> { viewModel.refresh(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun openDeviceDetail(device: GamepadDevice) {
        val intent = Intent(this, DeviceDetailActivity::class.java).apply {
            putExtra(DeviceDetailActivity.EXTRA_DEVICE_ID, device.id)
        }
        startActivity(intent)
    }
}
