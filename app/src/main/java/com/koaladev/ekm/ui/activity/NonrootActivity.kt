package com.koaladev.ekm.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.koaladev.ekm.R
import com.koaladev.ekm.databinding.ActivityNonrootBinding
import java.io.DataOutputStream
import kotlin.system.exitProcess

class NonrootActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNonrootBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNonrootBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.root.setPadding(insets.left, insets.top, insets.right, 0)

            WindowInsetsCompat.CONSUMED
        }

        binding.btnGrantRootAccess.setOnClickListener {
            requestRootAccess()
        }
    }

    private fun requestRootAccess() {
        try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("exit\n")
            os.flush()

            val exitValue = process.waitFor()
            if (exitValue == 0) {
                Toast.makeText(this, getString(R.string.root_access_granted), Toast.LENGTH_SHORT).show()
                restartApp()
            } else {
                Toast.makeText(this, getString(R.string.root_access_denied), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun restartApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }
}