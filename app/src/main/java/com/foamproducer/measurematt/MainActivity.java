package com.foamproducer.measurematt;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.nguyenhoanglam.imagepicker.activity.ImagePicker;
import com.nguyenhoanglam.imagepicker.activity.ImagePickerActivity;
import com.nguyenhoanglam.imagepicker.model.Image;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements IScanner,IEditor{


    private static final String CAMERA_FRAGMENT = "com.foamproduce.measurmatt.camerafragment";
    private static final String OUTPUT_FRAGMENT = "com.foamproduce.measurmatt.outputfragment";
    public static final String LAUNCH_KEY = "LaunchCamera";
    public static final int REQUEST_CODE =1001;
    public static final int CAMERA_REQUEST_CODE =1002;
    public static final int GALLERY_REQUEST_CODE =1003;
    private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
    private boolean hasCameraPermission;
    private ArrayList<Image> images = new ArrayList<>();
    private int REQUEST_CODE_PICKER=1004;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hasCameraPermission = permissionsDelegate.hasCameraPermission();


        int requestCode = getIntent().getIntExtra(LAUNCH_KEY,REQUEST_CODE);
        switch (requestCode)
        {
            case CAMERA_REQUEST_CODE:
                if (hasCameraPermission)
                {
                    launchFragment(CameraFragment.newInstance(this),CAMERA_FRAGMENT);
                    break;
                }
                else
                {
                    permissionsDelegate.requestCameraPermission();
                    break;
                }

            case GALLERY_REQUEST_CODE:   launchGallery();
                         break;

        }


    }

    private void launchGallery()
    {

        ImagePicker.create(this)
                .folderMode(true) // folder mode (false by default)
                .folderTitle("Folder") // folder selection title
                .imageTitle("Tap to select") // image selection title
                .single() // single mode
                .showCamera(true) // show camera or not (true by default)
                .imageDirectory("Camera") // directory name for captured image  ("Camera" folder by default)
                .origin(images) // original selected images, used in multi mode
                .start(REQUEST_CODE_PICKER); // start image picker activity with request code

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionsDelegate.resultGranted(requestCode, permissions, grantResults))
        {

            launchFragment(CameraFragment.newInstance(this),CAMERA_FRAGMENT);


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICKER && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(ImagePickerActivity.INTENT_EXTRA_SELECTED_IMAGES);
            // do your logic ....
        }
    }

    @Override
    public void onSaved(Bitmap bitmap) {

    }

    @Override
    public void onSelected(Bitmap bitmap)
    {
        launchFragment(OutPutFragment.newInstance(bitmap),OUTPUT_FRAGMENT);
    }

    public void launchFragment(Fragment fragment, String tag)
    {



        switch (tag)
        {

            case OUTPUT_FRAGMENT:getFragmentManager().beginTransaction().add(R.id.container,fragment,OUTPUT_FRAGMENT).commit();
                                  break;
            case CAMERA_FRAGMENT:getFragmentManager().beginTransaction().add(R.id.container,fragment,CAMERA_FRAGMENT).commit();
                                  break;


        }


    }


}
