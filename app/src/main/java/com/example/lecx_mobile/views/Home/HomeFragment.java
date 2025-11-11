package com.example.lecx_mobile.views.Home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lecx_mobile.R;
import com.google.android.material.button.MaterialButton;

public class HomeFragment extends Fragment {

    private MaterialButton btnTestFlashcard;
    private MaterialButton btnQuizDetail;
    private MaterialButton btnTestAddQuiz;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnTestFlashcard = view.findViewById(R.id.btnTestFlashcard);
        btnQuizDetail = view.findViewById(R.id.btnTestQuizDetail);
        btnTestAddQuiz = view.findViewById(R.id.btnTestAddQuiz);

        btnTestAddQuiz.setOnClickListener(v->{
            Navigation.findNavController(v).navigate(R.id.naviagtion_add_quiz);
        });

        btnQuizDetail.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("quizId",2);
            Navigation.findNavController(v).navigate(R.id.navigation_quiz_detail, args);
        });
        
        btnTestFlashcard.setOnClickListener(v -> {
            // Navigate to Flashcard Learning with quizLearningId = 1 (demo)
            Bundle args = new Bundle();
            args.putInt("quizLearningId", 3);
            Navigation.findNavController(v).navigate(R.id.navigation_flashcard_learning, args);
        });
    }
}