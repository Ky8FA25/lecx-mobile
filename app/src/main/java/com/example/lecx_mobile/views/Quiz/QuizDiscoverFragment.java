package com.example.lecx_mobile.views.Quiz;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lecx_mobile.R;
import com.example.lecx_mobile.adapters.QuizAdapter;
import com.example.lecx_mobile.databinding.FragmentQuizDiscoverBinding;
import com.example.lecx_mobile.models.Account;
import com.example.lecx_mobile.models.Quiz;
import com.example.lecx_mobile.repositories.implementations.AccountRepository;
import com.example.lecx_mobile.repositories.implementations.QuizRepository;
import com.example.lecx_mobile.repositories.interfaces.IAccountRepository;
import com.example.lecx_mobile.repositories.interfaces.IQuizRepository;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Fragment để tìm kiếm và khám phá quiz
 */
public class QuizDiscoverFragment extends Fragment {

    private FragmentQuizDiscoverBinding binding;
    private View loadingOverlay;

    // Repositories
    private final IQuizRepository quizRepository = new QuizRepository();
    private final IAccountRepository accountRepository = new AccountRepository();

    // Adapter
    private QuizAdapter quizAdapter;

    // Data
    private List<Quiz> allQuizzes = new ArrayList<>();
    private List<Quiz> displayedQuizzes = new ArrayList<>();
    private Map<Integer, Account> accountMap = new HashMap<>();

    // Search debounce
    private android.os.Handler searchHandler = new android.os.Handler();
    private Runnable searchRunnable;

    public QuizDiscoverFragment() {
        // Required empty public constructor
    }

    public static QuizDiscoverFragment newInstance() {
        return new QuizDiscoverFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentQuizDiscoverBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lấy loading overlay view
        loadingOverlay = view.findViewById(R.id.loadingOverlay);

        // Setup RecyclerView
        setupRecyclerView();

        // Setup search functionality
        setupSearch();

        // Load initial data
        loadData();
    }

    private void setupRecyclerView() {
        quizAdapter = new QuizAdapter(
                displayedQuizzes,
                accountMap,
                false, // Không hiển thị nút delete
                this::onQuizClick,
                null, // Không có delete listener
                true  // Sử dụng vertical layout
        );
        binding.rvQuizzes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvQuizzes.setAdapter(quizAdapter);
    }

