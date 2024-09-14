package com.azhdar.app10;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class colorPicker extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_color_picker);

    }
}