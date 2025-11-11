package com.example.lecx_mobile.services.interfaces;

import android.net.Uri;

import com.google.android.gms.tasks.Task;

public interface IStorageService {
    Task<String> uploadAvatar(Uri fileUri, String fileName);

    Task<String> uploadFlashcardImage(Uri fileUri, String fileName);
}
