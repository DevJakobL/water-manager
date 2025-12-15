package com.dev.jakob.watermanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.dev.jakob.watermanager.databinding.FragmentSettingsBinding
import com.dev.jakob.watermanager.ui.settings.adapter.ContainerAdapter
import com.dev.jakob.watermanager.ui.settings.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A [Fragment] for managing the list of water containers, refactored to use modern architecture.
 *
 * This fragment displays a list of containers using a [RecyclerView] and a [ContainerAdapter].
 * It follows the Unidirectional Data Flow (UDF) pattern:
 * - It observes a [StateFlow] from the [SettingsViewModel] to get the current list of containers.
 * - It sends user actions (events) like text changes, additions, or removals to the ViewModel.
 * - The ViewModel is the single source of truth and handles all business logic.
 */
class SettingsFragment : Fragment() {

    // Lazily inject the SettingsViewModel using Koin
    private val settingsViewModel: SettingsViewModel by viewModel()

    private var _binding: FragmentSettingsBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var containerAdapter: ContainerAdapter

    /**
     * Inflates the layout for this fragment using View Binding.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Sets up the RecyclerView, adapter, UI listeners, and observes the ViewModel's state.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeViewModelState()
    }

    /**
     * Cleans up the binding reference when the view is destroyed to prevent memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Initializes the [ContainerAdapter] and sets it on the [RecyclerView].
     * The adapter is configured with lambdas that pass user events directly to the [SettingsViewModel].
     */
    private fun setupRecyclerView() {
        containerAdapter = ContainerAdapter(
            onNameChanged = { container, newName ->
                settingsViewModel.onContainerNameChanged(container.id, newName)
            },
            onSizeChanged = { container, newSize ->
                settingsViewModel.onContainerSizeChanged(container.id, newSize)
            },
            onRemoveClicked = { container ->
                settingsViewModel.removeContainer(container)
            }
        )
        binding.containerRecyclerView.adapter = containerAdapter
    }

    /**
     * Sets up click listeners for the 'Add' and 'Save' buttons, delegating actions
     * to the [SettingsViewModel].
     */
    private fun setupClickListeners() {
        binding.addContainerButton.setOnClickListener {
            settingsViewModel.addContainer()
        }

        binding.saveButton.setOnClickListener {
            settingsViewModel.saveContainers()
            // Navigate back after saving. The ViewModel handles the actual saving logic.
            findNavController().popBackStack()
        }
    }

    /**
     * Observes the UI state [StateFlow] from the [SettingsViewModel].
     * When the state changes, it submits the new list to the [ContainerAdapter],
     * which efficiently updates the [RecyclerView].
     * After submitting, it scrolls to the last item if the list is not empty,
     * to ensure newly added items are visible.
     */
    private fun observeViewModelState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.uiState.collect { containers ->
                    containerAdapter.submitList(containers) {
                        // Scroll to the last item if a new one was added
                        if (containers.isNotEmpty()) {
                            binding.containerRecyclerView.smoothScrollToPosition(containers.size - 1)
                        }
                    }
                }
            }
        }
    }
}
