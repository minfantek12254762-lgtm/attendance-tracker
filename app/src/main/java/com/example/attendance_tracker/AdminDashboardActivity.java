package com.example.attendance_tracker;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AdminDashboardActivity extends AppCompatActivity {

    EditText searchInput;
    Spinner statusFilter;
    Button searchBtn, clearBtn, createBtn, logoutBtn;
    LinearLayout recordContainer;
    public static ArrayList<Record> records = new ArrayList<>();
    DatabaseReference attendanceRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        attendanceRef = FirebaseDatabase.getInstance("https://attendance-tracking-1f963-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("attendance_records");

        searchInput = findViewById(R.id.searchInput);
        statusFilter = findViewById(R.id.statusFilter);
        searchBtn = findViewById(R.id.searchBtn);
        clearBtn = findViewById(R.id.clearBtn);
        createBtn = findViewById(R.id.createBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        recordContainer = findViewById(R.id.recordContainer);

        setupStatusFilter();

        attendanceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                records.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Record r = data.getValue(Record.class);
                    if (r != null) {
                        r.firebaseKey = data.getKey();
                        records.add(0, r);
                    }
                }
                displayRecords(records);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboardActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });

        searchBtn.setOnClickListener(v -> filterRecords());
        clearBtn.setOnClickListener(v -> {
            searchInput.setText("");
            statusFilter.setSelection(0);
            displayRecords(records);
        });
        createBtn.setOnClickListener(v -> openRecordDialog(null));
        logoutBtn.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void displayRecords(ArrayList<Record> list) {
        if (recordContainer == null) return;
        recordContainer.removeAllViews();
        int[] widths = {40, 120, 100, 100, 100, 60, 60, 60, 80, 80, 80, 100, 100, 120, 120, 300};

        int sequentialId = 1;
        for (Record r : list) {
            r.id = sequentialId++;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(5), dp(5), dp(5), dp(5));

            double totalHoursCalculated = calculateTotalHoursFromTime(r.time);

            double displayPaidHours = totalHoursCalculated > 0 ? totalHoursCalculated : r.paidHours;
            double displayRegHours = displayPaidHours > 8 ? 8.0 : displayPaidHours;
            double displayOtHours = displayPaidHours > 8 ? displayPaidHours - 8.0 : 0.0;

            row.addView(createCell(String.valueOf(r.id), widths[0], "#000000", false));
            row.addView(createCell(r.name, widths[1], "#000000", false));
            row.addView(createCell(r.date, widths[2], "#000000", false));
            row.addView(createCell(r.type, widths[3], "#000000", false));
            row.addView(createCell(r.time, widths[4], "#000000", false));
            row.addView(createCell(String.format("%.1f", displayPaidHours), widths[5], "#000000", false));
            row.addView(createCell(String.format("%.1f", displayRegHours), widths[6], "#000000", false));
            row.addView(createCell(String.format("%.1f", displayOtHours), widths[7], "#000000", false));
            row.addView(createCell("250.00", widths[8], "#000000", false));
            row.addView(createCell(String.format("%.2f", r.bonus), widths[9], "#000000", false));
            row.addView(createCell(String.format("%.2f", r.deductions), widths[10], "#000000", false));

            double liveGrossPay = (displayRegHours * 250.00) + (displayOtHours * (250.00 * 1.25)) + r.bonus - r.deductions;
            row.addView(createCell(String.format("%.2f", liveGrossPay), widths[11], "#1E3A8A", true));

            String statusColor = "#7A5D00";
            if (r.status != null && r.status.equalsIgnoreCase("Approved")) {
                statusColor = "#15803D";
            } else if (r.status != null && r.status.equalsIgnoreCase("Rejected")) {
                statusColor = "#B91C1C";
            }
            row.addView(createCell(r.status, widths[12], statusColor, true));

            row.addView(createCell(r.location, widths[13], "#000000", false));
            row.addView(createCell(r.remarks, widths[14], "#000000", false));
            row.addView(createActionCell(r, widths[15]));
            recordContainer.addView(row);
        }
    }

    private double calculateTotalHoursFromTime(String timeRange) {
        if (timeRange == null || !timeRange.contains(" - ")) return 0.0;
        try {
            String[] parts = timeRange.split(" - ");
            if (parts.length < 2) return 0.0;

            SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date timeIn = format.parse(parts[0].trim());
            Date timeOut = format.parse(parts[1].trim());

            if (timeIn != null && timeOut != null) {
                long diffMilli = timeOut.getTime() - timeIn.getTime();
                if (diffMilli < 0) {
                    diffMilli += 24 * 60 * 60 * 1000;
                }
                double rawHours = (double) diffMilli / (1000 * 60 * 60);
                double computedPaid = rawHours - 1.0;
                return computedPaid > 0 ? computedPaid : 0.0;
            }
        } catch (Exception e) {
            return 0.0;
        }
        return 0.0;
    }

    private LinearLayout createActionCell(Record record, int widthDp) {
        LinearLayout actions = new LinearLayout(this);
        actions.setLayoutParams(new LinearLayout.LayoutParams(dp(widthDp), ViewGroup.LayoutParams.WRAP_CONTENT));
        actions.setGravity(Gravity.CENTER);

        Button edit = makeSmallButton("Edit", "#111827");
        Button delete = makeSmallButton("Delete", "#E50914");
        Button approve = makeSmallButton("Approve", "#15803D");
        Button reject = makeSmallButton("Reject", "#B91C1C");

        edit.setOnClickListener(v -> openRecordDialog(record));

        delete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Record")
                    .setMessage("Are you sure you want to delete this record?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        attendanceRef.child(record.firebaseKey).removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(AdminDashboardActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AdminDashboardActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        approve.setOnClickListener(v -> updateStatusInFirebase(record, "Approved"));
        reject.setOnClickListener(v -> updateStatusInFirebase(record, "Rejected"));

        actions.addView(edit);
        actions.addView(delete);
        actions.addView(approve);
        actions.addView(reject);

        if (record.status != null && (record.status.equalsIgnoreCase("Approved") || record.status.equalsIgnoreCase("Rejected"))) {
            approve.setVisibility(View.GONE);
            reject.setVisibility(View.GONE);
        } else {
            approve.setVisibility(View.VISIBLE);
            reject.setVisibility(View.VISIBLE);
        }

        return actions;
    }

    private Button makeSmallButton(String text, String color) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(8);
        b.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color)));
        b.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(65), dp(35));
        params.setMargins(dp(2), 0, dp(2), 0);
        b.setLayoutParams(params);
        return b;
    }

    private TextView createCell(String text, int widthDp, String hexColor, boolean isBold) {
        TextView tv = new TextView(this);
        tv.setLayoutParams(new LinearLayout.LayoutParams(dp(widthDp), ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setText(text == null ? "" : text);
        tv.setTextSize(11);
        tv.setTextColor(Color.parseColor(hexColor));
        if (isBold) {
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        tv.setGravity(Gravity.CENTER);
        return tv;
    }

    private void updateStatusInFirebase(Record record, String newStatus) {
        attendanceRef.child(record.firebaseKey).child("status").setValue(newStatus).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupStatusFilter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"All", "Pending", "Approved", "Rejected"});
        statusFilter.setAdapter(adapter);
    }

    private void filterRecords() {
        String query = searchInput.getText().toString().trim().toLowerCase();
        String filterStatus = statusFilter.getSelectedItem().toString();
        ArrayList<Record> filteredList = new ArrayList<>();

        for (Record r : records) {
            boolean matchesSearch = query.isEmpty() ||
                    (r.name != null && r.name.toLowerCase().contains(query)) ||
                    (r.date != null && r.date.toLowerCase().contains(query));
            boolean matchesStatus = filterStatus.equals("All") ||
                    (r.status != null && r.status.equalsIgnoreCase(filterStatus));

            if (matchesSearch && matchesStatus) {
                filteredList.add(r);
            }
        }
        displayRecords(filteredList);
    }

    private int dp(int value) { return Math.round(getResources().getDisplayMetrics().density * value); }

    private void openRecordDialog(Record record) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_record, null);
        builder.setView(dialogView);

        EditText employeeName = dialogView.findViewById(R.id.employeeName);
        EditText attendanceDate = dialogView.findViewById(R.id.attendanceDate);
        Spinner leaveType = dialogView.findViewById(R.id.leaveType);
        EditText timeIn = dialogView.findViewById(R.id.timeIn);
        EditText timeOut = dialogView.findViewById(R.id.timeOut);
        EditText location = dialogView.findViewById(R.id.location);
        EditText paidHours = dialogView.findViewById(R.id.paidHours);
        EditText hourlyRate = dialogView.findViewById(R.id.hourlyRate);
        EditText bonus = dialogView.findViewById(R.id.bonus);
        EditText deductions = dialogView.findViewById(R.id.deductions);
        Spinner status = dialogView.findViewById(R.id.status);
        EditText adminRemarks = dialogView.findViewById(R.id.adminRemarks);

        hourlyRate.setText("250.00");
        hourlyRate.setEnabled(false);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Regular Work", "Vacation Leave", "Sick Leave"});
        leaveType.setAdapter(typeAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Pending", "Approved", "Rejected"});
        status.setAdapter(statusAdapter);

        if (record != null) {
            employeeName.setText(record.name);
            attendanceDate.setText(record.date);

            int typePos = typeAdapter.getPosition(record.type);
            if (typePos >= 0) leaveType.setSelection(typePos);

            if (record.time != null && record.time.contains(" - ")) {
                String[] parts = record.time.split(" - ");
                if (parts.length >= 2) {
                    timeIn.setText(parts[0]);
                    timeOut.setText(parts[1]);
                } else if (parts.length == 1) {
                    timeIn.setText(parts[0]);
                }
            } else {
                timeIn.setText(record.time);
            }

            location.setText(record.location);
            paidHours.setText(String.valueOf(record.paidHours));
            bonus.setText(String.valueOf(record.bonus));
            deductions.setText(String.valueOf(record.deductions));

            int statusPos = statusAdapter.getPosition(record.status);
            if (statusPos >= 0) status.setSelection(statusPos);

            adminRemarks.setText(record.remarks);
        }

        builder.setTitle(record == null ? "Create Record" : "Edit Record");
        builder.setPositiveButton(record == null ? "Create" : "Update", (dialog, which) -> {
            String name = employeeName.getText().toString().trim();
            String date = attendanceDate.getText().toString().trim();
            String type = leaveType.getSelectedItem().toString();
            String tIn = timeIn.getText().toString().trim();
            String tOut = timeOut.getText().toString().trim();
            String loc = location.getText().toString().trim();
            String remarks = adminRemarks.getText().toString().trim();
            String stat = status.getSelectedItem().toString();

            double pHours = 0, bns = 0, ded = 0;
            try { pHours = Double.parseDouble(paidHours.getText().toString().trim()); } catch (Exception e){}
            try { bns = Double.parseDouble(bonus.getText().toString().trim()); } catch (Exception e){}
            try { ded = Double.parseDouble(deductions.getText().toString().trim()); } catch (Exception e){}

            double hRate = 250.00;
            double regHours = 0;
            double otHours = 0;

            if (pHours > 8) {
                regHours = 8;
                otHours = pHours - 8;
            } else {
                regHours = pHours;
                otHours = 0;
            }

            double calculatedGross = (regHours * hRate) + (otHours * (hRate * 1.25)) + bns - ded;

            Record r = (record == null) ? new Record() : record;
            r.name = name;
            r.date = date;
            r.type = type;
            r.time = tIn + (tOut.isEmpty() ? "" : " - " + tOut);
            r.location = loc;
            r.paidHours = pHours;
            r.regularHours = regHours;
            r.overtimeHours = otHours;
            r.hourlyRate = hRate;
            r.bonus = bns;
            r.deductions = ded;
            r.status = stat;
            r.remarks = remarks;
            r.grossPay = calculatedGross;

            if (record == null) {
                attendanceRef.push().setValue(r).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AdminDashboardActivity.this, "Create successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                attendanceRef.child(record.firebaseKey).setValue(r).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AdminDashboardActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", null);

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialog -> {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#15803D"));
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#6B7280"));
        });
        alertDialog.show();
    }

    public static class Record {
        public String firebaseKey, name, date, type, time, status, location, remarks;
        public int id;
        public double paidHours, regularHours, overtimeHours, hourlyRate, bonus, deductions, grossPay;
        public Record() {}
    }
}
