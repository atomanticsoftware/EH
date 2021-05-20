package com.atomantic.eh;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MainActivity extends AppCompatActivity {

    private boolean activityResumed = false;

    private static final String TAG = "MainActivity";

    private com.atomantic.eh.Location location;

    private boolean focused = false;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        focused = hasFocus;
//        if (hasFocus)
//            hideSystemUI();
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }

    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityResumed = false;
        AppCompatActivity a = MainActivity.this;
        if (location != null)
            location.stopLocationUpdates(a);
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityResumed = true;

        if (findViewById(R.id.main) == null) {
            setContentView(R.layout.activity_main);
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            startLocationExchange();
        } else {

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == Permissions.REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                AppCompatActivity a = MainActivity.this;
                Permissions.showSnackbar(R.string.permission_denied_explanation,
                        R.string.ajustes, view -> {
                            // Build intent that displays the App settings screen.
                            Intent intent = new Intent();
                            intent.setAction(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package",
                                    BuildConfig.APPLICATION_ID, null);
                            intent.setData(uri);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }, a);
            }
        }
    }

    private long now() {
        return System.currentTimeMillis();
    }

    private static android.location.Location myLocation, startLocation = null;

    @SuppressLint("SetTextI18n")
    private void startLocationExchange() {
        AppCompatActivity a = MainActivity.this;
        Log.d(TAG, "location exchange started");
        TextView altitud_out = findViewById(R.id.altitud_out);
        TextView velocidad_out = findViewById(R.id.velocidad_out);
        TextView medida_out = findViewById(R.id.medida_out);
        Button empezar_a_medir = findViewById(R.id.empezar_a_medir);
        //empezar_a_medir.setOnClickListener(v -> startLocation = myLocation);
        empezar_a_medir.setOnClickListener(v -> {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        startLocation = myLocation;
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Empezar a medir desde este punto?").setPositiveButton("Si", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        });
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!activityResumed || !focused)
                    continue;
                if (Permissions.checkPermissions(a)) {
                    if (location == null)
                        location = new com.atomantic.eh.Location(a);
                    location.startLocationUpdates(a);
                } else {
                    Permissions.requestPermissions(a);
                }
                if (location == null)
                    continue;
                myLocation = location.getLocation();
                if (myLocation == null)
                    continue;
                runOnUiThread(() -> {
                    double altitud = round(myLocation.getAltitude(), 1);
                    altitud_out.setText(altitud + "");
                    double velocidad = round(myLocation.getSpeed(), 1);
                    velocidad *= 3600;
                    velocidad /= 1000;
                    velocidad = round(velocidad, 1);
                    if (velocidad > 80) {
                        velocidad_out.setTextColor(Color.RED);
                    } else {
                        velocidad_out.setTextColor(Color.WHITE);
                    }
                    velocidad_out.setText(velocidad + "");
                    if (startLocation != null) {
                        if (myLocation.hasAccuracy()) {
                            float accuracy = myLocation.getAccuracy();
                            //if (accuracy < 10f) {
                            double medida = round(myLocation.distanceTo(startLocation), 1);
//                            medida_out.setText(medida + "\nP:" + accuracy);
                            medida_out.setText("" + medida);
                            //}
                        }
                    }
                });
            }
        }).start();
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd;
        try {
            bd = BigDecimal.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}