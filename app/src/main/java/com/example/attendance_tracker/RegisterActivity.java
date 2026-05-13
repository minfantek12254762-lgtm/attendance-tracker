package com.example.attendance_tracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    EditText fullName, username, password, confirmPassword;
    Button btnRegister, btnGoLogin;

    public static String registeredFullName = "";
    public static String registeredUsername = "";
    public static String registeredPassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fullName = findViewById(R.id.fullName);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        btnGoLogin = findViewById(R.id.btnGoLogin);

        btnRegister.setOnClickListener(v -> registerUser());

        btnGoLogin.setOnClickListener(v -> goToLogin());
    }

    private void registerUser() {

        String nameInput = fullName.getText().toString().trim();
        String userInput = username.getText().toString().trim();
        String passInput = password.getText().toString().trim();
        String confirmPassInput = confirmPassword.getText().toString().trim();

        if (nameInput.isEmpty()) {
            Toast.makeText(this, "Please enter full name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userInput.isEmpty()) {
            Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();
            return;
        }

        if (passInput.isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (confirmPassInput.isEmpty()) {
            Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!passInput.equals(confirmPassInput)) {
            Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show();
            return;
        }

        registeredFullName = nameInput;
        registeredUsername = userInput;
        registeredPassword = passInput;

        Toast.makeText(this, "Account registered successfully", Toast.LENGTH_SHORT).show();

        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
