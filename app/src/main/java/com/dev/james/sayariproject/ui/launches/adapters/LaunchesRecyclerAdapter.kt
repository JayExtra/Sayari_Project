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
import androidx.core.os.ConfigurationCompat
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
import com.dev.james.sayariproject.utilities.toDateString
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.O)
class LaunchesRecyclerAdapter(
    private val action : (LaunchList) -> Unit
) : PagingDataAdapter<LaunchList , LaunchesRecyclerAdapter.LaunchViewHolder>(DiffCallback()) {

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
                dateTxt.text = launch.startWindow.toDateString(context)
                val padName = launch.pad.name
                val location = launch.pad.location.name

                if(launch.stream){
                    watchStreamBtn.isVisible = true
                }

                launchLocation.text = "$padName | $location "
            }
            setStatus(launch , binding)
            setUpImage(launch , binding)
            setUpCountDownTimer(launch , binding)

            binding.root.setOnClickListener {
                action.invoke(launch)
            }
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
                    binding.launchCountdownTimer.text = "started!"
                    binding.launchCardTimerLabel.isVisible = false
                    binding.launchStatus.isVisible = false
                }

            }

            (countDownTimer as CountDownTimer).start()

        }


        private fun getLaunchDate(launch: LaunchList): Long {
            val zonedDateTime = ZonedDateTime.parse(launch.startWindow)
            val createdDateFormatted =
                zonedDateTime.withZoneSameInstant(ZoneId.systemDefault())
            val launchDate = Date.from(
                createdDateFormatted.withZoneSameLocal(ZoneId.systemDefault()).toInstant()
            ).time
            val cDate = Calendar.getInstance().timeInMillis

            return launchDate - cDate

        }

/*
        //will format the date from API into local time
        private fun String.formatDateFromApi(context : Context) : String {
            return try {
                val currentLocale = ConfigurationCompat.getLocales(context.resources.configuration)[0]
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("GMT")
                val passedDate: Date = inputFormat.parse(this) as Date

                //Here you put how you want your date to be, this looks like this Tue,Nov 2, 2021, 12:23 pm
                val outputFormatDay = SimpleDateFormat("dd-MM-yyyy HH:mm", currentLocale)
                outputFormatDay.timeZone = TimeZone.getDefault()
                val newDateString = outputFormatDay.format(passedDate)

                newDateString

            }catch (_ : Exception){
                "00:00:00"
            }
        }

        private fun String.toDate(dateFormat: String = "yyyy-MM-dd'T'HH:mm:ss'Z'", timeZone: TimeZone = TimeZone.getDefault()): Date? {
            Log.d("LaunchRecyclerAdapter", "timezone: $timeZone ")
            val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
            parser.timeZone = timeZone
            return parser.parse(this )
        }*/

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