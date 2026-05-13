package com.example.attendance_tracker;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText username, password;
    Button btnUser, btnAdmin, btnSignIn, btnGoRegister;

    String selectedRole = "user";

    String adminUsername = "admin";
    String adminPassword = "admin123";

    String defaultUserUsername = "user";
    String defaultUserPassword = "user123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);

        btnUser = findViewById(R.id.btnUser);
        btnAdmin = findViewById(R.id.btnAdmin);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnGoRegister = findViewById(R.id.btnGoRegister);

        btnUser.setOnClickListener(v -> {
            selectedRole = "user";

            btnGoRegister.setVisibility(View.VISIBLE);

            btnUser.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#1DB954"))
            );
            btnUser.setTextColor(Color.BLACK);

            btnAdmin.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#282828"))
            );
            btnAdmin.setTextColor(Color.WHITE);

            username.setText("");
            password.setText("");
        });

        btnAdmin.setOnClickListener(v -> {
            selectedRole = "admin";

            btnGoRegister.setVisibility(View.GONE);

            btnAdmin.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#1DB954"))
            );
            btnAdmin.setTextColor(Color.BLACK);

            btnUser.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#282828"))
            );
            btnUser.setTextColor(Color.WHITE);

            username.setText("");
            password.setText("");
        });

        btnSignIn.setOnClickListener(v -> login());

        btnGoRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void login() {

        String userInput = username.getText().toString().trim();
        String passInput = password.getText().toString().trim();

        if (userInput.isEmpty() || passInput.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedRole.equals("admin")) {

            if (userInput.equals(adminUsername) && passInput.equals(adminPassword)) {

                Toast.makeText(this, "Welcome Admin", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                startActivity(intent);

            } else {

                Toast.makeText(this, "Invalid admin credentials", Toast.LENGTH_SHORT).show();
            }

        } else {

            boolean defaultUser =
                    userInput.equals(defaultUserUsername)
                            && passInput.equals(defaultUserPassword);

            boolean registeredUser =
                    userInput.equals(RegisterActivity.registeredUsername)
                            && passInput.equals(RegisterActivity.registeredPassword);

            if (defaultUser || registeredUser) {

                Intent intent = new Intent(MainActivity.this, UserDashboardActivity.class);

                if (registeredUser && !RegisterActivity.registeredFullName.isEmpty()) {
                    intent.putExtra("username", RegisterActivity.registeredFullName);
                } else {
                    intent.putExtra("username", userInput);
                }

                startActivity(intent);

            } else {

                Toast.makeText(this, "Invalid user credentials", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
