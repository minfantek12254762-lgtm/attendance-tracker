package com.example.attendance_tracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class RegisterActivity extends AppCompatActivity {

    EditText fullName, username, password, confirmPassword, inputCaptchaReg;
    Button btnRegister, btnGoLogin, btnRefreshCaptchaReg;
    TextView tvCaptchaReg;

    public static String registeredFullName = "";
    public static String registeredUsername = "";
    public static String registeredPassword = "";
    String currentCaptcha = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fullName = findViewById(R.id.fullName);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        inputCaptchaReg = findViewById(R.id.inputCaptchaReg);
        tvCaptchaReg = findViewById(R.id.tvCaptchaReg);
        btnRefreshCaptchaReg = findViewById(R.id.btnRefreshCaptchaReg);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoLogin = findViewById(R.id.btnGoLogin);

        generateCaptcha();

        btnRefreshCaptchaReg.setOnClickListener(v -> generateCaptcha());
        btnRegister.setOnClickListener(v -> registerUser());
        btnGoLogin.setOnClickListener(v -> goToLogin());
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
        tvCaptchaReg.setText(currentCaptcha);
        inputCaptchaReg.setText("");
    }

    private void registerUser() {
        String nameInput = fullName.getText().toString().trim();
        String userInput = username.getText().toString().trim();
        String passInput = password.getText().toString().trim();
        String confirmPassInput = confirmPassword.getText().toString().trim();
        String captchaInput = inputCaptchaReg.getText().toString().trim();

        if (nameInput.isEmpty() || userInput.isEmpty() || passInput.isEmpty() || confirmPassInput.isEmpty() || captchaInput.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!passInput.equals(confirmPassInput)) {
            Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!captchaInput.equals(currentCaptcha)) {
            Toast.makeText(this, "Invalid CAPTCHA", Toast.LENGTH_SHORT).show();
            generateCaptcha();
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
