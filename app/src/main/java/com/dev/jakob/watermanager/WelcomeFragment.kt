package com.dev.jakob.watermanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.databinding.FragmentWelcomeBinding
import com.dev.jakob.watermanager.ui.welcome.viewmodel.WelcomeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A [Fragment] that displays the main screen of the Water Manager app.
 * It shows the total amount of water consumed and provides buttons to add water based on predefined containers.
 * This fragment follows a modern architecture, using View Binding to access UI elements
 * and a [WelcomeViewModel] to manage its state and business logic.
 */
class WelcomeFragment : Fragment() {

    // Lazily inject the WelcomeViewModel using Koin
    private val welcomeViewModel: WelcomeViewModel by viewModel()

    private var _binding: FragmentWelcomeBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    /**
     * Inflates the layout for this fragment using View Binding and sets up the initial view.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Sets up observers on the [WelcomeViewModel]'s LiveData to update the UI
     * whenever the data changes.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    /**
     * Called when the fragment is visible to the user.
     * It triggers a data refresh in the ViewModel to ensure the displayed data is up-to-date.
     */
    override fun onResume() {
        super.onResume()
        welcomeViewModel.refreshData()
    }

    /**
     * Cleans up the binding reference when the view is destroyed to prevent memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Observes [LiveData] from the [WelcomeViewModel] to update the UI.
     * It observes the total water amount and the list of containers.
     */
    private fun setupObservers() {
        welcomeViewModel.totalWaterToday.observe(viewLifecycleOwner) { totalWaterAmount ->
            updateWaterText(totalWaterAmount)
        }
        welcomeViewModel.containers.observe(viewLifecycleOwner) { containers ->
            populateButtons(containers)
        }
    }

    /**
     * Dynamically creates and adds buttons to the UI for each water container.
     * Each button is configured with an OnClickListener to add the corresponding amount of water.
     *
     * @param containers The list of [Container] objects to create buttons for.
     */
    private fun populateButtons(containers: List<Container>) {
        binding.buttonContainer.removeAllViews()
        for (container in containers) {
            val button = Button(requireContext()).apply {
                text = getString(R.string.container_button_text, container.name, container.size)
                setOnClickListener {
                    welcomeViewModel.addWater(container)
                }
            }
            binding.buttonContainer.addView(button)
        }
    }

    /**
     * Updates the TextView that displays the total water amount.
     *
     * @param totalWater The total amount of water in milliliters.
     */
    private fun updateWaterText(totalWater: Int) {
        binding.totalWaterText.text = getString(R.string.water_amount_ml, totalWater)
    }
}
