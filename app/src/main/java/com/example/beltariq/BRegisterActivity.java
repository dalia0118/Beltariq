package com.example.beltariq;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BRegisterActivity extends AppCompatActivity {

    private static final String TAG = "BRegisterActivity";
    TextInputEditText editTextEmail, editTextPassword, editTextBrandName, editTextLocation, editTextPhoneNumber;
    Button buttonBReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), BMainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bregister);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextBrandName = findViewById(R.id.regbrand);
        editTextLocation = findViewById(R.id.reglocation);
        editTextPhoneNumber = findViewById(R.id.ph_nr);
        buttonBReg = findViewById(R.id.btn_bregister);
        progressBar = findViewById(R.id.progressbar);
        textView = findViewById(R.id.bloginsw);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BLoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        buttonBReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();
                String brandName = editTextBrandName.getText().toString();
                String location = editTextLocation.getText().toString();
                String phoneNumber = editTextPhoneNumber.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(BRegisterActivity.this, "Enter E-mail", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(BRegisterActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        String userId = user.getUid();
                                        Log.d(TAG, "User ID: " + userId);
                                        // Save additional user information to the database
                                        saveUserDetails(userId, email, brandName, location, phoneNumber);
                                    }

                                    Toast.makeText(BRegisterActivity.this, "Account created.",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), BMainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.e(TAG, "Authentication failed: " + task.getException().getMessage());
                                    Toast.makeText(BRegisterActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void saveUserDetails(String userId, String email, String brandName, String location, String phoneNumber) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://beltariqproject-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users").child(userId);
        User user = new User(userId, email, brandName, location, phoneNumber);
        databaseReference.setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User details saved successfully");
                        } else {
                            Log.e(TAG, "Failed to save user details: " + task.getException().getMessage());
                        }
                    }
                });
    }

    public static class User {
        public String userId;
        public String email;
        public String brandName;
        public String location;
        public String phoneNumber;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String userId, String email, String brandName, String location, String phoneNumber) {
            this.userId = userId;
            this.email = email;
            this.brandName = brandName;
            this.location = location;
            this.phoneNumber = phoneNumber;
        }
    }
}
