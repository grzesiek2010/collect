package org.odk.collect.android.activities;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.views.CameraPreview;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;

public class CaptureSelfieActivity extends Activity {
    private Camera mCamera;
    private CameraPreview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager
                .LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_capture_selfie);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

        try {
            mCamera = getCameraInstance();
        } catch (Exception e) {
            Timber.e(e);
        }

        mPreview = new CameraPreview(this, mCamera);
        preview.addView(mPreview);

        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, mPicture);
            }
        });

        ToastUtils.showLongToast(R.string.take_selfie);
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            savePhoto(data);
            setResult(RESULT_OK);
            finish();
        }
    };

    private void savePhoto(byte[] data) {
        File tempFile = new File(Collect.TMPFILE_PATH);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(tempFile);
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    private Camera getCameraInstance() {
        Camera camera = null;
        for (int camNo = 0; camNo < Camera.getNumberOfCameras(); camNo++) {
            Camera.CameraInfo camInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(camNo, camInfo);

            if (camInfo.facing == (Camera.CameraInfo.CAMERA_FACING_FRONT)) {
                camera = Camera.open(camNo);
                camera.setDisplayOrientation(90);
            }
        }
        return camera;
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mCamera == null) {
            setContentView(R.layout.activity_capture_selfie);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

            try {
                mCamera = getCameraInstance();
            } catch (Exception e) {
                Timber.e(e);
            }

            mPreview = new CameraPreview(this, mCamera);
            preview.addView(mPreview);
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
}