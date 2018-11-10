package watcharaphans.bitcombine.co.th.bitcamera.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import watcharaphans.bitcombine.co.th.bitcamera.R;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.preferences);
        onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences(), "flash_mode");

        Preference pref = findPreference("setting_camera");
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //Toast.makeText(getActivity(), "OK", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.contentFragmentMain, new PictureSettingsFragment())
                        .addToBackStack(null)
                        .commit();
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);

        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
        }
    }
}