package com.koaladev.ekm.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.koaladev.ekm.R
import com.koaladev.ekm.databinding.ActivityMainBinding
import com.koaladev.ekm.helper.RootChecker
import com.koaladev.ekm.helper.ToolbarTitleUpdater

class MainActivity : AppCompatActivity(), ToolbarTitleUpdater {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!RootChecker.isDeviceRooted()) {
            startActivity(Intent(this, NonrootActivity::class.java))
            finish()
            return
        }

        toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        setupNavigation()
        setupEdgeToEdge()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.restartButton -> {
                showRebootDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun updateToolbarTitle(title: String) {
        toolbar.title = title
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavbar.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateToolbarTitleForDestination(destination)
        }

        // soon handle reselection to prevent unnecessary navigation
        binding.bottomNavbar.setOnItemReselectedListener { /* Do nothing */ }
    }


    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.root.setPadding(insets.left, insets.top, insets.right, 0)
            binding.bottomNavbar.setPadding(0, 0, 0, insets.bottom)

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun updateToolbarTitleForDestination(destination: NavDestination) {
        val title = when (destination.id) {
            R.id.homeFragment -> getString(R.string.fragment_home_title)
            R.id.managerFragment -> getString(R.string.fragment_manager_title)
            R.id.settingFragment -> getString(R.string.fragment_setting_title)
            else -> getString(R.string.app_name)
        }
        updateToolbarTitle(title)
    }

    private fun showRebootDialog() {
        val option = arrayOf(getString(R.string.restart_system), getString(R.string.restart_recovery))
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.restart_action))
            .setItems(option) { _, which ->
                when (which) {
                    0 -> rebootSystem()
                    1 -> rebootToRecovery()
                }
            }
            .setNeutralButton(getString(R.string.restart_cancel), null)
            .show()
    }

    private fun rebootSystem() {
        try {
            Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun rebootToRecovery() {
        try {
            Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot recovery"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}