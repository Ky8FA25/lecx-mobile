package com.example.lecx_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lecx_mobile.R;
import com.example.lecx_mobile.databinding.ItemQuizBinding;
import com.example.lecx_mobile.databinding.ItemQuizVerticalBinding;
import com.example.lecx_mobile.models.Account;
import com.example.lecx_mobile.models.Quiz;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;
import java.util.Map;

/**
 * Adapter cho RecyclerView hiển thị danh sách Quiz
 */
public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {

    private List<Quiz> quizzes;
    private Map<Integer, Account> accountMap;
    private boolean showDeleteButton;
    private OnQuizClickListener onQuizClickListener;
    private OnDeleteClickListener onDeleteClickListener;
    private boolean useVerticalLayout;

    public interface OnQuizClickListener {
        void onQuizClick(Quiz quiz);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Quiz quiz);
    }

    public QuizAdapter(List<Quiz> quizzes, Map<Integer, Account> accountMap, 
                       boolean showDeleteButton,
                       OnQuizClickListener onQuizClickListener,
                       OnDeleteClickListener onDeleteClickListener) {
        this(quizzes, accountMap, showDeleteButton, onQuizClickListener, onDeleteClickListener, false);
    }

    public QuizAdapter(List<Quiz> quizzes, Map<Integer, Account> accountMap, 
                       boolean showDeleteButton,
                       OnQuizClickListener onQuizClickListener,
                       OnDeleteClickListener onDeleteClickListener,
                       boolean useVerticalLayout) {
        this.quizzes = quizzes;
        this.accountMap = accountMap;
        this.showDeleteButton = showDeleteButton;
        this.onQuizClickListener = onQuizClickListener;
        this.onDeleteClickListener = onDeleteClickListener;
        this.useVerticalLayout = useVerticalLayout;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (useVerticalLayout) {
            ItemQuizVerticalBinding binding = ItemQuizVerticalBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new QuizViewHolder(binding);
        } else {
            ItemQuizBinding binding = ItemQuizBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new QuizViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        Quiz quiz = quizzes.get(position);
        holder.bind(quiz);
    }

    @Override
    public int getItemCount() {
        return quizzes != null ? quizzes.size() : 0;
    }

    /**
     * ViewHolder cho mỗi item Quiz
     */
    class QuizViewHolder extends RecyclerView.ViewHolder {
        private ItemQuizBinding binding;
        private ItemQuizVerticalBinding verticalBinding;
        private boolean isVertical;

        public QuizViewHolder(@NonNull ItemQuizBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.isVertical = false;
        }

        public QuizViewHolder(@NonNull ItemQuizVerticalBinding binding) {
            super(binding.getRoot());
            this.verticalBinding = binding;
            this.isVertical = true;
        }

        public void bind(Quiz quiz) {
            TextView tvQuizTitle;
            TextView tvQuizDescription;
            TextView tvFlashcardCount;
            TextView tvImageLabel;
            TextView tvAuthorName;
            ShapeableImageView ivAuthorAvatar;
            ImageButton btnDelete;
            View root;

            if (isVertical) {
                tvQuizTitle = verticalBinding.tvQuizTitle;
                tvQuizDescription = verticalBinding.tvQuizDescription;
                tvFlashcardCount = verticalBinding.tvFlashcardCount;
                tvImageLabel = verticalBinding.tvImageLabel;
                tvAuthorName = verticalBinding.tvAuthorName;
                ivAuthorAvatar = verticalBinding.ivAuthorAvatar;
                btnDelete = verticalBinding.btnDelete;
                root = verticalBinding.getRoot();
            } else {
                tvQuizTitle = binding.tvQuizTitle;
                tvQuizDescription = binding.tvQuizDescription;
                tvFlashcardCount = binding.tvFlashcardCount;
                tvImageLabel = binding.tvImageLabel;
                tvAuthorName = binding.tvAuthorName;
                ivAuthorAvatar = binding.ivAuthorAvatar;
                btnDelete = binding.btnDelete;
                root = binding.getRoot();
            }

            // Set title
            tvQuizTitle.setText(quiz.title != null ? quiz.title : "");

            // Set description
            tvQuizDescription.setText(quiz.description != null ? quiz.description : "");

            // Set flashcard count
            tvFlashcardCount.setText(quiz.numberOfFlashcards + " thuật ngữ");

            // Show image label if quiz has images (placeholder logic)
            // TODO: Check if quiz actually has images
            tvImageLabel.setVisibility(View.GONE);

            // Set author info
            Account account = accountMap.get(quiz.accountId);
            if (account != null) {
                tvAuthorName.setText(account.fullname != null ? account.fullname : account.username);
                
                // Load avatar
                if (account.avatar != null && !account.avatar.trim().isEmpty()) {
                    Glide.with(root.getContext())
                            .load(account.avatar)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .circleCrop()
                            .into(ivAuthorAvatar);
                } else {
                    ivAuthorAvatar.setImageResource(R.drawable.ic_profile);
                }
            } else {
                tvAuthorName.setText("Unknown");
                ivAuthorAvatar.setImageResource(R.drawable.ic_profile);
            }

            // Show/hide delete button
            if (showDeleteButton && onDeleteClickListener != null) {
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.setOnClickListener(v -> {
                    if (onDeleteClickListener != null) {
                        onDeleteClickListener.onDeleteClick(quiz);
                    }
                });
            } else {
                btnDelete.setVisibility(View.GONE);
            }

            // Set click listener for card
            root.setOnClickListener(v -> {
                if (onQuizClickListener != null) {
                    onQuizClickListener.onQuizClick(quiz);
                }
            });
        }
    }
}
