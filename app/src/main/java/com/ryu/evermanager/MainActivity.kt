package com.ryu.evermanager

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewStub
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.scottyab.rootbeer.RootBeer
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.FileReader
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var kernelInfo: TextView
    private lateinit var managerVerification: TextView
    private lateinit var managerVersion: TextView
    private var fullKernelVersion = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Set content view for rooted or non-rooted device
        if (isDeviceRooted()) {
            setContentView(R.layout.activity_main)
            initComponents()
            setupToolbar()

            kernelInfo.text = getString(R.string.kernel_info) + " " + getKernelInfo()
            managerVersion.text = getString(R.string.manager_version) + " " + getAppVersion()
            checkKernelAndProceed()
        } else {
            Toast.makeText(this, getString(R.string.non_rooted_device), Toast.LENGTH_LONG).show()
            setContentView(R.layout.activity_non_rooted_device)
        }

        // Set click listener for kernelInfo TextView
        kernelInfo.setOnClickListener {
            if (fullKernelVersion) {
                kernelInfo.text = getString(R.string.kernel_info) + " " + getKernelInfo()
                fullKernelVersion = false
            } else {
                kernelInfo.text = getFullKernelVersion()
                fullKernelVersion = true
            }
        }
    }

    private fun initComponents() {
        kernelInfo = findViewById(R.id.kernelInfoTextView)
        managerVerification = findViewById(R.id.managerVerificationTextView)
        managerVersion = findViewById(R.id.managerVersionTextView)
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun isDeviceRooted(): Boolean {
        val rootBeer = RootBeer(this)
        return rootBeer.isRooted && hasRootAccess()
    }

    private fun hasRootAccess(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("exit\n")
            os.flush()
            process.waitFor() == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun checkKernelAndProceed() {
        if (isKernelSupported()) {
            managerVerification.text = getString(R.string.manager_verification) + " " + getString(R.string.verified)
            val stub: ViewStub = findViewById(R.id.supportedFeatureStub)
            stub.inflate()
        } else {
            managerVerification.text = getString(R.string.manager_verification) + " " + getString(R.string.not_verified)
            val stub: ViewStub = findViewById(R.id.notSupportedFeatureStub)
            stub.inflate()
        }
    }

    private fun isKernelSupported(): Boolean {
        val kernelFile = File("/proc/evergreen-kernel")
        if (kernelFile.exists()) {
            val kernelFlag = kernelFile.readText().trim()
            return kernelFlag == "evergreen_kernel_verified"
        }
        return false
    }

    private fun getKernelInfo(): String {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            val reader = process.inputStream.bufferedReader()

            os.writeBytes("cat /proc/version\n")
            os.flush()
            os.writeBytes("exit\n")
            os.flush()

            reader.readLine().also {
                process.waitFor()
                reader.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Unavailable"
        }
    }

    private fun getFullKernelVersion(): String {
        return try {
            readLine("/proc/version")
        } catch (e: IOException) {
            e.printStackTrace()
            "Unavailable"
        }
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            e.printStackTrace()
            "Unknown package version"
        }
    }

    private fun readLine(filename: String): String {
        BufferedReader(FileReader(filename), 256).use { reader ->
            return reader.readLine()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                Toast.makeText(this, "Pengaturan diklik", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
