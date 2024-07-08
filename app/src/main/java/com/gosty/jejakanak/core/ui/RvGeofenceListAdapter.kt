package com.gosty.jejakanak.core.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gosty.jejakanak.R
import com.gosty.jejakanak.core.domain.models.GeofenceModel
import com.gosty.jejakanak.databinding.CardGeofenceItemBinding

class RvGeofenceListAdapter :
    ListAdapter<GeofenceModel, RvGeofenceListAdapter.RvGeofenceListViewHolder>(DIFF_CALLBACK) {
    private lateinit var onItemClickCallback: OnItemClickCallback

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvGeofenceListViewHolder {
        val binding = CardGeofenceItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RvGeofenceListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RvGeofenceListViewHolder, position: Int) {
        val geofence = getItem(position)
        holder.bind(geofence)
        holder.binding.btnEditGeofence.setOnClickListener {
            onItemClickCallback.onEditGeofenceClicked(geofence)
        }
        holder.binding.btnDeleteGeofence.setOnClickListener {
            onItemClickCallback.onDeleteGeofenceClicked(geofence)
        }
    }

    class RvGeofenceListViewHolder(val binding: CardGeofenceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(geofenceModel: GeofenceModel) {
            binding.apply {
                tvGeofenceLabel.text = geofenceModel.label
                tvGeofenceType.text = if (geofenceModel.type == "danger") {
                    itemView.context.getString(R.string.danger_zone)
                } else {
                    itemView.context.getString(R.string.safe_zone)
                }
                if (geofenceModel.type == "danger") {
                    tvGeofenceType.setTextColor(itemView.context.getColor(R.color.red_DC2020))
                }
            }
        }
    }

    interface OnItemClickCallback {
        fun onEditGeofenceClicked(geofenceModel: GeofenceModel)
        fun onDeleteGeofenceClicked(geofenceModel: GeofenceModel)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GeofenceModel>() {
            override fun areItemsTheSame(oldItem: GeofenceModel, newItem: GeofenceModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: GeofenceModel,
                newItem: GeofenceModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}