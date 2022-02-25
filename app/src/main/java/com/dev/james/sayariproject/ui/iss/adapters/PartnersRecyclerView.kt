package com.dev.james.sayariproject.ui.iss.adapters

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
import com.dev.james.sayariproject.models.iss.Owner

class PartnersRecyclerView : ListAdapter<Owner , PartnersRecyclerView.PartnerViewHolder>(DiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartnerViewHolder {
       val binding = SingleCrewMemberCardBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return PartnerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PartnerViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class PartnerViewHolder(
        private val binding : SingleCrewMemberCardBinding
    ) : RecyclerView.ViewHolder(binding.root){
        fun bind(owner : Owner){
            //load image
            loadOwnerImage(owner , binding)
            //set owner name
            binding.astroNameTxt.text = owner.name
            //set country code
            binding.astroAgency.text = owner.country_code
        }

        private fun loadOwnerImage(owner: Owner, binding: SingleCrewMemberCardBinding) {
            Glide.with(binding.root)
                .load(owner.image_url)
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

    class DiffCallBack : DiffUtil.ItemCallback<Owner>(){
        override fun areItemsTheSame(oldItem: Owner, newItem: Owner) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Owner, newItem: Owner) =
            oldItem == newItem
    }

}