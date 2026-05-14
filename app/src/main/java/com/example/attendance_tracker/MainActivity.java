package com.example.attendance_tracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    EditText username, password, inputCaptcha;
    Button btnUser, btnAdmin, btnSignIn, btnGoRegister, btnRefreshCaptcha;
    TextView tvForgotPassword, tvLoginTitle, tvCaptcha;

    String selectedRole = "user";
    String adminUsername = "admin";
    String adminPassword = "admin123";
    String defaultUserUsername = "user";
    String defaultUserPassword = "user123";
    String currentCaptcha = "";

    int userFailedAttempts = 0;
    int adminFailedAttempts = 0;
    final int MAX_ATTEMPTS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        inputCaptcha = findViewById(R.id.inputCaptcha);
        btnUser = findViewById(R.id.btnUser);
        btnAdmin = findViewById(R.id.btnAdmin);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnGoRegister = findViewById(R.id.btnGoRegister);
        btnRefreshCaptcha = findViewById(R.id.btnRefreshCaptcha);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvLoginTitle = findViewById(R.id.tvLoginTitle);
        tvCaptcha = findViewById(R.id.tvCaptcha);

        generateCaptcha();

        btnRefreshCaptcha.setOnClickListener(v -> generateCaptcha());

        btnUser.setOnClickListener(v -> {
            selectedRole = "user";
            tvLoginTitle.setText("IAAM Login");
            btnGoRegister.setVisibility(View.VISIBLE);
            tvForgotPassword.setVisibility(View.VISIBLE);
            btnUser.setBackgroundColor(Color.parseColor("#1DB954"));
            btnUser.setTextColor(Color.parseColor("#000000"));
            btnAdmin.setBackgroundColor(Color.parseColor("#333333"));
            btnAdmin.setTextColor(Color.parseColor("#FFFFFF"));

            username.setError(null);
            password.setError(null);
            inputCaptcha.setError(null);

            updateSignInButtonState();
        });

        btnAdmin.setOnClickListener(v -> {
            selectedRole = "admin";
            tvLoginTitle.setText("IAAM Admin Login");
            btnGoRegister.setVisibility(View.GONE);
            tvForgotPassword.setVisibility(View.GONE);
            btnAdmin.setBackgroundColor(Color.parseColor("#1DB954"));
            btnAdmin.setTextColor(Color.parseColor("#000000"));
            btnUser.setBackgroundColor(Color.parseColor("#333333"));
            btnUser.setTextColor(Color.parseColor("#FFFFFF"));

            username.setError(null);
            password.setError(null);
            inputCaptcha.setError(null);

            updateSignInButtonState();
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });

        btnGoRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnSignIn.setOnClickListener(v -> loginUser());
    }

    @Override
    protected void onResume() {
        super.onResume();

        username.setText("");
        password.setText("");
        inputCaptcha.setText("");

        username.setError(null);
        password.setError(null);
        inputCaptcha.setError(null);

        generateCaptcha();
        updateSignInButtonState();
    }

    private void updateSignInButtonState() {
        if (selectedRole.equals("admin") && adminFailedAttempts >= MAX_ATTEMPTS) {
            btnSignIn.setEnabled(false);
            btnSignIn.setBackgroundColor(Color.parseColor("#555555"));
            btnSignIn.setTextColor(Color.parseColor("#AAAAAA"));
        } else if (selectedRole.equals("user") && userFailedAttempts >= MAX_ATTEMPTS) {
            btnSignIn.setEnabled(false);
            btnSignIn.setBackgroundColor(Color.parseColor("#555555"));
            btnSignIn.setTextColor(Color.parseColor("#AAAAAA"));
        } else {
            btnSignIn.setEnabled(true);
            btnSignIn.setBackgroundColor(Color.parseColor("#1DB954"));
            btnSignIn.setTextColor(Color.parseColor("#000000"));
        }
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
        inputCaptcha.setError(null);
    }

    private void loginUser() {
        if (selectedRole.equals("admin") && adminFailedAttempts >= MAX_ATTEMPTS) {
            Toast.makeText(this, "Admin account locked.", Toast.LENGTH_LONG).show();
            return;
        } else if (selectedRole.equals("user") && userFailedAttempts >= MAX_ATTEMPTS) {
            Toast.makeText(this, "User account locked.", Toast.LENGTH_LONG).show();
            return;
        }

        String userInput = username.getText().toString().trim();
        String passInput = password.getText().toString().trim();
        String captchaInput = inputCaptcha.getText().toString().trim();

        boolean hasError = false;

        if (userInput.isEmpty()) {
            username.setError("Username is required");
            hasError = true;
        }

        if (passInput.isEmpty()) {
            password.setError("Password is required");
            hasError = true;
        }

        if (captchaInput.isEmpty()) {
            inputCaptcha.setError("CAPTCHA is required");
            hasError = true;
        }

        if (hasError) {
            return;
        }

        if (!captchaInput.equals(currentCaptcha)) {
            inputCaptcha.setError("Invalid CAPTCHA");
            generateCaptcha();
            return;
        }

        if (selectedRole.equals("admin")) {
            if (userInput.equals(adminUsername) && passInput.equals(adminPassword)) {
                adminFailedAttempts = 0;
                Toast.makeText(this, "Welcome Admin", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
            } else {
                handleFailedAttempt("admin");
                generateCaptcha();
            }
        } else {
            boolean defaultUser = userInput.equals(defaultUserUsername) && passInput.equals(defaultUserPassword);
            boolean registeredUser = userInput.equals(RegisterActivity.registeredUsername) && passInput.equals(RegisterActivity.registeredPassword);

            if (defaultUser || registeredUser) {
                userFailedAttempts = 0;
                Intent intent = new Intent(MainActivity.this, UserDashboardActivity.class);
                if (registeredUser && !RegisterActivity.registeredFullName.isEmpty()) {
                    intent.putExtra("username", RegisterActivity.registeredFullName);
                } else {
                    intent.putExtra("username", userInput);
                }
                startActivity(intent);
            } else {
                handleFailedAttempt("user");
                generateCaptcha();
            }
        }
    }

    private void handleFailedAttempt(String role) {
        if (role.equals("admin")) {
            adminFailedAttempts++;
            if (adminFailedAttempts >= MAX_ATTEMPTS) {
                updateSignInButtonState();
                Toast.makeText(this, "Admin account locked.", Toast.LENGTH_LONG).show();
            } else {
                int attemptsLeft = MAX_ATTEMPTS - adminFailedAttempts;
                Toast.makeText(this, "Invalid admin credentials. Attempts left: " + attemptsLeft, Toast.LENGTH_SHORT).show();
            }
        } else {
            userFailedAttempts++;
            if (userFailedAttempts >= MAX_ATTEMPTS) {
                updateSignInButtonState();
                Toast.makeText(this, "User account locked.", Toast.LENGTH_LONG).show();
            } else {
                int attemptsLeft = MAX_ATTEMPTS - userFailedAttempts;
                Toast.makeText(this, "Invalid user credentials. Attempts left: " + attemptsLeft, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
