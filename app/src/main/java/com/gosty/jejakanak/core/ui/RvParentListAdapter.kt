package com.gosty.jejakanak.core.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gosty.jejakanak.R
import com.gosty.jejakanak.core.domain.models.ParentModel
import com.gosty.jejakanak.databinding.CardParentItemBinding

class RvParentListAdapter :
    ListAdapter<ParentModel, RvParentListAdapter.RvParentListViewHolder>(DIFF_CALLBACK) {
    private lateinit var onItemClickCallback: OnItemClickCallback

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RvParentListViewHolder {
        val binding = CardParentItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RvParentListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RvParentListViewHolder, position: Int) {
        val parent = getItem(position)
        holder.bind(parent)
        holder.binding.root.setOnClickListener {
            onItemClickCallback.onItemClicked(parent)
        }
    }

    class RvParentListViewHolder(val binding: CardParentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(parentModel: ParentModel) {
            Glide.with(itemView.context)
                .load(parentModel.photo)
                .placeholder(R.drawable.ic_image_black)
                .error(R.drawable.ic_broken_image_black)
                .centerCrop()
                .into(binding.ivParentAvatar)

            binding.apply {
                tvParentName.text = buildString {
                    append(parentModel.firstName)
                    append(" ")
                    append(parentModel.lastName)
                }
                tvParentEmail.text = parentModel.email
                tvParentPhone.text = parentModel.phone
            }
        }
    }

    interface OnItemClickCallback {
        fun onItemClicked(parentModel: ParentModel)
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ParentModel> =
            object : DiffUtil.ItemCallback<ParentModel>() {
                override fun areItemsTheSame(oldItem: ParentModel, newItem: ParentModel): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: ParentModel,
                    newItem: ParentModel
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }
}