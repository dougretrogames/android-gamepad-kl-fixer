package com.dougretrogames.gamepadfixer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dougretrogames.gamepadfixer.R
import com.dougretrogames.gamepadfixer.model.GamepadDevice

/**
 * RecyclerView adapter for displaying a list of [GamepadDevice] items.
 */
class GamepadAdapter(
    private val onItemClick: (GamepadDevice) -> Unit
) : ListAdapter<GamepadDevice, GamepadAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gamepad, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvDeviceName)
        private val tvIds: TextView  = itemView.findViewById(R.id.tvDeviceIds)
        private val tvKl: TextView   = itemView.findViewById(R.id.tvKlFileName)

        fun bind(device: GamepadDevice) {
            tvName.text = device.name
            tvIds.text  = "VID: 0x${device.vendorHex}  PID: 0x${device.productHex}"
            tvKl.text   = device.klFileName
            itemView.setOnClickListener { onItemClick(device) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GamepadDevice>() {
            override fun areItemsTheSame(old: GamepadDevice, new: GamepadDevice) =
                old.id == new.id
            override fun areContentsTheSame(old: GamepadDevice, new: GamepadDevice) =
                old == new
        }
    }
}
