package com.example.jadwalin.ui.kalender;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jadwalin.R;
import com.example.jadwalin.data.model.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class KalenderFragment extends Fragment {

    private TextView tvMonthYear;
    private ImageButton btnPreviousMonth, btnNextMonth;
    private RecyclerView rvCalendarGrid;
    private LinearLayout dynamicTaskContainer;
    private FloatingActionButton fabAddTask;

    private Calendar currentCalendar;
    private String selectedDateStr; // Format: yyyy-MM-dd
    private HashSet<String> datesWithTasksSet = new HashSet<>(); // Titik penanda tanggal memiliki tugas
    private final List<Task> currentDayTaskList = new ArrayList<>(); // List tugas hari terpilih

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference taskCollection = db.collection("tasks");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_kalender, container, false);

        // Inisialisasi Komponen UI
        tvMonthYear = view.findViewById(R.id.tvMonthYear);
        btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        rvCalendarGrid = view.findViewById(R.id.rvCalendarGrid);
        dynamicTaskContainer = view.findViewById(R.id.dynamicTaskContainer);
        fabAddTask = view.findViewById(R.id.fabAddTask);

        currentCalendar = Calendar.getInstance();

        // Default tanggal terpilih awal saat dibuka (Hari ini)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDateStr = sdf.format(currentCalendar.getTime());

        rvCalendarGrid.setLayoutManager(new GridLayoutManager(getContext(), 7));

        // Navigasi Ganti Bulan Berjalan
        btnPreviousMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            fetchTasksForCurrentMonth();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            fetchTasksForCurrentMonth();
        });

        // Tombol tambah tugas baru
        if (fabAddTask != null) {
            fabAddTask.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AddTaskActivity.class);
                startActivity(intent);
            });
        }

        fetchTasksForCurrentMonth();

        return view;
    }

    /**
     * Membaca seluruh data untuk menandai tanggal mana saja yang memiliki tugas (tanda dot)
     */
    private void fetchTasksForCurrentMonth() {
        taskCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            datesWithTasksSet.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String dueDate = doc.getString("dueDate");
                if (dueDate != null && !dueDate.isEmpty()) {
                    datesWithTasksSet.add(dueDate);
                }
            }
            updateCalendarUi();
            loadTaskListForSelectedDate(); // Langsung muat tugas untuk tanggal aktif
        });
    }

    private void updateCalendarUi() {
        if (getContext() == null) return;

        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonthYear.setText(monthYearFormat.format(currentCalendar.getTime()));

        ArrayList<String> daysInMonth = new ArrayList<>();
        Calendar calItem = (Calendar) currentCalendar.clone();
        calItem.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = calItem.get(Calendar.DAY_OF_WEEK) - 1;

        for (int i = 0; i < firstDayOfWeek; i++) {
            daysInMonth.add("");
        }

        int maxDay = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= maxDay; i++) {
            daysInMonth.add(String.valueOf(i));
        }

        CalendarAdapter adapter = new CalendarAdapter(daysInMonth, currentCalendar, selectedDateStr, datesWithTasksSet, date -> {
            selectedDateStr = date;
            updateCalendarUi();
            loadTaskListForSelectedDate(); // Trigger muat ulang saat tanggal di klik
        });
        rvCalendarGrid.setAdapter(adapter);
    }

    /**
     * Mengambil tugas dari Firebase yang HANYA cocok dengan tanggal yang di-klik
     */
    private void loadTaskListForSelectedDate() {
        if (dynamicTaskContainer == null) return;

        taskCollection.whereEqualTo("dueDate", selectedDateStr).get().addOnSuccessListener(queryDocumentSnapshots -> {
            currentDayTaskList.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Task task = doc.toObject(Task.class);
                task.setId(doc.getId());
                currentDayTaskList.add(task);
            }
            displayTasks(); // Tampilkan langsung tanpa filter kategori
        }).addOnFailureListener(e -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Gagal memuat tugas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Menampilkan daftar tugas di bawah kalender tanpa penyaringan kategori
     */
    private void displayTasks() {
        dynamicTaskContainer.removeAllViews(); // Bersihkan list lama

        if (getContext() == null) return;

        for (Task task : currentDayTaskList) {
            View taskView = LayoutInflater.from(getContext()).inflate(R.layout.item_task_dynamic, dynamicTaskContainer, false);

            TextView tvTaskTitle = taskView.findViewById(R.id.tvDynamicTaskTitle);
            CheckBox checkTask = taskView.findViewById(R.id.checkDynamicTask);

            tvTaskTitle.setText(task.getTitle());
            checkTask.setChecked(task.isCompleted());

            // Aksi Checkbox real-time terhubung ke Firebase
            checkTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
                taskCollection.document(task.getId()).update("isCompleted", isChecked)
                        .addOnSuccessListener(aVoid -> {
                            task.setCompleted(isChecked);
                            Toast.makeText(getContext(), "Status tugas diperbarui", Toast.LENGTH_SHORT).show();
                        });
            });

            dynamicTaskContainer.addView(taskView);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data otomatis jika kembali dari form tambah tugas
        fetchTasksForCurrentMonth();
    }
}