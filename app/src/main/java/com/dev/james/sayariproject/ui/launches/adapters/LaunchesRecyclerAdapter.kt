package com.dev.james.sayariproject.ui.launches.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
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
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.O)
class LaunchesRecyclerAdapter : PagingDataAdapter<LaunchList , LaunchesRecyclerAdapter.LaunchViewHolder>(DiffCallback()) {

    private lateinit var  context : Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LaunchViewHolder {
        val binding = SingleLauchItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return LaunchViewHolder(binding)
    }
    override fun onBindViewHolder(holder: LaunchViewHolder, position: Int) {
        val currentItem = getItem(position)
        context = holder.itemView.context
        if(holder.countDownTimer != null){
            holder.countDownTimer!!.cancel()
        }
        if (currentItem != null) {
            holder.binding(currentItem)
        }
    }

    inner class LaunchViewHolder(
        private val binding : SingleLauchItemBinding
    ) : RecyclerView.ViewHolder(binding.root){

        var countDownTimer: CountDownTimer? = null

        fun binding(launch : LaunchList){
            binding.apply {
                launchCardTitle.text = launch.name
                launchCardDesc.text = launch.serviceProvider?.name
                orbitTxt.text = launch.mission?.orbit?.abbrev
                dateTxt.text = getLaunchDateString(launch)
                val padName = launch.pad.name
                val location = launch.pad.location.name

                launchLocation.text = "$padName | $location "
            }
            setStatus(launch , binding)
            setUpImage(launch , binding)
            setUpCountDownTimer(launch , binding)
        }


        private fun setUpCountDownTimer(launch: LaunchList, binding: SingleLauchItemBinding) {
            val timeDiff = getLaunchDate(launch)

            countDownTimer = object : CountDownTimer(timeDiff , 1000){
                override fun onTick(millscUntilFinish: Long) {
                   binding.launchCountdownTimer.text = context.getString(R.string.updated_timer,
                   TimeUnit.MILLISECONDS.toDays(millscUntilFinish) , TimeUnit.MILLISECONDS.toHours(millscUntilFinish) %24 ,
                   TimeUnit.MILLISECONDS.toMinutes(millscUntilFinish)%60 ,TimeUnit.MILLISECONDS.toSeconds(millscUntilFinish)%60)
                }

                override fun onFinish() {
                    Log.d("LaunchesRv", "onFinish: timer has finished its work")

                }

            }

            (countDownTimer as CountDownTimer).start()

        }


        private fun getLaunchDate(launch: LaunchList): Long {
            val zonedDateTime = ZonedDateTime.parse(launch.date)
            val createdDateFormatted =
                zonedDateTime.withZoneSameInstant(ZoneId.of("Africa/Nairobi"))
            val launchDate = Date.from(
                createdDateFormatted.withZoneSameLocal(ZoneId.systemDefault()).toInstant()
            ).time
            val cDate = Calendar.getInstance().timeInMillis

            return launchDate - cDate

        }

        private fun getLaunchDateString(launch: LaunchList): String {
            val dateFormat = ZonedDateTime.parse(launch.date)

            val dateTimeFormatter: DateTimeFormatter =
                DateTimeFormatter.ofPattern("dd-M-yyyy", Locale.ROOT)


            val createdDateFormatted = dateFormat.withZoneSameInstant(ZoneId.of("Africa/Nairobi"))

            // val formattedDate1 = createdDateFormatted.format(DateTimeFormatter.ofPattern(API_TIME_STAMP_PATTERN))

            return createdDateFormatted.format(dateTimeFormatter)

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
        private fun setStatus(launch: LaunchList, binding: SingleLauchItemBinding) {
            binding.apply {
                launchStatus.text = launch.status?.name

                if(launch.status?.id == 2 ){
                    launchStatus.setTextColor(Color.BLUE)

                }
                if(launch.status?.id == 3 || launch.status?.id == 4  ){

                    launchStatus.setTextColor(Color.GREEN)

                }
                if(launch.status?.id == 4){
                    launchStatus.setTextColor(Color.RED)
                }
                if(launch.status?.id == 8 ){
                    launchStatus.setTextColor(Color.MAGENTA)
                }

                if(launch.status?.id == 1){
                    launchStatus.setTextColor(Color.CYAN)

                }
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