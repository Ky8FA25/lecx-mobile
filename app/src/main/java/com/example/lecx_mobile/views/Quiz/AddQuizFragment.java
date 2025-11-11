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
import com.example.lecx_mobile.databinding.FragmentAddQuizBinding;
import com.example.lecx_mobile.models.Flashcard;
import com.example.lecx_mobile.models.Quiz;
import com.example.lecx_mobile.repositories.implementations.FlashcardRepository;
import com.example.lecx_mobile.repositories.implementations.QuizRepository;
import com.example.lecx_mobile.repositories.interfaces.IFlashcardRepository;
import com.example.lecx_mobile.repositories.interfaces.IQuizRepository;
import com.example.lecx_mobile.services.implementations.StorageService;
import com.example.lecx_mobile.services.interfaces.IStorageService;
import com.example.lecx_mobile.utils.FirebaseStorageUtils;
import com.example.lecx_mobile.utils.Prefs;
import com.example.lecx_mobile.views.Auth.LoginActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Fragment để thêm quiz mới
 */
public class AddQuizFragment extends Fragment {

    private FragmentAddQuizBinding binding;
    private FlashcardEditAdapter flashcardAdapter;
    private View loadingOverlay;
    private List<Flashcard> flashcards;

    // Repositories
    private final IQuizRepository quizRepository = new QuizRepository();
    private final IFlashcardRepository flashcardRepository = new FlashcardRepository();
    private final IStorageService storageService = new StorageService();

    // Image picker
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;
    private int editingFlashcardIndex = -1; // -1 means adding new, >= 0 means editing
    private DialogAddEditFlashcardBinding currentDialogBinding; // Store dialog binding for image preview
    private boolean imageRemoved = false; // Track if image was removed in current edit session

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Firebase Storage
        FirebaseStorageUtils.init(requireContext());

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
        binding = FragmentAddQuizBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingOverlay = view.findViewById(R.id.loadingOverlay);
        flashcards = new ArrayList<>();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup event handlers
        setupEventHandlers();
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
        updateFlashcardCount();
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

            // If image is selected, upload it first
            if (selectedImageUri != null) {
                uploadImageAndSaveFlashcard(dialog, frontText, backText, flashcard);
            } else {
                // Save flashcard without new image (or keep existing image if editing and not removed)
                String imageUrl = null;
                if (flashcard != null && finalOriginalImageUrl != null) {
                    // Editing existing flashcard in local list
                    if (imageRemoved) {
                        // Image was removed
                        imageUrl = null;
                    } else {
                        // Keep existing image
                        imageUrl = finalOriginalImageUrl;
                    }
                }
                saveFlashcardToLocal(frontText, backText, imageUrl, flashcard);
                dialog.dismiss();
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
            if (imageUrl != null) {
                flashcard.frontImg = imageUrl;
            }
            flashcardAdapter.updateFlashcard(editingFlashcardIndex, flashcard);
        } else {
            // Add new flashcard (quizId will be set when saving quiz)
            Flashcard flashcard = new Flashcard();
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
                    flashcards.remove(position);
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

        if (flashcards.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng thêm ít nhất một thẻ", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        // Get current user ID
        int userId = Prefs.getUserId(requireContext());
        if (userId == -1) {
            setLoading(false);
            Toast.makeText(requireContext(), "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
            Prefs.clearSession(requireContext());
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

        // Create quiz
        Quiz quiz = new Quiz();
        quiz.title = title;
        quiz.description = description;
        quiz.accountId = userId;
        quiz.numberOfFlashcards = flashcards.size();
        quiz.isPublic = isPublic;
        quiz.createdAt = System.currentTimeMillis();

        // Save quiz to database
        quizRepository.add(quiz)
                .thenAccept(savedQuiz -> {
                    if (savedQuiz == null) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                setLoading(false);
                                Toast.makeText(requireContext(), "Lỗi khi lưu quiz", Toast.LENGTH_SHORT).show();
                            });
                        }
                        return;
                    }

                    // Save all flashcards
                    saveFlashcards(savedQuiz.id, 0);
                })
                .exceptionally(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(requireContext(), "Lỗi khi lưu quiz: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                    return null;
                });
    }

    /**
     * Save flashcards recursively
     */
    private void saveFlashcards(int quizId, int index) {
        if (index >= flashcards.size()) {
            // All flashcards saved
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(requireContext(), "Lưu quiz thành công", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                });
            }
            return;
        }

        Flashcard flashcard = flashcards.get(index);
        flashcard.quizId = quizId;

        flashcardRepository.add(flashcard)
                .thenAccept(savedFlashcard -> {
                    if (savedFlashcard != null) {
                        // Save next flashcard
                        saveFlashcards(quizId, index + 1);
                    } else {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                setLoading(false);
                                Toast.makeText(requireContext(), "Lỗi khi lưu thẻ thứ " + (index + 1), Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                })
                .exceptionally(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(requireContext(), "Lỗi khi lưu thẻ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                    return null;
                });
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

