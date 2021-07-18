package by.anegin.vkcup21.features.taxi.ui.order.adapter

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import by.anegin.vkcup21.features.taxi.ui.order.models.RouteVariant
import by.anegin.vkcup21.features.taxi.ui.order.util.ViewBindingViewHolder
import by.anegin.vkcup21.taxi.R
import by.anegin.vkcup21.taxi.databinding.ListitemRouteVariantBinding

internal class RouteVariantViewHolder(
    parent: ViewGroup,
    onClick: (variant: RouteVariant) -> Unit
) : ViewBindingViewHolder<ListitemRouteVariantBinding>(parent, ListitemRouteVariantBinding::inflate) {

    private var variant: RouteVariant? = null

    init {
        binding.root.setOnClickListener {
            variant?.let(onClick)
        }
    }

    fun bind(variant: RouteVariant) {
        this.variant = variant
        binding.imageCategory.setImageResource(variant.categoryIcon)
        binding.textCategoryName.setText(variant.categoryTitle)
        binding.textInfo.text = variant.info
        binding.textCost.text = variant.cost

        if (variant.isSelected) {
            binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.selected_route_background))
        } else {
            binding.root.setBackgroundResource(R.drawable.selectable_background)
        }
    }

}