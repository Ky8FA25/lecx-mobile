package com.example.lecx_mobile;

import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.lecx_mobile.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Fix padding navView khi có Insets
        ViewCompat.setOnApplyWindowInsetsListener(navView, (v, insets) -> {
            v.setPadding(0, 0, 0, 0);
            return insets;
        });

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        // 👇 Override để giữ fragment cũ, tránh tạo lại khi bấm tab
        navView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.navigation_home) {
                navController.navigate(R.id.navigation_home, null,
                        new androidx.navigation.NavOptions.Builder()
                                .setLaunchSingleTop(true)
                                .setPopUpTo(R.id.navigation_home, false)
                                .build());
                return true;
            } else if (id == R.id.navigation_profile) {
                navController.navigate(R.id.navigation_profile, null,
                        new androidx.navigation.NavOptions.Builder()
                                .setLaunchSingleTop(true)
                                .setPopUpTo(R.id.navigation_profile, false)
                                .build());
                return true;
            } else if (id == R.id.navigation_quiz_discover) {
                navController.navigate(R.id.navigation_quiz_discover, null,
                        new androidx.navigation.NavOptions.Builder()
                                .setLaunchSingleTop(true)
                                .setPopUpTo(R.id.navigation_quiz_discover, false)
                                .build());
                return true;
            }
            return false;
        });

        // 👇 Ẩn BottomNav khi vào Flashcard Learning
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.navigation_flashcard_learning) {
                navView.setVisibility(View.GONE);
            } else {
                navView.setVisibility(View.VISIBLE);
            }
        });

        // 👇 Bắt nút Back để xử lý custom
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!navController.popBackStack()) {
                    // Nếu không còn fragment trong backstack → thoát app
                    finish();
                }
            }
        });
    }
}
