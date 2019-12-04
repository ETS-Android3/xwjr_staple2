package com.xwjr.staple.camera;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.xwjr.staple.R;
import com.xwjr.staple.constant.StapleConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class TakePhotoActivity extends AppCompatActivity implements CameraPreview.OnCameraStatusListener,
        SensorEventListener {
    public static final String PATH = StapleConfig.INSTANCE.getImgFilePath();
    CameraPreview mCameraPreview;
    ImageView ivIndicator;
    TextView tvHint;
    TextView tvTakePhoto;
    TextView tvCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.staple_activity_take_photo);
        // Initialize components of the app
        mCameraPreview = findViewById(R.id.cameraPreview);
        FocusView focusView = findViewById(R.id.view_focus);
        ivIndicator = findViewById(R.id.iv_indicator);
        tvHint = findViewById(R.id.tv_hint);
        tvTakePhoto = findViewById(R.id.tv_take_photo);
        tvCancel = findViewById(R.id.tv_cancel_take_photo);


        switch (getIntent().getStringExtra("source")) {
            case "wwxjk":
                if (getIntent().getStringExtra("side").equals("front")) {
                    ivIndicator.setImageDrawable(getResources().getDrawable(R.mipmap.staple_idcard_front_wwxjk));
                } else {
                    ivIndicator.setImageDrawable(getResources().getDrawable(R.mipmap.staple_idcard_back_wwxjk));
                }
                tvHint.setTextColor(Color.parseColor("#f0c41b"));
                tvTakePhoto.setBackgroundResource(R.drawable.staple_shape_solid_wwxjk);
                tvCancel.setBackgroundResource(R.drawable.staple_shape_border_wwxjk);
                tvCancel.setTextColor(Color.parseColor("#f0c41b"));
                break;
            case "xssq":
                if (getIntent().getStringExtra("side").equals("front")) {
                    ivIndicator.setImageDrawable(getResources().getDrawable(R.mipmap.staple_idcard_front_xssq));
                } else {
                    ivIndicator.setImageDrawable(getResources().getDrawable(R.mipmap.staple_idcard_back_xssq));
                }
                tvHint.setTextColor(Color.parseColor("#ff790d"));
                tvTakePhoto.setBackgroundResource(R.drawable.staple_shape_solid_xssq);
                tvCancel.setBackgroundResource(R.drawable.staple_shape_border_xssq);
                tvCancel.setTextColor(Color.parseColor("#ff790d"));
                break;
            case "wwnt":
                if (getIntent().getStringExtra("side").equals("front")) {
                    ivIndicator.setImageDrawable(getResources().getDrawable(R.mipmap.staple_idcard_front_wwnt));
                } else {
                    ivIndicator.setImageDrawable(getResources().getDrawable(R.mipmap.staple_idcard_back_wwnt));
                }
                tvHint.setTextColor(Color.parseColor("#21cdb7"));
                tvTakePhoto.setBackgroundResource(R.drawable.staple_shape_solid_wwnt);
                tvCancel.setBackgroundResource(R.drawable.staple_shape_border_wwnt);
                tvCancel.setTextColor(Color.parseColor("#21cdb7"));
                break;
        }


        mCameraPreview.setFocusView(focusView);
        mCameraPreview.setOnCameraStatusListener(this);

        mSensorManager = (SensorManager) getSystemService(Context.
                SENSOR_SERVICE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.
                TYPE_ACCELEROMETER);


        tvTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto(v);
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void takePhoto(View view) {
        if (mCameraPreview != null) {
            mCameraPreview.takePicture();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onCameraStopped(byte[] data) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        long dateTaken = System.currentTimeMillis();
        String filename = dateTaken + ".png";
        File dir = new File(PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        insertImage(PATH + "/" + filename, bitmap, data);
    }

    private void insertImage(String filename, Bitmap source, byte[] jpegData) {
        OutputStream outputStream = null;
        try {
            File file = new File(filename);
            if (file.createNewFile()) {
                outputStream = new FileOutputStream(file);
                if (source != null) {
                    source.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                } else {
                    outputStream.write(jpegData);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }

        Intent intent = new Intent();
        intent.putExtra("filePath", filename);
        intent.putExtra("side", getIntent().getStringExtra("side"));
        setResult(RESULT_OK, intent);
        finish();
//        ContentValues values = new ContentValues(7);
//        values.put(MediaStore.Images.Media.TITLE, name);
//        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
//        values.put(MediaStore.Images.Media.DATE_TAKEN, dateTaken);
//        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
//        values.put(MediaStore.Images.Media.DATA, filePath);
//        return cr.insert(IMAGE_URI, values);
    }


    private float mLastX = 0;
    private float mLastY = 0;
    private float mLastZ = 0;
    private boolean mInitialized = false;
    private SensorManager mSensorManager;
    private Sensor mAccel;

    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        if (!mInitialized) {
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            mInitialized = true;
        }
        float deltaX = Math.abs(mLastX - x);
        float deltaY = Math.abs(mLastY - y);
        float deltaZ = Math.abs(mLastZ - z);

        if (deltaX > 0.8 || deltaY > 0.8 || deltaZ > 0.8) {
            mCameraPreview.setFocus();
        }
        mLastX = x;
        mLastY = y;
        mLastZ = z;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
