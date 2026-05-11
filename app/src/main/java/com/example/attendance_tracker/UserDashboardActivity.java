package com.example.attendance_tracker;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserDashboardActivity extends AppCompatActivity {

    TextView helloText, clockText, statHours, statPay, statPending, estPay, emptyText;
    EditText dateInput, timeInInput, timeOutInput;
    Spinner requestType;
    Button submitBtn, logoutBtn;
    LinearLayout recordContainer;

    Handler handler = new Handler();

    double hourlyRate = 250.00;
    double approvedHours = 0;
    double approvedPay = 0;
    int pendingCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        helloText = findViewById(R.id.helloText);
        clockText = findViewById(R.id.clockText);
        statHours = findViewById(R.id.statHours);
        statPay = findViewById(R.id.statPay);
        statPending = findViewById(R.id.statPending);
        estPay = findViewById(R.id.estPay);
        dateInput = findViewById(R.id.dateInput);
        timeInInput = findViewById(R.id.timeInInput);
        timeOutInput = findViewById(R.id.timeOutInput);
        requestType = findViewById(R.id.requestType);
        submitBtn = findViewById(R.id.submitBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        recordContainer = findViewById(R.id.recordContainer);
        emptyText = findViewById(R.id.emptyText);

        String username = getIntent().getStringExtra("username");

        if (username == null || username.isEmpty()) {
            username = "user";
        }

        helloText.setText("Hello, " + username);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Regular Work", "Vacation Leave", "Sick Leave"}
        );

        requestType.setAdapter(adapter);

        dateInput.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        startClock();
        updateStats();

        requestType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String type = requestType.getSelectedItem().toString();

                if (!type.equals("Regular Work")) {
                    timeInInput.setText("");
                    timeOutInput.setText("");
                    timeInInput.setEnabled(false);
                    timeOutInput.setEnabled(false);
                    estPay.setText("PHP 0.00");
                } else {
                    timeInInput.setEnabled(true);
                    timeOutInput.setEnabled(true);
                    updateEstimate();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        timeInInput.setOnFocusChangeListener((v, hasFocus) -> updateEstimate());
        timeOutInput.setOnFocusChangeListener((v, hasFocus) -> updateEstimate());

        submitBtn.setOnClickListener(v -> submitRequest());

        logoutBtn.setOnClickListener(v -> finish());
    }

    private void startClock() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                clockText.setText(time);
                handler.postDelayed(this, 1000);
            }
        }, 0);
    }

    private void updateEstimate() {
        String type = requestType.getSelectedItem().toString();

        if (!type.equals("Regular Work")) {
            estPay.setText("PHP 0.00");
            return;
        }

        double paidHours = calculatePaidHours();
        double grossPay = paidHours * hourlyRate;

        estPay.setText("PHP " + String.format(Locale.getDefault(), "%.2f", grossPay));
    }

    private void submitRequest() {
        String date = dateInput.getText().toString().trim();
        String type = requestType.getSelectedItem().toString();
        String timeIn = timeInInput.getText().toString().trim();
        String timeOut = timeOutInput.getText().toString().trim();

        if (date.isEmpty()) {
            Toast.makeText(this, "Please enter date", Toast.LENGTH_SHORT).show();
            return;
        }

        double paidHours = 0;
        double overtime = 0;
        double grossPay = 0;
        String timeDisplay = "-";

        if (type.equals("Regular Work")) {
            if (timeIn.isEmpty() || timeOut.isEmpty()) {
                Toast.makeText(this, "Please enter Time In and Time Out", Toast.LENGTH_SHORT).show();
                return;
            }

            paidHours = calculatePaidHours();

            if (paidHours <= 0) {
                Toast.makeText(this, "Invalid time", Toast.LENGTH_SHORT).show();
                return;
            }

            overtime = Math.max(0, paidHours - 8);
            grossPay = paidHours * hourlyRate;
            timeDisplay = timeIn + " - " + timeOut;
        }

        pendingCount++;
        updateStats();

        addRecord(
                date,
                type,
                timeDisplay,
                paidHours,
                overtime,
                grossPay,
                "Pending",
                "Waiting for admin review"
        );

        timeInInput.setText("");
        timeOutInput.setText("");
        estPay.setText("PHP 0.00");

        Toast.makeText(this, "Request submitted", Toast.LENGTH_SHORT).show();
    }

    private double calculatePaidHours() {
        try {
            String timeIn = timeInInput.getText().toString().trim();
            String timeOut = timeOutInput.getText().toString().trim();

            if (timeIn.isEmpty() || timeOut.isEmpty()) {
                return 0;
            }

            SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());

            Date in = format.parse(timeIn);
            Date out = format.parse(timeOut);

            if (in == null || out == null) {
                return 0;
            }

            long difference = out.getTime() - in.getTime();

            if (difference <= 0) {
                return 0;
            }

            double hours = difference / 3600000.0;

            if (hours > 5) {
                hours -= 1;
            }

            return hours;

        } catch (Exception e) {
            return 0;
        }
    }

    private void addRecord(
            String date,
            String type,
            String time,
            double paidHours,
            double overtime,
            double grossPay,
            String status,
            String remarks
    ) {
        emptyText.setVisibility(View.GONE);

        TextView record = new TextView(this);

        record.setText(
                "Date: " + date +
                        "\nType: " + type +
                        "\nTime: " + time +
                        "\nPaid Hrs: " + String.format(Locale.getDefault(), "%.2f", paidHours) +
                        "\nOT: " + String.format(Locale.getDefault(), "%.2f", overtime) +
                        "\nGross Pay: PHP " + String.format(Locale.getDefault(), "%.2f", grossPay) +
                        "\nStatus: " + status +
                        "\nAdmin Remarks: " + remarks
        );

        record.setTextSize(14);
        record.setTextColor(Color.parseColor("#1E293B"));
        record.setPadding(18, 18, 18, 18);
        record.setBackgroundResource(R.drawable.record_bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 0, 0, 14);
        record.setLayoutParams(params);

        recordContainer.addView(record);
    }

    private void updateStats() {
        statHours.setText(String.format(Locale.getDefault(), "%.2fh", approvedHours));
        statPay.setText("PHP " + String.format(Locale.getDefault(), "%.2f", approvedPay));
        statPending.setText(String.valueOf(pendingCount));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
