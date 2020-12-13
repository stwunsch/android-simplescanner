package com.example.simplescanner;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Preference.SummaryProvider summary = new Preference.SummaryProvider() {
                @Override
                public CharSequence provideSummary(Preference preference) {
                    EditTextPreference p = findPreference(preference.getKey());
                    String value = p.getText().trim();
                    String extra = "";
                    if (preference.getKey().equals("page_width") && value.equals("210")) {
                        extra = " (DIN A4)";
                    } else if (preference.getKey().equals("page_height") && value.equals("297")) {
                        extra = " (DIN A4)";
                    }
                    return value + " mm" + extra;
                }
            };
            for(String key : new String[]{"page_height", "page_width"}) {
                EditTextPreference p = (EditTextPreference) getPreferenceManager().findPreference(key);
                p.setSummaryProvider(summary);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}