    private void setupSearch() {
        TextInputEditText etSearch = binding.etSearch;
        
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Hủy search request trước đó nếu có
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Tạo search request mới với debounce 500ms
                String keyword = s.toString().trim();
                searchRunnable = () -> performSearch(keyword);
                searchHandler.postDelayed(searchRunnable, 500);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String keyword) {
        if (keyword.isEmpty()) {
            // Nếu không có keyword, hiển thị tất cả quiz
            displayedQuizzes.clear();
            displayedQuizzes.addAll(allQuizzes);
            updateUI();
        } else {
            // Tìm kiếm với keyword
            showLoading(true);
            
            // Tìm quiz theo title/description
            quizRepository.findByKeyword(keyword)
                    .thenAccept(quizzesByContent -> {
                        // Tìm quiz theo tên tác giả (account fullname)
                        findQuizzesByAuthorName(keyword)
                                .thenAccept(quizzesByAuthor -> {
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> {
                                            // Kết hợp kết quả và loại bỏ trùng lặp
                                            List<Quiz> combinedQuizzes = new ArrayList<>();
                                            
                                            // Thêm quizzes tìm theo content
                                            combinedQuizzes.addAll(quizzesByContent);
                                            
                                            // Thêm quizzes tìm theo author (loại bỏ trùng)
                                            for (Quiz quiz : quizzesByAuthor) {
                                                boolean exists = false;
                                                for (Quiz existing : combinedQuizzes) {
                                                    if (existing.id == quiz.id) {
                                                        exists = true;
                                                        break;
                                                    }
                                                }
                                                if (!exists) {
                                                    combinedQuizzes.add(quiz);
                                                }
                                            }
                                            
                                            displayedQuizzes.clear();
                                            displayedQuizzes.addAll(combinedQuizzes);
                                            loadAccountInfo(combinedQuizzes);
                                            updateUI();
                                            showLoading(false);
                                        });
                                    }
                                })
                                .exceptionally(e -> {
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> {
                                            // Nếu tìm theo author lỗi, vẫn hiển thị kết quả tìm theo content
                                            displayedQuizzes.clear();
                                            displayedQuizzes.addAll(quizzesByContent);
                                            loadAccountInfo(quizzesByContent);
                                            updateUI();
                                            showLoading(false);
                                        });
                                    }
                                    return null;
                                });
                    })
                    .exceptionally(e -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                // Hiển thị lỗi nếu cần
                            });
                        }
                        return null;
                    });
        }
    }
    
    /**
     * Tìm quizzes theo tên tác giả (account fullname)
     */
    private java.util.concurrent.CompletableFuture<List<Quiz>> findQuizzesByAuthorName(String keyword) {
        java.util.concurrent.CompletableFuture<List<Quiz>> future = new java.util.concurrent.CompletableFuture<>();
        
        // Nếu accountMap chưa có dữ liệu, load trước
        if (accountMap.isEmpty()) {
            accountRepository.getAll()
                    .thenAccept(accounts -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                for (Account account : accounts) {
                                    accountMap.put(account.id, account);
                                }
                                // Sau khi load xong, tìm lại
                                findQuizzesByAuthorNameInternal(keyword, future);
                            });
                        }
                    })
                    .exceptionally(e -> {
                        future.complete(new ArrayList<>());
                        return null;
                    });
        } else {
            findQuizzesByAuthorNameInternal(keyword, future);
        }
        
        return future;
    }
    
    private void findQuizzesByAuthorNameInternal(String keyword, java.util.concurrent.CompletableFuture<List<Quiz>> future) {
        // Tìm accounts có fullname chứa keyword
        List<Integer> matchingAccountIds = new ArrayList<>();
        
        for (Account account : accountMap.values()) {
            if (account.fullname != null) {
                // Chuẩn hóa cả fullname và keyword: chuyển thành chữ thường và bỏ dấu
                // Sử dụng cùng logic normalize như PredicateUtils
                String normalizedFullname = com.example.lecx_mobile.utils.PredicateUtils.normalizeString(account.fullname);
                String normalizedKeyword = com.example.lecx_mobile.utils.PredicateUtils.normalizeString(keyword);
                
                if (normalizedFullname.contains(normalizedKeyword)) {
                    matchingAccountIds.add(account.id);
                }
            }
        }
        
        if (matchingAccountIds.isEmpty()) {
            future.complete(new ArrayList<>());
            return;
        }
        
        // Tìm quizzes của những accounts này
        List<Quiz> matchingQuizzes = allQuizzes.stream()
                .filter(quiz -> matchingAccountIds.contains(quiz.accountId))
                .collect(Collectors.toList());
        
        future.complete(matchingQuizzes);
    }

    private void loadData() {
        showLoading(true);
        
        // Load tất cả quizzes và accounts song song
        java.util.concurrent.CompletableFuture<List<Quiz>> quizzesFuture = quizRepository.getAll();
        java.util.concurrent.CompletableFuture<List<Account>> accountsFuture = accountRepository.getAll();
        
        quizzesFuture.thenCombine(accountsFuture, (quizzes, accounts) -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Load quizzes
                    allQuizzes.clear();
                    allQuizzes.addAll(quizzes);
                    displayedQuizzes.clear();
                    displayedQuizzes.addAll(quizzes);
                    
                    // Load accounts vào accountMap
                    accountMap.clear();
                    for (Account account : accounts) {
                        accountMap.put(account.id, account);
                    }
                    
                    updateUI();
                    showLoading(false);
                });
            }
            return null;
        }).exceptionally(e -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    showLoading(false);
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
            quizAdapter.notifyDataSetChanged();
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
                            quizAdapter.notifyDataSetChanged();
                        });
                    }
                })
                .exceptionally(e -> {
                    // Silent fail for account loading
                    return null;
                });
    }

    private void updateUI() {
        // Hiển thị/ẩn empty state
        if (displayedQuizzes.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvQuizzes.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvQuizzes.setVisibility(View.VISIBLE);
        }
        quizAdapter.notifyDataSetChanged();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up handler
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        binding = null;
    }
}