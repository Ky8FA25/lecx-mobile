package com.example.lecx_mobile.views.Home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lecx_mobile.R;
import com.example.lecx_mobile.databinding.FragmentHomeBinding;

/**
 * Fragment trang chủ
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup button để test navigate sang QuizDetailFragment
        binding.btnTestQuizDetail.setOnClickListener(v -> {
            // Tạo Bundle với quizId
            Bundle bundle = new Bundle();
            bundle.putInt("quizId", 1); // Test với quiz ID = 1

            // Navigate sang QuizDetailFragment
            Navigation.findNavController(v).navigate(R.id.navigation_quiz_detail, bundle);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}