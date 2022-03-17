package com.dev.james.sayariproject.ui.iss.adapters

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.SingleDockedVehicleCardBinding
import com.dev.james.sayariproject.models.iss.DockingLocation
import com.dev.james.sayariproject.models.iss.FlightVehicle
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class DockedVehiclesAdapter(
    private val action : (FlightVehicle?) -> Unit
) : ListAdapter<DockingLocation , DockedVehiclesAdapter.DockedVehiclesViewHolder>(DiffUtilCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DockedVehiclesViewHolder {
        val binding = SingleDockedVehicleCardBinding.inflate(
            LayoutInflater.from(parent.context) ,
            parent ,
            false
        )
        return DockedVehiclesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DockedVehiclesViewHolder, position: Int) {
        val dockingLocation = getItem(position)
        if(dockingLocation!=null){
            holder.bind(dockingLocation)
        }
    }

    inner class DockedVehiclesViewHolder(
        private val binding : SingleDockedVehicleCardBinding
    ) : RecyclerView.ViewHolder(binding.root){
        fun bind(dockingLocation: DockingLocation){
            loadImage(dockingLocation , binding)
            val formattedDate = dockingLocation.docked?.dockedDate?.let { formatDate(it) }
            binding.apply {
                vehicleSerialNumber.text = "SN: ${dockingLocation.docked?.flightVehicle?.spacecraft?.serialNumber}"
                portDockedTxt.text = "Port: ${dockingLocation.name}"
                vehicleName.text = dockingLocation.docked?.flightVehicle?.spacecraft?.name
                dockedDate.text = "Docked: $formattedDate"

                buttonReadMoreVehicle.setOnClickListener {
                    action.invoke(dockingLocation.docked?.flightVehicle)
                }
            }
        }

        private fun loadImage(
            dockingLocation: DockingLocation,
            binding: SingleDockedVehicleCardBinding
        ) {
            Glide.with(binding.root)
                .load(dockingLocation.docked?.flightVehicle?.spacecraft?.vehicleConfig?.imageUrl)
                .error(R.drawable.iss_patch)
                .centerCrop()
                .into(binding.vehicleImage)
        }

        private fun formatDate(dateString : String) : String {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                // only for OREO and newer versions
                val dateFormat = ZonedDateTime.parse(dateString)

                val API_TIME_STAMP_PATTERN = "dd-MM-yyyy hh:mm a"

                val dateTimeFormatter : DateTimeFormatter =
                    DateTimeFormatter.ofPattern(API_TIME_STAMP_PATTERN, Locale.ROOT)

                val createdDateFormatted = dateFormat.withZoneSameInstant(ZoneId.of("Africa/Nairobi"))

                val formattedDate2 = createdDateFormatted.format(dateTimeFormatter)

                Log.d("ArticlesRv", "setUpDate: $formattedDate2 ")
                
                return formattedDate2

            }else {
                val dateFormat : SimpleDateFormat =
                    SimpleDateFormat("dd-MM-yyyy'T'h:mm a")
                val eDate : Date = dateFormat.parse(dateString)

                return eDate.toString()
            }
        }
    }

    class DiffUtilCallBack : DiffUtil.ItemCallback<DockingLocation>(){
        override fun areItemsTheSame(oldItem: DockingLocation, newItem: DockingLocation) : Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: DockingLocation,
            newItem: DockingLocation
        ): Boolean =
            oldItem == newItem

    }
}