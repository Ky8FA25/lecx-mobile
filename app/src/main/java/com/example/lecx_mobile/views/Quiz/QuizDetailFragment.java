package com.example.lecx_mobile.views.Quiz;

import android.content.Intent;
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
import com.example.lecx_mobile.adapters.FlashcardAdapter;
import com.example.lecx_mobile.databinding.FragmentQuizDetailBinding;
import com.example.lecx_mobile.models.Account;
import com.example.lecx_mobile.models.Flashcard;
import com.example.lecx_mobile.models.Quiz;
import com.example.lecx_mobile.models.QuizLearning;
import com.example.lecx_mobile.repositories.implementations.AccountRepository;
import com.example.lecx_mobile.repositories.implementations.FlashcardRepository;
import com.example.lecx_mobile.repositories.implementations.QuizLearningRepository;
import com.example.lecx_mobile.repositories.implementations.QuizRepository;
import com.example.lecx_mobile.repositories.interfaces.IAccountRepository;
import com.example.lecx_mobile.repositories.interfaces.IFlashcardRepository;
import com.example.lecx_mobile.repositories.interfaces.IQuizLearningRepository;
import com.example.lecx_mobile.repositories.interfaces.IQuizRepository;
import com.example.lecx_mobile.utils.Constants;
import com.example.lecx_mobile.utils.Prefs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Fragment hiển thị chi tiết một Quiz cụ thể
 * Bao gồm: thông tin quiz, tác giả, danh sách flashcard, và các nút hành động
 */
public class QuizDetailFragment extends Fragment {

    private static final String ARG_QUIZ_ID = "quizId";

    private FragmentQuizDetailBinding binding;
    private FlashcardAdapter flashcardAdapter;
    private View loadingOverlay; // Loading overlay view

    // Repositories
    private final IQuizRepository quizRepository = new QuizRepository();
    private final IQuizLearningRepository quizLearningRepository = new QuizLearningRepository();
    private final IAccountRepository accountRepository = new AccountRepository();
    private final IFlashcardRepository flashcardRepository = new FlashcardRepository();

    // Data
    private Quiz quiz;
    private Account author;
    private List<Flashcard> flashcards;
    private int currentUserId;

