package com.example.simplescanner;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle("About");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView textAbout = findViewById(R.id.textAbout);
        textAbout.setLineSpacing(0, 1.2f);
        String text = "";

        text += "<b>Simple Scanner</b> Android application<br/>";
        text += "<b>Website</b> github.com/stwunsch/android-simplescanner<br/>";
        text += "<b>License</b> GPL v3.0<br/>";

        text += "<br/><b>Dependencies</b><br/>";

        text += "<br/>";
        text += "<b>BoofCV</b> Image processing library<br/>";
        text += "<b>Website</b> boofcv.org<br/>";
        text += "<b>License</b> Apache v2.0<br/>";

        text += "<br/>";
        text += "<b>PDFBox Android</b> PDF processing library<br/>";
        text += "<b>Website</b> github.com/TomRoush/PdfBox-Android/<br/>";
        text += "<b>License</b> Apache v2.0<br/>";

        textAbout.setText(Html.fromHtml(text));

        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            String version = pInfo.versionName;

            TextView textVersion = findViewById(R.id.textVersion);
            textVersion.setText("Version " + version);
        } catch (Exception e) {
            Log.d(getClass().getSimpleName(), "Failed to get version name: " + e.getMessage());
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