package com.example.attendance_tracker;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
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

    EditText searchInput;
    Spinner statusFilter;
    Button searchBtn, clearBtn, createBtn, logoutBtn;
    LinearLayout recordContainer;

    ArrayList<Record> records = new ArrayList<>();
    int nextId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        searchInput = findViewById(R.id.searchInput);
        statusFilter = findViewById(R.id.statusFilter);
        searchBtn = findViewById(R.id.searchBtn);
        clearBtn = findViewById(R.id.clearBtn);
        createBtn = findViewById(R.id.createBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        recordContainer = findViewById(R.id.recordContainer);

        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"All", "Pending", "Approved", "Rejected"}
        );

        statusFilter.setAdapter(filterAdapter);

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

    private void filterRecords() {
        String search = searchInput.getText().toString().trim().toLowerCase();
        String status = statusFilter.getSelectedItem().toString();

        ArrayList<Record> filtered = new ArrayList<>();

        for (Record record : records) {
            boolean matchesSearch =
                    record.name.toLowerCase().contains(search)
                            || record.date.toLowerCase().contains(search)
                            || record.type.toLowerCase().contains(search)
                            || record.status.toLowerCase().contains(search);

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
            empty.setPadding(20, 30, 20, 30);
            recordContainer.addView(empty);
            return;
        }

        for (Record record : list) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(18, 18, 18, 18);
            card.setBackgroundResource(R.drawable.card_bg);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            cardParams.setMargins(0, 0, 0, 14);
            card.setLayoutParams(cardParams);

            TextView details = new TextView(this);
            details.setText(
                    "ID: " + record.id +
                            "\nName: " + record.name +
                            "\nDate: " + record.date +
                            "\nType: " + record.type +
                            "\nTime: " + record.time +
                            "\nPaid Hours: " + format(record.paidHours) +
                            "\nRegular Hours: " + format(record.regularHours) +
                            "\nOT Hours: " + format(record.overtimeHours) +
                            "\nHourly Rate: PHP " + format(record.hourlyRate) +
                            "\nBonus: PHP " + format(record.bonus) +
                            "\nDeductions: PHP " + format(record.deductions) +
                            "\nGross Pay: PHP " + format(record.grossPay) +
                            "\nStatus: " + record.status +
                            "\nRemarks: " + record.remarks
            );

            details.setTextColor(Color.parseColor("#0F172A"));
            details.setTextSize(14);

            LinearLayout actions = new LinearLayout(this);
            actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.setPadding(0, 16, 0, 0);

            Button approve = makeButton("Approve", "#15803D");
            Button reject = makeButton("Reject", "#B91C1C");
            Button edit = makeButton("Edit", "#111827");
            Button delete = makeButton("Delete", "#E50914");

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

            card.addView(details);
            card.addView(actions);
            recordContainer.addView(card);
        }
    }

    private Button makeButton(String text, String color) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(12);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color)));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );

        params.setMargins(4, 0, 4, 0);
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
        EditText paidInput = view.findViewById(R.id.paidHours);
        EditText rateInput = view.findViewById(R.id.hourlyRate);
        EditText bonusInput = view.findViewById(R.id.bonus);
        EditText deductInput = view.findViewById(R.id.deductions);
        Spinner statusInput = view.findViewById(R.id.status);
        EditText remarksInput = view.findViewById(R.id.adminRemarks);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Regular Work", "Vacation Leave", "Sick Leave"}
        );

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Pending", "Approved", "Rejected"}
        );

        typeInput.setAdapter(typeAdapter);
        statusInput.setAdapter(statusAdapter);

        if (existingRecord != null) {
            nameInput.setText(existingRecord.name);
            dateInput.setText(existingRecord.date);

            if (existingRecord.type.equals("Vacation Leave")) {
                typeInput.setSelection(1);
            } else if (existingRecord.type.equals("Sick Leave")) {
                typeInput.setSelection(2);
            } else {
                typeInput.setSelection(0);
            }

            if (existingRecord.time.contains(" - ")) {
                String[] timeParts = existingRecord.time.split(" - ");
                timeInInput.setText(timeParts[0]);
                timeOutInput.setText(timeParts[1]);
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
                String remarks = remarksInput.getText().toString().trim();
                String status = statusInput.getSelectedItem().toString();

                if (name.isEmpty() || date.isEmpty()) {
                    Toast.makeText(this, "Name and date are required", Toast.LENGTH_SHORT).show();
                    return;
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
                    time = timeIn + " - " + timeOut;
                }

                if (remarks.isEmpty()) {
                    remarks = "-";
                }

                if (existingRecord == null) {
                    records.add(new Record(
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
                    existingRecord.remarks = remarks;
                }

                filterRecords();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private double calculateGrossPay(double regular, double overtime, double rate, double bonus, double deductions) {
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

    static class Record {
        int id;
        String name, date, type, time, status, remarks;
        double paidHours, regularHours, overtimeHours, hourlyRate, bonus, deductions, grossPay;

        Record(int id, String name, String date, String type, String time, double paidHours, double regularHours, double overtimeHours, double hourlyRate, double bonus, double deductions, String status, String remarks) {
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
            this.remarks = remarks;
        }
    }
}
