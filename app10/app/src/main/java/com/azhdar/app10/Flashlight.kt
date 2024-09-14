package com.azhdar.app10

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.util.TimeUtils
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import kotlin.coroutines.CoroutineContext

class Flashlight {
   fun toggleFlash1(state:Boolean,context:Context){


        val cameraManager:CameraManager=context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val camId=cameraManager.cameraIdList[0]

       if(state){flashOn(cameraManager,camId,context)}
       else{flashOff(cameraManager,camId,context)}








    }
    fun toggleFlasher1(viewModel: myViewModel, context: Context){
        val cameraManager:CameraManager=context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val camId=cameraManager.cameraIdList[0]

        CoroutineScope(Dispatchers.IO).launch {
            while(viewModel.flasherState.value){

                flashOn(cameraManager,camId,context)
                delay(1000)
                flashOff(cameraManager,camId,context)
                delay(1000)

            }
        }





    }


    fun flashOn(cameraManager:CameraManager,camId:String,context: Context){
        try {
            cameraManager.setTorchMode(camId, true)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            Log.e("camera","cameraeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")
        }
    }

    fun flashOff(cameraManager:CameraManager,camId:String,context: Context){
        try {
            cameraManager.setTorchMode(camId, false)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            Log.e("camera","cameraeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")
        }
    }
}