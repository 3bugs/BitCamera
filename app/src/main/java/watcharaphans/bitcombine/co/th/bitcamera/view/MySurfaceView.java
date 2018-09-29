package watcharaphans.bitcombine.co.th.bitcamera.view;

import android.content.Context;
import android.hardware.Camera;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@SuppressWarnings("deprecation")
public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = MySurfaceView.class.getName();

    private static Context mContext;
    private static Camera mCamera;

    public String imageC;

    public MySurfaceView(Context context) {
        this(context, null);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings("deprecation")
    public MySurfaceView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
        mContext = context;

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera = Camera.open();
            setWillNotDraw(false);
            mCamera.setPreviewDisplay(holder);

            Camera.Parameters params = mCamera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(params);

            /*mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    Toast.makeText(mContext, "Focus!", Toast.LENGTH_SHORT).show();
                }
            });*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        for (Camera.Size size : sizes) {
            Log.i(TAG, "Width: " + size.width + ", Height: " + size.height);
        }
        Log.i(TAG, "--------------------");

        List<int[]> fpsRanges = params.getSupportedPreviewFpsRange();
        for (int[] range : fpsRanges) {
            Log.i(TAG, "FPS Min: " + range[0] + ", FPS Max: " + range[1]);
        }

        Camera.Size selected = sizes.get(0);
        Log.i(TAG, "Selected width: " + selected.width + ", Selected height: " + selected.height);
        params.setPreviewSize(selected.width, selected.height);
        //params.setPreviewFrameRate(20);
        mCamera.setParameters(params);
        setCameraDisplayOrientation();
        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
    }

    public void setCameraDisplayOrientation() {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(0, info);

        int rotation = ((WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getRotation();

        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(result);
    }

    private static class TakePictureCallback implements Camera.PictureCallback {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                File imageFile = new File(Environment.getExternalStorageDirectory(),
                        "picture.jpg");
                FileOutputStream fos = new FileOutputStream(imageFile);
                fos.write(data);
                fos.flush();
                fos.close();

                Toast.makeText(mContext, "บันทึกภาพแล้ว", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            camera.startPreview();
        }
    }

    public static void takePicture() {
        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        Camera.Size selected = sizes.get(0);
        params.setPictureSize(selected.width, selected.height);
        //params.setPictureSize(700, 500);
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        mCamera.setParameters(params);
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean b, Camera camera) {
                TakePictureCallback callback = new TakePictureCallback();
                mCamera.takePicture(null, null, null, callback);
            }
        });
    }
}