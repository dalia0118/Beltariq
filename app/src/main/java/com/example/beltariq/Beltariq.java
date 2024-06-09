package com.example.beltariq;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class Beltariq extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);

        // Set the correct database URL
        FirebaseDatabase.getInstance("https://beltariqproject-default-rtdb.asia-southeast1.firebasedatabase.app").setPersistenceEnabled(true);
    }
}