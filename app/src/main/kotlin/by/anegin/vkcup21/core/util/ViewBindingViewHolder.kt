package by.anegin.vkcup21.core.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class ViewBindingViewHolder<B : ViewBinding>(
    parent: ViewGroup,
    bindingInflater: (inflater: LayoutInflater, container: ViewGroup, attach: Boolean) -> B,
    protected val binding: B = bindingInflater(LayoutInflater.from(parent.context), parent, false)
) : RecyclerView.ViewHolder(binding.root)