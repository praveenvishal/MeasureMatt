package com.foamproducer.measurematt;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.foamproducer.measurematt.widget.PolygonView;
import com.foamproducer.measurematt.widget.ProgressDialogFragment;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.Map;

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.FotoapparatSwitcher;
import io.fotoapparat.error.CameraErrorCallback;
import io.fotoapparat.hardware.CameraException;
import io.fotoapparat.parameter.LensPosition;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.photo.BitmapPhoto;
import io.fotoapparat.result.PendingResult;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.view.CameraView;
import static io.fotoapparat.log.Loggers.logcat;
import static io.fotoapparat.parameter.selector.AspectRatioSelectors.standardRatio;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.autoFocus;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.continuousFocus;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.fixed;
import static io.fotoapparat.parameter.selector.LensPositionSelectors.lensPosition;
import static io.fotoapparat.parameter.selector.Selectors.firstAvailable;
import static io.fotoapparat.parameter.selector.SizeSelectors.biggestSize;
import static io.fotoapparat.result.transformer.SizeTransformers.scaled;

/**
 * Created by sony on 8/22/2017.
 */

public class CameraFragment extends Fragment
{


    private CameraView cameraView;
    private FotoapparatSwitcher fotoapparatSwitcher;
    private Fotoapparat frontFotoapparat;
    private Fotoapparat backFotoapparat;
    private View switchCameraButton;
    private View captureButton;
    private PolygonView polygonView;
    private Bitmap original;
    private Bitmap sample;
    private ProgressDialogFragment progressDialogFragment;
    private static IScanner iScanner;



