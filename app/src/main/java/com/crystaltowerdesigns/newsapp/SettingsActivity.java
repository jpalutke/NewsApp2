package com.crystaltowerdesigns.newsapp;

import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import static android.support.v7.preference.Preference.OnPreferenceChangeListener;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public static class NewsAppPreferenceFragment extends PreferenceFragment implements OnPreferenceChangeListener {

        @Override
        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            this.addPreferencesFromResource(R.xml.settings_main);

            // log the current preference keys/values for debugging
            final PreferenceScreen prefScreen = getPreferenceScreen();
            int prefCount = prefScreen.getPreferenceCount();
            Log.v("SETTINGS Pref values", "These are the preferences and listeners within the SETTINGS ACTIVITY once it is launched.");

            // TODO: Need to set a preference ALTERED = true
            // for MainActivity to find on onCreate to force a reload on it's loader

            for (int i = 0; i < prefCount; i++) {
                Preference pref = prefScreen.getPreference(i);
                pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        String stringValue = o.toString();
                        //
                        // Validation code below, return false if invalid, true if valid
                        //
                        switch (preference.getKey()) {
                            case "start_date_time": {
                                preference.setSummary(stringValue);
                                break;
                            }
                            case "end_date_time": {
                                preference.setSummary(stringValue);
                                break;
                            }
                            case "settings_search_type_key": {
                                Toast.makeText(preference.getContext(), "Search Type Changed", Toast.LENGTH_SHORT).show();
                                break;
                            }
                            default: {
                                preference.setSummary(stringValue);
                                break;
                            }
                        }
                        return true;
                    }
                });
                Log.v("SETTINGS_ACTIVITY", "Listener added for: " + pref.getKey() + ", value='" + pref.toString() + "'");
            }

        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            return false;
        }
    }
}