    /**
     * Factory method để tạo instance của fragment với quizId
     *
     * @param quizId ID của quiz cần hiển thị
     * @return QuizDetailFragment instance
     */
    public static QuizDetailFragment newInstance(int quizId) {
        QuizDetailFragment fragment = new QuizDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_QUIZ_ID, quizId);
        fragment.setArguments(args);
        return fragment;
    }

    public QuizDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentQuizDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lấy loading overlay view
        loadingOverlay = view.findViewById(R.id.loadingOverlay);

        // Ẩn content ban đầu
        if (binding.scrollView != null) {
            binding.scrollView.setVisibility(View.GONE);
        }

        // Lấy quizId từ Bundle, mặc định là 1
        int quizId = 1;
        if (getArguments() != null) {
            quizId = getArguments().getInt(ARG_QUIZ_ID, 1);
        }

        // Lấy userId hiện tại để kiểm tra quyền sở hữu
        currentUserId = Prefs.getUserId(requireContext());

        // Khởi tạo RecyclerView
        setupRecyclerView();

        // Setup event handlers
        setupEventHandlers();

        // Load data từ database
        loadDataFromDatabase(quizId);
    }

    /**
     * Khởi tạo RecyclerView cho danh sách flashcard
     */
    private void setupRecyclerView() {
        flashcardAdapter = new FlashcardAdapter(new ArrayList<>());
        binding.rvFlashcards.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFlashcards.setAdapter(flashcardAdapter);
    }

    /**
     * Load dữ liệu từ database: quiz, author và flashcards
     *
     * @param quizId ID của quiz
     */
    private void loadDataFromDatabase(int quizId) {
        // Hiển thị ProgressBar
        setLoading(true);

        // Bước 1: Lấy quiz từ database
        quizRepository.getById(quizId)
                .thenAccept(quizResult -> {
                    if (quizResult == null) {
                        // Quiz không tồn tại
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                setLoading(false);
                                Toast.makeText(requireContext(), "Không tìm thấy quiz", Toast.LENGTH_SHORT).show();
                                // Quay lại màn hình trước
                                if (requireActivity() != null) {
                                    requireActivity().onBackPressed();
                                }
                            });
                        }
                        return;
                    }

                    // Lưu quiz
                    quiz = quizResult;

                    // Bước 2: Lấy author từ database dựa vào quiz.accountId
                    accountRepository.getById(quiz.accountId)
                            .thenAccept(authorResult -> {
                                if (authorResult == null) {
                                    // Author không tồn tại, dùng giá trị mặc định
                                    author = new Account();
                                    author.id = quiz.accountId;
                                    author.fullname = "Người dùng";
                                    author.username = "user";
                                    author.avatar = Constants.DEFAULT_AVATAR_URL;
                                } else {
                                    author = authorResult;
                                }

                                // Bước 3: Lấy danh sách flashcards từ database dựa vào quizId
                                flashcardRepository.where(flashcard -> flashcard.quizId == quizId)
                                        .thenAccept(flashcardsResult -> {
                                            if (flashcardsResult == null) {
                                                flashcards = new ArrayList<>();
                                            } else {
                                                flashcards = flashcardsResult;
                                            }

                                            // Cập nhật số lượng flashcard trong quiz (nếu cần)
                                            if (quiz.numberOfFlashcards != flashcards.size()) {
                                                quiz.numberOfFlashcards = flashcards.size();
                                            }

                                            // Cập nhật UI trên Main Thread
                                            if (getActivity() != null) {
                                                getActivity().runOnUiThread(() -> {
                                                    setLoading(false);
                                                    bindDataToUI();
                                                });
                                            }
                                        })
                                        .exceptionally(e -> {
                                            // Lỗi khi load flashcards
                                            if (getActivity() != null) {
                                                getActivity().runOnUiThread(() -> {
                                                    setLoading(false);
                                                    Toast.makeText(requireContext(),
                                                            "Lỗi khi tải danh sách thẻ: " + e.getMessage(),
                                                            Toast.LENGTH_SHORT).show();
                                                    // Vẫn hiển thị quiz và author nếu có
                                                    bindDataToUI();
                                                });
                                            }
                                            return null;
                                        });
                            })
                            .exceptionally(e -> {
                                // Lỗi khi load author, vẫn tiếp tục load flashcards
                                author = new Account();
                                author.id = quiz.accountId;
                                author.fullname = "Người dùng";
                                author.username = "user";
                                author.avatar = Constants.DEFAULT_AVATAR_URL;

                                // Tiếp tục load flashcards
                                flashcardRepository.where(flashcard -> flashcard.quizId == quizId)
                                        .thenAccept(flashcardsResult -> {
                                            if (flashcardsResult == null) {
                                                flashcards = new ArrayList<>();
                                            } else {
                                                flashcards = flashcardsResult;
                                            }

                                            if (quiz.numberOfFlashcards != flashcards.size()) {
                                                quiz.numberOfFlashcards = flashcards.size();
                                            }

                                            if (getActivity() != null) {
                                                getActivity().runOnUiThread(() -> {
                                                    setLoading(false);
                                                    bindDataToUI();
                                                });
                                            }
                                        })
                                        .exceptionally(e2 -> {
                                            // Đảm bảo author đã được khởi tạo
                                            if (author == null) {
                                                author = new Account();
                                                author.id = quiz.accountId;
                                                author.fullname = "Người dùng";
                                                author.username = "user";
                                                author.avatar = Constants.DEFAULT_AVATAR_URL;
                                            }
                                            // Đảm bảo flashcards đã được khởi tạo
                                            if (flashcards == null) {
                                                flashcards = new ArrayList<>();
                                            }
                                            if (getActivity() != null) {
                                                getActivity().runOnUiThread(() -> {
                                                    setLoading(false);
                                                    bindDataToUI();
                                                });
                                            }
                                            return null;
                                        });
                                return null;
                            });
                })
                .exceptionally(e -> {
                    // Xử lý lỗi khi load quiz
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(requireContext(),
                                    "Lỗi khi tải dữ liệu quiz: " + e.getMessage(),
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
                binding.scrollView.setEnabled(!loading);
                
                // Disable các nút khi đang load
                binding.btnStudy.setEnabled(!loading);
                binding.btnFlashcard.setEnabled(!loading);
                binding.btnEdit.setEnabled(!loading);
                binding.btnBack.setEnabled(!loading);
            });
        }
    }

    /**
     * Bind dữ liệu vào UI
     */
    private void bindDataToUI() {
        if (quiz == null) {
            return;
        }

        // Đảm bảo content được hiển thị
        if (binding.scrollView != null) {
            binding.scrollView.setVisibility(View.VISIBLE);
        }

        // Hiển thị tên quiz
        if (quiz.title != null) {
            binding.tvQuizTitle.setText(quiz.title);
        } else {
            binding.tvQuizTitle.setText("Không có tiêu đề");
        }

        // Hiển thị mô tả quiz
        if (quiz.description != null) {
            binding.tvQuizDescription.setText(quiz.description);
        } else {
            binding.tvQuizDescription.setText("Không có mô tả");
        }

        // Hiển thị thông tin tác giả
        if (author != null) {
            String authorName = author.fullname != null && !author.fullname.isEmpty() 
                    ? author.fullname 
                    : (author.username != null ? author.username : "Người dùng");
            binding.tvAuthorName.setText(authorName);
            
            if (author.avatar != null && !author.avatar.isEmpty()) {
                Glide.with(requireContext())
                        .load(author.avatar)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .circleCrop()
                        .into(binding.ivAuthorAvatar);
            } else {
                binding.ivAuthorAvatar.setImageResource(R.drawable.ic_profile);
            }
        } else {
            binding.tvAuthorName.setText("Người dùng");
            binding.ivAuthorAvatar.setImageResource(R.drawable.ic_profile);
        }

        // Hiển thị số lượng thẻ
        int cardCount = flashcards != null ? flashcards.size() : 0;
        binding.tvFlashcardCount.setText(cardCount + " thẻ");

        // Hiển thị nút Sửa nếu người dùng hiện tại là chủ sở hữu
        if (currentUserId == quiz.accountId && currentUserId != -1) {
            binding.btnEdit.setVisibility(View.VISIBLE);
        } else {
            binding.btnEdit.setVisibility(View.GONE);
        }

        // Cập nhật danh sách flashcard
        if (flashcards != null && !flashcards.isEmpty()) {
            flashcardAdapter.updateFlashcards(flashcards);
        } else {
            flashcardAdapter.updateFlashcards(new ArrayList<>());
        }
    }

    /**
     * Setup các event handlers cho các nút và view
     */
    private void setupEventHandlers() {
        // Nút quay lại
        binding.btnBack.setOnClickListener(v -> {
            if (requireActivity() != null) {
                requireActivity().onBackPressed();
            }
        });

        // Nút HỌC - Mở DoQuizActivity
        binding.btnStudy.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), DoQuizActivity.class);
            intent.putExtra(DoQuizActivity.EXTRA_QUIZ_ID, quiz.id);
            startActivity(intent);
        });

        binding.btnFlashcard.setOnClickListener(v -> {

            getLatestQuizLearningId()
                    .thenCompose(latestId -> {
                        // Nếu chưa có bản ghi học -> tạo mới
                        if (latestId == -1) {
                            return createNewQuizLearning();
                        } else {
                            // Trả về bản ghi cũ
                            return CompletableFuture.completedFuture(latestId);
                        }
                    })
                    .thenAccept(quizLearningId -> {
                        if (getActivity() == null) return;

                        getActivity().runOnUiThread(() -> {

                            Bundle args = new Bundle();
                            args.putInt("quizLearningId", quizLearningId);
                            Navigation.findNavController(v)
                                    .navigate(R.id.navigation_flashcard_learning, args);
                        });
                    })
                    .exceptionally(e -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(),
                                        "Lỗi khi khởi tạo tiến trình học: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                        return null;
                    });
        });


        // Nút Sửa (để trống logic tạm thời)
        binding.btnEdit.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("quizId",1);
            Navigation.findNavController(v).navigate(R.id.navigation_update_quiz, args);
        });

        // Click vào tác giả - Navigate đến profile của tác giả
        binding.layoutAuthor.setOnClickListener(v -> {
            if (author != null && author.id > 0) {
                Bundle args = new Bundle();
                args.putInt("accountId", author.id);
                Navigation.findNavController(v).navigate(R.id.navigation_profile_other_user, args);
            } else {
                Toast.makeText(requireContext(), "Không tìm thấy thông tin tác giả", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private CompletableFuture<Integer> getLatestQuizLearningId() {
        QuizLearningRepository repo = new QuizLearningRepository();
        int accountId = Prefs.getUserId(requireContext());
        CompletableFuture<Integer> future = new CompletableFuture<>();

        repo.getAll().thenAccept(list -> {
            // lọc theo quizId, accountId và status=false
            Optional<QuizLearning> latest = list.stream()
                    .filter(q -> q.quizId == quiz.id && q.accountId == accountId && !q.status)
                    .max(Comparator.comparingInt(q -> q.id)); // lấy bản ghi có id lớn nhất

            if (latest.isPresent()) {
                future.complete(latest.get().id);
            } else {
                future.complete(-1);
            }
        }).exceptionally(e -> {
            future.completeExceptionally(e);
            return null;
        });

        return future;
    }

    private CompletableFuture<Integer> createNewQuizLearning() {
        QuizLearningRepository repo = new QuizLearningRepository();
        int accountId = Prefs.getUserId(requireContext());
        CompletableFuture<Integer> future = new CompletableFuture<>();

        QuizLearning newLearning = new QuizLearning(
                quiz.id,
                accountId,
                "",      // learnedFlashCard
                0,       // learningFlashcardId
                false    // status
        );

        repo.add(newLearning)
                .thenAccept(saved -> {
                    if (saved != null) {
                        future.complete(saved.id);
                    } else {
                        future.complete(-1);
                    }
                })
                .exceptionally(e -> {
                    future.completeExceptionally(e);
                    return null;
                });

        return future;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