    private static CameraFragment mInstance = null;
    public static CameraFragment newInstance(IScanner iScann)
    {
        if(mInstance==null)
        {
            mInstance = new CameraFragment();
            iScanner = iScann;

        }
        return mInstance;

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera,container,false);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullscreen(getActivity());
        if (Build.VERSION.SDK_INT > 10) {
            registerSystemUiVisibility();
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cameraView = (CameraView)view.findViewById(R.id.camera_view);
        switchCameraButton = view.findViewById(R.id.switchCamera);
        captureButton =view.findViewById(R.id.capture);
        captureButton.setOnClickListener(captureListener);
        polygonView=(PolygonView)view.findViewById(R.id.polygonView);
        setupFotoapparat();
        takePictureOnClick(cameraView);
        focusOnLongClick(cameraView);
        setupSwitchCameraButton();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    private void setupFotoapparat() {
        frontFotoapparat = createFotoapparat(LensPosition.FRONT);
        backFotoapparat = createFotoapparat(LensPosition.BACK);
        fotoapparatSwitcher = FotoapparatSwitcher.withDefault(backFotoapparat);
    }

    private void setupSwitchCameraButton() {

        switchCameraButton.setVisibility(
                canSwitchCameras()
                        ? View.VISIBLE
                        : View.GONE
        );
        switchCameraOnClick(switchCameraButton);
    }

    private void switchCameraOnClick(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });
    }
    private void focusOnLongClick(View view) {
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                fotoapparatSwitcher.getCurrentFotoapparat().autoFocus();

                return true;
            }
        });
    }

    private View.OnClickListener captureListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            takePicture();

        }
    };

    private void takePictureOnClick(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
    }

    private boolean canSwitchCameras() {
        return frontFotoapparat.isAvailable() == backFotoapparat.isAvailable();
    }

    private Fotoapparat createFotoapparat(LensPosition position) {
        return Fotoapparat
                .with(getActivity())
                .into(cameraView)
                .previewScaleType(ScaleType.CENTER_CROP)
                .photoSize(standardRatio(biggestSize()))
                .lensPosition(lensPosition(position))
                .focusMode(firstAvailable(
                        continuousFocus(),
                        autoFocus(),
                        fixed()
                ))

                .cameraErrorCallback(new CameraErrorCallback() {
                    @Override
                    public void onError(CameraException e) {
                        Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
                    }
                })
                .build();
    }

    private void takePicture() {
        PhotoResult photoResult;
        photoResult = fotoapparatSwitcher.getCurrentFotoapparat().takePicture();
        photoResult
                .toBitmap(scaled(0.25f))
                .whenAvailable(new PendingResult.Callback<BitmapPhoto>() {
                    @Override
                    public void onResult(BitmapPhoto result) {


                        original = result.bitmap;
                        Mat mat = new Mat (original.getWidth(), original.getHeight(), CvType.CV_8UC1);


                        Utils.bitmapToMat(original,mat);

                        Map<Integer, PointF> points = polygonView.getPoints();
                        if (isScanPointsValid(points)) {
                            new ScanAsyncTask(points,mat).execute();
                        } else {
                            showErrorDialog();
                        }



                    }
                });
    }



    private Bitmap getScannedBitmap(Bitmap original, Map<Integer, PointF> points,Mat input) {
        int width = original.getWidth();
        int height = original.getHeight();
        //float xRatio = (float) original.getWidth() / sourceImageView.getWidth();
       // float yRatio = (float) original.getHeight() / sourceImageView.getHeight();

        float x1 = (points.get(0).x) ;
        float x2 = (points.get(1).x) ;
        float x3 = (points.get(2).x) ;
        float x4 = (points.get(3).x) ;
        float y1 = (points.get(0).y) ;
        float y2 = (points.get(1).y) ;
        float y3 = (points.get(2).y) ;
        float y4 = (points.get(3).y) ;
        Log.d("", "POints(" + x1 + "," + y1 + ")(" + x2 + "," + y2 + ")(" + x3 + "," + y3 + ")(" + x4 + "," + y4 + ")");
       // Bitmap _bitmap = ((ScanActivity) getActivity()).getScannedBitmap(original, x1, y1, x2, y2, x3, y3, x4, y4);
        input = drawPoints(x1,y1,x2,y2,x3,y3,x4,y4,input);
        Utils.matToBitmap(input,original);
        return original;
    }
    private class ScanAsyncTask extends AsyncTask<Void, Void, Bitmap> {

        private Map<Integer, PointF> points;
        private Mat input;

        public ScanAsyncTask(Map<Integer, PointF> points,Mat input) {
            this.points = points;
            this.input=input;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog("Processing....");
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            Mat mat = new Mat();
            Bitmap bitmap =  getScannedBitmap(original, points,input);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            iScanner.onSelected(bitmap);

        }
    }
    protected void showProgressDialog(String message) {
        progressDialogFragment = new ProgressDialogFragment(message);
        FragmentManager fm = getFragmentManager();
        progressDialogFragment.show(fm, ProgressDialogFragment.class.toString());
    }

    private void showErrorDialog() {
      Toast.makeText(getActivity(),"Error Occurred",Toast.LENGTH_LONG).show();
    }

    private boolean isScanPointsValid(Map<Integer, PointF> points) {
        return points.size() == 4;
    }
    private void switchCamera() {
        if (fotoapparatSwitcher.getCurrentFotoapparat() == frontFotoapparat) {
            fotoapparatSwitcher.switchTo(backFotoapparat);
        } else {
            fotoapparatSwitcher.switchTo(frontFotoapparat);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

            fotoapparatSwitcher.start();

    }

    @Override
    public void onStop() {
        super.onStop();

            fotoapparatSwitcher.stop();

    }
    public static boolean isImmersiveAvailable() {
        return android.os.Build.VERSION.SDK_INT >= 19;
    }




    public void setFullscreen(Activity activity) {
        if (Build.VERSION.SDK_INT > 10) {
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN;

            if (isImmersiveAvailable()) {
                flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }

            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
        } else {
            activity.getWindow()
                    .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public void exitFullscreen(Activity activity) {
        if (Build.VERSION.SDK_INT > 10) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        } else {
            activity.getWindow()
                    .setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
    }

    private Handler _handler = new Handler();

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void registerSystemUiVisibility() {
        final View decorView = getActivity().getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    setFullscreen(getActivity());
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void unregisterSystemUiVisibility() {
        final View decorView = getActivity().getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(null);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT > 10) {
            unregisterSystemUiVisibility();
        }
        exitFullscreen(getActivity());
    }

    public Mat drawPoints(double TopLeftX, double TopLeftY, double TopRightX,
                          double TopRightY, double BottomLeftX, double BottomLeftY, double BottomRightX, double BottomRightY, Mat input) {

        Point imageTopLeft = new Point(TopLeftX, TopLeftY);
        Point imageTopRight = new Point(TopRightX, TopRightY);
        Point imageBottomLeft = new Point(BottomLeftX, BottomLeftY);
        Point imageBottomRight = new Point(BottomRightX, BottomRightY);
        Mat output = new Mat();
        input.copyTo(output);
        Imgproc.drawMarker(output, imageTopLeft, new Scalar(0,0,255));
        Imgproc.drawMarker(output, imageTopRight, new Scalar(0,0,255));
        Imgproc.drawMarker(output, imageBottomLeft, new Scalar(0,0,255));
        Imgproc.drawMarker(output, imageBottomRight, new Scalar(0,0,255));
        return output;
    }




}
