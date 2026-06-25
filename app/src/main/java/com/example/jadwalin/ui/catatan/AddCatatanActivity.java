package com.example.jadwalin.ui.catatan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.jadwalin.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public class AddCatatanActivity extends AppCompatActivity {

    private static final String IMGBB_API_KEY = "8b24df85173736826a7569f1ad33c407";

    private EditText etTitle, etContent;
    private ImageView ivPreview;
    private Uri imageUri;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String noteId = null;
    private String existingImageUrl = "";

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    ivPreview.setVisibility(View.VISIBLE);
                    Glide.with(AddCatatanActivity.this).load(uri).into(ivPreview);
                }
            }
    );

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && imageUri != null) {
                    ivPreview.setVisibility(View.VISIBLE);
                    Glide.with(AddCatatanActivity.this).load(imageUri).into(ivPreview);
                } else {
                    Toast.makeText(this, "Pengambilan foto dibatalkan", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (result.getOrDefault(Manifest.permission.CAMERA, false)) {
                    bukaKameraAsli();
                } else {
                    Toast.makeText(this, "Izin kamera ditolak.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_catatan);

        etTitle = findViewById(R.id.etCatatanTitle);
        etContent = findViewById(R.id.etCatatanContent);
        ivPreview = findViewById(R.id.ivCatatanPreview);
        Button btnSave = findViewById(R.id.btnSaveCatatan);
        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        ImageView btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("noteId")) {
            noteId = intent.getStringExtra("noteId");
            etTitle.setText(intent.getStringExtra("title"));
            etContent.setText(intent.getStringExtra("content"));
            existingImageUrl = intent.getStringExtra("imageUrl");

            if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
                ivPreview.setVisibility(View.VISIBLE);
                Glide.with(this).load(existingImageUrl).into(ivPreview);
            }
            if (btnSave != null) btnSave.setText("Perbarui Catatan");
        }

        if (btnSelectImage != null) btnSelectImage.setOnClickListener(v -> showImagePickerDialog());
        if (btnSave != null) btnSave.setOnClickListener(v -> unggahDanSimpanData());
    }

    private void showImagePickerDialog() {
        String[] options = {"Kamera", "Galeri"};
        new AlertDialog.Builder(this)
                .setTitle("Pilih Foto Dari")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) cekIzinDanBukaKamera();
                    else pickImageLauncher.launch("image/*");
                }).show();
    }

    private void cekIzinDanBukaKamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            bukaKameraAsli();
        } else {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                requestPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
            } else {
                requestPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
            }
        }
    }

    private void bukaKameraAsli() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageName = "JADWALIN_" + timeStamp + ".jpg";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Jadwalin");
        }

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraLauncher.launch(intent);
    }

    private void unggahDanSimpanData() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Judul tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Sedang memproses catatan...", Toast.LENGTH_SHORT).show();

        if (imageUri != null) {
            try {
                // PROSES KOMPRESI OPTIMAL UNTUK RAM 3GB
                InputStream inputStream = getContentResolver().openInputStream(imageUri);

                // Langkah 1: Downsample Resolusi Gambar agar tidak memakan RAM raksasa
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2; // Memperkecil resolusi setengah dimensi asli (Sangat menghemat RAM)
                Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream, null, options);
                if (inputStream != null) inputStream.close();

                if (originalBitmap == null) {
                    Toast.makeText(this, "Gagal memproses gambar", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Langkah 2: Kompres Kualitas Gambar menjadi 60% format JPEG
                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteBuffer);
                byte[] imageBytes = byteBuffer.toByteArray();

                // Langsung daur ulang/bersihkan memori Bitmap dari RAM setelah selesai dikompres
                originalBitmap.recycle();

                // Membuat HTTP Request menggunakan hasil kompresi biner
                RequestBody requestFile = RequestBody.create(okhttp3.MediaType.parse("image/jpeg"), imageBytes);
                MultipartBody.Part body = MultipartBody.Part.createFormData("image", "catatan_gambar.jpg", requestFile);

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://api.imgbb.com/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                ImgBBService service = retrofit.create(AddCatatanActivity.ImgBBService.class);

                Call<ResponseBody> call = service.uploadImage(IMGBB_API_KEY, body);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                String jsonResponse = response.body().string();
                                JSONObject jsonObject = new JSONObject(jsonResponse);
                                String uploadedUrl = jsonObject.getJSONObject("data").getString("url");

                                saveNoteToFirestore(title, content, uploadedUrl);
                            } else {
                                if (response.errorBody() != null) {
                                    android.util.Log.e("ImgBB_Error", response.errorBody().string());
                                }
                                Toast.makeText(AddCatatanActivity.this, "Gagal upload ke ImgBB. Periksa API Key.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(AddCatatanActivity.this, "Eror Parsing Data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(AddCatatanActivity.this, "Koneksi Internet Bermasalah: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Gagal membaca file gambar", Toast.LENGTH_SHORT).show();
            }
        } else {
            saveNoteToFirestore(title, content, existingImageUrl);
        }
    }

    private void saveNoteToFirestore(String title, String content, String imageUrl) {
        String id = (noteId == null) ? db.collection("notes").document().getId() : noteId;
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        Map<String, Object> note = new HashMap<>();
        note.put("id", id);
        note.put("title", title);
        note.put("content", content);
        note.put("imageUrl", imageUrl);
        note.put("date", date);

        db.collection("notes").document(id).set(note)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Catatan Berhasil Disimpan", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal ke database", Toast.LENGTH_SHORT).show());
    }

    public interface ImgBBService {
        @Multipart
        @POST("1/upload")
        Call<ResponseBody> uploadImage(
                @Query("key") String apiKey,
                @Part MultipartBody.Part image
        );
    }
}