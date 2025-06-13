package com.eles.driver;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;

public class Driver extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        Log.d("FirebaseInit", "FirebaseApp initialized in Driver class");
    }
}