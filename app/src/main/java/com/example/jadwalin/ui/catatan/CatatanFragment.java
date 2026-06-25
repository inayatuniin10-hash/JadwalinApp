package com.example.jadwalin.ui.catatan;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jadwalin.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CatatanFragment extends Fragment {

    private RecyclerView rvCatatan;
    private FloatingActionButton fabAddNote;
    private NotesAdapter notesAdapter;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_catatan, container, false);

        rvCatatan = view.findViewById(R.id.rvCatatan);
        fabAddNote = view.findViewById(R.id.fabAddNote);

        // 1. Atur LayoutManager dan pasang Adapter Internal baru
        if (rvCatatan != null) {
            rvCatatan.setLayoutManager(new LinearLayoutManager(getContext()));
            notesAdapter = new NotesAdapter(new ArrayList<>());
            rvCatatan.setAdapter(notesAdapter);
        }

        if (fabAddNote != null) {
            fabAddNote.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AddCatatanActivity.class);
                startActivity(intent);
            });
        }

        // 2. Jalankan fungsi untuk mendengarkan perubahan data di Firestore
        ambilDataCatatanRealtime();

        return view;
    }

    // FUNGSI UTAMA: Menarik data catatan secara otomatis & real-time
    private void ambilDataCatatanRealtime() {
        db.collection("notes").addSnapshotListener((value, error) -> {
            if (error != null) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Gagal memuat catatan: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return;
            }

            if (value != null) {
                List<Map<String, Object>> listCatatan = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {
                    // Mengambil map data dari dokumen Firestore
                    listCatatan.add(doc.getData());
                }

                // Masukkan data baru ke adapter dan segarkan RecyclerView
                if (notesAdapter != null) {
                    notesAdapter.setData(listCatatan);
                }
            }
        });
    }

    // ================= ADAPTER INTERNAL RECYCLERVIEW CATATAN (UPDATED) =================
    private class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
        private List<Map<String, Object>> notesList;

        public NotesAdapter(List<Map<String, Object>> notesList) {
            this.notesList = notesList;
        }

        public void setData(List<Map<String, Object>> newList) {
            this.notesList = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new NoteViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
            Map<String, Object> note = notesList.get(position);

            String noteId = note.get("id") != null ? note.get("id").toString() : "";
            String title = note.get("title") != null ? note.get("title").toString() : "Catatan Tanpa Judul";
            String content = note.get("content") != null ? note.get("content").toString() : "";
            String imageUrl = note.get("imageUrl") != null ? note.get("imageUrl").toString() : "";
            String date = note.get("date") != null ? " [" + note.get("date").toString() + "]" : "";

            holder.textTitle.setText(title + date);
            holder.textBody.setText(content);

            holder.textTitle.setTextColor(0xFF6A2700);
            holder.textBody.setTextColor(0xFF555555);

            // FITUR 1: KLIK UNTUK EDIT CATATAN
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), AddCatatanActivity.class);
                intent.putExtra("noteId", noteId);
                intent.putExtra("title", title);
                intent.putExtra("content", content);
                intent.putExtra("imageUrl", imageUrl);
                v.getContext().startActivity(intent);
            });

            // FITUR 2: TAHAN LAMA UNTUK HAPUS CATATAN
            holder.itemView.setOnLongClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Hapus Catatan")
                        .setMessage("Apakah Anda yakin ingin menghapus catatan \"" + title + "\"?")
                        .setPositiveButton("Hapus", (dialog, which) -> {
                            if (!noteId.isEmpty()) {
                                db.collection("notes").document(noteId)
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            if (getContext() != null)
                                                Toast.makeText(getContext(), "Catatan dihapus", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        })
                        .setNegativeButton("Batal", null)
                        .show();
                return true; // Mengembalikan true agar klik biasa tidak ikut terpicu
            });
        }

        @Override
        public int getItemCount() {
            return notesList.size();
        }

        class NoteViewHolder extends RecyclerView.ViewHolder {
            TextView textTitle, textBody;

            NoteViewHolder(View itemView) {
                super(itemView);
                textTitle = itemView.findViewById(android.R.id.text1);
                textBody = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}