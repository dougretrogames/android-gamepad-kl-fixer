package com.dougretrogames.gamepadfixer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dougretrogames.gamepadfixer.databinding.ItemDeviceBinding
import com.dougretrogames.gamepadfixer.model.InputDeviceInfo

class DeviceAdapter(
    private val onGenerateKl: (InputDeviceInfo) -> Unit
) : ListAdapter<InputDeviceInfo, DeviceAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<InputDeviceInfo>() {
            override fun areItemsTheSame(a: InputDeviceInfo, b: InputDeviceInfo) = a.id == b.id
            override fun areContentsTheSame(a: InputDeviceInfo, b: InputDeviceInfo) = a == b
        }
    }

    inner class ViewHolder(private val binding: ItemDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(device: InputDeviceInfo) {
            binding.tvDeviceName.text = device.name
            binding.tvVendorProduct.text =
                "VID: %04x  PID: %04x".format(device.vendorId, device.productId)
            binding.tvDescriptor.text = "Descriptor: ${device.descriptor.take(32)}..."
            binding.tvKlFilename.text = "KL: ${device.klFileName()}"
            binding.tvAxesCount.text = "Axes: ${device.axes.size}  Buttons: ${device.keys.size}"
            binding.btnGenerateKl.setOnClickListener { onGenerateKl(device) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDeviceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
