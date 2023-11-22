package com.rushkar.pingzz.viewmodel

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rushkar.pingzz.api.RemoteCallback
import com.rushkar.pingzz.model.request_model.SendOTPRequestModel
import com.rushkar.pingzz.model.request_model.SendTokenRequestModel
import com.rushkar.pingzz.model.response_model.SendOtpResponseModel
import com.rushkar.pingzz.model.response_model.SendTokenResponseModel
import com.rushkar.pingzz.model.response_model.UserData
import com.rushkar.pingzz.repository.OrganizationUsersV1Repository

class OrganizationUsersV1ViewModel(private val repository: OrganizationUsersV1Repository) :
    ViewModel() {
    val otpData = MutableLiveData<UserData?>()
    val tokenData = MutableLiveData<SendTokenResponseModel?>()
    val errorMessageOtp = MutableLiveData<String>()
    val loadingStatus = MutableLiveData<Boolean>()
    val successStatus = MutableLiveData<Boolean>()
    val otpVerifyStatus = MutableLiveData<Boolean>()
    val otpSuccessStatus = MutableLiveData<Boolean>()
    val registerSuccessStatus = MutableLiveData<Boolean>()
    val OrganizationUsersSuccessStatus = MutableLiveData<Boolean>()

    fun sendOtpOnMobile(sendOTPRequestModel: SendOTPRequestModel) {
        repository.sendOtp(sendOTPRequestModel)
            .enqueue(object : RemoteCallback<SendOtpResponseModel>() {
                override fun onSuccess(response: SendOtpResponseModel?) {
                    successStatus.postValue(true)
                    otpData.postValue(response?.UserData)
                }

                override fun onFailed(throwable: Throwable) {
                    Log.d(ContentValues.TAG, "onFailed: " + throwable.message)
                    successStatus.postValue(false)
                    errorMessageOtp.postValue(throwable.message)
                }

                override fun onInternetFailed() {
                    successStatus.postValue(false)
                    errorMessageOtp.postValue("Internet Connection Failed")
                }

                override fun onComplete() {
                    loadingStatus.postValue(false)
                }
            })
    }

    fun sendTokenOnMobile(sendTokenRequestModel: SendTokenRequestModel) {
        repository.sendToken(sendTokenRequestModel)
            .enqueue(object : RemoteCallback<SendTokenResponseModel>() {
                override fun onSuccess(response: SendTokenResponseModel?) {
                    otpSuccessStatus.postValue(true)
                    tokenData.postValue(response)
                }

                override fun onFailed(throwable: Throwable) {
                    Log.d(ContentValues.TAG, "onFailed: " + throwable.message)
                    otpSuccessStatus.postValue(false)
                    errorMessageOtp.postValue(throwable.message)
                }

                override fun onInternetFailed() {
                    otpSuccessStatus.postValue(false)
                    errorMessageOtp.postValue("Internet Connection Failed")
                }

                override fun onComplete() {
                    loadingStatus.postValue(false)
                }
            })
    }

}


class UserViewModelFactory(private val repository: OrganizationUsersV1Repository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrganizationUsersV1ViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrganizationUsersV1ViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}