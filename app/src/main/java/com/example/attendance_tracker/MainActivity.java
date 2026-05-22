package com.example.attendance_tracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    EditText username, password, inputCaptcha;
    Button btnUser, btnAdmin, btnSignIn, btnGoRegister, btnRefreshCaptcha;
    TextView tvForgotPassword, tvLoginTitle, tvCaptcha;

    String selectedRole = "user";
    String currentCaptcha = "";

    int userFailedAttempts = 0;
    int adminFailedAttempts = 0;
    final int MAX_ATTEMPTS = 5;

    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       
        FirebaseApp.initializeApp(this);
        databaseReference = FirebaseDatabase.getInstance("https://attendance-tracking-1f963-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");

        createDefaultAdmin(); 

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        inputCaptcha = findViewById(R.id.inputCaptcha);
        tvCaptcha = findViewById(R.id.tvCaptcha);
        btnRefreshCaptcha = findViewById(R.id.btnRefreshCaptcha);
        btnUser = findViewById(R.id.btnUser);
        btnAdmin = findViewById(R.id.btnAdmin);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnGoRegister = findViewById(R.id.btnGoRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvLoginTitle = findViewById(R.id.tvLoginTitle);

        generateCaptcha();

        btnSignIn.setOnClickListener(v -> handleSignIn());

        btnUser.setOnClickListener(v -> {
            selectedRole = "user";
            btnUser.setBackgroundColor(Color.parseColor("#1DB954"));
            btnUser.setTextColor(Color.parseColor("#000000"));
            btnAdmin.setBackgroundColor(Color.parseColor("#333333"));
            btnAdmin.setTextColor(Color.parseColor("#FFFFFF"));
            updateSignInButtonState();
        });

        btnAdmin.setOnClickListener(v -> {
            selectedRole = "admin";
            btnAdmin.setBackgroundColor(Color.parseColor("#1DB954"));
            btnAdmin.setTextColor(Color.parseColor("#000000"));
            btnUser.setBackgroundColor(Color.parseColor("#333333"));
            btnUser.setTextColor(Color.parseColor("#FFFFFF"));
            updateSignInButtonState();
        });

        btnRefreshCaptcha.setOnClickListener(v -> generateCaptcha());

        btnGoRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void createDefaultAdmin() {
        databaseReference.child("admin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    UserHelper admin = new UserHelper("System Administrator", "admin", "admin123", "admin");
                    databaseReference.child("admin").setValue(admin);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void generateCaptcha() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder captchaBuilder = new StringBuilder();
        Random rnd = new Random();
        while (captchaBuilder.length() < 5) {
            int index = (int) (rnd.nextFloat() * chars.length());
            captchaBuilder.append(chars.charAt(index));
        }
        currentCaptcha = captchaBuilder.toString();
        tvCaptcha.setText(currentCaptcha);
        inputCaptcha.setText("");
    }

    private void updateSignInButtonState() {
        if ((selectedRole.equals("admin") && adminFailedAttempts >= MAX_ATTEMPTS) ||
                (selectedRole.equals("user") && userFailedAttempts >= MAX_ATTEMPTS)) {
            btnSignIn.setEnabled(false);
            btnSignIn.setText("LOCKED");
            btnSignIn.setBackgroundColor(Color.parseColor("#555555"));
            btnSignIn.setTextColor(Color.parseColor("#CCCCCC"));
        } else {
            btnSignIn.setEnabled(true);
            btnSignIn.setText("SIGN IN");
            btnSignIn.setBackgroundColor(Color.parseColor("#1DB954"));
            btnSignIn.setTextColor(Color.parseColor("#000000"));
        }
    }

    private void handleSignIn() {
        String user = username.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String captchaVal = inputCaptcha.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty() || captchaVal.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!captchaVal.equals(currentCaptcha)) {
            Toast.makeText(this, "Invalid CAPTCHA", Toast.LENGTH_SHORT).show();
            generateCaptcha();
            return;
        }

        
        databaseReference.child(user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String dbPass = snapshot.child("password").getValue(String.class);
                    String dbRole = snapshot.child("role").getValue(String.class);

                    if (dbPass != null && dbPass.equals(pass)) {
                        if (selectedRole.equals("admin") && "admin".equals(dbRole)) {
                            adminFailedAttempts = 0;
                            Toast.makeText(MainActivity.this, "Admin Login Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                            startActivity(intent);
                            finish();
                        } else if (selectedRole.equals("user") && "user".equals(dbRole)) {
                            userFailedAttempts = 0;
                            Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, UserDashboardActivity.class);
                            intent.putExtra("username", user);
                            startActivity(intent);
                            finish();
                        } else {
                            handleFailedAttempt(selectedRole, "Role mismatch.");
                        }
                    } else {
                        handleFailedAttempt(selectedRole, "Invalid password.");
                    }
                } else {
                    handleFailedAttempt(selectedRole, "User not found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFailedAttempt(String role, String errorMsg) {
        if (role.equals("admin")) {
            adminFailedAttempts++;
            if (adminFailedAttempts >= MAX_ATTEMPTS) {
                updateSignInButtonState();
                Toast.makeText(this, "Admin account locked.", Toast.LENGTH_LONG).show();
            } else {
                int attemptsLeft = MAX_ATTEMPTS - adminFailedAttempts;
                Toast.makeText(this, errorMsg + " Attempts left: " + attemptsLeft, Toast.LENGTH_SHORT).show();
            }
        } else {
            userFailedAttempts++;
            if (userFailedAttempts >= MAX_ATTEMPTS) {
                updateSignInButtonState();
                Toast.makeText(this, "User account locked.", Toast.LENGTH_LONG).show();
            } else {
                int attemptsLeft = MAX_ATTEMPTS - userFailedAttempts;
                Toast.makeText(this, errorMsg + " Attempts left: " + attemptsLeft, Toast.LENGTH_SHORT).show();
            }
        }
        generateCaptcha();
    }
}
