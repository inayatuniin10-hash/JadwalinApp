package com.example.jadwalin.ui.akun;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.jadwalin.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Objects;

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

public class AkunFragment extends Fragment {

    // Menggunakan API Key ImgBB
    private static final String IMGBB_API_KEY = "8b24df85173736826a7569f1ad33c407";

    private ImageView ivProfilePicture;
    private TextView tvProfileName, tvProfileEmail;
    private SwitchMaterial switchNotification;
    private RelativeLayout btnChangePassword;
    private Button btnLogOut;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private SharedPreferences sharedPreferences;

    private Uri imageUri;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    uploadProfilePictureToImgBB();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_akun, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (getActivity() != null) {
            sharedPreferences = getActivity().getSharedPreferences("JadwalinSettings", Context.MODE_PRIVATE);
        }

        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        switchNotification = view.findViewById(R.id.switchNotification);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnLogOut = view.findViewById(R.id.btnLogOut);
        FloatingActionButton fabChangePhoto = view.findViewById(R.id.fabChangePhoto);

        loadUserData();
        setupNotificationSwitch();

        if (fabChangePhoto != null) {
            fabChangePhoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        }

        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }

        if (btnLogOut != null) {
            btnLogOut.setOnClickListener(v -> {
                mAuth.signOut();
                Toast.makeText(getContext(), "Berhasil Keluar Akun", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) getActivity().finish();
            });
        }

        return view;
    }

    private void loadUserData() {
        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            String email = currentUser.getEmail();
            Uri photoUrl = currentUser.getPhotoUrl();

            tvProfileName.setText(name != null && !name.isEmpty() ? name : "User Jadwalin");
            tvProfileEmail.setText(email);

            if (photoUrl != null && getContext() != null) {
                Glide.with(getContext())
                        .load(photoUrl)
                        .placeholder(android.R.drawable.sym_def_app_icon)
                        .into(ivProfilePicture);
            }
        }
    }

    private void setupNotificationSwitch() {
        if (sharedPreferences == null || switchNotification == null) return;

        boolean isNotifEnabled = sharedPreferences.getBoolean("isNotificationEnabled", true);
        switchNotification.setChecked(isNotifEnabled);

        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isNotificationEnabled", isChecked);
            editor.apply();

            if (isChecked) {
                Toast.makeText(getContext(), "Notifikasi diaktifkan", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Notifikasi dimatikan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fungsi upload foto profil dengan Kompresi otomatis untuk RAM 3GB
    private void uploadProfilePictureToImgBB() {
        if (imageUri != null && currentUser != null && getContext() != null) {
            Toast.makeText(getContext(), "Memperbarui foto profil...", Toast.LENGTH_SHORT).show();

            try {
                // 1. Ambil input stream dari URI gambar pilihan
                InputStream inputStream = Objects.requireNonNull(getContext()).getContentResolver().openInputStream(imageUri);

                // 2. Downsample Resolusi Gambar agar dimensinya tidak terlalu raksasa di RAM
                android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
                options.inSampleSize = 2; // Memotong setengah dimensi gambar (Sangat menghemat memori)
                android.graphics.Bitmap originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream, null, options);
                if (inputStream != null) inputStream.close();

                if (originalBitmap == null) {
                    Toast.makeText(getContext(), "Gagal memproses gambar profil", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 3. Kompres kualitas gambar menjadi 60% format JPEG
                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                originalBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 60, byteBuffer);
                byte[] imageBytes = byteBuffer.toByteArray();

                // Langsung bersihkan objek Bitmap dari RAM setelah selesai dikompres
                originalBitmap.recycle();

                // 4. Bungkus hasil kompresi byte array ke dalam MultipartBody
                RequestBody requestFile = RequestBody.create(okhttp3.MediaType.parse("image/jpeg"), imageBytes);
                MultipartBody.Part body = MultipartBody.Part.createFormData("image", "avatar_profile.jpg", requestFile);

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://api.imgbb.com/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                ImgBBAkunService service = retrofit.create(AkunFragment.ImgBBAkunService.class);

                Call<ResponseBody> call = service.uploadImage(IMGBB_API_KEY, body);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                String jsonResponse = response.body().string();
                                JSONObject jsonObject = new JSONObject(jsonResponse);
                                String uploadedUrl = jsonObject.getJSONObject("data").getString("url");

                                // Sukses dapat link publik, lalu update profil Firebase Auth Anda
                                updateFirebaseUserProfile(Uri.parse(uploadedUrl));
                            } else {
                                Toast.makeText(getContext(), "Gagal upload foto profil ke ImgBB.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Eror memproses data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(getContext(), "Koneksi internet bermasalah: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Gagal membaca berkas gambar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateFirebaseUserProfile(Uri newPhotoUri) {
        if (currentUser != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(newPhotoUri)
                    .build();

            currentUser.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful() && getActivity() != null) {
                    Glide.with(this).load(newPhotoUri).into(ivProfilePicture);
                    Toast.makeText(getContext(), "Foto profil berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showChangePasswordDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Ubah Kata Sandi");

        final EditText input = new EditText(getContext());
        input.setHint("Masukkan password baru");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String newPassword = input.getText().toString().trim();
            if (newPassword.length() >= 6 && currentUser != null) {
                currentUser.updatePassword(newPassword).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Password berhasil diganti", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Gagal ganti password. Silakan Re-login.", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public interface ImgBBAkunService {
        @Multipart
        @POST("1/upload")
        Call<ResponseBody> uploadImage(
                @Query("key") String apiKey,
                @Part MultipartBody.Part image
        );
    }
}