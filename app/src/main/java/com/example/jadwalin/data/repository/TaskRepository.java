package com.example.jadwalin.data.repository;

import com.example.jadwalin.data.model.Task;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Map;

public class TaskRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference taskCollection = db.collection("tasks");

    // Menggunakan parameter Objek Task Utuh agar terstruktur rapi
    public void addTask(Task task, OnSuccessListener<Void> successListener, com.google.android.gms.tasks.OnFailureListener failureListener) {
        taskCollection.document(task.getId()).set(task)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public void getAllTasks(OnSuccessListener<QuerySnapshot> listener) {
        taskCollection.get().addOnSuccessListener(listener);
    }

    // String field disamakan menjadi "isCompleted" agar sesuai dengan Model Java Objek
    public void updateTaskStatus(String id, boolean isCompleted) {
        taskCollection.document(id).update("isCompleted", isCompleted);
    }

    public void deleteTask(String id) {
        taskCollection.document(id).delete();
    }
}