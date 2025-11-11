package com.example.lecx_mobile.services.implementations;

import android.net.Uri;

import com.example.lecx_mobile.services.interfaces.IStorageService;
import com.example.lecx_mobile.utils.FirebaseStorageUtils;
import com.google.android.gms.tasks.Task;

public class StorageService implements IStorageService {

    @Override
    public Task<String> uploadAvatar(Uri fileUri, String fileName) {
        return FirebaseStorageUtils.uploadAvatar(fileUri, fileName);
    }

    @Override
    public Task<String> uploadFlashcardImage(Uri fileUri, String fileName) {
        return FirebaseStorageUtils.uploadFlashcardImage(fileUri, fileName);
    }
}