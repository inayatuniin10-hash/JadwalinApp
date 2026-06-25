package com.example.jadwalin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.jadwalin.ui.kalender.KalenderFragment;
import com.example.jadwalin.ui.akun.AkunFragment;
// TODO: Pastikan nama package Fragment Jadwal dan Catatan Anda sudah benar di bawah ini!
import com.example.jadwalin.ui.jadwal.JadwalFragment;
import com.example.jadwalin.ui.catatan.CatatanFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        // Menampilkan KalenderFragment sebagai halaman utama default
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new KalenderFragment())
                    .commit();
            // Membuat menu Kalender langsung terpilih secara visual saat pertama buka
            bottomNavigation.setSelectedItemId(R.id.nav_kalender);
        }

        // Aksi klik menu navigasi bawah responsif
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_jadwal) {
                selectedFragment = new JadwalFragment();
            } else if (id == R.id.nav_kalender) {
                selectedFragment = new KalenderFragment();
            } else if (id == R.id.nav_catatan) {
                selectedFragment = new CatatanFragment();
            } else if (id == R.id.nav_akun) {
                selectedFragment = new AkunFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }
}