package com.dougretrogames.gamepadklfixer.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dougretrogames.gamepadklfixer.databinding.ItemDeviceBinding
import com.dougretrogames.gamepadklfixer.model.GamepadDevice

class DeviceAdapter(
    private val onItemClick: (GamepadDevice) -> Unit
) : ListAdapter<GamepadDevice, DeviceAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(device: GamepadDevice) {
            binding.tvName.text = device.name
            binding.tvIds.text = "Vendor: 0x${device.vendorHex}  Product: 0x${device.productHex}"
            binding.tvKlFile.text = device.klFileName
            binding.root.setOnClickListener { onItemClick(device) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<GamepadDevice>() {
            override fun areItemsTheSame(a: GamepadDevice, b: GamepadDevice) = a.id == b.id
            override fun areContentsTheSame(a: GamepadDevice, b: GamepadDevice) = a == b
        }
    }
}
