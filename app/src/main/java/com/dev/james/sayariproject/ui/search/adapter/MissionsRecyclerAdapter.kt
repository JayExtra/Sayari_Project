package com.dev.james.sayariproject.ui.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.SingleMissionCardBinding
import com.dev.james.sayariproject.models.discover.ActiveMissions
import de.hdodenhof.circleimageview.CircleImageView

class MissionsRecyclerAdapter : ListAdapter<ActiveMissions , MissionsRecyclerAdapter.MissionsViewHolder>(DiffCallback())  {

    inner class MissionsViewHolder(
        private val binding : SingleMissionCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(mission: ActiveMissions, patch: CircleImageView){
            binding.apply {
                missionName.text = mission.title
            }
            loadImage(binding , mission , patch)
        }

        private fun loadImage(
            binding: SingleMissionCardBinding,
            mission: ActiveMissions,
            patch: CircleImageView
        ) {
            Glide.with(binding.root)
                .load(mission.missionPatch)
                .centerCrop()
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_broken_image)
                .into(patch)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ActiveMissions>(){
        override fun areItemsTheSame(oldItem: ActiveMissions, newItem: ActiveMissions) : Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ActiveMissions, newItem: ActiveMissions): Boolean =
            oldItem == newItem

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionsViewHolder {
        val binding = SingleMissionCardBinding.inflate(LayoutInflater.from(parent.context) ,parent ,  false)
        return MissionsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MissionsViewHolder, position: Int) {
        val missionItem = getItem(position)
        val patch = holder.itemView.findViewById<CircleImageView>(R.id.missionPatch)
        if(missionItem!=null){
            holder.bind(missionItem , patch)
        }

    }
}