package com.foamproducer.measurematt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by sony on 8/22/2017.
 */

public class LaunchActivity extends AppCompatActivity
{
    private Button cameraButton;
    private Button galleryButton;
    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        context = this;
        cameraButton = (Button)findViewById(R.id.cameraBtn);
        cameraButton.setOnClickListener(cameraBtnListener);
        galleryButton = (Button)findViewById(R.id.galleryBtn);
        galleryButton.setOnClickListener(galleryBtnListener);
    }


    private View.OnClickListener cameraBtnListener = new View.OnClickListener()
    {


        @Override
        public void onClick(View v)
        {
            Intent intent = new Intent(context,MainActivity.class);
            intent.putExtra(MainActivity.LAUNCH_KEY,1002);
            startActivity(intent);

        }
    };


    private View.OnClickListener galleryBtnListener = new View.OnClickListener()
    {


        @Override
        public void onClick(View v)
        {
            Intent intent = new Intent(context,MainActivity.class);
            intent.putExtra(MainActivity.LAUNCH_KEY,1003);
            startActivity(intent);
        }
    };





}
