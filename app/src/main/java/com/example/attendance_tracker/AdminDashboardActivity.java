package com.example.attendance_tracker;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
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

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class AdminDashboardActivity extends AppCompatActivity {

    TextView adminTitle;
    EditText searchInput;
    Spinner statusFilter;
    Button searchBtn, clearBtn, createBtn, logoutBtn;
    LinearLayout recordContainer;

    public static ArrayList<Record> records = new ArrayList<>();
    public static int nextId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        adminTitle = findViewById(R.id.adminTitle);
        searchInput = findViewById(R.id.searchInput);
        statusFilter = findViewById(R.id.statusFilter);
        searchBtn = findViewById(R.id.searchBtn);
        clearBtn = findViewById(R.id.clearBtn);
        createBtn = findViewById(R.id.createBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        recordContainer = findViewById(R.id.recordContainer);

        setAdminTitleColor();
        setupStatusFilter();

        displayRecords(records);

        searchBtn.setOnClickListener(v -> filterRecords());

        clearBtn.setOnClickListener(v -> {
            searchInput.setText("");
            statusFilter.setSelection(0);
            displayRecords(records);
        });

        createBtn.setOnClickListener(v -> openRecordDialog(null));

        logoutBtn.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        filterRecords();
    }

    private void setAdminTitleColor() {
        String titleText = "Admin Management Panel";
        SpannableString span = new SpannableString(titleText);

        int start = "Admin ".length();

        span.setSpan(
                new StyleSpan(Typeface.BOLD),
                0,
                titleText.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        span.setSpan(
                new ForegroundColorSpan(Color.parseColor("#1B5E20")),
                start,
                titleText.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        adminTitle.setText(span);
    }

    private void setupStatusFilter() {
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"All", "Pending", "Approved", "Rejected"}
        );

        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusFilter.setAdapter(filterAdapter);
    }

    public static void addUserSubmittedRecord(
            String name,
            String date,
            String type,
            String time,
            double paidHours,
            double overtimeHours,
            double grossPay,
            String status,
            String location,
            String remarks
    ) {
        double hourlyRate = 250.00;
        double regularHours = Math.min(paidHours, 8);
        double bonus = 0;
        double deductions = 0;

        Record record = new Record(
                nextId++,
                name,
                date,
                type,
                time,
                paidHours,
                regularHours,
                overtimeHours,
                hourlyRate,
                bonus,
                deductions,
                status,
                location,
                remarks
        );

        record.grossPay = grossPay;
        records.add(0, record);
    }

    private void filterRecords() {
        String search = searchInput.getText().toString().trim().toLowerCase();
        String status = statusFilter.getSelectedItem() == null
                ? "All"
                : statusFilter.getSelectedItem().toString();

        ArrayList<Record> filtered = new ArrayList<>();

        for (Record record : records) {
            boolean matchesSearch =
                    record.name.toLowerCase().contains(search)
                            || record.date.toLowerCase().contains(search)
                            || record.type.toLowerCase().contains(search)
                            || record.status.toLowerCase().contains(search)
                            || record.location.toLowerCase().contains(search)
                            || record.remarks.toLowerCase().contains(search);

            boolean matchesStatus = status.equals("All") || record.status.equals(status);

            if (matchesSearch && matchesStatus) {
                filtered.add(record);
            }
        }

        displayRecords(filtered);
    }

    private void displayRecords(ArrayList<Record> list) {
        recordContainer.removeAllViews();

        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No attendance or leave records found");
            empty.setTextColor(Color.parseColor("#64748B"));
            empty.setTextSize(14);
            empty.setPadding(dp(8), dp(20), dp(8), dp(20));
            recordContainer.addView(empty);
            return;
        }

        for (Record record : list) {
            LinearLayout wrapper = new LinearLayout(this);
            wrapper.setOrientation(LinearLayout.VERTICAL);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(10), 0, dp(10));

            row.addView(createCell(String.valueOf(record.id), 50, false));
            row.addView(createCell(record.name, 110, true));
            row.addView(createCell(record.date, 110, false));
            row.addView(createCell(record.type, 130, false));
            row.addView(createCell(record.time, 150, false));
            row.addView(createCell(format(record.paidHours), 70, false));
            row.addView(createCell(format(record.regularHours), 70, false));
            row.addView(createCell(format(record.overtimeHours), 60, false));
            row.addView(createCell("PHP " + format(record.hourlyRate), 90, false));
            row.addView(createCell("PHP " + format(record.bonus), 90, false));
            row.addView(createCell("PHP " + format(record.deductions), 90, false));
            row.addView(createCell("PHP " + format(record.grossPay), 110, true));
            row.addView(createStatusCell(record.status, 100));
            row.addView(createLocationCell(record.location, 170));
            row.addView(createCell(record.remarks, 140, false));
            row.addView(createActionCell(record, 230));

            View divider = new View(this);
            divider.setBackgroundColor(Color.parseColor("#E2E8F0"));

            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(1)
            );

            divider.setLayoutParams(dividerParams);

            wrapper.addView(row);
            wrapper.addView(divider);

            recordContainer.addView(wrapper);
        }
    }

    private TextView createCell(String text, int widthDp, boolean bold) {
        TextView textView = new TextView(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dp(widthDp),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        textView.setLayoutParams(params);
        textView.setText(text);
        textView.setTextColor(Color.parseColor("#0F172A"));
        textView.setTextSize(12);
        textView.setPadding(0, 0, dp(8), 0);
        textView.setSingleLine(false);

        if (bold) {
            textView.setTypeface(null, Typeface.BOLD);
        }

        return textView;
    }

    private LinearLayout createStatusCell(String status, int widthDp) {
        LinearLayout container = new LinearLayout(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dp(widthDp),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        container.setLayoutParams(params);
        container.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        TextView badge = new TextView(this);
        badge.setText(status);
        badge.setTextSize(12);
        badge.setTypeface(null, Typeface.BOLD);
        badge.setPadding(dp(9), dp(5), dp(9), dp(5));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(100));

        if (status.equalsIgnoreCase("Approved")) {
            bg.setColor(Color.parseColor("#DCFCE7"));
            badge.setTextColor(Color.parseColor("#15803D"));
        } else if (status.equalsIgnoreCase("Rejected")) {
            bg.setColor(Color.parseColor("#FEE2E2"));
            badge.setTextColor(Color.parseColor("#B91C1C"));
        } else {
            bg.setColor(Color.parseColor("#FEF3C7"));
            badge.setTextColor(Color.parseColor("#B45309"));
        }

        badge.setBackground(bg);
        container.addView(badge);

        return container;
    }

    private LinearLayout createLocationCell(String location, int widthDp) {
        LinearLayout container = new LinearLayout(this);

        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                dp(widthDp),
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView locationBadge = new TextView(this);
        locationBadge.setText(location == null || location.trim().isEmpty() ? "-" : location);
        locationBadge.setTextSize(11);
        locationBadge.setTypeface(null, Typeface.BOLD);
        locationBadge.setTextColor(Color.parseColor("#B45309"));
        locationBadge.setPadding(dp(9), dp(5), dp(9), dp(5));

        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(Color.parseColor("#FEF3C7"));
        badgeBg.setCornerRadius(dp(100));

        locationBadge.setBackground(badgeBg);

        TextView locationNote = new TextView(this);
        locationNote.setText("User submitted location");
        locationNote.setTextSize(11);
        locationNote.setTextColor(Color.parseColor("#334155"));
        locationNote.setPadding(0, dp(5), 0, 0);

        Button viewMapBtn = new Button(this);
        viewMapBtn.setText("View Map");
        viewMapBtn.setTextSize(11);
        viewMapBtn.setTransformationMethod(null);
        viewMapBtn.setTextColor(Color.WHITE);
        viewMapBtn.setTypeface(null, Typeface.BOLD);
        viewMapBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#475569")));

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                dp(90),
                dp(38)
        );

        btnParams.setMargins(0, dp(6), 0, 0);
        viewMapBtn.setLayoutParams(btnParams);

        viewMapBtn.setOnClickListener(v -> {
            String displayLocation = location == null || location.trim().isEmpty() ? "-" : location;
            Toast.makeText(this, "Location: " + displayLocation, Toast.LENGTH_LONG).show();
        });

        container.addView(locationBadge);
        container.addView(locationNote);
        container.addView(viewMapBtn);

        return container;
    }

    private LinearLayout createActionCell(Record record, int widthDp) {
        LinearLayout actions = new LinearLayout(this);

        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setLayoutParams(new LinearLayout.LayoutParams(
                dp(widthDp),
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        Button approve = makeSmallButton("Approve", "#15803D");
        Button reject = makeSmallButton("Reject", "#B91C1C");
        Button edit = makeSmallButton("Edit", "#111827");
        Button delete = makeSmallButton("Delete", "#E50914");

        approve.setOnClickListener(v -> {
            record.status = "Approved";
            record.remarks = "Approved by admin";
            filterRecords();
        });

        reject.setOnClickListener(v -> {
            record.status = "Rejected";
            record.remarks = "Rejected by admin";
            filterRecords();
        });

        edit.setOnClickListener(v -> openRecordDialog(record));

        delete.setOnClickListener(v -> {
            records.remove(record);
            filterRecords();
        });

        if (record.status.equals("Pending")) {
            actions.addView(approve);
            actions.addView(reject);
        }

        actions.addView(edit);
        actions.addView(delete);

        return actions;
    }

    private Button makeSmallButton(String text, String color) {
        Button button = new Button(this);

        button.setText(text);
        button.setTextSize(10);
        button.setTransformationMethod(null);
        button.setTextColor(Color.WHITE);
        button.setTypeface(null, Typeface.BOLD);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color)));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dp(70),
                dp(38)
        );

        params.setMargins(dp(3), 0, dp(3), 0);
        button.setLayoutParams(params);

        return button;
    }

    private void openRecordDialog(Record existingRecord) {
        View view = getLayoutInflater().inflate(R.layout.dialog_record, null);

        EditText nameInput = view.findViewById(R.id.employeeName);
        EditText dateInput = view.findViewById(R.id.attendanceDate);
        Spinner typeInput = view.findViewById(R.id.leaveType);
        EditText timeInInput = view.findViewById(R.id.timeIn);
        EditText timeOutInput = view.findViewById(R.id.timeOut);
        EditText locationInput = view.findViewById(R.id.location);
        EditText paidInput = view.findViewById(R.id.paidHours);
        EditText rateInput = view.findViewById(R.id.hourlyRate);
        EditText bonusInput = view.findViewById(R.id.bonus);
        EditText deductInput = view.findViewById(R.id.deductions);
        Spinner statusInput = view.findViewById(R.id.status);
        EditText remarksInput = view.findViewById(R.id.adminRemarks);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Regular Work", "Vacation Leave", "Sick Leave"}
        );

        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeInput.setAdapter(typeAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Pending", "Approved", "Rejected"}
        );

        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusInput.setAdapter(statusAdapter);

        if (existingRecord != null) {
            nameInput.setText(existingRecord.name);
            dateInput.setText(existingRecord.date);
            locationInput.setText(existingRecord.location);

            if (existingRecord.type.equals("Vacation Leave")) {
                typeInput.setSelection(1);
            } else if (existingRecord.type.equals("Sick Leave")) {
                typeInput.setSelection(2);
            } else {
                typeInput.setSelection(0);
            }

            if (existingRecord.time.contains(" - ")) {
                String[] timeParts = existingRecord.time.split(" - ");

                if (timeParts.length == 2) {
                    timeInInput.setText(timeParts[0]);
                    timeOutInput.setText(timeParts[1]);
                }
            }

            paidInput.setText(format(existingRecord.paidHours));
            rateInput.setText(format(existingRecord.hourlyRate));
            bonusInput.setText(format(existingRecord.bonus));
            deductInput.setText(format(existingRecord.deductions));

            if (existingRecord.status.equals("Approved")) {
                statusInput.setSelection(1);
            } else if (existingRecord.status.equals("Rejected")) {
                statusInput.setSelection(2);
            } else {
                statusInput.setSelection(0);
            }

            remarksInput.setText(existingRecord.remarks);
        } else {
            rateInput.setText("250");
            bonusInput.setText("0");
            deductInput.setText("0");
            paidInput.setText("0");
            locationInput.setText("");
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(existingRecord == null ? "Create Record" : "Edit Record")
                .setView(view)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button save = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            save.setOnClickListener(v -> {
                String name = nameInput.getText().toString().trim();
                String date = dateInput.getText().toString().trim();
                String type = typeInput.getSelectedItem().toString();
                String timeIn = timeInInput.getText().toString().trim();
                String timeOut = timeOutInput.getText().toString().trim();
                String location = locationInput.getText().toString().trim();
                String remarks = remarksInput.getText().toString().trim();
                String status = statusInput.getSelectedItem().toString();

                if (name.isEmpty() || date.isEmpty()) {
                    Toast.makeText(this, "Name and date are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (location.isEmpty()) {
                    location = "-";
                }

                double paid = parseDouble(paidInput.getText().toString());
                double rate = parseDouble(rateInput.getText().toString());
                double bonus = parseDouble(bonusInput.getText().toString());
                double deductions = parseDouble(deductInput.getText().toString());

                double regular = Math.min(paid, 8);
                double overtime = Math.max(0, paid - 8);
                double gross = calculateGrossPay(regular, overtime, rate, bonus, deductions);

                String time = "-";

                if (type.equals("Regular Work")) {
                    if (!timeIn.isEmpty() && !timeOut.isEmpty()) {
                        time = timeIn + " - " + timeOut;
                    }
                }

                if (remarks.isEmpty()) {
                    remarks = "-";
                }

                if (existingRecord == null) {
                    records.add(0, new Record(
                            nextId++,
                            name,
                            date,
                            type,
                            time,
                            paid,
                            regular,
                            overtime,
                            rate,
                            bonus,
                            deductions,
                            status,
                            location,
                            remarks
                    ));
                } else {
                    existingRecord.name = name;
                    existingRecord.date = date;
                    existingRecord.type = type;
                    existingRecord.time = time;
                    existingRecord.paidHours = paid;
                    existingRecord.regularHours = regular;
                    existingRecord.overtimeHours = overtime;
                    existingRecord.hourlyRate = rate;
                    existingRecord.bonus = bonus;
                    existingRecord.deductions = deductions;
                    existingRecord.grossPay = gross;
                    existingRecord.status = status;
                    existingRecord.location = location;
                    existingRecord.remarks = remarks;
                }

                filterRecords();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private double calculateGrossPay(
            double regular,
            double overtime,
            double rate,
            double bonus,
            double deductions
    ) {
        double regularPay = regular * rate;
        double overtimePay = overtime * (rate * 1.25);

        return regularPay + overtimePay + bonus - deductions;
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private String format(double value) {
        return String.format(Locale.getDefault(), "%.2f", value);
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }

    public static class Record {
        int id;
        String name;
        String date;
        String type;
        String time;
        String status;
        String location;
        String remarks;

        double paidHours;
        double regularHours;
        double overtimeHours;
        double hourlyRate;
        double bonus;
        double deductions;
        double grossPay;

        Record(
                int id,
                String name,
                String date,
                String type,
                String time,
                double paidHours,
                double regularHours,
                double overtimeHours,
                double hourlyRate,
                double bonus,
                double deductions,
                String status,
                String location,
                String remarks
        ) {
            this.id = id;
            this.name = name;
            this.date = date;
            this.type = type;
            this.time = time;
            this.paidHours = paidHours;
            this.regularHours = regularHours;
            this.overtimeHours = overtimeHours;
            this.hourlyRate = hourlyRate;
            this.bonus = bonus;
            this.deductions = deductions;
            this.grossPay = regularHours * hourlyRate + overtimeHours * (hourlyRate * 1.25) + bonus - deductions;
            this.status = status;
            this.location = location;
            this.remarks = remarks;
        }
    }
}
