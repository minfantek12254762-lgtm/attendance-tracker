package com.example.attendance_tracker;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class UserDashboardActivity extends AppCompatActivity {

    TextView helloText, clockText, statHours, statPay, statPending, estPay, emptyText;
    EditText dateInput, timeInInput, timeOutInput, locationInput;
    Spinner requestType;
    Button submitBtn, logoutBtn;
    LinearLayout recordContainer;

    Handler handler = new Handler(Looper.getMainLooper());
    String username = "user";
    DatabaseReference attendanceRef;

    double hourlyRate = 250.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        attendanceRef = FirebaseDatabase.getInstance("https://attendance-tracking-1f963-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("attendance_records");

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

        String passedUsername = getIntent().getStringExtra("username");
        if (passedUsername != null) username = passedUsername;

        helloText.setText("Hello, " + username);
        dateInput.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        requestType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Regular Work", "Vacation Leave", "Sick Leave"}));

        startClock();
        loadUserRecords();

        TextWatcher timeWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateEstimatedPay();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        timeInInput.addTextChangedListener(timeWatcher);
        timeOutInput.addTextChangedListener(timeWatcher);

        requestType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = requestType.getSelectedItem().toString();
                if (selectedType.contains("Leave")) {
                    timeInInput.setText("");
                    timeOutInput.setText("");
                    locationInput.setText("");

                    timeInInput.setEnabled(false);
                    timeOutInput.setEnabled(false);
                    locationInput.setEnabled(false);

                    timeInInput.setHint("Not Required");
                    timeOutInput.setHint("Not Required");
                    locationInput.setHint("Not Required");
                } else {
                    timeInInput.setEnabled(true);
                    timeOutInput.setEnabled(true);
                    locationInput.setEnabled(true);

                    timeInInput.setHint("08:00");
                    timeOutInput.setHint("17:00");
                    locationInput.setHint("Enter location");
                }
                calculateEstimatedPay();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        submitBtn.setOnClickListener(v -> submitRequest());

        logoutBtn.setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void startClock() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                clockText.setText(new SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(new Date()));
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void calculateEstimatedPay() {
        String selectedType = requestType.getSelectedItem().toString();
        if (selectedType.contains("Leave")) {
            double estimatedPay = 8.0 * hourlyRate;
            estPay.setText(String.format(Locale.getDefault(), "Estimated Gross Pay: PHP %.2f", estimatedPay));
            return;
        }

        String timeIn = timeInInput.getText().toString().trim();
        String timeOut = timeOutInput.getText().toString().trim();

        if (timeIn.isEmpty() || timeOut.isEmpty()) {
            estPay.setText("PHP 0.00");
            return;
        }

        try {
            String[] inParts = timeIn.split(":");
            String[] outParts = timeOut.split(":");

            if (inParts.length < 2 || outParts.length < 2) {
                estPay.setText("PHP 0.00");
                return;
            }

            double inHour = Double.parseDouble(inParts[0]);
            double inMin = Double.parseDouble(inParts[1].replaceAll("[^0-9]", ""));
            double outHour = Double.parseDouble(outParts[0]);
            double outMin = Double.parseDouble(outParts[1].replaceAll("[^0-9]", ""));

            if (timeIn.toLowerCase().contains("pm") && inHour < 12) inHour += 12;
            if (timeIn.toLowerCase().contains("am") && inHour == 12) inHour = 0;
            if (timeOut.toLowerCase().contains("pm") && outHour < 12) outHour += 12;
            if (timeOut.toLowerCase().contains("am") && outHour == 12) outHour = 0;

            double inTime = inHour + (inMin / 60.0);
            double outTime = outHour + (outMin / 60.0);
            double totalHours = 0;

            if (outTime >= inTime) {
                totalHours = outTime - inTime;
            } else {
                totalHours = (24.0 - inTime) + outTime;
            }

            double regularHours = 0;
            double otHours = 0;

            if (totalHours > 8.0) {
                regularHours = 8.0;
                otHours = totalHours - 8.0;
            } else {
                regularHours = totalHours;
                otHours = 0.0;
            }

            double estimatedPay = (regularHours * hourlyRate) + (otHours * hourlyRate * 1.25);
            estPay.setText(String.format(Locale.getDefault(), "Estimated Gross Pay: PHP %.2f", estimatedPay));

        } catch (Exception e) {
            estPay.setText("Estimated Gross Pay: PHP 0.00");
        }
    }

    private void submitRequest() {
        String date = dateInput.getText().toString().trim();
        String timeIn = timeInInput.getText().toString().trim();
        String timeOut = timeOutInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String type = requestType.getSelectedItem().toString();

        boolean isLeave = type.contains("Leave");

        if (date.isEmpty() || (!isLeave && (location.isEmpty() || timeIn.isEmpty() || timeOut.isEmpty()))) {
            showBeautifulError("Please completely fill out all required fields.");
            return;
        }

        final double finalRegularHours;
        final double finalOtHours;
        final String combinedTime;
        final String finalLocation;

        if (isLeave) {
            finalRegularHours = 8.0;
            finalOtHours = 0.0;
            combinedTime = "08:00 - 17:00";
            finalLocation = "Not Required";
        } else {
            try {
                String[] inParts = timeIn.split(":");
                String[] outParts = timeOut.split(":");

                double inHour = Double.parseDouble(inParts[0]);
                double inMin = Double.parseDouble(inParts[1].replaceAll("[^0-9]", ""));
                double outHour = Double.parseDouble(outParts[0]);
                double outMin = Double.parseDouble(outParts[1].replaceAll("[^0-9]", ""));

                if (timeIn.toLowerCase().contains("pm") && inHour < 12) inHour += 12;
                if (timeIn.toLowerCase().contains("am") && inHour == 12) inHour = 0;
                if (timeOut.toLowerCase().contains("pm") && outHour < 12) outHour += 12;
                if (timeOut.toLowerCase().contains("am") && outHour == 12) outHour = 0;

                double inTime = inHour + (inMin / 60.0);
                double outTime = outHour + (outMin / 60.0);
                double totalHours = 0;

                if (outTime >= inTime) {
                    totalHours = outTime - inTime;
                } else {
                    totalHours = (24.0 - inTime) + outTime;
                }

                if (totalHours > 8.0) {
                    finalRegularHours = 8.0;
                    finalOtHours = totalHours - 8.0;
                } else {
                    finalRegularHours = totalHours;
                    finalOtHours = 0.0;
                }
                combinedTime = timeIn + " - " + timeOut;
                finalLocation = location;
            } catch (Exception e) {
                showBeautifulError("Invalid time format. Please use HH:mm (e.g., 08:00).");
                return;
            }
        }

        attendanceRef.orderByChild("name").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isDuplicate = false;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String existingDate = dataSnapshot.child("date").getValue(String.class);
                    String existingTime = dataSnapshot.child("time").getValue(String.class);

                    if (date.equals(existingDate) && combinedTime.equals(existingTime)) {
                        isDuplicate = true;
                        break;
                    }
                }

                if (isDuplicate) {
                    showBeautifulError("Conflict Detected: An attendance record for " + date + " (" + combinedTime + ") already exists.");
                } else {
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("name", username);
                    data.put("date", date);
                    data.put("type", type);
                    data.put("time", combinedTime);
                    data.put("location", finalLocation);
                    data.put("status", "Pending");
                    data.put("remarks", "");
                    data.put("paidHours", finalRegularHours);
                    data.put("overtimeHours", finalOtHours);
                    data.put("grossPay", (finalRegularHours * hourlyRate) + (finalOtHours * hourlyRate * 1.25));

                    attendanceRef.push().setValue(data).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showSuccessMessage("Attendance request submitted successfully!");
                            timeInInput.setText("");
                            timeOutInput.setText("");
                            locationInput.setText("");
                            estPay.setText("Estimated Gross Pay: PHP 0.00");
                            calculateEstimatedPay();
                        } else {
                            showBeautifulError("Submission failed. Please check your internet connection.");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showBeautifulError("Database error occurred while checking entries.");
            }
        });
    }

    private void loadUserRecords() {
        attendanceRef.orderByChild("name").equalTo(username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recordContainer.removeAllViews();

                double totalApprovedHours = 0;
                double totalApprovedPay = 0;
                int totalPending = 0;

                if (!snapshot.exists()) {
                    emptyText.setVisibility(View.VISIBLE);
                } else {
                    emptyText.setVisibility(View.GONE);

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String date = dataSnapshot.child("date").getValue(String.class);
                        String type = dataSnapshot.child("type").getValue(String.class);
                        String time = dataSnapshot.child("time").getValue(String.class);
                        String status = dataSnapshot.child("status").getValue(String.class);
                        String loc = dataSnapshot.child("location").getValue(String.class);
                        String remarks = dataSnapshot.child("remarks").getValue(String.class);

                        Double pHours = dataSnapshot.child("paidHours").getValue(Double.class);
                        Double otHours = dataSnapshot.child("overtimeHours").getValue(Double.class);
                        Double gPay = dataSnapshot.child("grossPay").getValue(Double.class);

                        if (pHours == null) pHours = 0.0;
                        if (otHours == null) otHours = 0.0;
                        if (gPay == null) gPay = 0.0;
                        if (remarks == null || remarks.isEmpty()) remarks = "-";
                        if (loc == null || loc.isEmpty()) loc = "-";

                        if ("Pending".equalsIgnoreCase(status)) {
                            totalPending++;
                        } else if ("Approved".equalsIgnoreCase(status)) {
                            totalApprovedHours += pHours;
                            totalApprovedPay += gPay;
                        }

                        CardView cardView = new CardView(UserDashboardActivity.this);
                        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        cardParams.setMargins(dp(12), dp(6), dp(12), dp(6));
                        cardView.setLayoutParams(cardParams);
                        cardView.setRadius(dp(10));
                        cardView.setCardElevation(dp(3));
                        cardView.setUseCompatPadding(true);

                        LinearLayout row = new LinearLayout(UserDashboardActivity.this);
                        row.setOrientation(LinearLayout.HORIZONTAL);
                        row.setPadding(dp(12), dp(14), dp(12), dp(14));
                        row.setGravity(Gravity.CENTER_VERTICAL);

                        if ("Approved".equalsIgnoreCase(status)) {
                            row.setBackgroundColor(Color.parseColor("#F4FAF7"));
                        } else if ("Rejected".equalsIgnoreCase(status)) {
                            row.setBackgroundColor(Color.parseColor("#FFF5F5"));
                        } else {
                            row.setBackgroundColor(Color.parseColor("#FFFDF6"));
                        }

                        TextView tvDate = new TextView(UserDashboardActivity.this);
                        tvDate.setLayoutParams(new LinearLayout.LayoutParams(dp(110), ViewGroup.LayoutParams.WRAP_CONTENT));
                        tvDate.setText(date);
                        tvDate.setTextColor(Color.parseColor("#2D3748"));
                        tvDate.setTypeface(null, Typeface.BOLD);
                        row.addView(tvDate);

                        TextView tvType = new TextView(UserDashboardActivity.this);
                        tvType.setLayoutParams(new LinearLayout.LayoutParams(dp(120), ViewGroup.LayoutParams.WRAP_CONTENT));
                        tvType.setText(type);
                        tvType.setTextColor(Color.parseColor("#4A5568"));
                        row.addView(tvType);

                        TextView tvTime = new TextView(UserDashboardActivity.this);
                        tvTime.setLayoutParams(new LinearLayout.LayoutParams(dp(170), ViewGroup.LayoutParams.WRAP_CONTENT));
                        tvTime.setText(time);
                        tvTime.setTextColor(Color.parseColor("#4A5568"));
                        row.addView(tvTime);

                        TextView tvPaidHours = new TextView(UserDashboardActivity.this);
                        tvPaidHours.setLayoutParams(new LinearLayout.LayoutParams(dp(90), ViewGroup.LayoutParams.WRAP_CONTENT));
                        tvPaidHours.setText(String.format(Locale.getDefault(), "%.2fh", pHours));
                        tvPaidHours.setTextColor(Color.parseColor("#4A5568"));
                        row.addView(tvPaidHours);

                        TextView tvOT = new TextView(UserDashboardActivity.this);
                        tvOT.setLayoutParams(new LinearLayout.LayoutParams(dp(70), ViewGroup.LayoutParams.WRAP_CONTENT));
                        tvOT.setText(String.format(Locale.getDefault(), "%.2fh", otHours));
                        tvOT.setTextColor(Color.parseColor("#E53E3E"));
                        row.addView(tvOT);

                        TextView tvGrossPay = new TextView(UserDashboardActivity.this);
                        tvGrossPay.setLayoutParams(new LinearLayout.LayoutParams(dp(140), ViewGroup.LayoutParams.WRAP_CONTENT));
                        tvGrossPay.setText(String.format(Locale.getDefault(), "PHP %.2f", gPay));
                        tvGrossPay.setTextColor(Color.parseColor("#2B6CB0"));
                        tvGrossPay.setTypeface(null, Typeface.BOLD);
                        tvGrossPay.setMaxLines(1);
                        tvGrossPay.setHorizontallyScrolling(true);
                        row.addView(tvGrossPay);

                        LinearLayout statusContainer = createStatusBadge(status);
                        statusContainer.setLayoutParams(new LinearLayout.LayoutParams(dp(120), ViewGroup.LayoutParams.WRAP_CONTENT));
                        row.addView(statusContainer);

                        TextView tvLoc = new TextView(UserDashboardActivity.this);
                        tvLoc.setLayoutParams(new LinearLayout.LayoutParams(dp(180), ViewGroup.LayoutParams.WRAP_CONTENT));
                        tvLoc.setText(loc);
                        tvLoc.setTextColor(Color.parseColor("#718096"));
                        row.addView(tvLoc);

                        TextView tvRemarks = new TextView(UserDashboardActivity.this);
                        tvLoc.setEllipsize(android.text.TextUtils.TruncateAt.END);
                        tvRemarks.setLayoutParams(new LinearLayout.LayoutParams(dp(170), ViewGroup.LayoutParams.WRAP_CONTENT));
                        tvRemarks.setText(remarks);
                        tvRemarks.setTextColor(Color.parseColor("#718096"));
                        row.addView(tvRemarks);

                        cardView.addView(row);
                        recordContainer.addView(cardView);
                    }
                }

                statHours.setText(String.format(Locale.getDefault(), "%.2fh", totalApprovedHours));
                statPay.setText("PHP " + String.format(Locale.getDefault(), "%.2f", totalApprovedPay));
                statPending.setText(String.valueOf(totalPending));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showBeautifulError("Failed to fetch historical attendance records.");
            }
        });
    }

    private LinearLayout createStatusBadge(String status) {
        LinearLayout container = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.setLayoutParams(params);
        container.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        TextView badge = new TextView(this);
        if (status == null) status = "Pending";
        badge.setText(status.toUpperCase());
        badge.setTextSize(11);
        badge.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        badge.setPadding(dp(12), dp(6), dp(12), dp(6));

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(dp(20));

        if (status.equalsIgnoreCase("Approved")) {
            badge.setTextColor(Color.parseColor("#198754"));
            shape.setColor(Color.parseColor("#D1E7DD"));
        } else if (status.equalsIgnoreCase("Rejected")) {
            badge.setTextColor(Color.parseColor("#DC3545"));
            shape.setColor(Color.parseColor("#F8D7DA"));
        } else {
            badge.setTextColor(Color.parseColor("#977000"));
            shape.setColor(Color.parseColor("#FFF3CD"));
        }

        badge.setBackground(shape);
        container.addView(badge);
        return container;
    }

    private void showBeautifulError(String message) {
        View contextView = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(contextView, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(Color.parseColor("#DC3545"));
        snackbar.setTextColor(Color.WHITE);

        TextView textView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        if (textView != null) {
            textView.setTextSize(14);
            textView.setTypeface(null, Typeface.BOLD);
            textView.setMaxLines(3);
        }
        snackbar.show();
    }

    private void showSuccessMessage(String message) {
        View contextView = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(contextView, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(Color.parseColor("#198754"));
        snackbar.setTextColor(Color.WHITE);
        snackbar.show();
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
