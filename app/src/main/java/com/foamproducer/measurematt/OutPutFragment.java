package com.foamproducer.measurematt;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by sony on 8/22/2017.
 */

public class OutPutFragment extends Fragment
{

    private Bitmap bitmap;
    private static OutPutFragment mInstance = null;
    public static OutPutFragment newInstance(Bitmap bitmap)
    {

            if(mInstance==null)
            {

                mInstance= new OutPutFragment();

            }
            return mInstance;


    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}
