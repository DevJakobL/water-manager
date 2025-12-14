package com.dev.jakob.watermanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.databinding.ContainerItemBinding
import com.dev.jakob.watermanager.databinding.FragmentSettingsBinding
import com.dev.jakob.watermanager.ui.settings.viewmodel.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A [Fragment] for managing the list of water containers.
 * Users can add, edit, and remove containers. This fragment uses View Binding and a [SettingsViewModel]
 * to manage its state and interactions with the data layer.
 *
 * **Note for future improvement:** The current implementation manually manages views in a LinearLayout.
 * For better performance and code structure, this should be refactored to use a `RecyclerView` with a `RecyclerView.Adapter`.
 */
class SettingsFragment : Fragment() {

    // Lazily inject the SettingsViewModel using Koin
    private val settingsViewModel: SettingsViewModel by viewModel()

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    /**
     * Inflates the layout for this fragment using View Binding.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Sets up UI listeners and observes ViewModel LiveData after the view has been created.
     *
     * @param view The View returned by [onCreateView].
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeContainers()
        settingsViewModel.loadContainers()
    }

    /**
     * Cleans up the binding reference when the view is destroyed to prevent memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Sets up click listeners for the 'Add' and 'Save' buttons.
     */
    private fun setupClickListeners() {
        binding.addContainerButton.setOnClickListener {
            // Add a new empty container view for the user to fill out
            addContainerView(Container("", 0))
        }

        binding.saveButton.setOnClickListener {
            saveContainersFromUi()
            // Navigate back to the home screen after saving.
            (activity as? MainActivity)?.navigateToHome()
        }
    }

    /**
     * Observes the list of containers from the [SettingsViewModel] and updates the UI accordingly.
     */
    private fun observeContainers() {
        settingsViewModel.containers.observe(viewLifecycleOwner) { containers ->
            populateContainerList(containers)
        }
    }

    /**
     * Populates the list of container views based on the data from the ViewModel.
     * It clears the existing list before adding the new views.
     *
     * @param containers The list of [Container]s to display.
     */
    private fun populateContainerList(containers: List<Container>) {
        binding.containerList.removeAllViews()
        containers.forEach { container ->
            addContainerView(container)
        }
    }

    /**
     * Adds a new row to the UI for a single container, either new or existing.
     * This method inflates a dedicated item layout and sets its data.
     *
     * @param container The [Container] to display in the new row.
     */
    private fun addContainerView(container: Container) {
        val inflater = LayoutInflater.from(requireContext())
        val itemBinding = ContainerItemBinding.inflate(inflater, binding.containerList, false)

        itemBinding.containerNameInput.setText(container.name)
        itemBinding.containerSizeInput.setText(if (container.size > 0) container.size.toString() else "")

        itemBinding.removeContainerButton.setOnClickListener {
            binding.containerList.removeView(itemBinding.root)
        }

        // Storing the binding in the tag is a workaround to retrieve it later.
        // This would be unnecessary with a RecyclerView implementation.
        itemBinding.root.tag = itemBinding
        binding.containerList.addView(itemBinding.root)
    }

    /**
     * Gathers the data from all container input fields in the UI,
     * creates a new list of [Container] objects, and tells the ViewModel to save them.
     * This method contains UI logic that should ideally be handled by a RecyclerView adapter.
     */
    private fun saveContainersFromUi() {
        val newContainers = mutableListOf<Container>()
        for (i in 0 until binding.containerList.childCount) {
            val view = binding.containerList.getChildAt(i)
            // Retrieve the binding safely from the tag.
            val itemBinding = view.tag as? ContainerItemBinding ?: continue

            val name = itemBinding.containerNameInput.text.toString()
            val size = itemBinding.containerSizeInput.text.toString().toIntOrNull() ?: 0
            if (name.isNotBlank() && size > 0) {
                newContainers.add(Container(name, size))
            }
        }
        settingsViewModel.saveContainers(newContainers)
    }
}
