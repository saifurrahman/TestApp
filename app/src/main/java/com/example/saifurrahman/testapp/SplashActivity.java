package com.example.saifurrahman.testapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

public class SplashActivity extends Activity {

    private boolean alreadyLogin;
    private GoogleCloudMessaging gcm;
    private SharedPreferences sharedPreferences;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        sharedPreferences = getSharedPreferences(
                Config.CRIMATRIX_GLOBAL_SHARED_PREF, MODE_PRIVATE);
        alreadyLogin = sharedPreferences
                .getBoolean(Config.ALREADY_LOGIN, false);

        Thread welcomeThread = new Thread() {
            int wait = 0;

            @Override
            public void run() {
                try {
                    super.run();


                    while (wait < Config.WELCOME_SCREEN_DISPLAY_TIME) {
                        sleep(100);
                        wait += 100;
                    }
                } catch (Exception e) {
                    Log.d(Config.CRIMATRIX_LOG,
                            "Exception in running splash screen thread");
                } finally {

                    if (isOnline()) {
                        registerGCMInBackground();
                    } else {
                        Log.d(Config.CRIMATRIX_LOG,
                                "Please connect to the internet");
                    }
                    Log.d(Config.CRIMATRIX_LOG, "First Time login");



                }
            }
        };
        welcomeThread.start();
    }

    private void registerGCMInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging
                                .getInstance(getApplicationContext());
                    }
                    String regId = gcm.register(Config.GOOGLE_PROJECT_ID);
                    Log.d(Config.CRIMATRIX_LOG, "GCM REG ID=: " + regId);
                    storeRegistrationId(regId);
                } catch (IOException ex) {
                    Log.d(Config.CRIMATRIX_LOG,
                            "GCM REG ID=: " + ex.getMessage());
                }

                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {

                Intent mainIntent = new Intent(getApplicationContext(),
                        MainActivity.class);
                startActivity(mainIntent);
                Log.d(Config.CRIMATRIX_LOG, "Already login");
                finish();
            }
        }.execute(null, null, null);
    }

    protected void storeRegistrationId(String regId) {
        Editor editor = sharedPreferences.edit();
        editor.putString(Config.GOOGLE_APP_REG_ID, regId);
        editor.commit();
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
}
