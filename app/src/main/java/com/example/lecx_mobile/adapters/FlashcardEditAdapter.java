package com.example.lecx_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lecx_mobile.R;
import com.example.lecx_mobile.databinding.ItemFlashcardEditBinding;
import com.example.lecx_mobile.models.Flashcard;

import java.util.List;

/**
 * Adapter cho RecyclerView hiển thị danh sách Flashcard với nút sửa/xóa
 */
public class FlashcardEditAdapter extends RecyclerView.Adapter<FlashcardEditAdapter.FlashcardEditViewHolder> {

    private List<Flashcard> flashcards;
    private OnFlashcardActionListener listener;

    public interface OnFlashcardActionListener {
        void onEditFlashcard(Flashcard flashcard, int position);
        void onDeleteFlashcard(Flashcard flashcard, int position);
    }

    public FlashcardEditAdapter(List<Flashcard> flashcards, OnFlashcardActionListener listener) {
        this.flashcards = flashcards;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FlashcardEditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFlashcardEditBinding binding = ItemFlashcardEditBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FlashcardEditViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardEditViewHolder holder, int position) {
        Flashcard flashcard = flashcards.get(position);
        holder.bind(flashcard, position);
    }

    @Override
    public int getItemCount() {
        return flashcards != null ? flashcards.size() : 0;
    }

    /**
     * Cập nhật danh sách flashcard
     */
    public void updateFlashcards(List<Flashcard> newFlashcards) {
        this.flashcards = newFlashcards;
        notifyDataSetChanged();
    }

    /**
     * Thêm flashcard mới
     */
    public void addFlashcard(Flashcard flashcard) {
        if (flashcards != null) {
            flashcards.add(flashcard);
            notifyItemInserted(flashcards.size() - 1);
        }
    }

    /**
     * Cập nhật flashcard tại vị trí
     */
    public void updateFlashcard(int position, Flashcard flashcard) {
        if (flashcards != null && position >= 0 && position < flashcards.size()) {
            flashcards.set(position, flashcard);
            notifyItemChanged(position);
        }
    }

    /**
     * Xóa flashcard tại vị trí
     */
    public void removeFlashcard(int position) {
        if (flashcards != null && position >= 0 && position < flashcards.size()) {
            flashcards.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, flashcards.size());
        }
    }

    /**
     * Lấy danh sách flashcards
     */
    public List<Flashcard> getFlashcards() {
        return flashcards;
    }

    /**
     * ViewHolder cho mỗi item Flashcard
     */
    class FlashcardEditViewHolder extends RecyclerView.ViewHolder {
        private ItemFlashcardEditBinding binding;

        public FlashcardEditViewHolder(@NonNull ItemFlashcardEditBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Flashcard flashcard, int position) {
            // Hiển thị frontText
            binding.tvFrontText.setText(flashcard.frontText != null ? flashcard.frontText : "");

            // Hiển thị backText
            binding.tvBackText.setText(flashcard.backText != null ? flashcard.backText : "");

            // Hiển thị ảnh nếu có
            if (flashcard.frontImg != null && !flashcard.frontImg.trim().isEmpty()) {
                binding.ivFlashcardImage.setVisibility(View.VISIBLE);
                Glide.with(binding.getRoot().getContext())
                        .load(flashcard.frontImg)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .centerCrop()
                        .into(binding.ivFlashcardImage);
            } else {
                binding.ivFlashcardImage.setVisibility(View.GONE);
            }

            // Xử lý nút Sửa
            binding.btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditFlashcard(flashcard, position);
                }
            });

            // Xử lý nút Xóa
            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteFlashcard(flashcard, position);
                }
            });
        }
    }
}

