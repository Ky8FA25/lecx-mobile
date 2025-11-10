package com.example.lecx_mobile.repositories.implementations;

import com.example.lecx_mobile.models.Flashcard;
import com.example.lecx_mobile.repositories.interfaces.IFlashcardRepository;
import com.example.lecx_mobile.utils.FirebaseUtils;

public class FlashcardRepository
        extends GenericRepository<Flashcard>
        implements IFlashcardRepository {

    public FlashcardRepository() {
        super(FirebaseUtils.flashcardsRef(), Flashcard.class);
    }
}
