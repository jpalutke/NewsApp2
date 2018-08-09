package com.crystaltowerdesigns.newsapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;

import java.util.Map;

import static android.support.v7.preference.Preference.OnPreferenceChangeListener;

/**
 * {@LINK SettingsActivity}
 * Our settings activity complete with toolbar for menu up action
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * {@LINK NewsAppPreferenceFragment}
     * Our preference fragment implementation
     */
    public static class NewsAppPreferenceFragment extends PreferenceFragment implements OnPreferenceChangeListener {

        /**
         * @param bundle Bundle passed when starting the fragment.
         *               Updates all the summaries to match the
         *               user chosen or default settings
         */
        @Override
        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            this.addPreferencesFromResource(R.xml.settings_main);
            // loop through the preferences and set
            // onChangeListeners and the preferences summaries
            final PreferenceScreen prefScreen = getPreferenceScreen();
            int prefCount = prefScreen.getPreferenceCount();
            for (int index = 0; index < prefCount; index++) {
                Preference preference;
                preference = prefScreen.getPreference(index);
                preference.setOnPreferenceChangeListener(this);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());

                for (Map.Entry<String, ?> key : sharedPreferences.getAll().entrySet()) {
                    Object result = key.getValue();
                    preference = findPreference(key.getKey());
                    if (result instanceof Boolean)
                        onPreferenceChange(preference, result);
                    else
                        onPreferenceChange(preference, sharedPreferences.getString(preference.getKey(), ""));
                }
            }
        }

        /**
         * Seemingly duplicitous method requirement for code to compile.
         * Might have something to do with App Compatibility.
         *
         * @param bundle Bundle
         * @param s      String
         */
        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
        }

        /**
         * {@LINK onPreferenceChange}
         * This method will update the user's display after a setting has been changed.
         *
         * @param preference The preference item we want to update on the user's display.
         * @param object     Object containing the info we want to display.
         *
         * @return boolean  True if value is accepted, false if invalid.
         * (Validation is up to the programmer)
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object object) {
            String stringValue = object.toString();
            preference.setSummary(stringValue);
            return true;
        }

    }
}

