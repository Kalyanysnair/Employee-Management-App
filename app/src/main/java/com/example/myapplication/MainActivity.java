
package com.example.myapplication;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText etName, etDepartment, etPhone, etSearch;
    Button btnAdd, btnSearch, btnView;
    ListView lvEmployees;

    DBHelper dbHelper;

    ArrayList<String> employeeList = new ArrayList<>();
    ArrayList<Integer> employeeIds = new ArrayList<>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        etDepartment = findViewById(R.id.etDepartment);
        etPhone = findViewById(R.id.etPhone);
        etSearch = findViewById(R.id.etSearch);

        btnAdd = findViewById(R.id.btnAdd);
        btnSearch = findViewById(R.id.btnSearch);
        btnView = findViewById(R.id.btnView);
        lvEmployees = findViewById(R.id.lvEmployees);

        dbHelper = new DBHelper(this);

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                employeeList
        );

        lvEmployees.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> addEmployee());

        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();

            if (keyword.isEmpty()) {
                Toast.makeText(this, "Enter a name to search",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            loadEmployees(keyword);
        });

        btnView.setOnClickListener(v -> {
            etSearch.setText("");
            loadEmployees("");
        });

        lvEmployees.setOnItemClickListener((parent, view, position, id) ->
                showEmployeeOptions(employeeIds.get(position)));

        loadEmployees("");
    }

    private void addEmployee() {
        String name = etName.getText().toString().trim();
        String department = etDepartment.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty() || department.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.insertEmployee(name, department, phone)) {
            Toast.makeText(this, "Employee added successfully",
                    Toast.LENGTH_SHORT).show();

            etName.setText("");
            etDepartment.setText("");
            etPhone.setText("");
            etSearch.setText("");

            loadEmployees("");
        } else {
            Toast.makeText(this, "Unable to add employee",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void loadEmployees(String keyword) {
        employeeList.clear();
        employeeIds.clear();

        Cursor cursor;

        if (keyword.isEmpty()) {
            cursor = dbHelper.getEmployees("");
        } else {
            cursor = dbHelper.getReadableDatabase().rawQuery(
                    "SELECT * FROM employees " +
                            "WHERE name LIKE ? " +
                            "ORDER BY CASE " +
                            "WHEN name = ? COLLATE NOCASE THEN 0 " +
                            "WHEN name LIKE ? COLLATE NOCASE THEN 1 " +
                            "ELSE 2 END, id DESC",
                    new String[]{
                            "%" + keyword + "%",
                            keyword,
                            keyword + "%"
                    }
            );
        }

        try {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow("id"));

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow("name"));

                String department = cursor.getString(
                        cursor.getColumnIndexOrThrow("department"));

                String phone = cursor.getString(
                        cursor.getColumnIndexOrThrow("phone"));

                employeeIds.add(id);

                employeeList.add(
                        name +
                                "\nDepartment: " + department +
                                "\nPhone: " + phone +
                                "\nEmployee ID: " + id +
                                "\nTap to Update or Delete"
                );
            }
        } finally {
            cursor.close();
        }

        adapter.notifyDataSetChanged();

        if (employeeList.isEmpty()) {
            Toast.makeText(this, "No employees found",
                    Toast.LENGTH_SHORT).show();
        }

        lvEmployees.setSelection(0);
    }

    private void showEmployeeOptions(int id) {
        new AlertDialog.Builder(this)
                .setTitle("Employee Options")
                .setItems(
                        new String[]{"Update Employee", "Delete Employee"},
                        (dialog, choice) -> {
                            if (choice == 0) {
                                showUpdateDialog(id);
                            } else {
                                confirmDelete(id);
                            }
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showUpdateDialog(int id) {
        Cursor cursor = dbHelper.getEmployees("");
        String name = "";
        String department = "";
        String phone = "";
        boolean found = false;

        try {
            while (cursor.moveToNext()) {
                if (cursor.getInt(
                        cursor.getColumnIndexOrThrow("id")) == id) {

                    name = cursor.getString(
                            cursor.getColumnIndexOrThrow("name"));

                    department = cursor.getString(
                            cursor.getColumnIndexOrThrow("department"));

                    phone = cursor.getString(
                            cursor.getColumnIndexOrThrow("phone"));

                    found = true;
                    break;
                }
            }
        } finally {
            cursor.close();
        }

        if (!found) {
            Toast.makeText(this, "Employee not found",
                    Toast.LENGTH_SHORT).show();
            loadEmployees("");
            return;
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        int padding = (int) (20 * getResources()
                .getDisplayMetrics().density);
        layout.setPadding(padding, 0, padding, 0);

        EditText nameInput = new EditText(this);
        nameInput.setHint("Employee Name");
        nameInput.setSingleLine(true);
        nameInput.setText(name);
        layout.addView(nameInput);

        EditText departmentInput = new EditText(this);
        departmentInput.setHint("Department");
        departmentInput.setSingleLine(true);
        departmentInput.setText(department);
        layout.addView(departmentInput);

        EditText phoneInput = new EditText(this);
        phoneInput.setHint("Phone Number");
        phoneInput.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneInput.setSingleLine(true);
        phoneInput.setText(phone);
        layout.addView(phoneInput);

        new AlertDialog.Builder(this)
                .setTitle("Update Employee")
                .setView(layout)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newName = nameInput.getText().toString().trim();
                    String newDepartment = departmentInput.getText()
                            .toString().trim();
                    String newPhone = phoneInput.getText().toString().trim();

                    if (newName.isEmpty() || newDepartment.isEmpty()
                            || newPhone.isEmpty()) {
                        Toast.makeText(this, "Please fill in all fields",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (dbHelper.updateEmployee(
                            id, newName, newDepartment, newPhone)) {

                        Toast.makeText(this,
                                "Employee updated successfully",
                                Toast.LENGTH_SHORT).show();

                        loadEmployees(
                                etSearch.getText().toString().trim());
                    } else {
                        Toast.makeText(this, "Update failed",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(int id) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Employee")
                .setMessage("Are you sure you want to delete this employee?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (dbHelper.deleteEmployee(id)) {
                        Toast.makeText(this,
                                "Employee deleted successfully",
                                Toast.LENGTH_SHORT).show();

                        loadEmployees(
                                etSearch.getText().toString().trim());
                    } else {
                        Toast.makeText(this, "Delete failed",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
