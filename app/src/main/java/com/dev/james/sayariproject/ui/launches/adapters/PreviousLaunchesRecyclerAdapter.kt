package com.dev.james.sayariproject.ui.launches.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isGone
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
import com.dev.james.sayariproject.models.launch.LaunchList
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class PreviousLaunchesRecyclerAdapter : PagingDataAdapter<LaunchList , PreviousLaunchesRecyclerAdapter.PreviousLaunchesViewHolder>(DiffUtilCallback()) {

    private lateinit var  context : Context


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviousLaunchesViewHolder {
        val binding = SingleLauchItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return PreviousLaunchesViewHolder(binding)
    }
    override fun onBindViewHolder(holder: PreviousLaunchesViewHolder, position: Int) {
        val currentItem = getItem(position)
        context = holder.itemView.context
        if (currentItem != null) {
            holder.binding(currentItem)
        }
    }

    inner class PreviousLaunchesViewHolder(
        private val binding : SingleLauchItemBinding
    ) : RecyclerView.ViewHolder(binding.root){

        @RequiresApi(Build.VERSION_CODES.O)
        fun binding(launch : LaunchList){

            binding.apply {
                launchCardTitle.text = launch.name
                launchCardDesc.text = launch.serviceProvider?.name
                orbitTxt.text = launch.mission?.orbit?.abbrev
                dateTxt.text = getLaunchDateString(launch)

                missionDesc.isVisible = true
                missionDesc.text = launch.mission?.description

                launchCountdownTimer.isInvisible = true
                launchCardRemindBtn.isInvisible = true
                watchStreamBtn.isInvisible = true
                launchStatus.isInvisible = true
                launchCardTimerLabel.isInvisible = true

                val padName = launch.pad.name
                val location = launch.pad.location.name

                launchLocation.text = "$padName | $location "
            }


            setStatus(launch , binding)
            setUpImage(launch , binding)
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

        @RequiresApi(Build.VERSION_CODES.O)
        private fun getLaunchDateString(launch: LaunchList): String {
            val dateFormat = ZonedDateTime.parse(launch.date)

            val dateTimeFormatter: DateTimeFormatter =
                DateTimeFormatter.ofPattern("dd-M-yyyy", Locale.ROOT)


            val createdDateFormatted = dateFormat.withZoneSameInstant(ZoneId.of("Africa/Nairobi"))

            // val formattedDate1 = createdDateFormatted.format(DateTimeFormatter.ofPattern(API_TIME_STAMP_PATTERN))

            return createdDateFormatted.format(dateTimeFormatter)

        }
        private fun setStatus(launch: LaunchList, binding: SingleLauchItemBinding) {
            binding.apply {
                launchStatus2.isVisible = true
                launchStatus2.text = launch.status?.name

                if(launch.status?.id == 2 ){
                    launchStatus2.setTextColor(Color.BLUE)

                }
                if(launch.status?.id == 3 || launch.status?.id == 4  ){

                    launchStatus2.setTextColor(Color.GREEN)

                }
                if(launch.status?.id == 4){
                    launchStatus2.setTextColor(Color.RED)
                }
                if(launch.status?.id == 8 ){
                    launchStatus2.setTextColor(Color.MAGENTA)
                }

                if(launch.status?.id == 1){
                    launchStatus2.setTextColor(Color.CYAN)

                }
            }

        }

    }

    class DiffUtilCallback : DiffUtil.ItemCallback<LaunchList>() {
        override fun areItemsTheSame(oldItem: LaunchList, newItem: LaunchList): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: LaunchList, newItem: LaunchList): Boolean =
            oldItem == newItem
    }


}