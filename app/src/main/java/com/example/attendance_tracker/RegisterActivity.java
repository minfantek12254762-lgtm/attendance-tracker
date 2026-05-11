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

        btnRegister.setOnClickListener(v -> register());

        btnGoLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void register() {
        String nameInput = fullName.getText().toString().trim();
        String userInput = username.getText().toString().trim();
        String passInput = password.getText().toString().trim();
        String confirmInput = confirmPassword.getText().toString().trim();

        if (nameInput.isEmpty() || userInput.isEmpty() || passInput.isEmpty() || confirmInput.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!passInput.equals(confirmInput)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        registeredFullName = nameInput;
        registeredUsername = userInput;
        registeredPassword = passInput;

        Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
