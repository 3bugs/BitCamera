package watcharaphans.bitcombine.co.th.bitcamera.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import java.io.FileOutputStream;
import java.io.IOException;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import watcharaphans.bitcombine.co.th.bitcamera.R;
import watcharaphans.bitcombine.co.th.bitcamera.TakePhotoActivity;
import watcharaphans.bitcombine.co.th.bitcamera.model.QrCodeData;
import watcharaphans.bitcombine.co.th.bitcamera.utility.MyConstant;

public class MainFragment extends Fragment implements View.OnClickListener {

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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_take_photo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textView = view.findViewById(R.id.txtResult);
        String styledText = "<font color='blue'>" + this.qrCodeData.id + "</font>" + ", " + this.qrCodeData.licenseCar;
        textView.setText(Html.fromHtml(styledText), TextView.BufferType.SPANNABLE);

        String DateIn = this.qrCodeData.dateString.substring(4, 6) + "/"
                + this.qrCodeData.dateString.substring(2, 4);
        String TimeIn = this.qrCodeData.timeString.substring(0, 2) + ":"
                + this.qrCodeData.timeString.substring(2, 4);
        String DateTimeIn = DateIn + " " + TimeIn;

        TextView textView2 = view.findViewById(R.id.txtResult2);
        textView2.setText(DateTimeIn);

        cameraCImageView = view.findViewById(R.id.imvCameraC);
        cameraCImageView.setOnClickListener(this);

        cameraDImageView = view.findViewById(R.id.imvCameraD);
        cameraDImageView.setOnClickListener(this);

        Button cancelButton = view.findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(this);

        Button saveButton = view.findViewById(R.id.btnSave);
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
        switch (viewId) {
            case R.id.imvCameraC:
                //todo
                Intent intent = new Intent(getContext(), TakePhotoActivity.class);
                intent.putExtra("file_name_c", qrCodeData.fileNameC);
                startActivity(intent);
                break;
            case R.id.imvCameraD:
                //todo
                break;
            case R.id.btnCancel:
                getActivity()
                        .getSupportFragmentManager()
                        .popBackStack();
                break;
            case R.id.btnSave:
                if (cameraCABoolean || cameraDABoolean) {
                    uploadPhotoToServer();
                } else {
                    Toast.makeText(getActivity(), "Please Take Photo", Toast.LENGTH_SHORT).show();
                }
                break;
        }
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
}
