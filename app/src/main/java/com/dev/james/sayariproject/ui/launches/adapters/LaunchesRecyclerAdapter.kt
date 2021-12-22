package com.dev.james.sayariproject.ui.launches.adapters

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.SingleLauchItemBinding
import com.dev.james.sayariproject.models.articles.Article
import com.dev.james.sayariproject.models.launch.LaunchList

class LaunchesRecyclerAdapter : PagingDataAdapter<LaunchList , LaunchesRecyclerAdapter.LaunchViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LaunchViewHolder {
        val binding = SingleLauchItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return LaunchViewHolder(binding)
    }
    override fun onBindViewHolder(holder: LaunchViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null) {
            holder.binding(currentItem)
        }
    }

    inner class LaunchViewHolder(
        private val binding : SingleLauchItemBinding
    ) : RecyclerView.ViewHolder(binding.root){

        fun binding(launch : LaunchList){
            binding.apply {
                launchCardTitle.text = launch.name
                launchCardDesc.text = launch.serviceProvider?.name
                launchStatus.text = launch.status?.description
            }
            setUpImage(launch , binding)
            setUpCountDownTimer(launch , binding)
        }

        private fun setUpCountDownTimer(launch: LaunchList, binding: SingleLauchItemBinding) {
            val launchDate = getLaunchDate(launch)
        }

        private fun getLaunchDate(launch: LaunchList): Any {
            TODO("Not yet implemented")
        }

        private fun setUpImage(launch: LaunchList, binding: SingleLauchItemBinding) {

            binding.apply {
                Glide.with(binding.root)
                    .load(launch.image)
                    .centerCrop()
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_broken_image)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            imageProgressBar.isInvisible = true
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            imageProgressBar.isVisible = false
                            return false
                        }
                    })
                    .into(launchImage)
            }

        }

        }


    class DiffCallback : DiffUtil.ItemCallback<LaunchList>(){
        override fun areItemsTheSame(oldItem: LaunchList, newItem: LaunchList): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: LaunchList, newItem: LaunchList): Boolean =
            oldItem == newItem

    }

}