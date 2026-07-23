package com.dougretrogames.gamepadklfixer.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dougretrogames.gamepadklfixer.databinding.ItemKeyEventBinding
import com.dougretrogames.gamepadklfixer.model.KeyEventRecord

class KeyEventAdapter : ListAdapter<KeyEventRecord, KeyEventAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemKeyEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: KeyEventRecord) {
            binding.tvKeyCode.text = "${record.keyCodeName} (${record.keyCode})"
            binding.tvScanCode.text = "Scan: ${record.scanCode}"
            binding.tvAction.text = record.actionName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemKeyEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<KeyEventRecord>() {
            override fun areItemsTheSame(a: KeyEventRecord, b: KeyEventRecord) =
                a.timestamp == b.timestamp && a.keyCode == b.keyCode
            override fun areContentsTheSame(a: KeyEventRecord, b: KeyEventRecord) = a == b
        }
    }
}
