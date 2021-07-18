package by.anegin.vkcup21.features.taxi.ui.order.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import by.anegin.vkcup21.features.taxi.ui.order.models.RouteVariant

internal class RouteVariantsAdapter(
    private val onClick: (variant: RouteVariant) -> Unit
) : ListAdapter<RouteVariant, RouteVariantViewHolder>(RouteVariantDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteVariantViewHolder {
        return RouteVariantViewHolder(parent, onClick)
    }

    override fun onBindViewHolder(holder: RouteVariantViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RouteVariantDiffCallback : DiffUtil.ItemCallback<RouteVariant>() {
        override fun areItemsTheSame(oldItem: RouteVariant, newItem: RouteVariant): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RouteVariant, newItem: RouteVariant): Boolean {
            return oldItem == newItem
        }
    }

}