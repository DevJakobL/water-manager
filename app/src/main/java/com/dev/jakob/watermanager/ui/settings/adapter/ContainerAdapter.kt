package com.dev.jakob.watermanager.ui.settings.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.databinding.ContainerItemBinding

/**
 * A [ListAdapter] for displaying and editing a list of [Container] objects in a [RecyclerView].
 * It follows modern Android architecture principles by communicating user interactions (events)
 * back to the ViewModel via lambda functions.
 *
 * @param onNameChanged A lambda function invoked when the container's name is changed by the user and the EditText loses focus.
 *                      It now passes the container's ID and the new name.
 * @param onSizeChanged A lambda function invoked when the container's size is changed by the user and the EditText loses focus.
 *                      It now passes the container's ID and the new size.
 * @param onRemoveClicked A lambda function invoked when the user clicks the remove button.
 */
class ContainerAdapter(
    private val onNameChanged: (String?, String) -> Unit, // Changed to pass ID and new name
    private val onSizeChanged: (String?, Int) -> Unit,    // Changed to pass ID and new size
    private val onRemoveClicked: (Container) -> Unit
) : ListAdapter<Container, ContainerAdapter.ContainerViewHolder>(ContainerDiffCallback()) {

    /**
     * Creates a new [ContainerViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContainerViewHolder {
        val binding = ContainerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContainerViewHolder(binding)
    }

    /**
     * Binds a [Container] to a [ContainerViewHolder], setting up its data and listeners.
     */
    override fun onBindViewHolder(holder: ContainerViewHolder, position: Int) {
        val container = getItem(position)
        holder.bind(container, onNameChanged, onSizeChanged, onRemoveClicked)
    }

    /**
     * The [RecyclerView.ViewHolder] for a single container item.
     * It manages the binding and the listeners for the input fields.
     */
    class ContainerViewHolder(private val binding: ContainerItemBinding) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds a [Container] object to the view.
         * Updates to the ViewModel are triggered when the EditText fields lose focus.
         *
         * @param container The container to display.
         * @param onNameChanged The callback for name changes.
         * @param onSizeChanged The callback for size changes.
         * @param onRemoveClicked The callback for the remove button.
         */
        fun bind(
            container: Container,
            onNameChanged: (String?, String) -> Unit, // Changed to pass ID and new name
            onSizeChanged: (String?, Int) -> Unit,    // Changed to pass ID and new size
            onRemoveClicked: (Container) -> Unit
        ) {
            // Set the current data directly. DiffUtil will handle efficient updates.
            binding.containerNameInput.setText(container.name)
            val sizeText = if (container.size > 0) container.size.toString() else ""
            binding.containerSizeInput.setText(sizeText)

            // Set OnFocusChangeListener for name input
            binding.containerNameInput.onFocusChangeListener =
                View.OnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) { // If focus is lost
                        val newName = binding.containerNameInput.text.toString()
                        if (newName != container.name) { // Only update if value actually changed
                            onNameChanged(container.id, newName) // Pass ID and new name
                        }
                    }
                }

            // Set OnFocusChangeListener for size input
            binding.containerSizeInput.onFocusChangeListener =
                View.OnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) { // If focus is lost
                        val newSize = binding.containerSizeInput.text.toString().toIntOrNull() ?: 0
                        if (newSize != container.size) { // Only update if value actually changed
                            onSizeChanged(container.id, newSize) // Pass ID and new size
                        }
                    }
                }

            binding.removeContainerButton.setOnClickListener {
                onRemoveClicked(container)
            }
        }
    }

    /**
     * A [DiffUtil.ItemCallback] for calculating the difference between two [Container] lists.
     * This allows the [ListAdapter] to perform efficient UI updates.
     */
    class ContainerDiffCallback : DiffUtil.ItemCallback<Container>() {
        override fun areItemsTheSame(oldItem: Container, newItem: Container): Boolean {
            // Compare IDs. Since id is now String?, we compare them directly.
            // This handles cases where both are null, one is null, or both are non-null.
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Container,
            newItem: Container
        ): Boolean {
            return oldItem == newItem
        }
    }
}
