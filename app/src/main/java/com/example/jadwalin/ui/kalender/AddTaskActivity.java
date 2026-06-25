package com.example.jadwalin.ui.kalender;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jadwalin.R;
import com.example.jadwalin.data.model.Task;
import com.example.jadwalin.data.repository.TaskRepository;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;

public class AddTaskActivity extends AppCompatActivity {
    private EditText etTaskTitle, etTaskDescription;
    private TextView tvDueDateValue, tvReminderValue;
    private ChipGroup chipGroupCategory;
    private Button btnSaveTask;
    private TaskRepository taskRepository;
    private String selectedCategory = "Pribadi";
    private String selectedTime = "Tidak";

    // 🟢 AMAN: Gunakan variabel global ini tunggal untuk membedakan mode Tambah vs Edit
    private String taskId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        taskRepository = new TaskRepository();

        ImageView btnBack = findViewById(R.id.btnBack);
        etTaskTitle = findViewById(R.id.etTaskTitle);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        chipGroupCategory = findViewById(R.id.chipGroupCategory);
        tvDueDateValue = findViewById(R.id.tvDueDateValue);
        tvReminderValue = findViewById(R.id.tvReminderValue);
        RelativeLayout layoutDueDate = findViewById(R.id.layoutDueDate);
        RelativeLayout layoutReminderTime = findViewById(R.id.layoutReminderTime);
        btnSaveTask = findViewById(R.id.btnSaveTask);

        btnBack.setOnClickListener(v -> finish());

        chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipNone) {
                    selectedCategory = "Tidak ada";
                } else if (checkedId == R.id.chipCollege) {
                    selectedCategory = "Tugas Kuliah";
                } else if (checkedId == R.id.chipPersonal) {
                    selectedCategory = "Pribadi";
                }
            }
        });

        layoutDueDate.setOnClickListener(v -> showDatePicker());
        layoutReminderTime.setOnClickListener(v -> showTimePicker());
        btnSaveTask.setOnClickListener(v -> saveTaskToFirebase());

        // FITUR EDIT: Tangkap data kiriman dari JadwalFragment
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("taskId")) {
            taskId = intent.getStringExtra("taskId");
            etTaskTitle.setText(intent.getStringExtra("title"));
            tvDueDateValue.setText(intent.getStringExtra("dueDate"));

            // Atur otomatis pilihan Chip berdasarkan kategori data yang mau diedit
            String kategoriLama = intent.getStringExtra("kategori");
            if (kategoriLama != null) {
                selectedCategory = kategoriLama;
                if (kategoriLama.equals("Tidak ada")) {
                    chipGroupCategory.check(R.id.chipNone);
                } else if (kategoriLama.equals("Tugas Kuliah")) {
                    chipGroupCategory.check(R.id.chipCollege);
                } else if (kategoriLama.equals("Pribadi")) {
                    chipGroupCategory.check(R.id.chipPersonal);
                }
            }
            // Ganti teks tombol utama menjadi Perbarui
            btnSaveTask.setText("Perbarui Tugas");
        }
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = selectedYear + "-" +
                            String.format("%02d", (selectedMonth + 1)) + "-" +
                            String.format("%02d", selectedDay);
                    tvDueDateValue.setText(formattedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    selectedTime = String.format("%02d", selectedHour) + ":" + String.format("%02d", selectedMinute);
                    tvReminderValue.setText(selectedTime);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void saveTaskToFirebase() {
        String title = etTaskTitle.getText().toString().trim();
        String description = etTaskDescription.getText().toString().trim();
        String dueDate = tvDueDateValue.getText().toString().trim();

        if (title.isEmpty()) {
            etTaskTitle.setError("Nama tugas tidak boleh kosong!");
            return;
        }

        btnSaveTask.setEnabled(false);

        // Ambil ID Pengguna yang sedang login saat ini
        String currentUserId = "";
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // Jika taskId null (artinya buat baru), generate ID baru. Jika tidak, pakai ID lama.
        if (this.taskId == null) {
            this.taskId = FirebaseFirestore.getInstance().collection("tasks").document().getId();
        }

        // Membuat Objek Task dengan urutan konstruktor baru
        Task task = new Task(this.taskId, currentUserId, title, selectedCategory, dueDate, false);

        taskRepository.addTask(task,
                aVoid -> {
                    String pesanSukses = (getIntent().hasExtra("taskId")) ? "Tugas berhasil diperbarui!" : "Tugas berhasil disimpan!";
                    Toast.makeText(AddTaskActivity.this, pesanSukses, Toast.LENGTH_SHORT).show();
                    finish();
                },
                e -> {
                    btnSaveTask.setEnabled(true);
                    Toast.makeText(AddTaskActivity.this, "Gagal memproses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}