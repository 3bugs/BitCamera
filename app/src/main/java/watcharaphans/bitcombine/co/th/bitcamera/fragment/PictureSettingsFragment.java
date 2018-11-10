package watcharaphans.bitcombine.co.th.bitcamera.fragment;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import watcharaphans.bitcombine.co.th.bitcamera.R;

public class PictureSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.picture_preferences);
    }
}
