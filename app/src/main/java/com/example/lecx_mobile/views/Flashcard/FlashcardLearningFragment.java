package com.example.lecx_mobile.views.Flashcard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.lecx_mobile.R;
import com.example.lecx_mobile.models.Flashcard;
import com.example.lecx_mobile.models.Quiz;
import com.example.lecx_mobile.models.QuizLearning;
import com.example.lecx_mobile.repositories.implementations.FlashcardRepository;
import com.example.lecx_mobile.repositories.implementations.QuizLearningRepository;
import com.example.lecx_mobile.repositories.implementations.QuizRepository;
import com.example.lecx_mobile.repositories.interfaces.IFlashcardRepository;
import com.example.lecx_mobile.repositories.interfaces.IQuizLearningRepository;
import com.example.lecx_mobile.repositories.interfaces.IQuizRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class FlashcardLearningFragment extends Fragment {

    // UI Components
    private ImageButton btnBack;
    private TextView tvQuizTitle, tvQuizSubtitle, tvProgress;
    private CardView cardFlashcard;
    private ScrollView cardFront, cardBack;
    private LinearLayout cardFrontLayout, cardBackLayout, actionButtonsLayout;
    private TextView tvFrontText, tvFrontHint, tvFrontSubtext;
    private ImageView ivFrontImage;
    private TextView tvBackText, tvBackDescription;
    private ProgressBar progressBar;
    private FloatingActionButton btnKnow, btnDontKnow;
    private View loadingOverlay; // Loading overlay view
    private View mainContent; // Main content view

    // Data
    private int quizLearningId = 1; // 📌 Mặc định = 1, có thể nhận từ arguments
    private QuizLearning quizLearning;
    private Quiz currentQuiz;
    
    // 📝 Danh sách tất cả flashcards của quiz
    private List<Flashcard> allFlashcards = new ArrayList<>();
    
    // 📋 LIST: Danh sách flashcards chưa học (trừ đi learnedIds)
    private List<Flashcard> unlearnedFlashcards = new ArrayList<>();
    
    // 🎯 Flashcard hiện tại đang hiển thị
    private Flashcard displayFlashcard;
    
    // 📊 IDs của các flashcard đã học
    private List<Integer> learnedIds = new ArrayList<>();
    
    // 🔄 Trạng thái lật thẻ
    private boolean isShowingFront = true;

    // 🔄 Flags để track loading state
    private boolean isQuizLoaded = false;
    private boolean isFlashcardsLoaded = false;

    // Repositories
    private final IQuizLearningRepository quizLearningRepo = new QuizLearningRepository();
    private final IQuizRepository quizRepo = new QuizRepository();
    private final IFlashcardRepository flashcardRepo = new FlashcardRepository();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 📌 Nhận quizLearningId từ arguments (nếu có)
        if (getArguments() != null) {
            quizLearningId = getArguments().getInt("quizLearningId", 1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_flashcard_learning, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
        
        // 🚀 Bắt đầu flow: Load QuizLearning
        loadQuizLearning();
    }

    private void initViews(View view) {
        // Loading overlay và main content
        loadingOverlay = view.findViewById(R.id.loadingOverlay);
        mainContent = view.findViewById(R.id.mainContent);

        btnBack = view.findViewById(R.id.btnBack);
        tvQuizTitle = view.findViewById(R.id.tvQuizTitle);
        tvQuizSubtitle = view.findViewById(R.id.tvQuizSubtitle);
        tvProgress = view.findViewById(R.id.tvProgress);
        progressBar = view.findViewById(R.id.progressBar);

        cardFlashcard = view.findViewById(R.id.cardFlashcard);
        cardFront = view.findViewById(R.id.cardFront);
        cardBack = view.findViewById(R.id.cardBack);
        cardFrontLayout = view.findViewById(R.id.cardFrontLayout);
        cardBackLayout = view.findViewById(R.id.cardBackLayout);

        tvFrontText = view.findViewById(R.id.tvFrontText);
        tvFrontHint = view.findViewById(R.id.tvFrontHint);
        tvFrontSubtext = view.findViewById(R.id.tvFrontSubtext);
        ivFrontImage = view.findViewById(R.id.ivFrontImage);

        tvBackText = view.findViewById(R.id.tvBackText);
        tvBackDescription = view.findViewById(R.id.tvBackDescription);

        actionButtonsLayout = view.findViewById(R.id.actionButtonsLayout);
        btnKnow = view.findViewById(R.id.btnKnow);
        btnDontKnow = view.findViewById(R.id.btnDontKnow);

        // Ẩn content ban đầu
        if (mainContent != null) {
            mainContent.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        // 🔙 Nút Back
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        // 🔄 Click vào card để lật thẻ (set cho LinearLayout bên trong)
        cardFrontLayout.setOnClickListener(v -> flipCard());
        cardBackLayout.setOnClickListener(v -> flipCard());
        
        // Backup: Click vào ScrollView cũng lật thẻ
        cardFront.setOnClickListener(v -> flipCard());
        cardBack.setOnClickListener(v -> flipCard());

        // ✅ Nút OK (I Know) - Đánh dấu đã học
        btnKnow.setOnClickListener(v -> handleKnowButton());

        // ❌ Nút X (Don't Know) - Đổi flashcard khác
        btnDontKnow.setOnClickListener(v -> handleDontKnowButton());
    }

    // ========================================================================
    // 🔷 1️⃣ LOAD QUIZ LEARNING
    // ========================================================================
    
    /**
     * Bước 1: Nhận quizLearningId và load QuizLearning từ Firebase
     */
    private void loadQuizLearning() {
        // Hiển thị loading
        setLoading(true);

        quizLearningRepo.getById(quizLearningId).thenAccept(ql -> {
            if (getActivity() == null) return;
            
            getActivity().runOnUiThread(() -> {
                if (ql == null) {
                    setLoading(false);
                    Toast.makeText(getContext(), "QuizLearning not found!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                    return;
                }
                
                // 📝 Lưu QuizLearning
                quizLearning = ql;
                
                // 📊 Bước 2: Parse learnedFlashCard string thành List<Integer>
                learnedIds = parseLearnedIds(quizLearning.learnedFlashCard);
                
                // 🎯 Bước 3: Load Quiz và Flashcards
                loadQuizAndFlashcards();
            });
        }).exceptionally(e -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(getContext(), "Error loading QuizLearning: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                });
            }
            return null;
        });
    }
    
    // ========================================================================
    // 🔷 2️⃣ PARSE LEARNED IDS
    // ========================================================================
    
    /**
     * Parse chuỗi "1,2,3" thành List<Integer>
     */
    private List<Integer> parseLearnedIds(String learnedFlashCard) {
        List<Integer> ids = new ArrayList<>();
        
        if (learnedFlashCard == null || learnedFlashCard.trim().isEmpty()) {
            return ids; // Trả về list rỗng nếu chưa học gì
        }
        
        try {
            String[] parts = learnedFlashCard.split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    ids.add(Integer.parseInt(trimmed));
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        
        return ids;
    }
    
    // ========================================================================
    // 🔷 3️⃣ LOAD QUIZ & FLASHCARDS
    // ========================================================================
    
    /**
     * Load Quiz info và tất cả Flashcards của Quiz đó
     */
    private void loadQuizAndFlashcards() {
        // Reset flags
        isQuizLoaded = false;
        isFlashcardsLoaded = false;

        // 📖 Load Quiz
        quizRepo.getById(quizLearning.quizId).thenAccept(quiz -> {
            if (getActivity() == null) return;
            
            getActivity().runOnUiThread(() -> {
                if (quiz != null) {
                    currentQuiz = quiz;
                    tvQuizTitle.setText(quiz.title);
                    tvQuizSubtitle.setText(quiz.description != null ? quiz.description : "");
                    isQuizLoaded = true;
                } else {
                    // Quiz không tồn tại
                    setLoading(false);
                    Toast.makeText(getContext(), "Quiz not found!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                    return;
                }
                
                // Kiểm tra xem cả hai đã load xong chưa
                checkAndFinishLoading();
            });
        }).exceptionally(e -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(getContext(), "Error loading Quiz: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                });
            }
            return null;
        });
        
        // 🃏 Load tất cả Flashcards của Quiz
        flashcardRepo.where(flashcard -> flashcard.quizId == quizLearning.quizId)
                .thenAccept(allCards -> {
                    if (getActivity() == null) return;
                    
                    getActivity().runOnUiThread(() -> {
                        if (allCards == null || allCards.isEmpty()) {
                            setLoading(false);
                            Toast.makeText(getContext(), "No flashcards found", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(requireView()).navigateUp();
                            return;
                        }
                        
                        // Lưu danh sách flashcards
                        allFlashcards.clear();
                        allFlashcards.addAll(allCards);
                        
                        // 📋 Tạo LIST: flashcards chưa học (trừ đi learnedIds)
                        filterUnlearnedFlashcards();
                        
                        isFlashcardsLoaded = true;
                        
                        // Kiểm tra xem cả hai đã load xong chưa
                        checkAndFinishLoading();
                    });
                }).exceptionally(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(getContext(), "Error loading flashcards: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(requireView()).navigateUp();
                        });
                    }
                    return null;
                });
    }

    /**
     * Kiểm tra xem cả quiz và flashcards đã load xong chưa, nếu xong thì ẩn loading và hiển thị UI
     */
    private void checkAndFinishLoading() {
        if (isQuizLoaded && isFlashcardsLoaded) {
            // 🎯 Bước 4: Hiển thị flashcard đầu tiên
            displayInitialFlashcard();
            
            // Ẩn loading và hiển thị content
            setLoading(false);
        }
    }
    
    // ========================================================================
    // 🔷 4️⃣ FILTER UNLEARNED FLASHCARDS
    // ========================================================================
    
    /**
     * Tạo LIST = allFlashcards trừ đi các flashcard đã học
     */
    private void filterUnlearnedFlashcards() {
        unlearnedFlashcards.clear();
        
        for (Flashcard card : allFlashcards) {
            if (!learnedIds.contains(card.id)) {
                unlearnedFlashcards.add(card);
            }
        }
    }
    
    // ========================================================================
    // 🔷 5️⃣ DISPLAY INITIAL FLASHCARD
    // ========================================================================
    
    /**
     * Hiển thị flashcard ban đầu:
     * - Nếu có learningFlashcardId -> hiển thị flashcard đó
     * - Nếu không -> chọn random từ LIST
     */
    private void displayInitialFlashcard() {
        if (unlearnedFlashcards.isEmpty()) {
            showCompletionMessage();
            return;
        }
        
        // 🎯 Kiểm tra xem có learningFlashcardId không
        if (quizLearning.learningFlashcardId > 0) {
            // Tìm flashcard theo ID
            Flashcard found = null;
            for (Flashcard card : unlearnedFlashcards) {
                if (card.id == quizLearning.learningFlashcardId) {
                    found = card;
                    break;
                }
            }
            
            if (found != null) {
                displayFlashcard(found);
            } else {
                // Không tìm thấy -> chọn random
                displayRandomFlashcard();
            }
        } else {
            // Không có learningFlashcardId -> chọn random
            displayRandomFlashcard();
        }
        
        updateProgress();
    }

    // ========================================================================
    // 🔷 6️⃣ DISPLAY FLASHCARD (UI)
    // ========================================================================
    
    /**
     * Hiển thị flashcard lên UI
     */
    private void displayFlashcard(Flashcard card) {
        if (card == null) return;
        
        // 🎯 Lưu flashcard hiện tại
        displayFlashcard = card;
        isShowingFront = true;
        
        // 📍 Cập nhật learningFlashcardId vào QuizLearning
        quizLearning.learningFlashcardId = card.id;
        updateQuizLearning();
        
        // 🔄 Reset card về mặt trước
        cardFront.setVisibility(View.VISIBLE);
        cardBack.setVisibility(View.GONE);
        cardFront.setRotationY(0);
        cardBack.setRotationY(180);
        
        // 📝 Hiển thị nội dung mặt trước
        tvFrontText.setText(card.frontText);
        
        // 🎨 Điều chỉnh UI dựa trên độ dài text và có/không có ảnh
        adjustFrontTextSize(card.frontText, card.frontImg);
        
        // 🖼️ Hiển thị hình ảnh (nếu có)
        if (card.frontImg != null && !card.frontImg.isEmpty()) {
            ivFrontImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(card.frontImg)
                    .placeholder(R.drawable.ic_logo)
                    .error(R.drawable.ic_logo)
                    .into(ivFrontImage);
        } else {
            ivFrontImage.setVisibility(View.GONE);
        }
        
        // 📝 Hiển thị nội dung mặt sau
        tvBackText.setText(card.backText);
        
        // Ẩn tvBackDescription (không cần nữa)
        tvBackDescription.setVisibility(View.GONE);
        
        // 🎨 Điều chỉnh kích thước text mặt sau
        adjustBackTextSize(card.backText);
        
        // 🔘 Buttons luôn hiển thị
        actionButtonsLayout.setVisibility(View.VISIBLE);
        
        // 📜 Scroll về đầu
        cardFront.post(() -> cardFront.scrollTo(0, 0));
        cardBack.post(() -> cardBack.scrollTo(0, 0));
    }
    
    /**
     * Điều chỉnh kích thước text mặt trước dựa trên độ dài và có/không có ảnh
     */
    private void adjustFrontTextSize(String text, String imageUrl) {
        int length = text.length();
        boolean hasImage = imageUrl != null && !imageUrl.isEmpty();
        
        if (length <= 3) {
            // Text ngắn (1-3 ký tự) - Lớn
            tvFrontText.setTextSize(hasImage ? 48 : 64);
        } else if (length <= 20) {
            // Text trung bình (4-20 ký tự)
            tvFrontText.setTextSize(hasImage ? 32 : 40);
        } else if (length <= 50) {
            // Text dài (21-50 ký tự)
            tvFrontText.setTextSize(hasImage ? 24 : 28);
        } else {
            // Text rất dài (>50 ký tự)
            tvFrontText.setTextSize(hasImage ? 18 : 22);
        }
    }
    
    /**
     * Điều chỉnh kích thước text mặt sau dựa trên độ dài
     */
    private void adjustBackTextSize(String text) {
        int length = text.length();
        
        if (length <= 20) {
            tvBackText.setTextSize(32);
        } else if (length <= 50) {
            tvBackText.setTextSize(24);
        } else {
            tvBackText.setTextSize(20);
        }
    }
    
    // ========================================================================
    // 🔷 7️⃣ DISPLAY RANDOM FLASHCARD (DISPLAY NEW FLASHCARD)
    // ========================================================================
    
    /**
     * Chọn ngẫu nhiên 1 flashcard từ LIST (trừ flashcard hiện tại)
     */
    private void displayRandomFlashcard() {
        if (unlearnedFlashcards.isEmpty()) {
            showCompletionMessage();
            return;
        }
        
        // 🎲 Chọn random flashcard (trừ flashcard hiện tại)
        Flashcard newCard;
        Random random = new Random();
        
        if (unlearnedFlashcards.size() == 1) {
            // Chỉ còn 1 thẻ -> hiển thị thẻ đó
            newCard = unlearnedFlashcards.get(0);
        } else {
            // Có nhiều thẻ -> chọn random (trừ thẻ hiện tại)
            do {
                int randomIndex = random.nextInt(unlearnedFlashcards.size());
                newCard = unlearnedFlashcards.get(randomIndex);
            } while (displayFlashcard != null && newCard.id == displayFlashcard.id);
        }
        
        // 🎯 Hiển thị flashcard mới
        displayFlashcard(newCard);
    }

    // ========================================================================
    // 🔷 8️⃣ FLIP CARD ANIMATION
    // ========================================================================
    
    /**
     * Lật thẻ từ mặt trước sang mặt sau hoặc ngược lại
     */
    private void flipCard() {
        if (isShowingFront) {
            // 🔄 Lật sang mặt sau
            animateFlip(cardFront, cardBack);
            isShowingFront = false;
        } else {
            // 🔄 Lật về mặt trước
            animateFlip(cardBack, cardFront);
            isShowingFront = true;
        }
    }
    
    /**
     * Animation lật thẻ
     */
    private void animateFlip(View fromView, View toView) {
        fromView.animate()
                .rotationY(90)
                .setDuration(200)
                .withEndAction(() -> {
                    fromView.setVisibility(View.GONE);
                    fromView.setRotationY(0);
                    
                    toView.setRotationY(-90);
                    toView.setVisibility(View.VISIBLE);
                    toView.animate()
                            .rotationY(0)
                            .setDuration(200)
                            .start();
                })
                .start();
    }
    
    // ========================================================================
    // 🔷 9️⃣ HANDLE BUTTON ACTIONS
    // ========================================================================
    
    /**
     * 4️⃣ Khi nhấn nút OK (I Know):
     * - Thêm displayFlashcard.id vào learnedFlashCard
     * - Update QuizLearning
     * - Xóa khỏi LIST
     * - Hiển thị flashcard mới hoặc hoàn thành
     */
    private void handleKnowButton() {
        if (displayFlashcard == null) return;
        
        // ✅ Thêm ID vào learnedIds
        learnedIds.add(displayFlashcard.id);
        
        // 📝 Convert List thành String "1,2,3"
        String learnedString = convertIdsToString(learnedIds);
        quizLearning.learnedFlashCard = learnedString;
        
        // 🗑️ Xóa flashcard này khỏi LIST
        unlearnedFlashcards.remove(displayFlashcard);
        
        // ✨ Kiểm tra còn flashcard nào không
        if (unlearnedFlashcards.isEmpty()) {
            // 📍 Đã học xong -> set learningFlashcardId = 0
            quizLearning.learningFlashcardId = 0;
            quizLearning.status = true;
            
            // 🔄 Update QuizLearning lên Firebase
            updateQuizLearning();
            
            // 📊 Cập nhật progress lần cuối (đã học xong)
            updateProgress();
            showCompletionMessage();
        } else {
            // Hiển thị flashcard mới (sẽ tự động update learningFlashcardId trong displayFlashcard)
            displayRandomFlashcard();
            // 📊 Cập nhật progress sau khi đã có flashcard mới
            updateProgress();
        }
    }
    
    /**
     * 5️⃣ Khi nhấn nút X (Don't Know):
     * - Hiển thị flashcard ngẫu nhiên khác (trừ flashcard hiện tại)
     */
    private void handleDontKnowButton() {
        // ❌ Chỉ đổi flashcard khác, không cập nhật QuizLearning
        displayRandomFlashcard();
    }
    
    // ========================================================================
    // 🔷 🔟 UPDATE QUIZ LEARNING
    // ========================================================================
    
    /**
     * Update QuizLearning lên Firebase
     */
    private void updateQuizLearning() {
        quizLearningRepo.update(quizLearning).thenAccept(updated -> {
            // Success - không cần thông báo gì
        }).exceptionally(e -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Failed to update progress", Toast.LENGTH_SHORT).show()
                );
            }
            return null;
        });
    }
    
    // ========================================================================
    // 🔷 1️⃣1️⃣ HELPER METHODS
    // ========================================================================
    
    /**
     * Convert List<Integer> thành String "1,2,3"
     */
    private String convertIdsToString(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            sb.append(ids.get(i));
            if (i < ids.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
    
    /**
     * Cập nhật progress bar và text
     */
    private void updateProgress() {
        int total = allFlashcards.size();
        int learned = learnedIds.size();
        
        // 🎯 Logic đúng:
        // - Nếu còn thẻ chưa học: hiển thị số thẻ đang xem (learned + 1)
        // - Nếu đã học xong: hiển thị tổng số đã học (learned)
        int current = unlearnedFlashcards.isEmpty() ? learned : (learned + 1);
        
        tvProgress.setText(current + "/" + total);
        
        int progressPercent = (int) ((learned * 100.0) / total);
        progressBar.setProgress(progressPercent);
    }
    
    /**
     * Hiển thị thông báo hoàn thành
     */
    private void showCompletionMessage() {
        Toast.makeText(getContext(), 
                "🎉 Congratulations! You've completed all flashcards!", 
                Toast.LENGTH_LONG).show();
        
        // Navigate back sau 2 giây
        requireView().postDelayed(() -> {
            if (isAdded()) {
                Navigation.findNavController(requireView()).navigateUp();
            }
        }, 2000);
    }

    // ========================================================================
    // 🔷 LOADING OVERLAY METHODS
    // ========================================================================

    /**
     * Hiển thị/ẩn Loading Overlay khi đang load dữ liệu
     */
    private void setLoading(boolean loading) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // Hiển thị/ẩn loading overlay
                if (loadingOverlay != null) {
                    loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
                }
                
                // Hiển thị/ẩn main content
                if (mainContent != null) {
                    mainContent.setVisibility(loading ? View.GONE : View.VISIBLE);
                }
                
                // Disable các nút khi đang load
                if (btnBack != null) {
                    btnBack.setEnabled(!loading);
                }
                if (btnKnow != null) {
                    btnKnow.setEnabled(!loading);
                }
                if (btnDontKnow != null) {
                    btnDontKnow.setEnabled(!loading);
                }
            });
        }
    }
}

