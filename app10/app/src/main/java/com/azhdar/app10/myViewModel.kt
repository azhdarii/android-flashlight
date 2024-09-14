package com.azhdar.app10

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.jetbrains.annotations.NotNull

public class myViewModel:ViewModel() {

    var flashlightState = mutableStateOf(false)
    var flasherState = mutableStateOf(false)

    fun toggleFlashlight(){

        flashlightState.value=!flashlightState.value

    }

    fun toggleFlasher(){

        flasherState.value=!flasherState.value

    }


}
