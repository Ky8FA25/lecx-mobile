package com.example.lecx_mobile.views.Profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lecx_mobile.R;
import com.example.lecx_mobile.adapters.QuizAdapter;
import com.example.lecx_mobile.databinding.FragmentProfileOtherUserBinding;
import com.example.lecx_mobile.models.Account;
import com.example.lecx_mobile.models.Quiz;
import com.example.lecx_mobile.repositories.implementations.AccountRepository;
import com.example.lecx_mobile.repositories.implementations.QuizRepository;
import com.example.lecx_mobile.repositories.interfaces.IAccountRepository;
import com.example.lecx_mobile.repositories.interfaces.IQuizRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Fragment hiển thị thông tin profile của người dùng khác
 */
public class OtherUserProfileFragment extends Fragment {

    private static final String ARG_ACCOUNT_ID = "accountId";

    private FragmentProfileOtherUserBinding binding;
    private View loadingOverlay;

    // Repositories
    private final IAccountRepository accountRepository = new AccountRepository();
    private final IQuizRepository quizRepository = new QuizRepository();

    // Adapter
    private QuizAdapter quizAdapter;

    // Data
    private Account account;
    private List<Quiz> userQuizzes = new ArrayList<>();
    private Map<Integer, Account> accountMap = new HashMap<>();

    public OtherUserProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method để tạo instance của fragment với accountId
     *
     * @param accountId ID của account cần hiển thị
     * @return OtherUserProfileFragment instance
     */
    public static OtherUserProfileFragment newInstance(int accountId) {
        OtherUserProfileFragment fragment = new OtherUserProfileFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ACCOUNT_ID, accountId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileOtherUserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lấy loading overlay view
        loadingOverlay = view.findViewById(R.id.loadingOverlay);

        // Ẩn content ban đầu
        if (binding.profileScrollView != null) {
            binding.profileScrollView.setVisibility(View.GONE);
        }

        // Lấy accountId từ Bundle
        int accountId = 1;
        if (getArguments() != null) {
            accountId = getArguments().getInt(ARG_ACCOUNT_ID, 1);
        }

        // Setup RecyclerView
        setupRecyclerView();

        // Setup event handlers
        setupEventHandlers();

        // Load data từ database
        loadAccountData(accountId);
    }

    /**
     * Setup RecyclerView cho danh sách quiz
     */
    private void setupRecyclerView() {
        quizAdapter = new QuizAdapter(
                userQuizzes,
                accountMap,
                false, // Không hiển thị nút delete
                this::onQuizClick,
                null, // Không có delete listener
                true  // Sử dụng vertical layout
        );
        binding.rvQuizzes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvQuizzes.setAdapter(quizAdapter);
    }

    /**
     * Setup các event handlers
     */
    private void setupEventHandlers() {
        // Nút quay lại
        binding.btnBack.setOnClickListener(v -> {
            if (requireActivity() != null) {
                requireActivity().onBackPressed();
            }
        });
    }

    /**
     * Load thông tin account từ database
     *
     * @param accountId ID của account
     */
    private void loadAccountData(int accountId) {
        // Hiển thị loading
        setLoading(true);

        accountRepository.getById(accountId)
                .thenAccept(accountResult -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (accountResult == null) {
                                // Account không tồn tại
                                setLoading(false);
                                Toast.makeText(requireContext(), "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
                                // Quay lại màn hình trước
                                if (requireActivity() != null) {
                                    requireActivity().onBackPressed();
                                }
                                return;
                            }

                            // Lưu account
                            account = accountResult;

                            // Thêm account vào accountMap cho adapter
                            accountMap.put(account.id, account);

                            // Bind data vào UI
                            bindDataToUI();

                            // Load quizzes của user này (loading sẽ được ẩn trong loadUserQuizzes)
                            loadUserQuizzes(accountId);
                        });
                    }
                })
                .exceptionally(e -> {
                    // Xử lý lỗi khi load account
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(requireContext(),
                                    "Lỗi khi tải thông tin người dùng: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            // Quay lại màn hình trước nếu có lỗi
                            if (requireActivity() != null) {
                                requireActivity().onBackPressed();
                            }
                        });
                    }
                    return null;
                });
    }

    /**
     * Hiển thị/ẩn Loading Overlay khi đang load dữ liệu
     */
    private void setLoading(boolean loading) {
        if (binding != null && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // Hiển thị/ẩn loading overlay
                if (loadingOverlay != null) {
                    loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
                }

                // Disable scroll khi đang load
                if (binding.profileScrollView != null) {
                    binding.profileScrollView.setEnabled(!loading);
                }
            });
        }
    }

    /**
     * Bind dữ liệu vào UI
     */
    private void bindDataToUI() {
        if (account == null) {
            return;
        }

        // Đảm bảo content được hiển thị
        if (binding.profileScrollView != null) {
            binding.profileScrollView.setVisibility(View.VISIBLE);
        }

        // Hiển thị avatar
        if (account.avatar != null && !account.avatar.isEmpty()) {
            Glide.with(requireContext())
                    .load(account.avatar)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .circleCrop()
                    .into(binding.ivAvatar);
        } else {
            binding.ivAvatar.setImageResource(R.drawable.ic_profile);
        }

        // Hiển thị tên đầy đủ
        String fullName = account.fullname != null && !account.fullname.isEmpty()
                ? account.fullname
                : (account.username != null ? account.username : "Người dùng");
        binding.tvFullName.setText(fullName);
        binding.tvFullNameValue.setText(fullName);

        // Hiển thị username
        String username = account.username != null ? account.username : "N/A";
        binding.tvUsername.setText("@" + username);
        binding.tvUsernameValue.setText(username);

        // Hiển thị email
        String email = account.email != null ? account.email : "N/A";
        binding.tvEmail.setText(email);
        binding.tvEmailValue.setText(email);
    }

    /**
     * Load danh sách quiz của user
     *
     * @param accountId ID của account
     */
    private void loadUserQuizzes(int accountId) {
        quizRepository.getAll()
                .thenAccept(quizzes -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // Lọc quizzes của user này
                            List<Quiz> filteredQuizzes = quizzes.stream()
                                    .filter(q -> q.accountId == accountId)
                                    .collect(Collectors.toList());

                            userQuizzes.clear();
                            userQuizzes.addAll(filteredQuizzes);
                            quizAdapter.notifyDataSetChanged();

                            // Update empty state
                            if (filteredQuizzes.isEmpty()) {
                                binding.tvEmptyQuizzes.setVisibility(View.VISIBLE);
                                binding.rvQuizzes.setVisibility(View.GONE);
                            } else {
                                binding.tvEmptyQuizzes.setVisibility(View.GONE);
                                binding.rvQuizzes.setVisibility(View.VISIBLE);
                            }

                            // Ẩn loading sau khi load xong quizzes
                            setLoading(false);
                        });
                    }
                })
                .exceptionally(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // Nếu lỗi, vẫn ẩn loading và hiển thị empty state
                            setLoading(false);
                            binding.tvEmptyQuizzes.setVisibility(View.VISIBLE);
                            binding.rvQuizzes.setVisibility(View.GONE);
                        });
                    }
                    return null;
                });
    }

    /**
     * Xử lý khi click vào quiz
     */
    private void onQuizClick(Quiz quiz) {
        // Navigate to quiz detail
        Bundle args = new Bundle();
        args.putInt("quizId", quiz.id);
        Navigation.findNavController(requireView()).navigate(R.id.navigation_quiz_detail, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}