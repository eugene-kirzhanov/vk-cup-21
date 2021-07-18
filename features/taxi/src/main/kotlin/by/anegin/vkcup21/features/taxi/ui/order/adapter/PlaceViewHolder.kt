package by.anegin.vkcup21.features.taxi.ui.order.adapter

import android.view.ViewGroup
import by.anegin.vkcup21.core.util.ViewBindingViewHolder
import by.anegin.vkcup21.features.taxi.data.models.Place
import by.anegin.vkcup21.taxi.databinding.ListitemPlaceBinding

internal class PlaceViewHolder(
    parent: ViewGroup,
    onClick: (place: Place) -> Unit
) : ViewBindingViewHolder<ListitemPlaceBinding>(parent, ListitemPlaceBinding::inflate) {

    private var place: Place? = null

    init {
        binding.root.setOnClickListener {
            place?.let(onClick)
        }
    }

    fun bind(place: Place) {
        this.place = place
        binding.textTitle.text = place.title
        binding.textSubtitle.text = place.address
    }

}