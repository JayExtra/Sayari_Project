package com.dev.james.sayariproject.ui.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailViewModel @Inject constructor() : ViewModel() {

    //create validation status sharedflow
    private val _validationStatusSharedFlow = MutableSharedFlow<ErrorInfo>()
    val validationSharedFlow = _validationStatusSharedFlow.asSharedFlow()


    fun validateAndSendEmail(subject : String, message: String) = viewModelScope.launch {
         if(subject.isNotEmpty() && message.isNotEmpty()){
             _validationStatusSharedFlow.emit(
                 ErrorInfo(
                     code = 0,
                     message = "Good" ,
                     subject = subject,
                     mail = message
                 )
             )
         }else if (subject.isEmpty()){
             _validationStatusSharedFlow.emit(
                 ErrorInfo(
                     code = 1,
                     message = "subject should not be empty.",
                     null,
                     null
                 )
             )
         }else if (message.isEmpty()){
             _validationStatusSharedFlow.emit(
                 ErrorInfo(
                     code = 2,
                     message = "message should not be empty.",
                     null,
                     null
                 )
             )
         }

    }




}
data class ErrorInfo(
    val code : Int ,
    val message : String,
    val subject : String?,
    val mail : String?
)