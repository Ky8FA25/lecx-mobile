package com.example.lecx_mobile.services.implementations;

import android.net.Uri;

import com.example.lecx_mobile.services.interfaces.IStorageService;
import com.example.lecx_mobile.utils.FirebaseStorageUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;

public class StorageService implements IStorageService {

    @Override
    public Task<String> uploadAvatar(Uri fileUri, String fileName) {
        return uploadWithAuth(() -> FirebaseStorageUtils.uploadAvatar(fileUri, fileName));
    }

    @Override
    public Task<String> uploadFlashcardImage(Uri fileUri, String fileName) {
        return uploadWithAuth(() -> FirebaseStorageUtils.uploadFlashcardImage(fileUri, fileName));
    }

    private Task<String> uploadWithAuth(TaskSupplier taskSupplier) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            // User đã login → upload thẳng
            return taskSupplier.get();
        } else {
            // Chưa login → signInAnonymous rồi upload
            Task<Void> signInTask = FirebaseAuth.getInstance().signInAnonymously()
                    .continueWith(task -> {
                        if (!task.isSuccessful()) throw task.getException();
                        return null;
                    });

            return signInTask.onSuccessTask(ignored -> taskSupplier.get());
        }
    }

    // Interface functional để truyền hàm upload
    private interface TaskSupplier {
        Task<String> get();
    }
}
