package com.example.attendance_tracker;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
    EditText dateInput, timeInInput, timeOutInput, locationInput;
    Spinner requestType;
    Button submitBtn, logoutBtn;
    LinearLayout recordContainer;

    Handler handler = new Handler(Looper.getMainLooper());

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
        emptyText = findViewById(R.id.emptyText);

        dateInput = findViewById(R.id.dateInput);
        timeInInput = findViewById(R.id.timeInInput);
        timeOutInput = findViewById(R.id.timeOutInput);
        locationInput = findViewById(R.id.locationInput);

        requestType = findViewById(R.id.requestType);

        submitBtn = findViewById(R.id.submitBtn);
        logoutBtn = findViewById(R.id.logoutBtn);

        recordContainer = findViewById(R.id.recordContainer);

        String username = getIntent().getStringExtra("username");
        if (username == null || username.trim().isEmpty()) {
            username = "user";
        }

        setGreeting(username);
        setCurrentDate();
        setupSpinner();
        startClock();
        updateStats();
        updateEstimate();

        requestType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String type = requestType.getSelectedItem().toString();

                if (type.equals("Regular Work")) {
                    timeInInput.setEnabled(true);
                    timeOutInput.setEnabled(true);
                } else {
                    timeInInput.setEnabled(false);
                    timeOutInput.setEnabled(false);
                    timeInInput.setText("");
                    timeOutInput.setText("");
                }

                updateEstimate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        timeInInput.addTextChangedListener(simpleWatcher);
        timeOutInput.addTextChangedListener(simpleWatcher);

        submitBtn.setOnClickListener(v -> submitRequest());
        logoutBtn.setOnClickListener(v -> finish());
    }

    private final TextWatcher simpleWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            updateEstimate();
        }
    };

    private void setGreeting(String username) {
        String fullText = "Hello, " + username;
        SpannableString span = new SpannableString(fullText);

        int start = "Hello, ".length();

        span.setSpan(new StyleSpan(Typeface.BOLD), 0, fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ForegroundColorSpan(Color.parseColor("#7A8B2E")), start, fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        helloText.setText(span);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Regular Work", "Vacation Leave", "Sick Leave"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        requestType.setAdapter(adapter);
    }

    private void setCurrentDate() {
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        dateInput.setText(today);
    }

    private void startClock() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                String time = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(new Date());
                clockText.setText(time);
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void updateEstimate() {
        String type = requestType.getSelectedItem() != null ? requestType.getSelectedItem().toString() : "Regular Work";

        if (!type.equals("Regular Work")) {
            estPay.setText("Estimated Gross Pay: PHP 0.00");
            return;
        }

        double paidHours = calculatePaidHours();
        double grossPay = paidHours * hourlyRate;

        estPay.setText("Estimated Gross Pay: PHP " + String.format(Locale.getDefault(), "%.2f", grossPay));
    }

    private void submitRequest() {
        String date = dateInput.getText().toString().trim();
        String type = requestType.getSelectedItem().toString();
        String timeIn = timeInInput.getText().toString().trim();
        String timeOut = timeOutInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();

        if (date.isEmpty()) {
            Toast.makeText(this, "Please enter date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (location.isEmpty()) {
            Toast.makeText(this, "Please enter location", Toast.LENGTH_SHORT).show();
            return;
        }

        double paidHours = 0;
        double overtime = 0;
        double grossPay = 0;
        String timeDisplay = "-";
        String remarks = "-";

        if (type.equals("Regular Work")) {
            if (timeIn.isEmpty() || timeOut.isEmpty()) {
                Toast.makeText(this, "Please enter Time In and Time Out", Toast.LENGTH_SHORT).show();
                return;
            }

            paidHours = calculatePaidHours();

            if (paidHours <= 0) {
                Toast.makeText(this, "Invalid time input", Toast.LENGTH_SHORT).show();
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
                location,
                remarks
        );

        timeInInput.setText("");
        timeOutInput.setText("");
        locationInput.setText("");
        updateEstimate();

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
            String location,
            String remarks
    ) {
        emptyText.setVisibility(View.GONE);

        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(10), 0, dp(10));

        row.addView(createCell(date, 110));
        row.addView(createCell(type, 120));
        row.addView(createCell(time, 170));
        row.addView(createCell(String.format(Locale.getDefault(), "%.2f", paidHours), 90));
        row.addView(createCell(String.format(Locale.getDefault(), "%.2f", overtime), 70));
        row.addView(createCell("PHP " + String.format(Locale.getDefault(), "%.2f", grossPay), 120));
        row.addView(createStatusCell(status, 120));
        row.addView(createCell(location, 180));
        row.addView(createCell(remarks, 170));

        View divider = new View(this);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(1)
        );
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(Color.parseColor("#E2E8F0"));

        wrapper.addView(row);
        wrapper.addView(divider);

        recordContainer.addView(wrapper);
    }

    private TextView createCell(String text, int widthDp) {
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(widthDp), ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(params);
        textView.setText(text);
        textView.setTextSize(14);
        textView.setTextColor(Color.parseColor("#0F172A"));
        textView.setSingleLine(false);
        textView.setPadding(0, 0, dp(8), 0);
        return textView;
    }

    private LinearLayout createStatusCell(String status, int widthDp) {
        LinearLayout container = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(widthDp), ViewGroup.LayoutParams.WRAP_CONTENT);
        container.setLayoutParams(params);
        container.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        TextView badge = new TextView(this);
        badge.setText(status);
        badge.setTextSize(13);
        badge.setTypeface(null, Typeface.BOLD);
        badge.setPadding(dp(10), dp(5), dp(10), dp(5));

        if (status.equalsIgnoreCase("Approved")) {
            badge.setTextColor(Color.parseColor("#15803D"));
            badge.setBackgroundResource(R.drawable.status_approved_bg);
        } else if (status.equalsIgnoreCase("Rejected")) {
            badge.setTextColor(Color.parseColor("#B91C1C"));
            badge.setBackgroundResource(R.drawable.status_rejected_bg);
        } else {
            badge.setTextColor(Color.parseColor("#B45309"));
            badge.setBackgroundResource(R.drawable.status_pending_bg);
        }

        container.addView(badge);
        return container;
    }

    private void updateStats() {
        statHours.setText(String.format(Locale.getDefault(), "%.2fh", approvedHours));
        statPay.setText("PHP " + String.format(Locale.getDefault(), "%.2f", approvedPay));
        statPending.setText(String.valueOf(pendingCount));
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
