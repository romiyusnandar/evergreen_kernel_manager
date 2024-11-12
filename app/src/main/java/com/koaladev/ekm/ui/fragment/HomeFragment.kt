package com.koaladev.ekm.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.koaladev.ekm.R
import com.koaladev.ekm.databinding.FragmentHomeBinding
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.FileReader
import java.io.IOException

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardSupport.setOnClickListener {
            openSupportLink()
        }

        checkEvergreenKernel()
        updateDeviceInfo()
    }

    private fun checkEvergreenKernel() {
        val isEvergreenSupported = KernelChecker.isEvergreenKernel()
        updateKernelStatusUI(isEvergreenSupported)
    }

    private fun updateKernelStatusUI(isSupported: Boolean) {
        with(binding) {
            if (isSupported) {
                cardKernelStatus.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_theme_secondaryContainer))
                ivStatus.setImageResource(R.drawable.ic_check_badge)
                ivStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.md_theme_primary))
                tvKernelStatus.text = getString(R.string.evergreen_kernel_supported)
            } else {
                cardKernelStatus.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_theme_errorContainer))
                ivStatus.setImageResource(R.drawable.ic_close_circle)
                ivStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.md_theme_error))
                tvKernelStatus.text = getString(R.string.evergreen_kernel_not_supported)
            }
        }
    }

    private fun updateDeviceInfo() {
        binding.apply {
            tvKernelVersionSummary.text = getKernelVersion()
            tvFingerprintVersionSummary.text = getFingerprint()
            tvSeLinuxSummary.text = getSeLinuxStatus()
        }
    }

    private fun getKernelVersion(): String {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            val inputStream = process.inputStream.bufferedReader()

            try {
                os.writeBytes("cat /proc/version\n")
                os.flush()
                val info = inputStream.readLine()
                os.writeBytes("exit\n")
                os.flush()

                info ?: getString(R.string.unknown)
            } finally {
                os.close()
                inputStream.close()
                process.destroy()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            try {
                // Fallback to non-root method if su fails
                val reader = BufferedReader(FileReader("/proc/version"))
                val kernelVersion = reader.readLine()
                reader.close()
                kernelVersion ?: getString(R.string.unknown)
            } catch (e: Exception) {
                e.printStackTrace()
                getString(R.string.unknown)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            getString(R.string.unknown)
        }
    }

    private fun getFingerprint(): String {
        return Build.FINGERPRINT
    }

    private fun getSeLinuxStatus(): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "getenforce"))
            val result = process.inputStream.bufferedReader().use { it.readText().trim() }
            process.waitFor()
            if (result.isNotEmpty()) result else getString(R.string.unknown)
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                // Fallback to non-root method if su fails
                val reader = BufferedReader(FileReader("/sys/fs/selinux/enforce"))
                val status = reader.readLine()
                reader.close()
                when (status) {
                    "0" -> "Permissive"
                    "1" -> "Enforcing"
                    else -> getString(R.string.unknown)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                getString(R.string.unknown)
            }
        }
    }

    private fun openSupportLink() {
        val url = "https://t.me/romiyusna"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}