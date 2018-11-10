package watcharaphans.bitcombine.co.th.bitcamera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import watcharaphans.bitcombine.co.th.bitcamera.touch2focus.CameraPreview;
import watcharaphans.bitcombine.co.th.bitcamera.touch2focus.DrawingView;
import watcharaphans.bitcombine.co.th.bitcamera.touch2focus.PreviewSurfaceView;

@SuppressWarnings("deprecation")
public class TouchActivity extends AppCompatActivity
        implements CameraPreview.CameraPreviewCallback,
                   View.OnClickListener {

    private static final String TAG = TouchActivity.class.getName();

    private PreviewSurfaceView camView;
    private CameraPreview cameraPreview;
    private DrawingView drawingView;
    private Button flashButton;

    //private QrCodeData qrCodeData;
    //private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch);

        Intent intent = getIntent();
        String fileName = intent.getStringExtra("filename");

        /*String qrCodeDataJson = intent.getStringExtra("qr_code_data");
        Gson gson = new Gson();
        this.qrCodeData = gson.fromJson(qrCodeDataJson, QrCodeData.class);*/

        camView = (PreviewSurfaceView) findViewById(R.id.preview_surface);
        SurfaceHolder camHolder = camView.getHolder();

        cameraPreview = new CameraPreview(this, this, fileName);
        camHolder.addCallback(cameraPreview);
        camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        camView.setListener(cameraPreview);
        //cameraPreview.changeExposureComp(-currentAlphaAngle);
        drawingView = (DrawingView) findViewById(R.id.drawing_surface);
        camView.setDrawingView(drawingView);

        ImageView takePhotoImageView = (ImageView) findViewById(R.id.imvTakePhoto);
        takePhotoImageView.setOnClickListener(this);

        flashButton = (Button) findViewById(R.id.btnFlash);
        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(TouchActivity.this, flashButton);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.flash_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_flash_on:
                                setFlash("on");
                                break;
                            case R.id.action_flash_off:
                                setFlash("off");
                                break;
                            case R.id.action_flash_auto:
                                setFlash("auto");
                                break;
                        }
                        return true;
                    }
                });

                popup.show(); //showing popup menu
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String flashValue = prefs.getString("flash_mode", "auto");

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setFlash(flashValue);
            }
        }, 200);

        Log.i(TAG, "Flash value --> " + flashValue);
    }

    private void setFlash(String mode) {
        switch (mode) {
            case "on":
                cameraPreview.setFlashMode(CameraPreview.FLASH_MODE_ON);
                flashButton.setCompoundDrawablesWithIntrinsicBounds(
                        0, R.drawable.ic_flash_on_white, 0, 0);
                break;
            case "off":
                cameraPreview.setFlashMode(CameraPreview.FLASH_MODE_OFF);
                flashButton.setCompoundDrawablesWithIntrinsicBounds(
                        0, R.drawable.ic_flash_off_white, 0, 0);
                break;
            case "auto":
                cameraPreview.setFlashMode(CameraPreview.FLASH_MODE_AUTO);
                flashButton.setCompoundDrawablesWithIntrinsicBounds(
                        0, R.drawable.ic_flash_auto_white, 0, 0);
                break;
        }
    }

    @Override
    public void onCameraFocus(boolean isInitial) {
        if (isInitial) {
            camView.drawInitialFocusRect();
        } else {
            camView.drawFocusRect();
        }
    }

    @Override
    public void onPictureSaved() {
        //Intent intent = new Intent();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onHello() {
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.imvTakePhoto:
                cameraPreview.takePicture();
                break;
        }
    }
}
