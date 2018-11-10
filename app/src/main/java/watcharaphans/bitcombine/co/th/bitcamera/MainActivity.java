package watcharaphans.bitcombine.co.th.bitcamera;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;

import watcharaphans.bitcombine.co.th.bitcamera.fragment.MainFragment;
import watcharaphans.bitcombine.co.th.bitcamera.fragment.ScanQrCodeFragment;
import watcharaphans.bitcombine.co.th.bitcamera.service.UploadFilesService;

public class MainActivity extends AppCompatActivity
        implements MainFragment.MainFragmentListener {

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    public static final String KEY_MESSENGER_INTENT = BuildConfig.APPLICATION_ID + ".KEY_MESSENGER_INTENT";

    public static final String FILENAME_C = "picture_c.jpg";
    public static final String FILENAME_D = "picture_d.jpg";

    int PERMISSION_ALL = 1;
    private String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private IncomingMessageHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!hasPermissions(this, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Permission is not granted
            /*ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION
            );*/
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else { // Permission granted.
            main();
        }

        ImageView settingsImageView = (ImageView) findViewById(R.id.imvSettings);
        settingsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        mHandler = new IncomingMessageHandler(this);
        scheduleJob(4);
    }  // Main Method

    @Override
    protected void onStart() {
        super.onStart();
        // Start service and provide it a way to communicate with this class.
        Intent intent = new Intent(this, UploadFilesService.class);
        intent.putExtra(KEY_MESSENGER_INTENT, new Messenger(mHandler));
        startService(intent);
    }

    @Override
    protected void onStop() {
        // A service can be "started" and/or "bound". In this case, it's "started" by this Activity
        // and "bound" to the JobScheduler (also called "Scheduled" by the JobScheduler). This call
        // to stopService() won't prevent scheduled jobs to be processed. However, failing
        // to call stopService() would keep it alive indefinitely.
        stopService(new Intent(this, UploadFilesService.class));
        super.onStop();
    }

    private void scheduleJob(int jobId) {
        final JobInfo.Builder builder = new JobInfo.Builder(
                jobId,
                new ComponentName(
                        getPackageName(),
                        UploadFilesService.class.getName()
                )
        );

        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setPersisted(true);
        builder.setPeriodic(60 * 1000);

        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler.schedule(builder.build()) <= 0) {
            Toast.makeText(this, "Job schedule ERROR! - id: " + jobId, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Job schedule OK - id: " + jobId, Toast.LENGTH_LONG).show();
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void main() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.contentFragmentMain, new ScanQrCodeFragment())
                .commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    main();
                } else {
                    // permission denied
                    String msg = "แอพไม่สามารถทำงานได้ เพราะไม่ได้รับอนุญาตให้เข้าถึงกล้อง";
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(msg)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            })
                            .show();
                }
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public void setToolbarVisibility(int visibility) {
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.toolbarMain);
        mainToolbar.setVisibility(visibility);
    }

    private static final int REQUEST_CODE_FILENAME_C = 1;
    private static final int REQUEST_CODE_FILENAME_D = 2;

    @Override
    public void onClickCameraImageC() {
        Intent intent;
        intent = new Intent(this, TouchActivity.class);
                /*Gson gson = new Gson();
                String qrCodeDataJson = gson.toJson(qrCodeData);
                intent.putExtra("qr_code_data", qrCodeDataJson);*/
        intent.putExtra("filename", FILENAME_C);
        startActivityForResult(intent, REQUEST_CODE_FILENAME_C);
    }

    @Override
    public void onClickCameraImageD() {
        Intent intent;
        intent = new Intent(this, TouchActivity.class);
                /*Gson gson = new Gson();
                String qrCodeDataJson = gson.toJson(qrCodeData);
                intent.putExtra("qr_code_data", qrCodeDataJson);*/
        intent.putExtra("filename", FILENAME_D);
        startActivityForResult(intent, REQUEST_CODE_FILENAME_D);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_FILENAME_C:
                //todo:
                if (resultCode == RESULT_OK) {
                    File imageFile = new File(getFilesDir(), FILENAME_C);
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

                    MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentByTag("main_fragment");
                    fragment.setImageViewC(bitmap);
                }
                break;
            case REQUEST_CODE_FILENAME_D:
                //todo:
                if (resultCode == RESULT_OK) {
                    File imageFile = new File(getFilesDir(), FILENAME_D);
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

                    MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentByTag("main_fragment");
                    fragment.setImageViewD(bitmap);
                }
                break;
        }
    }

    /**
     * A {@link Handler} allows you to send messages associated with a thread. A {@link Messenger}
     * uses this handler to communicate from {@link UploadFilesService}.
     */
    private static class IncomingMessageHandler extends Handler {

        // Prevent possible leaks with a weak reference.
        private WeakReference<MainActivity> mActivity;

        IncomingMessageHandler(MainActivity activity) {
            super(/* default looper */);
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = mActivity.get();
            if (mainActivity == null) {
                // Activity is no longer available, exit.
                return;
            }
            //final TextView testTextView = (TextView) mainActivity.findViewById(R.id.test_text_view);
            Message m;
        }
    }

}  // Main class
