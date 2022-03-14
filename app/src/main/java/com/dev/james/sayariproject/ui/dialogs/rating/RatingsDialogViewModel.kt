package com.dev.james.sayariproject.ui.dialogs.rating

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RatingsDialogViewModel @Inject constructor() : ViewModel() {

    private var _ratingCount : MutableLiveData<Float> = MutableLiveData()
    val ratingCount get() = _ratingCount

    fun updateRatingEmoji(rating : Float) {
        _ratingCount.postValue(rating)
    }
}