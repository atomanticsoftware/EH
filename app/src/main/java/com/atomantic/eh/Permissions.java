package com.atomantic.eh;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;

public class Permissions {
    private static final String TAG = "Permissions";
    static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    static boolean checkPermissions(AppCompatActivity a) {
        int permissionState = ActivityCompat.checkSelfPermission(a,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    static void requestPermissions(AppCompatActivity a) {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(a,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.location_permission,
                    android.R.string.ok, view -> {
                        // Request permission
                        ActivityCompat.requestPermissions(a,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_PERMISSIONS_REQUEST_CODE);
                    }, a);
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(a,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    static void showSnackbar(final int mainTextStringId, final int actionStringId,
                             View.OnClickListener listener, AppCompatActivity a) {
        Snackbar.make(
                a.findViewById(android.R.id.content),
                a.getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(a.getString(actionStringId), listener).show();
    }


}
