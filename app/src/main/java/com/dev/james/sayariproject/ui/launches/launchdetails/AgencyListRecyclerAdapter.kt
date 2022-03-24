package com.dev.james.sayariproject.ui.launches.launchdetails

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.SingleCrewMemberCardBinding
import com.dev.james.sayariproject.models.iss.Agency

class AgencyListRecyclerAdapter : ListAdapter<Agency , AgencyListRecyclerAdapter.AgencyListViewHolder>(DiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgencyListViewHolder {
        val binding = SingleCrewMemberCardBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return AgencyListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AgencyListViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }


    inner class AgencyListViewHolder(
        private val binding: SingleCrewMemberCardBinding
    ) : RecyclerView.ViewHolder(binding.root){

        fun bind(agency : Agency){
            //load image
            loadOwnerImage(agency , binding)
            //set owner name
            binding.astroNameTxt.text = agency.name
            //set country code
            binding.astroAgency.text = agency.type
        }

        private fun loadOwnerImage(agency: Agency, binding: SingleCrewMemberCardBinding) {
            Glide.with(binding.root)
                .load(R.drawable.iss_patch)
                .centerCrop()
                .error(R.drawable.iss_patch)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.astroImgProgress.isInvisible = true
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.astroImgProgress.isInvisible = true
                        return false
                    }
                })
                .into(binding.astroImg)
        }

    }

    class DiffCallBack : DiffUtil.ItemCallback<Agency>(){
        override fun areItemsTheSame(oldItem: Agency, newItem: Agency) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Agency, newItem: Agency) =
            oldItem == newItem
    }
}