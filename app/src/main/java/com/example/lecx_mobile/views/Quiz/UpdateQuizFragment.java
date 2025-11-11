package com.example.lecx_mobile.views.Quiz;

import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.lecx_mobile.R;
import com.example.lecx_mobile.adapters.FlashcardEditAdapter;
import com.example.lecx_mobile.databinding.DialogAddEditFlashcardBinding;
import com.example.lecx_mobile.databinding.FragmentUpdateQuizBinding;
import com.example.lecx_mobile.models.Flashcard;
import com.example.lecx_mobile.models.Quiz;
import com.example.lecx_mobile.models.QuizLearning;
import com.example.lecx_mobile.repositories.implementations.FlashcardRepository;
import com.example.lecx_mobile.repositories.implementations.QuizLearningRepository;
import com.example.lecx_mobile.repositories.implementations.QuizRepository;
import com.example.lecx_mobile.repositories.interfaces.IFlashcardRepository;
import com.example.lecx_mobile.repositories.interfaces.IQuizLearningRepository;
import com.example.lecx_mobile.repositories.interfaces.IQuizRepository;
import com.example.lecx_mobile.services.implementations.StorageService;
import com.example.lecx_mobile.services.interfaces.IStorageService;
import com.example.lecx_mobile.utils.FirebaseStorageUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Fragment để sửa quiz
 */
public class UpdateQuizFragment extends Fragment {

    private static final String ARG_QUIZ_ID = "quizId";

    private FragmentUpdateQuizBinding binding;
    private FlashcardEditAdapter flashcardAdapter;
    private View loadingOverlay;
    private List<Flashcard> flashcards;
    private List<Flashcard> originalFlashcards; // Store original flashcards for comparison
    private Quiz quiz;
    private int quizId;

    // Repositories
    private final IQuizRepository quizRepository = new QuizRepository();
    private final IFlashcardRepository flashcardRepository = new FlashcardRepository();
    private final IQuizLearningRepository quizLearningRepository = new QuizLearningRepository();
    private final IStorageService storageService = new StorageService();

    // Image picker
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;
    private int editingFlashcardIndex = -1; // -1 means adding new, >= 0 means editing
    private DialogAddEditFlashcardBinding currentDialogBinding; // Store dialog binding for image preview
    private boolean imageRemoved = false; // Track if image was removed in current edit session

