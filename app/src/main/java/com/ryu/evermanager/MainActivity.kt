package com.ryu.evermanager

import android.os.Bundle
import android.view.ViewStub
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.scottyab.rootbeer.RootBeer
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var kernelName: TextView
    private lateinit var kernelVersion: TextView
    private lateinit var managerVerification: TextView
    private lateinit var managerVersion: TextView

    private fun initComponents() {
        kernelName = findViewById(R.id.kernelNameTextView)
        kernelVersion = findViewById(R.id.kernelVersionTextView)
        managerVerification = findViewById(R.id.managerVerificationTextView)
        managerVersion = findViewById(R.id.managerVersionTextView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (isDeviceRooted()) {
            setContentView(R.layout.activity_main)
            initComponents()
            kernelName.text = getString(R.string.kernel_name) + " " + getKernelName()
            kernelVersion.text = getString(R.string.kernel_version) + " " + getKernelVersion()
            managerVersion.text = getString(R.string.manager_version) + " " + getAppVersion()
            checkKernelAndProceed()
        } else {
            Toast.makeText(this, getString(R.string.non_rooted_device), Toast.LENGTH_LONG).show()
            setContentView(R.layout.activity_non_rooted_device)
        }
    }

    private fun isDeviceRooted(): Boolean {
        val rootBeer = RootBeer(this)
        return rootBeer.isRooted
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

    private fun getKernelName(): String {
        var kernelName = "Unknown"
        try {
            val versionFile = File("/proc/version")
            val bufferedReader = BufferedReader(FileReader(versionFile))
            val versionLine = bufferedReader.readLine()
            bufferedReader.close()

            kernelName = versionLine.split(" ")[2]
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return kernelName
    }

    private fun getKernelVersion(): String {
        var kernelVersion = "Unknown"
        try {
            val versionFile = File("/proc/version")
            val bufferedReader = BufferedReader(FileReader(versionFile))
            val versionLine = bufferedReader.readLine()
            bufferedReader.close()

            kernelVersion = versionLine.split(" ")[3]
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return kernelVersion
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            e.printStackTrace()
            "Unknown"
        }
    }
}
