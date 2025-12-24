package com.example.myprojcet;

import static android.content.ContentValues.TAG;

import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import android.content.Intent;
import android.credentials.GetCredentialRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.CustomCredential;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
public class RegisterActivity extends AppCompatActivity {
    EditText emailField,passwordField,passwordAgainFeild;
    String email, password,passwordAgain;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailField = findViewById(R.id.email_field);
        passwordField = findViewById(R.id.password_field);
        passwordAgainFeild = findViewById(R.id.password_again_field);


        Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener((v -> {
                email = emailField.getText().toString().trim();
                password = passwordField.getText().toString().trim();
                passwordAgain = passwordAgainFeild.getText().toString().trim();

                registerButton.setEnabled(false);

                // Handle registration logic here (e.g., store user data)
                // After successful registration, navigate to Login screen or Main Activity

                if (password.isEmpty() || passwordAgain.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "You have to fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
//              if (!email.contains("@")&&!email.contains(".com")) {
                if (!email.contains("@")) {
                    Toast.makeText(RegisterActivity.this, "Please enter a valid e-mail", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.length() <8) {
                    Toast.makeText(RegisterActivity.this, "Password should contain 8 characters at least", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(passwordAgain)) {
                    Toast.makeText(RegisterActivity.this, "Passwords must match", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Register with Firebase
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(RegisterActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                finish();

                                // Store email & password in Firestore also #####################
//                                if (user != null) {
//                                    Map<String, Object> userData = new HashMap<>();
//                                    userData.put("email", email);
//                                    userData.put("password",password);
//
                            } else {
                                registerButton.setEnabled(true);
                                Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            }));
    }
    public void navigateToLogin(View view) {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
