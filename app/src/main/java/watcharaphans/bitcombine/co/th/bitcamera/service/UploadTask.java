package watcharaphans.bitcombine.co.th.bitcamera.service;

import android.os.AsyncTask;

public class UploadTask extends AsyncTask<String, Void, Boolean> {

    private String filePath;
    private UploadTaskCallback callback;

    public UploadTask(String filePath, UploadTaskCallback callback) {
        this.filePath = filePath;
        this.callback = callback;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        //todo:
        // คำสั่ง upload ไฟล์
        boolean success = true; //todo:
        return success;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            callback.onUploadSuccess();
        } else {
            callback.onUploadFailed();
        }
    }

    public interface UploadTaskCallback {
        public void onUploadSuccess();
        public void onUploadFailed();
    }
}