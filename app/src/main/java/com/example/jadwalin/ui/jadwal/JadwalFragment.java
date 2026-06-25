package com.example.jadwalin.ui.jadwal;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jadwalin.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JadwalFragment extends Fragment {

    private RecyclerView rvJadwalTugas;
    private TaskAdapter taskAdapter;

    // Jadikan variabel tombol global agar bisa diakses oleh fungsi pembaru warna
    private Button btnAll, btnPersonal, btnCollege;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentUserId = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jadwal, container, false);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        rvJadwalTugas = view.findViewById(R.id.rvJadwalTugas);
        if (rvJadwalTugas != null) {
            rvJadwalTugas.setLayoutManager(new LinearLayoutManager(getActivity()));
            taskAdapter = new TaskAdapter(new ArrayList<>());
            rvJadwalTugas.setAdapter(taskAdapter);
        }

        // Hubungkan ke variabel global kelas
        btnAll = view.findViewById(R.id.btnAll);
        btnPersonal = view.findViewById(R.id.btnPersonal);
        btnCollege = view.findViewById(R.id.btnCollege);

        if (btnAll != null) {
            btnAll.setOnClickListener(v -> ambilDataJadwal("Semua"));
        }
        if (btnPersonal != null) {
            btnPersonal.setOnClickListener(v -> ambilDataJadwal("Pribadi"));
        }
        if (btnCollege != null) {
            btnCollege.setOnClickListener(v -> ambilDataJadwal("Tugas Kuliah"));
        }

        FloatingActionButton fabAddTask = view.findViewById(R.id.fabAddTask);
        if (fabAddTask != null) {
            fabAddTask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), com.example.jadwalin.ui.kalender.AddTaskActivity.class);
                    startActivity(intent);
                }
            });
        }

        ambilDataJadwal("Semua");

        return view;
    }

    private void ambilDataJadwal(String kategori) {
        if (currentUserId.isEmpty()) return;

        // Picu perubahan warna tombol setiap kali kategori dipilih
        updateButtonVisuals(kategori);

        Query query = db.collection("tasks").whereEqualTo("userId", currentUserId);

        if (!kategori.equals("Semua")) {
            query = query.whereEqualTo("kategori", kategori);
        }

        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Gagal memuat data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return;
            }

            if (value != null) {
                List<Map<String, Object>> listTugas = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {
                    listTugas.add(doc.getData());
                }

                if (taskAdapter != null) {
                    taskAdapter.setData(listTugas);
                }
            }
        });
    }

    //  Mengubah warna tombol secara dinamis dan adaptif
    private void updateButtonVisuals(String kategoriAktif) {
        int warnaKrem = 0xFFFDE9D6;   // Warna default background (#FDE9D6)
        int warnaCokelat = 0xFFA44930; // Warna cokelat tema aktif (#A44930)
        int warnaPutih = 0xFFFFFFFF;   // Warna teks putih (#FFFFFF)

        if (btnAll != null && btnPersonal != null && btnCollege != null) {
            // RESET STATE: Kembalikan semua tombol ke status tidak aktif (Krem teks Cokelat)
            btnAll.setBackgroundColor(warnaKrem);
            btnAll.setTextColor(warnaCokelat);

            btnPersonal.setBackgroundColor(warnaKrem);
            btnPersonal.setTextColor(warnaCokelat);

            btnCollege.setBackgroundColor(warnaKrem);
            btnCollege.setTextColor(warnaCokelat);

            // SET STATE AKTIF: Ubah tombol yang dipilih menjadi Cokelat teks Putih
            if (kategoriAktif.equals("Semua")) {
                btnAll.setBackgroundColor(warnaCokelat);
                btnAll.setTextColor(warnaPutih);
            } else if (kategoriAktif.equals("Pribadi")) {
                btnPersonal.setBackgroundColor(warnaCokelat);
                btnPersonal.setTextColor(warnaPutih);
            } else if (kategoriAktif.equals("Tugas Kuliah")) {
                btnCollege.setBackgroundColor(warnaCokelat);
                btnCollege.setTextColor(warnaPutih);
            }
        }
    }

    // ================= ADAPTER INTERNAL RECYCLERVIEW TUGAS (UPDATED) =================
    private class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
        private List<Map<String, Object>> taskList;

        public TaskAdapter(List<Map<String, Object>> taskList) {
            this.taskList = taskList;
        }

        public void setData(List<Map<String, Object>> newList) {
            this.taskList = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_dynamic, parent, false);
            return new TaskViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
            Map<String, Object> task = taskList.get(position);

            String taskId = task.get("id") != null ? task.get("id").toString() : "";
            String title = task.get("title") != null ? task.get("title").toString() : "Tanpa Judul";
            String kategori = task.get("kategori") != null ? task.get("kategori").toString() : "Pribadi";
            String dueDate = task.get("dueDate") != null ? task.get("dueDate").toString() : "";

            boolean isCompleted = false;
            if (task.get("isCompleted") != null) {
                isCompleted = (boolean) task.get("isCompleted");
            }

            holder.tvDynamicTaskTitle.setText(title);
            holder.checkDynamicTask.setOnCheckedChangeListener(null);
            holder.checkDynamicTask.setChecked(isCompleted);

            // Efek coret teks
            if (isCompleted) {
                holder.tvDynamicTaskTitle.setPaintFlags(holder.tvDynamicTaskTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                holder.tvDynamicTaskTitle.setTextColor(0xFF888888);
            } else {
                holder.tvDynamicTaskTitle.setPaintFlags(holder.tvDynamicTaskTitle.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
                holder.tvDynamicTaskTitle.setTextColor(0xFF333333);
            }

            holder.checkDynamicTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!taskId.isEmpty()) {
                    db.collection("tasks").document(taskId).update("isCompleted", isChecked);
                }
            });

            // FITUR 1: KLIK BIASA UNTUK EDIT TUGAS (Kirim data ke AddTaskActivity)
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), com.example.jadwalin.ui.kalender.AddTaskActivity.class);
                intent.putExtra("taskId", taskId);
                intent.putExtra("title", title);
                intent.putExtra("kategori", kategori);
                intent.putExtra("dueDate", dueDate);
                v.getContext().startActivity(intent);
            });

            // FITUR 2: TAHAN LAMA UNTUK HAPUS TUGAS
            holder.itemView.setOnLongClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Hapus Tugas")
                        .setMessage("Hapus tugas \"" + title + "\"?")
                        .setPositiveButton("Hapus", (dialog, which) -> {
                            if (!taskId.isEmpty()) {
                                db.collection("tasks").document(taskId).delete()
                                        .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Tugas dihapus", Toast.LENGTH_SHORT).show());
                            }
                        })
                        .setNegativeButton("Batal", null)
                        .show();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return taskList.size();
        }

        class TaskViewHolder extends RecyclerView.ViewHolder {
            TextView tvDynamicTaskTitle;
            CheckBox checkDynamicTask;

            TaskViewHolder(View itemView) {
                super(itemView);
                tvDynamicTaskTitle = itemView.findViewById(R.id.tvDynamicTaskTitle);
                checkDynamicTask = itemView.findViewById(R.id.checkDynamicTask);
            }
        }
    }
}