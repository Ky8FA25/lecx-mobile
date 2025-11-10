package com.example.lecx_mobile.repositories.implementations;

import android.net.Uri;

import com.example.lecx_mobile.repositories.interfaces.IStorageRepository;
import com.example.lecx_mobile.utils.FirebaseStorageUtils;
import com.google.android.gms.tasks.Task;

public class StorageRepository implements IStorageRepository {

    @Override
    public Task<String> uploadAvatar(Uri fileUri, String fileName) {
        return FirebaseStorageUtils.uploadAvatar(fileUri, fileName);
    }

    @Override
    public Task<String> uploadFlashcardImage(Uri fileUri, String fileName) {
        return FirebaseStorageUtils.uploadFlashcardImage(fileUri, fileName);
    }
}