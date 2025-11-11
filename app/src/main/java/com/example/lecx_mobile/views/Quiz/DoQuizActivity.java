package com.example.lecx_mobile.views.Quiz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.lecx_mobile.utils.GeminiUtils;
import com.google.android.material.button.MaterialButton;

import com.bumptech.glide.Glide;
import com.example.lecx_mobile.R;
import com.example.lecx_mobile.databinding.ActivityDoQuizBinding;
import com.example.lecx_mobile.models.Flashcard;
import com.example.lecx_mobile.models.Question;
import com.example.lecx_mobile.repositories.implementations.FlashcardRepository;
import com.example.lecx_mobile.repositories.implementations.QuestionRepository;
import com.example.lecx_mobile.repositories.interfaces.IFlashcardRepository;
import com.example.lecx_mobile.repositories.interfaces.IQuestionRepository;
import com.example.lecx_mobile.utils.GeminiUtils;
import com.example.lecx_mobile.utils.LoadingUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Activity để làm quiz
 * Logic:
 * 1. Nhận quizId từ Intent
 * 2. Kiểm tra xem quiz đã có câu hỏi chưa
 * 3. Nếu có → dùng trực tiếp
 * 4. Nếu không → tạo từ flashcards + Gemini API
 * 5. Hiển thị quiz và chấm điểm
 */
public class DoQuizActivity extends AppCompatActivity {

    // Intent key
    public static final String EXTRA_QUIZ_ID = "quizId";

    // ViewBinding
    private ActivityDoQuizBinding binding;

    // Repositories
    private final IQuestionRepository questionRepository = new QuestionRepository();
    private final IFlashcardRepository flashcardRepository = new FlashcardRepository();
    private final GeminiUtils geminiService = new GeminiUtils();

    // Data
    private int quizId;
    private List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private String selectedAnswer = "";
    private boolean isAnswerSelected = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lấy quizId từ Intent, mặc định = 1 (tạm thời để test)
        quizId = getIntent().getIntExtra(EXTRA_QUIZ_ID, 1);
        if (quizId == -1) {
            quizId = 1; // Fallback về 1 nếu -1
        }

        // Setup UI
        setupUI();

