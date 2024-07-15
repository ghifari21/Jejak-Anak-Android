package com.gosty.jejakanak.core.ui

import android.location.Location
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.gosty.jejakanak.R
import com.gosty.jejakanak.core.domain.models.ChildModel
import com.gosty.jejakanak.databinding.CardChildItemBinding
import com.gosty.jejakanak.utils.formatFloatWithSeparator
import com.gosty.jejakanak.utils.toKm

class RvChildListAdapter :
    ListAdapter<ChildModel, RvChildListAdapter.RvChildListViewHolder>(DIFF_CALLBACK) {
    private lateinit var onItemClickCallback: OnItemClickCallback
    private var currentParentLocation: LatLng? = null

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvChildListViewHolder {
        val binding = CardChildItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RvChildListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RvChildListViewHolder, position: Int) {
        val child = getItem(position)
        holder.bind(child, currentParentLocation)
        holder.binding.btnSeeChild.setOnClickListener {
            onItemClickCallback.onSeeChildClicked(child)
        }
        holder.binding.btnDeleteChild.setOnClickListener {
            onItemClickCallback.onDeleteChildClicked(child)
        }
    }

    fun updateParentLocation(parentLocation: LatLng) {
        currentParentLocation = parentLocation
        notifyDataSetChanged()
    }

    class RvChildListViewHolder(val binding: CardChildItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(childModel: ChildModel, currentParentLocation: LatLng?) {
            binding.apply {
                Glide.with(itemView.context)
                    .load(childModel.photo)
                    .placeholder(R.drawable.ic_image_black)
                    .error(R.drawable.ic_broken_image_black)
                    .centerCrop()
                    .into(ivChildAvatar)

                tvChildName.text = childModel.firstName
                tvChildEmail.text = childModel.email
                if (childModel.coordinate != null && currentParentLocation != null) {
                    val distanceResult = FloatArray(1)
                    Location.distanceBetween(
                        currentParentLocation.latitude,
                        currentParentLocation.longitude,
                        childModel.coordinate.latitude!!,
                        childModel.coordinate.longitude!!,
                        distanceResult
                    )
                    val distanceKm = distanceResult[0].toKm()
                    tvChildDistance.text =
                        itemView.context.getString(
                            R.string.distance_km,
                            distanceKm.formatFloatWithSeparator()
                        )
                    if (distanceKm < 10f) {
                        tvChildDistance.setTextColor(itemView.context.getColor(R.color.yellow_E9A100))
                    }
                    if (distanceKm < 5f) {
                        tvChildDistance.setTextColor(itemView.context.getColor(R.color.green_29823B))
                    }
                } else {
                    tvChildDistance.text = itemView.context.getString(R.string.distance_unknown)
                }
            }
        }
    }

    interface OnItemClickCallback {
        fun onSeeChildClicked(childModel: ChildModel)
        fun onDeleteChildClicked(childModel: ChildModel)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ChildModel>() {
            override fun areItemsTheSame(oldItem: ChildModel, newItem: ChildModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ChildModel, newItem: ChildModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}