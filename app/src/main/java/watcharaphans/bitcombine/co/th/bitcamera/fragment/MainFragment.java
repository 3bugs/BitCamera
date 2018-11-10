package watcharaphans.bitcombine.co.th.bitcamera.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import watcharaphans.bitcombine.co.th.bitcamera.MainActivity;
import watcharaphans.bitcombine.co.th.bitcamera.R;
import watcharaphans.bitcombine.co.th.bitcamera.model.QrCodeData;
import watcharaphans.bitcombine.co.th.bitcamera.service.UploadTask;
import watcharaphans.bitcombine.co.th.bitcamera.utility.MyConstant;

import static watcharaphans.bitcombine.co.th.bitcamera.MainActivity.FILENAME_C;
import static watcharaphans.bitcombine.co.th.bitcamera.MainActivity.FILENAME_D;

public class MainFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = MainFragment.class.getName();

    private String resultQRString;
    private ImageView cameraCImageView, cameraDImageView;
    private Uri cameraCUri, cameraDUri;
    private File[] cameraFile = new File[2];
    private File resizeCameraCFile;

    private boolean cameraCABoolean = false, cameraDABoolean = false;

    private String qrCode;
    private QrCodeData qrCodeData;

    //Uri =  path  mี่เก็บค่าต่างๆ
    public static MainFragment takePhotoInstance(String qrCode) {
        MainFragment mainFragment = new MainFragment();
        Bundle bundle = new Bundle();
        bundle.putString("Result", qrCode);
        mainFragment.setArguments(bundle);
        return mainFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.qrCode = bundle.getString("Result");
            this.qrCodeData = new QrCodeData(this.qrCode);
        }

        monitorDirectoryChange();
    }

    private FileObserver observer;

    private void monitorDirectoryChange() {
        final String pathToWatch = getPublicStorageDir().getAbsolutePath();
        observer = new FileObserver(pathToWatch) {
            @Override
            public void onEvent(int event, final String file) {
                //if(event == FileObserver.CREATE && !file.equals(".probe")){ // check if its a "create" and not equal to .probe because thats created every time camera is launched
                //}
                if (event == FileObserver.CREATE) {
                    String msg = "File created [" + pathToWatch + "/" + file + "]";
                    Log.d(TAG, msg);
                    //Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();

                    final String filePath = pathToWatch + "/" + file;
                    Log.i(TAG, "Before UploadTask execute.");
                    UploadTask uploadTask = new UploadTask(
                            filePath,
                            new UploadTask.UploadTaskCallback() {
                                @Override
                                public void onUploadSuccess() {
                                    Log.i(TAG, "onUploadSuccess");
                                    // ลบไฟล์ทิ้งไป
                                    File file = new File(filePath);
                                    if (file.delete()) {
                                        Log.i(TAG, "Delete file successfully");
                                    } else {
                                        Log.e(TAG, "Error deleting file");
                                    }
                                }

                                @Override
                                public void onUploadFailed() {
                                    // ไม่ต้องทำอะไร, รอเก็บตก
                                }
                            }
                    );
                    uploadTask.execute();
                }
            }
        };
        observer.startWatching(); //START OBSERVING
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).setToolbarVisibility(View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_take_photo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textView = (TextView) view.findViewById(R.id.txtResult);
        String styledText = "<font color='blue'>" + this.qrCodeData.id + "</font>" + ", " + this.qrCodeData.licenseCar;
        textView.setText(Html.fromHtml(styledText), TextView.BufferType.SPANNABLE);

        String DateIn = this.qrCodeData.dateString.substring(4, 6) + "/"
                + this.qrCodeData.dateString.substring(2, 4);
        String TimeIn = this.qrCodeData.timeString.substring(0, 2) + ":"
                + this.qrCodeData.timeString.substring(2, 4);
        String DateTimeIn = DateIn + " " + TimeIn;

        TextView textView2 = (TextView) view.findViewById(R.id.txtResult2);
        textView2.setText(DateTimeIn);

        cameraCImageView = (ImageView) view.findViewById(R.id.imvCameraC);
        cameraCImageView.setOnClickListener(this);
        cameraCImageView.setTag(false);

        cameraDImageView = (ImageView) view.findViewById(R.id.imvCameraD);
        cameraDImageView.setOnClickListener(this);
        cameraDImageView.setTag(false);

        Button cancelButton = (Button) view.findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(this);

        Button saveButton = (Button) view.findViewById(R.id.btnSave);
        saveButton.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Create Directory
        createFiles();
    }

    private void uploadPhotoToServer() {
        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy
                .Builder().permitAll().build();
        StrictMode.setThreadPolicy(threadPolicy);

        FTPClient ftpClient = new FTPClient();
        MyConstant myConstant = new MyConstant();
        String tag = "31AugV3";

//        For C
        if (cameraCABoolean) {
            try {
                ftpClient.connect(myConstant.getHostString(), myConstant.getPortAnInt());
                ftpClient.login(myConstant.getUserString(), myConstant.getPasswdString());
                ftpClient.setType(FTPClient.TYPE_BINARY);
                ftpClient.changeDirectory("AoTest");
                ftpClient.upload(cameraFile[0], new MyCheckUploadListener());
            } catch (Exception e) {
                Log.d(tag, "e upload ===> " + e.toString());
                try {
                } catch (Exception e1) {
                    Log.d(tag, "e1 upload ===> " + e1.toString());
                }
            }
        } // if
//        For D
    }// upload

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        Intent intent;
        switch (viewId) {
            case R.id.imvCameraC:
                mListener.onClickCameraImageC();
                break;
            case R.id.imvCameraD:
                mListener.onClickCameraImageD();
                break;
            case R.id.btnCancel:
                getActivity()
                        .getSupportFragmentManager()
                        .popBackStack();
                break;
            case R.id.btnSave:
                boolean imageCReady = (Boolean) cameraCImageView.getTag();
                boolean imageDReady = (Boolean) cameraDImageView.getTag();

                if (imageCReady && imageDReady) {
                    //uploadPhotoToServer();
                    File dir = getPublicStorageDir();

                    File srcFileC = new File(getContext().getFilesDir(), FILENAME_C);
                    File dstFileC = new File(dir, qrCodeData.fileNameC);
                    Log.i(TAG, "SRC C --> " + srcFileC.getAbsolutePath());
                    Log.i(TAG, "DST C --> " + dstFileC.getAbsolutePath());
                    //boolean moveFileCResult = srcFileC.renameTo(dstFileC);
                    try {
                        fileCopy(srcFileC, dstFileC);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    File srcFileD = new File(getContext().getFilesDir(), FILENAME_D);
                    File dstFileD = new File(dir, qrCodeData.fileNameD);
                    Log.i(TAG, "SRC D --> " + srcFileD.getAbsolutePath());
                    Log.i(TAG, "DST D --> " + dstFileD.getAbsolutePath());
                    //boolean moveFileDResult = srcFileD.renameTo(dstFileD);
                    try {
                        fileCopy(srcFileD, dstFileD);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    /*if (moveFileCResult && moveFileDResult) {
                        Toast.makeText(getContext(), "ย้ายรูปภาพเรียบร้อย", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "ย้ายรูปภาพเรียบร้อย");
                    } else {
                        Toast.makeText(getContext(), "ย้ายรูปภาพไม่สำเร็จ!!!", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "ย้ายรูปภาพไม่สำเร็จ!!!");
                    }*/
                } else {
                    Toast.makeText(getActivity(), "กรุณาถ่ายรูปให้ครบ 2 รูป", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void fileCopy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

    public File getPublicStorageDir() {
        // Get the directory for the user's public pictures directory.
        //Log.i(TAG, "DIR --> " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath());

        File appDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "BitCamera");
        File dir = new File(appDir, qrCodeData.dirName);
        if (dir.mkdirs()) {
            Log.i(TAG, "Directory " + dir.getAbsolutePath() + " created successfully");
        } else {
            Log.e(TAG, "Directory already exists or error creating directory: " + dir.getAbsolutePath());
        }

        return dir;
    }

    public class MyCheckUploadListener implements FTPDataTransferListener {
        @Override
        public void started() {
            Toast.makeText(getActivity(), "Start Upload ", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void transferred(int i) {
            Toast.makeText(getActivity(), "Transfer Upload ", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void completed() {
            Toast.makeText(getActivity(), "Completed Upload ", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void aborted() {
            Toast.makeText(getActivity(), "Aborted Upload ", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void failed() {
            Toast.makeText(getActivity(), "Failed Upload ", Toast.LENGTH_SHORT).show();

        }
    }  // MyCheck class

    private void createFiles() {
        String destinationPath = Environment.getExternalStorageDirectory() + "/" + this.qrCodeData.dirName;

        File cameraDir = new File(destinationPath);
        if (!cameraDir.exists()) {
            cameraDir.mkdir();
        }

        cameraFile[0] = new File(cameraDir, qrCodeData.fileNameC);
        cameraFile[1] = new File(cameraDir, qrCodeData.fileNameD);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }  //  onActivity Result

    public static void ResizeImages(String sPath, String sTo) throws IOException {
        Bitmap photo = BitmapFactory.decodeFile(sPath);
        photo = Bitmap.createScaledBitmap(photo, 300, 300, false);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        File f = new File(sTo);
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.close();

        File file = new File(sPath);
        file.delete();
    }

    public void setImageViewC(Bitmap bitmap) {
        cameraCImageView.setImageBitmap(bitmap);
        cameraCImageView.setTag(true);
    }

    public void setImageViewD(Bitmap bitmap) {
        cameraDImageView.setImageBitmap(bitmap);
        cameraDImageView.setTag(true);
    }

    private void showPhoto(int requestCode) {
        try {
            // ทดสอบภาษาไทย

            //* cameraCImageView.setImageBitmap(rowBitmap);
            switch (requestCode) {
                case 1:
                    Bitmap rowBitmap = BitmapFactory.decodeStream(getActivity().getContentResolver()
                            .openInputStream(cameraCUri));

                    // Command resize resolution file Image and View  640 x 480
                    Bitmap resizeCBitmap = Bitmap.createScaledBitmap(rowBitmap, 640, 480, false);

                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    resizeCBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

                    File f = new File(cameraCUri.getPath());
                    f.createNewFile();
                    FileOutputStream fo = new FileOutputStream(f);
                    fo.write(bytes.toByteArray());
                    fo.close();

                    cameraCImageView.setImageBitmap(resizeCBitmap);
                    break;
                case 2:
                    Bitmap rowBitmap1 = BitmapFactory.decodeStream(getActivity().getContentResolver()
                            .openInputStream(cameraDUri));

                    // Command resize and show
                    Bitmap resizeDBitmap = Bitmap.createScaledBitmap(rowBitmap1, 640, 480, false);

                    ByteArrayOutputStream bytes1 = new ByteArrayOutputStream();
                    resizeDBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes1);

                    File f1 = new File(cameraDUri.getPath());
                    f1.createNewFile();
                    FileOutputStream fo1 = new FileOutputStream(f1);
                    fo1.write(bytes1.toByteArray());
                    fo1.close();

                    cameraDImageView.setImageBitmap(resizeDBitmap);
                    break;
            }
        } catch (Exception e) {
            Log.d("31AugV1", "e showPhoto --> " + e.toString());
        }

    }// show Photo

    private MainFragmentListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (MainFragmentListener) context;
    }

    public interface MainFragmentListener {
        public void onClickCameraImageC();

        public void onClickCameraImageD();
    }
}
