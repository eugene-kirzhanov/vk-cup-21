package by.anegin.vkcup21.features.taxi.ui

import android.view.ViewGroup
import by.anegin.vkcup21.features.taxi.models.Place
import by.anegin.vkcup21.features.taxi.ui.util.ViewBindingViewHolder
import by.anegin.vkcup21.taxi.databinding.ListitemPlaceBinding

class PlaceViewHolder(
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