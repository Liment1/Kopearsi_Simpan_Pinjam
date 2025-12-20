package com.example.project_map.ui.user.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_map.databinding.FragmentUserNotificationBinding

class UserNotificationFragment : Fragment() {

    private var _binding: FragmentUserNotificationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserNotificationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Toolbar Setup
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.rvNotifications.layoutManager = LinearLayoutManager(context)
        val adapter = UserNotificationAdapter(emptyList())
        binding.rvNotifications.adapter = adapter

        viewModel.notifications.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvNotifications.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvNotifications.visibility = View.VISIBLE
                adapter.updateList(list)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}