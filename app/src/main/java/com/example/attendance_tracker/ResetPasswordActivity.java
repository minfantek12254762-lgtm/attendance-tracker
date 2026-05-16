package com.example.attendance_tracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ResetPasswordActivity extends AppCompatActivity {

    EditText resetUsername, newPassword, confirmNewPassword;
    Button btnResetPassword, btnBackToLoginReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        resetUsername = findViewById(R.id.resetUsername);
        newPassword = findViewById(R.id.newPassword);
        confirmNewPassword = findViewById(R.id.confirmNewPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBackToLoginReset = findViewById(R.id.btnBackToLoginReset);

        btnResetPassword.setOnClickListener(v -> handleResetPassword());

        btnBackToLoginReset.setOnClickListener(v -> finish());
    }

    private void handleResetPassword() {
        String username = resetUsername.getText().toString().trim();
        String pass = newPassword.getText().toString().trim();
        String confirmPass = confirmNewPassword.getText().toString().trim();

        if (username.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(confirmPass)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.equals(RegisterActivity.registeredUsername) && !RegisterActivity.registeredUsername.isEmpty()) {
            RegisterActivity.registeredPassword = pass;
            Toast.makeText(this, "Password reset successful!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show();
        }
    }
}
