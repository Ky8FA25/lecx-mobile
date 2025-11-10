package com.example.lecx_mobile.utils;

import android.content.Context;
import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseStorageUtils {

    private static final String STORAGE_URL = "gs://online-88d8b.appspot.com";

    private static FirebaseStorage storage;
    private static StorageReference storageRef;

    // Khởi tạo Firebase Storage
    public static void init(Context context) {
        if (storage == null) {
            FirebaseApp.initializeApp(context);
            storage = FirebaseStorage.getInstance();
            storageRef = storage.getReferenceFromUrl(STORAGE_URL);
        }
    }

    public static StorageReference getStorageRef() {
        if (storageRef == null) {
            storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(STORAGE_URL);
        }
        return storageRef;
    }

    // Upload file chung và trả về URL sau khi upload thành công
    public static Task<String> uploadFile(Uri fileUri, String folderName, String fileName) {
        StorageReference ref = getStorageRef().child(folderName + "/" + fileName);

        return ref.putFile(fileUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Upload xong, lấy download URL
                    return ref.getDownloadUrl();
                })
                .continueWith(task -> task.getResult().toString());
    }

    // ==========================
    // Upload avatar và trả URL
    // ==========================
    public static Task<String> uploadAvatar(Uri fileUri, String fileName) {
        return uploadFile(fileUri, Constants.FOLDER_AVATARS, fileName);
    }

    // ==========================
    // Upload flashcard image và trả URL
    // ==========================
    public static Task<String> uploadFlashcardImage(Uri fileUri, String fileName) {
        return uploadFile(fileUri, Constants.FOLDER_FLASHCARD_IMAGES, fileName);
    }
}
