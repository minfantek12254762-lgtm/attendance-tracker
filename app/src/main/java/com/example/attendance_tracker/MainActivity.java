package com.example.attendance_tracker;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText username, password;
    Button btnUser, btnAdmin, btnSignIn;

    String selectedRole = "user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);

        btnUser = findViewById(R.id.btnUser);
        btnAdmin = findViewById(R.id.btnAdmin);
        btnSignIn = findViewById(R.id.btnSignIn);

        btnUser.setOnClickListener(v -> {
            selectedRole = "user";

            btnUser.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#1DB954"))
            );

            btnAdmin.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#282828"))
            );
        });

        btnAdmin.setOnClickListener(v -> {
            selectedRole = "admin";

            btnAdmin.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#1DB954"))
            );

            btnUser.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#282828"))
            );
        });

        btnSignIn.setOnClickListener(v -> login());
    }

    private void login() {

        String userInput = username.getText().toString().trim();
        String passInput = password.getText().toString().trim();

        if (selectedRole.equals("admin")) {

            if (userInput.equals("admin")
                    && passInput.equals("admin123")) {

                Toast.makeText(this,
                        "Welcome Admin!",
                        Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(this,
                        "Admin account only!",
                        Toast.LENGTH_SHORT).show();
            }

        } else {

            if (userInput.equals("user")
                    && passInput.equals("user123")) {

                Toast.makeText(this,
                        "Welcome User!",
                        Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(this,
                        "User account only!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}