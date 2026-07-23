package com.dougretrogames.gamepadfixer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dougretrogames.gamepadfixer.databinding.ItemEventBinding
import com.dougretrogames.gamepadfixer.viewmodel.CapturedEvent

class EventAdapter : ListAdapter<CapturedEvent, EventAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<CapturedEvent>() {
            override fun areItemsTheSame(a: CapturedEvent, b: CapturedEvent) =
                a.description == b.description && a.type == b.type
            override fun areContentsTheSame(a: CapturedEvent, b: CapturedEvent) = a == b
        }
    }

    inner class ViewHolder(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: CapturedEvent) {
            binding.tvEventType.text = event.type
            binding.tvEventDescription.text = event.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
