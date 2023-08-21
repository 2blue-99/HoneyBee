package com.example.receiptcareapp.viewModel.activityViewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.data.manager.PreferenceManager
import com.example.domain.model.receive.DomainServerResponse
import com.example.domain.model.receive.ServerResponseData
import com.example.domain.usecase.login.LoginUseCase
import com.example.receiptcareapp.State.ConnectedState
import com.example.receiptcareapp.base.BaseViewModel
import com.example.receiptcareapp.dto.LoginData
import com.example.receiptcareapp.util.ResponseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.net.SocketTimeoutException
import javax.inject.Inject

/**
 * 2023-06-21
 * pureum
 */
@HiltViewModel
class LoginActivityViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val preferenceManager: PreferenceManager
): BaseViewModel() {

    //서버 연결 유무 관리
    val loading: LiveData<Boolean> get() = isLoading

    private var _response = MutableLiveData<ServerResponseData>()
    val response : LiveData<ServerResponseData> get() = _response

    lateinit var loginData: Pair<String,String>

    fun requestLogin(email:String, password:String){
        modelScope.launch {
            withTimeoutOrNull(waitTime){
                isLoading.postValue(true)
                    _response.postValue(loginUseCase.invoke(
                        email = email,
                        password = password
                    ))
                loginData = Pair(email,password)
                isLoading.postValue(false)
            } ?: SocketTimeoutException()
        }
    }

    //여기는 어차피 항상 성공할때만 들어오는것같음
    //이걸 지워야하나
//    private fun updateLoginState(response: DomainServerResponse, email: String, password: String){
//        when(response.status){
//            "200"->{
//                putLoginData(email, password)
//                _response.postValue(ResponseState.SUCCESS)
//            }
//            else -> _response.postValue(ResponseState.FALSE)
//        }
//    }

    fun putLoginData(data: LoginData){
        preferenceManager.putLogin(data.id!!)
        preferenceManager.putPassword(data.pw!!)
    }

    fun getLoginData(): LoginData = LoginData(preferenceManager.getLogin(), preferenceManager.getPassword())


    override fun onCleared() {
        super.onCleared()
        Log.e("TAG", "LoginActivityViewModel 삭제: ", )
    }

    init { Log.e("TAG", "LoginActivityViewModel 생성: ", ) }
}