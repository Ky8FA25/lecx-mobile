package com.example.lecx_mobile.views.Home;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lecx_mobile.R;
import com.example.lecx_mobile.adapters.QuizAdapter;
import com.example.lecx_mobile.databinding.FragmentHomePageBinding;
import com.example.lecx_mobile.models.Account;
import com.example.lecx_mobile.models.Quiz;
import com.example.lecx_mobile.models.QuizLearning;
import com.example.lecx_mobile.repositories.implementations.AccountRepository;
import com.example.lecx_mobile.repositories.implementations.QuizLearningRepository;
import com.example.lecx_mobile.repositories.implementations.QuizRepository;
import com.example.lecx_mobile.repositories.interfaces.IAccountRepository;
import com.example.lecx_mobile.repositories.interfaces.IQuizLearningRepository;
import com.example.lecx_mobile.repositories.interfaces.IQuizRepository;
import com.example.lecx_mobile.utils.Prefs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HomePageFragment extends Fragment {

    private FragmentHomePageBinding binding;
    private View loadingOverlay;

    // Repositories
    private final IQuizRepository quizRepository = new QuizRepository();
    private final IQuizLearningRepository quizLearningRepository = new QuizLearningRepository();
    private final IAccountRepository accountRepository = new AccountRepository();

    // Adapters
    private QuizAdapter learningQuizAdapter;
    private QuizAdapter myQuizAdapter;

    // Data
    private List<Quiz> learningQuizzes = new ArrayList<>();
    private List<Quiz> myQuizzes = new ArrayList<>();
    private Map<Integer, Account> accountMap = new HashMap<>();
    private int currentUserId;
    
    // State for learning quizzes layout
    private boolean isLearningQuizzesVertical = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomePageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lấy loading overlay view
        loadingOverlay = view.findViewById(R.id.loadingOverlay);

        currentUserId = Prefs.getUserId(requireContext());

        setupRecyclerViews();
        setupAddButton();
        setupSeeAllButton();
        loadData();
    }

    private void setupRecyclerViews() {
        // RecyclerView ngang cho quiz đang học
        learningQuizAdapter = new QuizAdapter(learningQuizzes, accountMap, false, this::onQuizClick, null);
        binding.rvLearningQuizzes.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvLearningQuizzes.setAdapter(learningQuizAdapter);

        // RecyclerView dọc cho quiz đã tạo (sử dụng layout vertical để trải ra hết màn hình)
        myQuizAdapter = new QuizAdapter(myQuizzes, accountMap, true, this::onQuizClick, this::onDeleteQuiz, true);
        binding.rvMyQuizzes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMyQuizzes.setAdapter(myQuizAdapter);
    }

    private void setupAddButton() {
        binding.fabAddQuiz.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.naviagtion_add_quiz);
        });
    }

    private void setupSeeAllButton() {
        binding.btnToggleViewLearning.setOnClickListener(v -> {
            toggleLearningQuizzesLayout();
        });
    }

    private void toggleLearningQuizzesLayout() {
        isLearningQuizzesVertical = !isLearningQuizzesVertical;
        
        // Tạo adapter mới với layout tương ứng
        learningQuizAdapter = new QuizAdapter(
            learningQuizzes, 
            accountMap, 
            false, 
            this::onQuizClick, 
            null,
            isLearningQuizzesVertical
        );
        
        // Tạo layout manager mới
        LinearLayoutManager layoutManager;
        if (isLearningQuizzesVertical) {
            // Layout dọc (giống như rvMyQuizzes)
            layoutManager = new LinearLayoutManager(requireContext());
            // Icon hiển thị hành động sẽ xảy ra: click để chuyển về grid (horizontal)
            binding.btnToggleViewLearning.setImageResource(R.drawable.ic_view_grid);
        } else {
            // Layout ngang (mặc định)
            layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
            // Icon hiển thị hành động sẽ xảy ra: click để chuyển sang list (vertical)
            binding.btnToggleViewLearning.setImageResource(R.drawable.ic_view_list);
        }
        
        // Áp dụng layout manager và adapter mới
        binding.rvLearningQuizzes.setLayoutManager(layoutManager);
        binding.rvLearningQuizzes.setAdapter(learningQuizAdapter);
        
        // Cập nhật adapter để refresh data
        learningQuizAdapter.notifyDataSetChanged();
    }

    private void loadData() {
        showLoading(true);

        // Load quiz đang học và quiz đã tạo song song
        loadLearningQuizzes();
        loadMyQuizzes();
    }

    private void loadLearningQuizzes() {
        // Lấy tất cả QuizLearning của user hiện tại
        quizLearningRepository.getAll()
                .thenCompose(quizLearnings -> {
                    // Lọc quizLearnings của user hiện tại
                    List<QuizLearning> userQuizLearnings = quizLearnings.stream()
                            .filter(ql -> ql.accountId == currentUserId && !ql.status)
                            .collect(Collectors.toList());

                    if (userQuizLearnings.isEmpty()) {
                        return java.util.concurrent.CompletableFuture.completedFuture(new ArrayList<Quiz>());
                    }

                    // Lấy danh sách quizId
                    List<Integer> quizIds = userQuizLearnings.stream()
                            .map(ql -> ql.quizId)
                            .distinct()
                            .collect(Collectors.toList());

                    // Load tất cả quiz
                    return quizRepository.getAll()
                            .thenApply(quizzes -> {
                                List<Quiz> filteredQuizzes = quizzes.stream()
                                        .filter(q -> quizIds.contains(q.id))
                                        .collect(Collectors.toList());
                                return filteredQuizzes;
                            });
                })
                .thenAccept((List<Quiz> quizzes) -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            learningQuizzes.clear();
                            learningQuizzes.addAll(quizzes);
                            learningQuizAdapter.notifyDataSetChanged();

                            // Update empty state
                            if (binding.tvEmptyLearning != null) {
                                binding.tvEmptyLearning.setVisibility(quizzes.isEmpty() ? View.VISIBLE : View.GONE);
                            }

                            // Load account info cho các quiz
                            loadAccountInfo(quizzes);
                            checkLoadingComplete();
                        });
                    }
                })
                .exceptionally(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Error loading learning quizzes: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            checkLoadingComplete();
                        });
                    }
                    return null;
                });
    }

    private void loadMyQuizzes() {
        // Lấy tất cả quiz của user hiện tại
        quizRepository.getAll()
                .thenAccept(quizzes -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            List<Quiz> filteredQuizzes = quizzes.stream()
                                    .filter(q -> q.accountId == currentUserId)
                                    .collect(Collectors.toList());
                            
                            myQuizzes.clear();
                            myQuizzes.addAll(filteredQuizzes);
                            myQuizAdapter.notifyDataSetChanged();

                            // Update empty state
                            if (binding.tvEmptyMyQuizzes != null) {
                                binding.tvEmptyMyQuizzes.setVisibility(filteredQuizzes.isEmpty() ? View.VISIBLE : View.GONE);
                            }

                            // Load account info cho các quiz
                            loadAccountInfo(myQuizzes);
                            checkLoadingComplete();
                        });
                    }
                })
                .exceptionally(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Error loading my quizzes: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            checkLoadingComplete();
                        });
                    }
                    return null;
                });
    }

    private void loadAccountInfo(List<Quiz> quizzes) {
        // Lấy danh sách accountId cần load
        List<Integer> accountIds = quizzes.stream()
                .map(q -> q.accountId)
                .distinct()
                .filter(id -> !accountMap.containsKey(id))
                .collect(Collectors.toList());

        if (accountIds.isEmpty()) {
            return;
        }

        // Load tất cả accounts
        accountRepository.getAll()
                .thenAccept(accounts -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            for (Account account : accounts) {
                                accountMap.put(account.id, account);
                            }
                            learningQuizAdapter.notifyDataSetChanged();
                            myQuizAdapter.notifyDataSetChanged();
                        });
                    }
                })
                .exceptionally(e -> {
                    // Silent fail for account loading
                    return null;
                });
    }

    private int loadingCount = 0;
    private final int TOTAL_LOADING_TASKS = 2;

    private void checkLoadingComplete() {
        loadingCount++;
        if (loadingCount >= TOTAL_LOADING_TASKS) {
            showLoading(false);
            loadingCount = 0;
        }
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void onQuizClick(Quiz quiz) {
        // Navigate to quiz detail
        Bundle args = new Bundle();
        args.putInt("quizId", quiz.id);
        Navigation.findNavController(requireView()).navigate(R.id.navigation_quiz_detail, args);
    }

    private void onDeleteQuiz(Quiz quiz) {
        // Show confirm dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa Quiz")
                .setMessage("Bạn có chắc chắn muốn xóa quiz \"" + quiz.title + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteQuiz(quiz);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteQuiz(Quiz quiz) {
        showLoading(true);

        quizRepository.delete(quiz.id)
                .thenAccept(success -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            showLoading(false);
                            if (success) {
                                // Remove from list
                                int position = myQuizzes.indexOf(quiz);
                                if (position != -1) {
                                    myQuizzes.remove(position);
                                    myQuizAdapter.notifyItemRemoved(position);
                                }
                                Toast.makeText(requireContext(), "Quiz đã được xóa", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Không thể xóa quiz", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .exceptionally(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            showLoading(false);
                            Toast.makeText(requireContext(), "Lỗi khi xóa quiz: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                    return null;
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
