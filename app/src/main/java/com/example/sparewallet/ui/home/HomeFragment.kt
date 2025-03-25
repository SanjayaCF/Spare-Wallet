package com.example.sparewallet.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.sparewallet.ComingSoonActivity
import com.example.sparewallet.HistoryActivity
import com.example.sparewallet.TopUpActivity
import com.example.sparewallet.TransferActivity
import com.example.sparewallet.databinding.FragmentHomeBinding
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel.balance.observe(viewLifecycleOwner) { balanceStr ->
            val balanceNumber = balanceStr.toDoubleOrNull() ?: 0.0
            val formattedBalance = NumberFormat.getNumberInstance(Locale.US).format(balanceNumber)
            binding.textBalance.text = "Balance: Rp. $formattedBalance"
            binding.textBalance.textSize = 20f
        }

        homeViewModel.accountNumber.observe(viewLifecycleOwner) { accountNumber ->
            binding.textAccountNumber.text = "Account Number: $accountNumber"
        }

        homeViewModel.name.observe(viewLifecycleOwner) { name ->
            binding.textName.text = name
        }

        binding.textAccountNumber.setOnClickListener {
            val accountText = binding.textAccountNumber.text.toString().replace("Account Number: ", "")
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Account Number", accountText)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Account number copied", Toast.LENGTH_SHORT).show()
        }

        binding.buttonTopUp.setOnClickListener {
            val intent = Intent(requireContext(), TopUpActivity::class.java)
            startActivity(intent)
        }

        binding.buttonTransfer.setOnClickListener {
            val intent = Intent(requireContext(), TransferActivity::class.java)
            startActivity(intent)
        }

        binding.buttonHistory.setOnClickListener {
            val intent = Intent(requireContext(), HistoryActivity::class.java)
            startActivity(intent)
        }

        binding.buttonCart.setOnClickListener {
            val intent = Intent(requireContext(), ComingSoonActivity::class.java)
            intent.putExtra("menuName", "Cart")
            startActivity(intent)
        }

        binding.buttonDebit.setOnClickListener {
            val intent = Intent(requireContext(), ComingSoonActivity::class.java)
            intent.putExtra("menuName", "Debit Card")
            startActivity(intent)
        }

        binding.buttonFlazz.setOnClickListener {
            val intent = Intent(requireContext(), ComingSoonActivity::class.java)
            intent.putExtra("menuName", "Flazz")
            startActivity(intent)
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.refreshData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
