package com.example.telegramspam.ui.login

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.telegramspam.NO_INTERNET
import com.example.telegramspam.data.Repository
import com.example.telegramspam.models.Event
import com.example.telegramspam.ui.current_chat.CurrentChatViewModel
import com.example.telegramspam.utils.connected

class LoginViewModel(private val repository: Repository) : ViewModel() {
   val login = MutableLiveData("")
    val password = MutableLiveData("")

    val openAccountsFragment = MutableLiveData<Event<String>>()
    val toast = MutableLiveData<Event<String>>()

     fun requestRegistration(view: View){
         if(login.value!!.isNotBlank()){
             if(connected(view)){
                 repository.sendPasswordEmail(login.value!!)
                 toast.value = Event("Вы запросили регистрацию")
             }else{
                 toast.value = Event(NO_INTERNET)
             }
         } else{
             toast.value = Event("Введите логин")
         }


    }

    fun login(view:View){
        if(connected(view)){
            if(password.value!!.isNotBlank() && login.value!!.isNotBlank()){
                if(repository.checkRegisterData(login.value!!, password.value!!)){
                    openAccountsFragment.value = Event("")
                    toast.value = Event("Успешный вход")
                } else{
                    toast.value = Event("Неверный логин или пароль")
                }
            } else{
                toast.value = Event("Введите логин и пароль")
            }

        }else{
            toast.value = Event(NO_INTERNET)
        }
    }


}

@Suppress("UNCHECKED_CAST")
class LoginViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LoginViewModel(
            repository
        ) as T
    }

}