package com.smartroll.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.smartroll.MainActivity
import com.smartroll.R
import com.smartroll.repository.MainRepository
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var repository: MainRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = MainRepository(requireContext())

        loadUserData(view)

        val tvProfileServerIp = view.findViewById<TextView>(R.id.tvProfileServerIp)
        tvProfileServerIp.text = "🌐 Server: ${com.smartroll.api.ApiService.serverUrl}"

        view.findViewById<Button>(R.id.btnChangeServerIp).setOnClickListener {
            val input = android.widget.EditText(requireContext())
            input.hint = "https://blinkerp-live.loca.lt"
            input.setText(com.smartroll.api.ApiService.serverUrl)

            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Server URL")
                .setMessage("Default: https://blinkerp-live.loca.lt\n(Works on all networks & 4G/5G)")
                .setView(input)
                .setPositiveButton("Save & Test") { _, _ ->
                    val ip = input.text.toString().trim()
                    com.smartroll.api.ApiService.updateUrl(requireContext(), ip)
                    tvProfileServerIp.text = "🌐 Server: ${com.smartroll.api.ApiService.serverUrl}"
                    
                    lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        val (ok, msg) = com.smartroll.api.ApiService.testConnection()
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            if (ok) {
                                Toast.makeText(requireContext(), "✅ Connected: $msg", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(requireContext(), "⚠️ Warning: $msg (Check Wi-Fi)", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun loadUserData(view: View) {
        lifecycleScope.launch {
            val user = repository.getCurrentUser() ?: return@launch
            view.findViewById<TextView>(R.id.tvUserName).text = user.name
            view.findViewById<TextView>(R.id.tvUserRole).text = user.role.uppercase()
            
            val info = StringBuilder()
            info.append("Course: ${user.course ?: "N/A"}\n")
            info.append("Year: ${user.year ?: "N/A"}\n")
            info.append("Branch: ${user.branch ?: "N/A"}\n")
            info.append("Section: ${user.section ?: "N/A"}\n")
            if (user.role == "teacher") info.append("Subject: ${user.subject ?: "N/A"}")
            
            view.findViewById<TextView>(R.id.tvUserInfo).text = info.toString()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                repository.logout()
                Toast.makeText(requireContext(), "Session Cleared", Toast.LENGTH_SHORT).show()
                
                // FORCE RESTART TO SPLASH TO RE-EVALUATE LOGIN
                val intent = Intent(requireActivity(), com.smartroll.SplashActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                requireActivity().finishAffinity()
            }
            .setNegativeButton("No", null)
            .show()
    }
}