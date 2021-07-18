package by.anegin.vkcup21.features.taxi.ui.order.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import by.anegin.vkcup21.features.taxi.data.models.Place

internal class PlacesAdapter(
    private val onClick: (place: Place) -> Unit
) : ListAdapter<Place, PlaceViewHolder>(PlacesDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        return PlaceViewHolder(parent, onClick)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PlacesDiffCallback : DiffUtil.ItemCallback<Place>() {
        override fun areItemsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem.position == newItem.position
        }

        override fun areContentsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem.title == newItem.title
                && oldItem.address == newItem.address
        }
    }

}