    /**
     * Factory method để tạo instance của fragment với quizId
     *
     * @param quizId ID của quiz cần sửa
     * @return UpdateQuizFragment instance
     */
    public static UpdateQuizFragment newInstance(int quizId) {
        UpdateQuizFragment fragment = new UpdateQuizFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_QUIZ_ID, quizId);
        fragment.setArguments(args);
        return fragment;
    }

    public UpdateQuizFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Storage
        FirebaseStorageUtils.init(requireContext());

        // Get quizId from arguments, default to 1
        if (getArguments() != null) {
            quizId = getArguments().getInt(ARG_QUIZ_ID, 1);
        } else {
            quizId = 1; // Temporary default
        }

        // Initialize image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            selectedImageUri = imageUri;
                            // Update dialog image preview if dialog is showing
                            if (currentDialogBinding != null) {
                                currentDialogBinding.ivImagePreview.setVisibility(View.VISIBLE);
                                Glide.with(requireContext())
                                        .load(imageUri)
                                        .placeholder(R.drawable.ic_profile)
                                        .error(R.drawable.ic_profile)
                                        .centerCrop()
                                        .into(currentDialogBinding.ivImagePreview);
                                currentDialogBinding.btnRemoveImage.setVisibility(View.VISIBLE);
                                imageRemoved = false; // Reset removal flag when new image is selected
                            }
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUpdateQuizBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingOverlay = view.findViewById(R.id.loadingOverlay);
        flashcards = new ArrayList<>();

        // Hide content initially
        binding.scrollView.setVisibility(View.GONE);

        // Setup RecyclerView
        setupRecyclerView();

        // Setup event handlers
        setupEventHandlers();

        // Load data from database
        loadDataFromDatabase();
    }

    /**
     * Khởi tạo RecyclerView cho danh sách flashcard
     */
    private void setupRecyclerView() {
        flashcardAdapter = new FlashcardEditAdapter(flashcards, new FlashcardEditAdapter.OnFlashcardActionListener() {
            @Override
            public void onEditFlashcard(Flashcard flashcard, int position) {
                showAddEditFlashcardDialog(flashcard, position);
            }

            @Override
            public void onDeleteFlashcard(Flashcard flashcard, int position) {
                showDeleteConfirmDialog(flashcard, position);
            }
        });
        binding.rvFlashcards.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFlashcards.setAdapter(flashcardAdapter);
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

        // Nút thêm flashcard
        binding.btnAddFlashcard.setOnClickListener(v -> {
            showAddEditFlashcardDialog(null, -1);
        });

        // Nút lưu quiz
        binding.btnSave.setOnClickListener(v -> {
            saveQuiz();
        });
    }

    /**
     * Load dữ liệu từ database: quiz và flashcards
     */
    private void loadDataFromDatabase() {
        setLoading(true);

        // Load quiz
        quizRepository.getById(quizId)
                .thenAccept(quizResult -> {
                    if (quizResult == null) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                setLoading(false);
                                Toast.makeText(requireContext(), "Không tìm thấy quiz", Toast.LENGTH_SHORT).show();
                                requireActivity().onBackPressed();
                            });
                        }
                        return;
                    }

                    quiz = quizResult;

                    // Load flashcards
                    flashcardRepository.where(flashcard -> flashcard.quizId == quizId)
                            .thenAccept(flashcardsResult -> {
                                if (flashcardsResult != null) {
                                    flashcards = new ArrayList<>(flashcardsResult);
                                    // Store original flashcards for comparison
                                    originalFlashcards = new ArrayList<>();
                                    for (Flashcard fc : flashcardsResult) {
                                        // Create a copy to store original state
                                        Flashcard copy = new Flashcard();
                                        copy.id = fc.id;
                                        copy.quizId = fc.quizId;
                                        copy.frontText = fc.frontText;
                                        copy.backText = fc.backText;
                                        copy.frontImg = fc.frontImg;
                                        originalFlashcards.add(copy);
                                    }
                                } else {
                                    flashcards = new ArrayList<>();
                                    originalFlashcards = new ArrayList<>();
                                }

                                // Update UI on Main Thread
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        setLoading(false);
                                        bindDataToUI();
                                    });
                                }
                            })
                            .exceptionally(e -> {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        setLoading(false);
                                        Toast.makeText(requireContext(),
                                                "Lỗi khi tải danh sách thẻ: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                        // Still show quiz data
                                        bindDataToUI();
                                    });
                                }
                                return null;
                            });
                })
                .exceptionally(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(requireContext(),
                                    "Lỗi khi tải dữ liệu quiz: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            requireActivity().onBackPressed();
                        });
                    }
                    return null;
                });
    }

    /**
     * Bind dữ liệu vào UI
     */
    private void bindDataToUI() {
        if (quiz == null) {
            return;
        }

        // Show content
        binding.scrollView.setVisibility(View.VISIBLE);

        // Set title
        if (quiz.title != null) {
            binding.etTitle.setText(quiz.title);
        }

        // Set description
        if (quiz.description != null) {
            binding.etDescription.setText(quiz.description);
        }

        // Set isPublic
        binding.switchIsPublic.setChecked(quiz.isPublic);

        // Update flashcard list
        flashcardAdapter.updateFlashcards(flashcards);
        updateFlashcardCount();
    }

    /**
     * Hiển thị dialog thêm/sửa flashcard
     */
    private void showAddEditFlashcardDialog(Flashcard flashcard, int position) {
        editingFlashcardIndex = position;
        selectedImageUri = null;
        imageRemoved = false;

        // Inflate dialog layout
        DialogAddEditFlashcardBinding dialogBinding = DialogAddEditFlashcardBinding.inflate(getLayoutInflater());
        currentDialogBinding = dialogBinding; // Store reference for image picker

        // Store original image URL for editing
        String originalImageUrl = null;

        // Set title
        if (flashcard == null) {
            dialogBinding.tvDialogTitle.setText("Thêm thẻ");
        } else {
            dialogBinding.tvDialogTitle.setText("Sửa thẻ");
            dialogBinding.etFrontText.setText(flashcard.frontText != null ? flashcard.frontText : "");
            dialogBinding.etBackText.setText(flashcard.backText != null ? flashcard.backText : "");

            // Store original image URL
            if (flashcard.frontImg != null && !flashcard.frontImg.trim().isEmpty()) {
                originalImageUrl = flashcard.frontImg;
                dialogBinding.ivImagePreview.setVisibility(View.VISIBLE);
                Glide.with(requireContext())
                        .load(flashcard.frontImg)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .centerCrop()
                        .into(dialogBinding.ivImagePreview);
                dialogBinding.btnRemoveImage.setVisibility(View.VISIBLE);
            }
        }

        final String finalOriginalImageUrl = originalImageUrl;

        // Button select image
        dialogBinding.btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Button remove image
        dialogBinding.btnRemoveImage.setOnClickListener(v -> {
            selectedImageUri = null;
            imageRemoved = true;
            dialogBinding.ivImagePreview.setVisibility(View.GONE);
            dialogBinding.btnRemoveImage.setVisibility(View.GONE);
        });

        // Create dialog
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.getRoot())
                .setCancelable(true)
                .create();

        // Button cancel
        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Button save
        dialogBinding.btnSave.setOnClickListener(v -> {
            dialog.dismiss();
            setLoading(true);
            String frontText = dialogBinding.etFrontText.getText() != null
                    ? dialogBinding.etFrontText.getText().toString().trim()
                    : "";
            String backText = dialogBinding.etBackText.getText() != null
                    ? dialogBinding.etBackText.getText().toString().trim()
                    : "";

            if (frontText.isEmpty() || backText.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // If new image is selected, upload it first
            if (selectedImageUri != null) {
                uploadImageAndSaveFlashcard(dialog, frontText, backText, flashcard);
            } else {
                // Save flashcard without new image (or keep existing image if editing and not removed)
                String imageUrl = null;
                if (flashcard != null && finalOriginalImageUrl != null) {
                    // Editing existing flashcard
                    if (imageRemoved) {
                        // Image was removed
                        imageUrl = null;
                    } else {
                        // Keep existing image
                        imageUrl = finalOriginalImageUrl;
                    }
                }
                saveFlashcardToLocal(frontText, backText, imageUrl, flashcard);
                setLoading(false);
                currentDialogBinding = null;
            }
        });

        dialog.setOnDismissListener(d -> {
            currentDialogBinding = null;
            selectedImageUri = null;
            imageRemoved = false;
        });

        dialog.show();
    }

    /**
     * Upload image and save flashcard
     */
    private void uploadImageAndSaveFlashcard(AlertDialog dialog, String frontText, String backText, Flashcard existingFlashcard) {
        setLoading(true);

        String fileName = UUID.randomUUID().toString() + ".jpg";
        storageService.uploadFlashcardImage(selectedImageUri, fileName)
                .addOnSuccessListener(imageUrl -> {
                    saveFlashcardToLocal(frontText, backText, imageUrl, existingFlashcard);
                    dialog.dismiss();
                    currentDialogBinding = null;
                    setLoading(false);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(requireContext(), "Lỗi khi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Save flashcard to local list
     */
    private void saveFlashcardToLocal(String frontText, String backText, String imageUrl, Flashcard existingFlashcard) {
        if (editingFlashcardIndex >= 0 && editingFlashcardIndex < flashcards.size()) {
            // Update existing flashcard
            Flashcard flashcard = flashcards.get(editingFlashcardIndex);
            flashcard.frontText = frontText;
            flashcard.backText = backText;
            flashcard.frontImg = imageUrl;
            flashcardAdapter.updateFlashcard(editingFlashcardIndex, flashcard);
        } else {
            // Add new flashcard
            Flashcard flashcard = new Flashcard();
            flashcard.quizId = quizId;
            flashcard.frontText = frontText;
            flashcard.backText = backText;
            flashcard.frontImg = imageUrl;
            flashcardAdapter.addFlashcard(flashcard);
        }
        updateFlashcardCount();
    }

    /**
     * Show delete confirmation dialog
     */
    private void showDeleteConfirmDialog(Flashcard flashcard, int position) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa thẻ")
                .setMessage("Bạn có chắc chắn muốn xóa thẻ này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    flashcardAdapter.removeFlashcard(position);
                    updateFlashcardCount();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Update flashcard count text
     */
    private void updateFlashcardCount() {
        int count = flashcards.size();
        binding.tvFlashcardCount.setText(count + " thẻ");
    }

    /**
     * Save quiz and flashcards to database
     */
    private void saveQuiz() {
        String title = binding.etTitle.getText() != null
                ? binding.etTitle.getText().toString().trim()
                : "";
        String description = binding.etDescription.getText() != null
                ? binding.etDescription.getText().toString().trim()
                : "";
        boolean isPublic = binding.switchIsPublic.isChecked();

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show();
            return;
        }

        if (quiz == null) {
            Toast.makeText(requireContext(), "Lỗi: Quiz không tồn tại", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        // Update quiz
        quiz.title = title;
        quiz.description = description;
        quiz.isPublic = isPublic;
        quiz.numberOfFlashcards = flashcards.size();
        quiz.updatedAt = System.currentTimeMillis();

        // Update quiz in database
        quizRepository.update(quiz)
                .thenAccept(updatedQuiz -> {
                    if (updatedQuiz == null) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                setLoading(false);
                                Toast.makeText(requireContext(), "Lỗi khi cập nhật quiz", Toast.LENGTH_SHORT).show();
                            });
                        }
                        return;
                    }

                    // Delete removed flashcards first
                    deleteRemovedFlashcards();
                    deleteAllQuizLearningProcess();
                })
                .exceptionally(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(requireContext(), "Lỗi khi cập nhật quiz: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                    return null;
                });
    }

    private void deleteAllQuizLearningProcess() {

        quizLearningRepository.getAll().thenAccept(list -> {
            // lọc ra các quizLearning cần xóa
            List<QuizLearning> toDelete = list.stream()
                    .filter(q -> q.quizId == quizId && !q.status)
                    .collect(Collectors.toList());

            if (toDelete.isEmpty()) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                    });
                }
                return;
            }

            // thực hiện xóa lần lượt bằng CompletableFuture chain
            CompletableFuture<Boolean> chain = CompletableFuture.completedFuture(null);
            for (QuizLearning item : toDelete) {
                chain = chain.thenCompose(v -> quizLearningRepository.delete(item.id));
            }

            chain.thenRun(() -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        setLoading(false);
                    });
                }
            }).exceptionally(e -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
                return null;
            });

        }).exceptionally(e -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Không thể tải dữ liệu tiến trình học", Toast.LENGTH_SHORT).show();
                });
            }
            return null;
        });
    }


    /**
     * Delete flashcards that were removed from the list
     */
    private void deleteRemovedFlashcards() {
        if (originalFlashcards == null || originalFlashcards.isEmpty()) {
            // No original flashcards, just save current ones
            saveFlashcards(0);
            return;
        }

        // Find flashcards that were removed (exist in original but not in current)
        List<Flashcard> toDelete = new ArrayList<>();
        for (Flashcard original : originalFlashcards) {
            boolean found = false;
            for (Flashcard current : flashcards) {
                if (current.id > 0 && current.id == original.id) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                toDelete.add(original);
            }
        }

        if (toDelete.isEmpty()) {
            // No flashcards to delete, just save current ones
            saveFlashcards(0);
            return;
        }

        // Delete removed flashcards
        deleteFlashcardsRecursive(toDelete, 0);
    }

    /**
     * Delete flashcards recursively
     */
    private void deleteFlashcardsRecursive(List<Flashcard> toDelete, int index) {
        if (index >= toDelete.size()) {
            // All deletions done, save current flashcards
            saveFlashcards(0);
            return;
        }

        Flashcard flashcard = toDelete.get(index);
        flashcardRepository.delete(flashcard.id)
                .thenAccept(success -> {
                    if (success) {
                        // Continue deleting next flashcard
                        deleteFlashcardsRecursive(toDelete, index + 1);
                    } else {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                setLoading(false);
                                Toast.makeText(requireContext(), "Lỗi khi xóa thẻ", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                })
                .exceptionally(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(requireContext(), "Lỗi khi xóa thẻ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                    return null;
                });
    }

    /**
     * Save flashcards recursively (add new, update existing)
     */
    private void saveFlashcards(int index) {
        if (index >= flashcards.size()) {
            // All flashcards processed
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(requireContext(), "Cập nhật quiz thành công", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                });
            }
            return;
        }

        Flashcard flashcard = flashcards.get(index);
        flashcard.quizId = quizId;

        // Check if flashcard has ID (existing) or not (new)
        if (flashcard.id > 0) {
            // Update existing flashcard
            flashcardRepository.update(flashcard)
                    .thenAccept(savedFlashcard -> {
                        if (savedFlashcard != null) {
                            // Process next flashcard
                            saveFlashcards(index + 1);
                        } else {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    setLoading(false);
                                    Toast.makeText(requireContext(), "Lỗi khi cập nhật thẻ thứ " + (index + 1), Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    })
                    .exceptionally(e -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                setLoading(false);
                                Toast.makeText(requireContext(), "Lỗi khi cập nhật thẻ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                        return null;
                    });
        } else {
            // Add new flashcard
            flashcardRepository.add(flashcard)
                    .thenAccept(savedFlashcard -> {
                        if (savedFlashcard != null) {
                            // Update local flashcard with new ID
                            flashcard.id = savedFlashcard.id;
                            // Process next flashcard
                            saveFlashcards(index + 1);
                        } else {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    setLoading(false);
                                    Toast.makeText(requireContext(), "Lỗi khi thêm thẻ thứ " + (index + 1), Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    })
                    .exceptionally(e -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                setLoading(false);
                                Toast.makeText(requireContext(), "Lỗi khi thêm thẻ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                        return null;
                    });
        }
    }

    /**
     * Hiển thị/ẩn Loading Overlay
     */
    private void setLoading(boolean loading) {
        if (binding != null && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (loadingOverlay != null) {
                    loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
                }
                binding.scrollView.setEnabled(!loading);
                binding.btnSave.setEnabled(!loading);
                binding.btnAddFlashcard.setEnabled(!loading);
                binding.btnBack.setEnabled(!loading);
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

