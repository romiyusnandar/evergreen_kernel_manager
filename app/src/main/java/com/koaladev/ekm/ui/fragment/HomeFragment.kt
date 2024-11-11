package com.koaladev.ekm.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.koaladev.ekm.R
import com.koaladev.ekm.databinding.FragmentHomeBinding

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

        checkEvergreenKernel()
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
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}