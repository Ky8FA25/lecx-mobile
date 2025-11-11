package com.example.lecx_mobile.utils;

import android.view.View;

/**
 * Utility class để quản lý loading overlay
 * Sử dụng với loading_overlay.xml đã được include trong layout
 */
public class LoadingUtils {

    /**
     * Hiển thị loading overlay
     * 
     * @param loadingOverlay View của loading overlay (từ binding.loadingOverlay)
     */
    public static void showLoading(View loadingOverlay) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Ẩn loading overlay
     * 
     * @param loadingOverlay View của loading overlay (từ binding.loadingOverlay)
     */
    public static void hideLoading(View loadingOverlay) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
        }
    }

    /**
     * Set trạng thái loading (hiện/ẩn)
     * 
     * @param loadingOverlay View của loading overlay (từ binding.loadingOverlay)
     * @param isLoading true để hiển thị, false để ẩn
     */
    public static void setLoading(View loadingOverlay, boolean isLoading) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }
}

