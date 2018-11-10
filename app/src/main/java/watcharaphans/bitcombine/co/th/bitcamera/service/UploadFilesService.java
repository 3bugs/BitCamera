package watcharaphans.bitcombine.co.th.bitcamera.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Messenger;
import android.util.Log;

import static watcharaphans.bitcombine.co.th.bitcamera.MainActivity.KEY_MESSENGER_INTENT;

public class UploadFilesService extends JobService {

    private static final String TAG = UploadFilesService.class.getName();

    private Messenger mActivityMessenger;

    /**
     * When the app's MainActivity is created, it starts this service. This is so that the
     * activity and this service can communicate back and forth. See "setUiCallback()"
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mActivityMessenger = intent.getParcelableExtra(KEY_MESSENGER_INTENT);
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.i(TAG, "Start upload files!!!");
        return false;

        // งานสั้นๆ ใช้เวลาไม่นาน ให้ return false;
        // งานใช้เวลานาน ต้องสร้าง worker thread แล้วทำงานนั้นใน worker thread, return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}
