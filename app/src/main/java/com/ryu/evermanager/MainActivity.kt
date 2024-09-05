package com.ryu.evermanager

import android.os.Bundle
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.scottyab.rootbeer.RootBeer
import java.io.DataOutputStream
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var managerVerification: TextView
    private lateinit var managerVerificationDesc: TextView
    private lateinit var kernelInfo: TextView
    private lateinit var statusImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isDeviceRooted()) {
            setContentView(R.layout.activity_main)
            initComponents()

            kernelInfo.text = getKernelInfo()
            checkKernelAndProceed()
        } else {
            Toast.makeText(this, getString(R.string.non_rooted_device), Toast.LENGTH_LONG).show()
            setContentView(R.layout.activity_non_rooted_device)
        }
    }

    private fun initComponents() {
        kernelInfo = findViewById(R.id.kernelDescTextView)
        managerVerification = findViewById(R.id.verificationTextView)
        managerVerificationDesc = findViewById(R.id.verificationDescTextView)
        statusImage = findViewById(R.id.statusImageView)
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
            managerVerification.text = getString(R.string.verified_title)
            managerVerificationDesc.text = getString(R.string.verified_desc)
            statusImage.setImageDrawable(getDrawable(R.mipmap.ic_success))
            findViewById<ViewStub>(R.id.supportedFeatureStub).inflate()
        } else {
            managerVerification.text = getString(R.string.not_verified_title)
            managerVerificationDesc.text = getString(R.string.not_verified_desc)
            statusImage.setImageDrawable(getDrawable(R.mipmap.ic_failed))
            findViewById<ViewStub>(R.id.notSupportedFeatureStub).inflate()
        }
    }

    private fun isKernelSupported(): Boolean {
        val kernelFile = File("/proc/evergreen-kernel")
        return kernelFile.exists() && kernelFile.readText().trim() == "evergreen_kernel_verified"
    }

    private fun getKernelInfo(): String {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("cat /proc/version\n")
            os.flush()
            val info = process.inputStream.bufferedReader().readLine()
            os.writeBytes("exit\n")
            os.flush()
            info
        } catch (e: Exception) {
            e.printStackTrace()
            "Unavailable"
        }
    }
}