        // Load questions
        loadQuestions();
    }

    /**
     * Setup UI components và event handlers
     */
    private void setupUI() {
        // Nút Back
        binding.btnBack.setOnClickListener(v -> finish());

        // Answer card listeners
        binding.cardAnswerA.setOnClickListener(v -> onAnswerSelected("A"));
        binding.cardAnswerB.setOnClickListener(v -> onAnswerSelected("B"));
        binding.cardAnswerC.setOnClickListener(v -> onAnswerSelected("C"));
        binding.cardAnswerD.setOnClickListener(v -> onAnswerSelected("D"));
    }
    
    /**
     * Xử lý khi người dùng chọn đáp án
     */
    private void onAnswerSelected(String answerLetter) {
        if (isAnswerSelected) {
            android.util.Log.d("DoQuizActivity", "Answer already selected, ignoring click");
            return; // Đã chọn rồi, không cho chọn lại
        }
        
        android.util.Log.d("DoQuizActivity", "Answer selected: " + answerLetter);
        isAnswerSelected = true;
        Question question = questions.get(currentQuestionIndex);
        
        // Lấy text đáp án đã chọn
        TextView selectedTextView = getAnswerTextView(answerLetter);
        if (selectedTextView == null) {
            android.util.Log.e("DoQuizActivity", "Selected TextView is null for letter: " + answerLetter);
            return;
        }
        
        selectedAnswer = selectedTextView.getText().toString();
        android.util.Log.d("DoQuizActivity", "Selected answer text: " + selectedAnswer);
        
        // Vô hiệu hóa tất cả cards
        disableAllAnswerCards();
        
        // Kiểm tra đúng/sai
        String correctAnswer = question.correctAnswer != null ? question.correctAnswer.trim() : "";
        android.util.Log.d("DoQuizActivity", "Correct answer: " + correctAnswer);
        boolean isCorrect = selectedAnswer.trim().equalsIgnoreCase(correctAnswer);
        android.util.Log.d("DoQuizActivity", "Is correct: " + isCorrect);
        
        // Tìm đáp án đúng
        String correctAnswerLetter = findCorrectAnswerLetter(question);
        android.util.Log.d("DoQuizActivity", "Correct answer letter: " + correctAnswerLetter);
        
        // Hiển thị kết quả
        if (isCorrect) {
            score++;
            android.util.Log.d("DoQuizActivity", "Showing check icon for: " + answerLetter);
            // Hiển thị tích xanh ở đáp án đã chọn
            showResultIcon(answerLetter, true);
        } else {
            android.util.Log.d("DoQuizActivity", "Showing X icon for: " + answerLetter);
            // Hiển thị tích X ở đáp án sai
            showResultIcon(answerLetter, false);
            // Hiển thị tích xanh ở đáp án đúng
            if (correctAnswerLetter != null) {
                android.util.Log.d("DoQuizActivity", "Showing check icon for correct: " + correctAnswerLetter);
                showResultIcon(correctAnswerLetter, true);
            }
        }
        
        // Cập nhật score
        binding.tvScore.setText(String.format("%02d", score));
        
        // Tự động chuyển sang câu tiếp theo sau 1.5 giây
        handler.postDelayed(() -> {
            if (currentQuestionIndex < questions.size() - 1) {
                showQuestion(currentQuestionIndex + 1);
            } else {
                finishQuiz();
            }
        }, 1500);
    }
    
    /**
     * Lấy TextView của đáp án theo letter
     */
    private TextView getAnswerTextView(String letter) {
        switch (letter) {
            case "A": return binding.tvAnswerA;
            case "B": return binding.tvAnswerB;
            case "C": return binding.tvAnswerC;
            case "D": return binding.tvAnswerD;
            default: return null;
        }
    }
    
    /**
     * Lấy ImageView result icon theo letter
     */
    private ImageView getResultImageView(String letter) {
        switch (letter) {
            case "A": return binding.ivResultA;
            case "B": return binding.ivResultB;
            case "C": return binding.ivResultC;
            case "D": return binding.ivResultD;
            default: return null;
        }
    }
    
    /**
     * Hiển thị icon tích xanh hoặc X
     */
    private void showResultIcon(String letter, boolean isCorrect) {
        ImageView iconView = getResultImageView(letter);
        if (iconView == null) {
            android.util.Log.e("DoQuizActivity", "Result ImageView is null for letter: " + letter);
            return;
        }
        
        android.util.Log.d("DoQuizActivity", "Setting icon for " + letter + ", isCorrect: " + isCorrect);
        
        // Chạy trên UI thread để đảm bảo
        runOnUiThread(() -> {
            iconView.setVisibility(View.VISIBLE);
            if (isCorrect) {
                iconView.setImageResource(R.drawable.ic_check_circle);
                android.util.Log.d("DoQuizActivity", "Set check icon for " + letter);
            } else {
                iconView.setImageResource(R.drawable.ic_cancel_circle);
                android.util.Log.d("DoQuizActivity", "Set cancel icon for " + letter);
            }
            
            // Force refresh
            iconView.invalidate();
            iconView.requestLayout();
        });
    }
    
    /**
     * Tìm letter của đáp án đúng
     */
    private String findCorrectAnswerLetter(Question question) {
        String correctAnswer = question.correctAnswer != null ? question.correctAnswer.trim() : "";
        
        if (question.answerA != null && question.answerA.trim().equalsIgnoreCase(correctAnswer)) {
            return "A";
        } else if (question.answerB != null && question.answerB.trim().equalsIgnoreCase(correctAnswer)) {
            return "B";
        } else if (question.answerC != null && question.answerC.trim().equalsIgnoreCase(correctAnswer)) {
            return "C";
        } else if (question.answerD != null && question.answerD.trim().equalsIgnoreCase(correctAnswer)) {
            return "D";
        }
        return null;
    }
    
    /**
     * Vô hiệu hóa tất cả answer cards
     */
    private void disableAllAnswerCards() {
        binding.cardAnswerA.setClickable(false);
        binding.cardAnswerB.setClickable(false);
        binding.cardAnswerC.setClickable(false);
        binding.cardAnswerD.setClickable(false);
    }
    
    /**
     * Kích hoạt lại tất cả answer cards
     */
    private void enableAllAnswerCards() {
        binding.cardAnswerA.setClickable(true);
        binding.cardAnswerB.setClickable(true);
        binding.cardAnswerC.setClickable(true);
        binding.cardAnswerD.setClickable(true);
        
        // Ẩn tất cả result icons
        binding.ivResultA.setVisibility(View.GONE);
        binding.ivResultB.setVisibility(View.GONE);
        binding.ivResultC.setVisibility(View.GONE);
        binding.ivResultD.setVisibility(View.GONE);
    }

    /**
     * Load questions từ database hoặc tạo mới từ flashcards
     */
    private void loadQuestions() {
        LoadingUtils.showLoading(binding.loadingOverlay);

        // Bước 1: Kiểm tra xem quiz đã có câu hỏi chưa
        questionRepository.where(q -> q.quizId == quizId)
                .thenAccept(existingQuestions -> {
                    if (existingQuestions != null && !existingQuestions.isEmpty()) {
                        // Quiz đã có câu hỏi → sử dụng trực tiếp
                        runOnUiThread(() -> {
                            questions = existingQuestions;
                            score = 0; // Reset score
                            binding.tvScore.setText("00");
                            LoadingUtils.hideLoading(binding.loadingOverlay);
                            showQuestion(0);
                        });
                    } else {
                        // Quiz chưa có câu hỏi → tạo mới từ flashcards
                        runOnUiThread(() -> {
                            // Hiển thị loading với message khi đang generate
                            LoadingUtils.showLoading(binding.loadingOverlay);
                        });
                        generateQuestionsFromFlashcards();
                    }
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        LoadingUtils.hideLoading(binding.loadingOverlay);
                        Toast.makeText(this, "Lỗi khi tải câu hỏi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return null;
                });
    }

    /**
     * Tạo câu hỏi từ flashcards và Gemini API
     */
    private void generateQuestionsFromFlashcards() {
        // Lấy danh sách flashcards
        flashcardRepository.where(f -> f.quizId == quizId)
                .thenAccept(flashcards -> {
                    if (flashcards == null || flashcards.isEmpty()) {
                        runOnUiThread(() -> {
                            LoadingUtils.hideLoading(binding.loadingOverlay);
                            Toast.makeText(this, "Quiz không có flashcard nào", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                        return;
                    }

                    // Đảm bảo loading được hiển thị khi đang generate với Gemini
                    runOnUiThread(() -> {
                        LoadingUtils.showLoading(binding.loadingOverlay);
                    });

                    // Tạo questions từ flashcards
                    List<CompletableFuture<Question>> questionFutures = new ArrayList<>();

                    for (Flashcard flashcard : flashcards) {
                        CompletableFuture<Question> questionFuture = createQuestionFromFlashcard(flashcard);
                        questionFutures.add(questionFuture);
                    }

                    // Đợi tất cả questions được tạo xong
                    CompletableFuture.allOf(questionFutures.toArray(new CompletableFuture[0]))
                            .thenRun(() -> {
                                List<Question> generatedQuestions = new ArrayList<>();
                                for (CompletableFuture<Question> future : questionFutures) {
                                    try {
                                        Question q = future.join();
                                        if (q != null) {
                                            generatedQuestions.add(q);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                // Lưu questions vào database
                                saveQuestions(generatedQuestions);
                            });
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        LoadingUtils.hideLoading(binding.loadingOverlay);
                        Toast.makeText(this, "Lỗi khi tải flashcards: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return null;
                });
    }

    /**
     * Tạo một Question từ Flashcard
     */
    private CompletableFuture<Question> createQuestionFromFlashcard(Flashcard flashcard) {
        CompletableFuture<Question> future = new CompletableFuture<>();

        // Tạo question từ flashcard
        String questionText = flashcard.frontText != null ? flashcard.frontText : "";
        String correctAnswer = flashcard.backText != null ? flashcard.backText : "";
        String questionImg = flashcard.frontImg;

        // Gọi Gemini để tạo 3 đáp án sai
        geminiService.generateWrongAnswers(questionText, correctAnswer)
                .thenAccept(wrongAnswers -> {
                    // Gộp wrongAnswers + correctAnswer và trộn ngẫu nhiên
                    List<String> allAnswers = new ArrayList<>(wrongAnswers);
                    allAnswers.add(correctAnswer);
                    Collections.shuffle(allAnswers);

                    // Tạo Question object
                    Question question = new Question();
                    question.quizId = quizId;
                    question.question = questionText;
                    question.questionImg = questionImg;
                    question.answerA = allAnswers.get(0);
                    question.answerB = allAnswers.size() > 1 ? allAnswers.get(1) : "";
                    question.answerC = allAnswers.size() > 2 ? allAnswers.get(2) : "";
                    question.answerD = allAnswers.size() > 3 ? allAnswers.get(3) : "";
                    question.correctAnswer = correctAnswer;

                    future.complete(question);
                })
                .exceptionally(e -> {
                    // Nếu lỗi, vẫn tạo question với mock answers
                    List<String> mockAnswers = new ArrayList<>();
                    mockAnswers.add("Không phải " + correctAnswer);
                    mockAnswers.add("Câu trả lời khác");
                    mockAnswers.add("Đáp án khác");
                    mockAnswers.add(correctAnswer);
                    Collections.shuffle(mockAnswers);

                    Question question = new Question();
                    question.quizId = quizId;
                    question.question = questionText;
                    question.questionImg = questionImg;
                    question.answerA = mockAnswers.get(0);
                    question.answerB = mockAnswers.size() > 1 ? mockAnswers.get(1) : "";
                    question.answerC = mockAnswers.size() > 2 ? mockAnswers.get(2) : "";
                    question.answerD = mockAnswers.size() > 3 ? mockAnswers.get(3) : "";
                    question.correctAnswer = correctAnswer;

                    future.complete(question);
                    return null;
                });

        return future;
    }

    /**
     * Lưu danh sách questions vào database (lưu tuần tự để tránh conflict ID)
     */
    private void saveQuestions(List<Question> questionsToSave) {
        if (questionsToSave == null || questionsToSave.isEmpty()) {
            runOnUiThread(() -> {
                LoadingUtils.hideLoading(binding.loadingOverlay);
                Toast.makeText(this, "Không có câu hỏi để lưu", Toast.LENGTH_SHORT).show();
                finish();
            });
            return;
        }

        android.util.Log.d("DoQuizActivity", "Bắt đầu lưu " + questionsToSave.size() + " questions");
        
        final List<Question> finalQuestionsToSave = questionsToSave;
        
        // Lưu tuần tự để tránh conflict ID khi generate
        saveQuestionsSequentially(questionsToSave, 0, new ArrayList<>());
    }
    
    /**
     * Lưu questions tuần tự (recursive)
     */
    private void saveQuestionsSequentially(List<Question> questionsToSave, int index, List<Question> savedQuestions) {
        if (index >= questionsToSave.size()) {
            // Đã lưu xong tất cả
            final int finalSuccessCount = savedQuestions.size();
            final int totalCount = questionsToSave.size();
            final List<Question> finalQuestionsToSave = questionsToSave;
            
            android.util.Log.d("DoQuizActivity", "Đã lưu thành công " + finalSuccessCount + "/" + totalCount + " questions");
            
            runOnUiThread(() -> {
                if (finalSuccessCount < totalCount) {
                    Toast.makeText(this, 
                        "Đã lưu " + finalSuccessCount + "/" + totalCount + " câu hỏi. Một số câu hỏi có thể chưa được lưu.", 
                        Toast.LENGTH_LONG).show();
                }
                questions = finalQuestionsToSave;
                score = 0; // Reset score
                binding.tvScore.setText("00");
                LoadingUtils.hideLoading(binding.loadingOverlay);
                showQuestion(0);
            });
            return;
        }
        
        Question question = questionsToSave.get(index);
        final int currentIndex = index;
        
        android.util.Log.d("DoQuizActivity", "Đang lưu question " + (currentIndex + 1) + "/" + questionsToSave.size() + ": " + question.question);
        
        // Lưu question hiện tại
        questionRepository.add(question)
                .thenAccept(savedQuestion -> {
                    if (savedQuestion != null) {
                        android.util.Log.d("DoQuizActivity", "Đã lưu thành công question " + (currentIndex + 1) + ": " + savedQuestion.question + " (ID: " + savedQuestion.id + ")");
                        savedQuestions.add(savedQuestion);
                    } else {
                        android.util.Log.w("DoQuizActivity", "Question " + (currentIndex + 1) + " được lưu nhưng trả về null");
                    }
                    // Lưu question tiếp theo
                    saveQuestionsSequentially(questionsToSave, currentIndex + 1, savedQuestions);
                })
                .exceptionally(e -> {
                    android.util.Log.e("DoQuizActivity", "Lỗi khi lưu question " + (currentIndex + 1) + ": " + e.getMessage(), e);
                    // Vẫn tiếp tục lưu question tiếp theo dù có lỗi
                    saveQuestionsSequentially(questionsToSave, currentIndex + 1, savedQuestions);
                    return null;
                });
    }

    /**
     * Hiển thị câu hỏi tại index
     */
    private void showQuestion(int index) {
        if (index < 0 || index >= questions.size()) {
            finishQuiz();
            return;
        }

        Question question = questions.get(index);
        currentQuestionIndex = index;
        isAnswerSelected = false;
        selectedAnswer = "";

        // Cập nhật progress
        int totalQuestions = questions.size();
        int currentQuestion = index + 1;
        binding.tvProgress.setText(currentQuestion + "/" + totalQuestions + "Q");
        updateProgressBar(index, totalQuestions);

        // Hiển thị câu hỏi
        binding.tvQuestion.setText(question.question);

        // Hiển thị ảnh nếu có
        if (question.questionImg != null && !question.questionImg.isEmpty()) {
            binding.ivQuestionImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(question.questionImg)
                    .placeholder(R.drawable.bg_card)
                    .error(R.drawable.bg_card)
                    .into(binding.ivQuestionImage);
        } else {
            binding.ivQuestionImage.setVisibility(View.GONE);
        }

        // Hiển thị đáp án (không có prefix A. B. C. D. vì đã có badge)
        binding.tvAnswerA.setText(question.answerA != null ? question.answerA : "");
        binding.tvAnswerB.setText(question.answerB != null ? question.answerB : "");
        binding.tvAnswerC.setText(question.answerC != null ? question.answerC : "");
        binding.tvAnswerD.setText(question.answerD != null ? question.answerD : "");

        // Kích hoạt lại cards
        enableAllAnswerCards();
    }
    
    /**
     * Cập nhật progress bar với segments
     */
    private void updateProgressBar(int currentIndex, int totalQuestions) {
        LinearLayout container = binding.progressBarContainer;
        container.removeAllViews();
        
        for (int i = 0; i < totalQuestions; i++) {
            View segment = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 16, 1.0f);
            params.setMargins(4, 0, 4, 0);
            segment.setLayoutParams(params);
            
            if (i <= currentIndex) {
                segment.setBackgroundColor(getResources().getColor(R.color.primary, null));
            } else {
                segment.setBackgroundColor(getResources().getColor(R.color.divider, null));
            }
            
            container.addView(segment);
        }
    }


    /**
     * Kết thúc quiz và hiển thị kết quả
     */
    private void finishQuiz() {
        int totalQuestions = questions.size();
        
        // Tạo dialog kết quả
        View resultView = getLayoutInflater().inflate(R.layout.dialog_quiz_result, null);
        TextView tvFinalScore = resultView.findViewById(R.id.tvFinalScore);
        TextView tvCongratulations = resultView.findViewById(R.id.tvCongratulations);
        TextView tvMessage = resultView.findViewById(R.id.tvMessage);
        ImageView ivCelebrationGif = resultView.findViewById(R.id.ivCelebrationGif);
        MaterialButton btnBackToHome = resultView.findViewById(R.id.btnBackToHome);
        
        tvFinalScore.setText(score + "/" + totalQuestions);
        
        // Load GIF từ drawable
        Glide.with(this)
                .asGif()
                .load(R.drawable.celebration)
                .placeholder(R.drawable.ic_profile) // Placeholder khi đang load
                .error(R.drawable.ic_profile) // Hiển thị nếu lỗi
                .into(ivCelebrationGif);
        
        // Customize message based on score
        if (score == totalQuestions) {
            tvCongratulations.setText("Perfect!");
            tvMessage.setText("Excellent! You got all questions right!");
        } else if (score >= totalQuestions * 0.8) {
            tvCongratulations.setText("Great Job!");
            tvMessage.setText("Well done! You did very well!");
        } else if (score >= totalQuestions * 0.5) {
            tvCongratulations.setText("Good Try!");
            tvMessage.setText("Not bad! Keep practicing!");
        } else {
            tvCongratulations.setText("Keep Learning!");
            tvMessage.setText("Don't give up! Practice makes perfect!");
        }
        
        AlertDialog resultDialog = new AlertDialog.Builder(this)
                .setView(resultView)
                .setCancelable(false)
                .create();
        
        btnBackToHome.setOnClickListener(v -> {
            resultDialog.dismiss();
            finish();
        });
        
        resultDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}

