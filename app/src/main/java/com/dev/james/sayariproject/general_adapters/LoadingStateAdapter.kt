package com.dev.james.sayariproject.general_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dev.james.sayariproject.databinding.LoadStateFooterBinding

class LoadingStateAdapter(private val retry: () -> Unit) :
LoadStateAdapter<LoadingStateAdapter.LoadStateViewHolder>(){

    class LoadStateViewHolder(val binding : LoadStateFooterBinding) :
            RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        val progress = holder.binding.lsProgressBar
        val txtErrorMessage = holder.binding.txtErrorMess
        val errorBtn = holder.binding.lsRetryButton

        progress.isVisible = loadState is LoadState.Loading
        txtErrorMessage.isVisible = loadState is LoadState.Error
        errorBtn.isVisible = loadState is LoadState.Error

        if(loadState is LoadState.Error){
            txtErrorMessage.text = loadState.error.localizedMessage
        }
        errorBtn.setOnClickListener {
            retry.invoke()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
       return LoadStateViewHolder(
           LoadStateFooterBinding.inflate(
               LayoutInflater.from(parent.context),parent ,false
           )
       )
    }
}