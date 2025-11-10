package com.example.lecx_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lecx_mobile.R;
import com.example.lecx_mobile.databinding.ItemFlashcardBinding;
import com.example.lecx_mobile.models.Flashcard;

import java.util.List;

/**
 * Adapter cho RecyclerView hiển thị danh sách Flashcard
 */
public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder> {

    private List<Flashcard> flashcards;

    public FlashcardAdapter(List<Flashcard> flashcards) {
        this.flashcards = flashcards;
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFlashcardBinding binding = ItemFlashcardBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FlashcardViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        Flashcard flashcard = flashcards.get(position);
        holder.bind(flashcard);
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
     * ViewHolder cho mỗi item Flashcard
     */
    static class FlashcardViewHolder extends RecyclerView.ViewHolder {
        private ItemFlashcardBinding binding;

        public FlashcardViewHolder(@NonNull ItemFlashcardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Flashcard flashcard) {
            // Hiển thị frontText
            binding.tvFrontText.setText(flashcard.frontText != null ? flashcard.frontText : "");

            // Hiển thị backText
            binding.tvBackText.setText(flashcard.backText != null ? flashcard.backText : "");

            // Chỉ hiển thị ảnh nếu có URL/path hợp lệ
            if (flashcard.frontImg != null && !flashcard.frontImg.trim().isEmpty()) {
                binding.ivFlashcardImage.setVisibility(View.VISIBLE);
                Glide.with(binding.getRoot().getContext())
                        .load(flashcard.frontImg)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .centerCrop()
                        .into(binding.ivFlashcardImage);
            } else {
                // Hoàn toàn ẩn ImageView nếu không có ảnh
                binding.ivFlashcardImage.setVisibility(View.GONE);
            }
        }
    }
}

