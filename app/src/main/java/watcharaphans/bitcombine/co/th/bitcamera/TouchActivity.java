package watcharaphans.bitcombine.co.th.bitcamera;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;

import watcharaphans.bitcombine.co.th.bitcamera.touch2focus.CameraPreview;
import watcharaphans.bitcombine.co.th.bitcamera.touch2focus.DrawingView;
import watcharaphans.bitcombine.co.th.bitcamera.touch2focus.PreviewSurfaceView;

@SuppressWarnings("deprecation")
public class TouchActivity extends AppCompatActivity implements CameraPreview.CameraPreviewCallback {

    private PreviewSurfaceView camView;
    private CameraPreview cameraPreview;
    private DrawingView drawingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch);

        camView = findViewById(R.id.preview_surface);
        SurfaceHolder camHolder = camView.getHolder();

        cameraPreview = new CameraPreview(this, this);
        camHolder.addCallback(cameraPreview);
        camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        camView.setListener(cameraPreview);
        //cameraPreview.changeExposureComp(-currentAlphaAngle);
        drawingView = findViewById(R.id.drawing_surface);
        camView.setDrawingView(drawingView);
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
    public void onHello() {
    }
}
