package watcharaphans.bitcombine.co.th.bitcamera.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import watcharaphans.bitcombine.co.th.bitcamera.R;

public class ScanQrCodeFragment extends Fragment implements ZXingScannerView.ResultHandler{

    private ZXingScannerView zXingScannerView;
    private String resultString;
    private String tag = "17AugV1";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        zXingScannerView = new ZXingScannerView(getActivity());
        return zXingScannerView;
    }

    @Override
    public void onResume() {
        super.onResume();
        zXingScannerView.setResultHandler(this);
        zXingScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        zXingScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result) {
        resultString = result.getText().trim();
        Log.d(tag, "Result 200ms ---> " + resultString);
        if (!resultString.isEmpty()) {
//            Replace Fragment
            getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contentFragmentMain, MainFragment.takePhotoInstance(resultString))
                    .addToBackStack(null)
                    .commit();
        }

        /*Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                zXingScannerView.resumeCameraPreview(ScanQrCodeFragment.this);
            }
        }, 2000);*/

    }
} //Main Class
