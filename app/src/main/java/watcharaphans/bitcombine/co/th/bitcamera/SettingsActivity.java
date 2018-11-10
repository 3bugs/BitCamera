package watcharaphans.bitcombine.co.th.bitcamera;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import watcharaphans.bitcombine.co.th.bitcamera.fragment.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentFragmentMain, new SettingsFragment())
                .commit();
    }
}
