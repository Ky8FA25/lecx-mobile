package com.example.lecx_mobile.views.Profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lecx_mobile.R;
import com.example.lecx_mobile.api.ApiClient;
import com.example.lecx_mobile.models.Account;
import com.example.lecx_mobile.repositories.implementations.AccountRepository;
import com.example.lecx_mobile.repositories.interfaces.IAccountRepository;
import com.example.lecx_mobile.services.implementations.StorageService;
import com.example.lecx_mobile.services.interfaces.IStorageService;
import com.example.lecx_mobile.utils.Prefs;
import com.example.lecx_mobile.utils.Validator;
import com.example.lecx_mobile.databinding.FragmentProfileCurrentUserBinding;
import com.example.lecx_mobile.views.Auth.LoginActivity;

import com.example.lecx_mobile.services.implementations.OtpService;
import com.example.lecx_mobile.services.interfaces.IOtpService;
import retrofit2.Retrofit;
import com.example.lecx_mobile.api.OtpApiResponse;

public class CurrentUserProfileFragment extends Fragment {

    private FragmentProfileCurrentUserBinding binding;

    // ✅ Khởi tạo Repository trực tiếp (Giả định UserRepository implements IUserRepository)
    private final IAccountRepository userRepo = new AccountRepository();
    private final IStorageService storageService = new StorageService();
    
    private Account currentUser;
    
    // ActivityResultLauncher for image picker
    private ActivityResultLauncher<String> imagePickerLauncher;
    
    // OTP Timer
    private CountDownTimer otpTimer;
    private static final long OTP_RESEND_INTERVAL = 60000; // 60 seconds
    private IOtpService otpService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Tên binding cần khớp với tên file layout của bạn (fragment_profile.xml)
        binding = FragmentProfileCurrentUserBinding.inflate(inflater, container, false);

        Retrofit retrofit = ApiClient.getClient(getString(R.string.base_url));
        otpService = new OtpService(retrofit);

        // Initialize image picker launcher
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadAvatar(uri);
                }
            }
        );

        int userId = Prefs.getUserId(requireContext());

        // ✅ Bắt đầu load data - Call GetProfile API
        getProfile(userId);

        // Setup click listeners
        binding.btnLogout.setOnClickListener(v -> handleLogout());
        binding.fabEditAvatar.setOnClickListener(v -> openImagePicker());
        binding.ivAvatar.setOnClickListener(v -> openImagePicker());
        binding.btnEditFullName.setOnClickListener(v -> showEditFullNameDialog());
        binding.btnEditUsername.setOnClickListener(v -> showEditUsernameDialog());
        binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        binding.btnVerifyEmail.setOnClickListener(v -> handleVerifyEmail());

        return binding.getRoot();
    }

    /**
     * GetProfile API - Lấy thông tin profile người dùng
     * TODO: Replace with actual API call when backend is ready
     */
    private void getProfile(int userId) {
        setLoading(true);

        // Currently using Firebase repository, replace with API call:
        // Example: apiService.getProfile(userId).thenAccept(...)
        userRepo.getById(userId)
                .thenAccept(user -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (user == null) {
                                Toast.makeText(requireContext(), "Không tìm thấy người dùng hoặc phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
                            } else {
                                currentUser = user;
                                bindUserInfo(user);
                                checkEmailVerification(user.email);
                            }
                            setLoading(false);
                        });
                    }
                })
                .exceptionally(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Lỗi khi tải hồ sơ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            setLoading(false);
                        });
                    }
                    return null;
                });
    }

    /**
     * VerifiedEmail API - Kiểm tra trạng thái xác thực email
     * TODO: Replace with actual API call when backend is ready
     */
    private void checkEmailVerification(String email) {
        // Currently using Account model field, replace with API call:
        // Example: apiService.verifiedEmail(email).thenAccept(isVerified -> {...})
        if (currentUser != null) {
            if (currentUser.isEmailConfirmed) {
                binding.tvEmailVerified.setVisibility(View.VISIBLE);
                binding.tvEmailNotVerified.setVisibility(View.GONE);
                binding.btnVerifyEmail.setVisibility(View.GONE);
            } else {
                binding.tvEmailVerified.setVisibility(View.GONE);
                binding.tvEmailNotVerified.setVisibility(View.VISIBLE);
                binding.btnVerifyEmail.setVisibility(View.VISIBLE);
            }
        }
    }

    private void bindUserInfo(Account user) {
        if (user == null) return;

        // Update header section
        binding.tvFullName.setText(user.fullname);
        binding.tvUsername.setText("@" + (user.username != null ? user.username : ""));
        binding.tvEmail.setText(user.email);

        // Update edit section
        binding.tvFullNameValue.setText(user.fullname);
        binding.tvUsernameValue.setText(user.username != null ? user.username : "");

        // Load avatar
        Glide.with(this)
                .load(user.avatar)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .circleCrop()
                .into(binding.ivAvatar);
    }

    private void setLoading(boolean loading) {
        binding.btnLogout.setEnabled(!loading);
        binding.btnEditFullName.setEnabled(!loading);
        binding.btnEditUsername.setEnabled(!loading);
        binding.btnChangePassword.setEnabled(!loading);
        binding.fabEditAvatar.setEnabled(!loading);
        binding.btnVerifyEmail.setEnabled(!loading);
        // Có thể thêm logic ẩn/hiện ProgressBar ở đây
    }

    private void handleLogout() {
        Prefs.clearSession(requireContext());
        Toast.makeText(requireContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // ==================== Avatar Upload ====================

    private void openImagePicker() {
        imagePickerLauncher.launch("image/*");
    }

    private void uploadAvatar(Uri imageUri) {
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Không load được user", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        String fileName = "avatar_" + currentUser.id + "_" + System.currentTimeMillis() + ".jpg";

        storageService.uploadAvatar(imageUri, fileName)
                .addOnSuccessListener(downloadUrl -> {
                    // Update user avatar URL
                    currentUser.avatar = downloadUrl;
                    updateProfile(currentUser);
                })
                .addOnFailureListener(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Lỗi khi tải ảnh đại diện: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            setLoading(false);
                        });
                    }
                });
    }

    // ==================== Edit Profile Dialogs ====================

    private void showEditFullNameDialog() {
        if (currentUser == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Sửa tên");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(currentUser.fullname);
        input.setHint("Điền tên");
        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newFullName = input.getText().toString().trim();
            if (Validator.isNonEmpty(newFullName)) {
                currentUser.fullname = newFullName;
                updateProfile(currentUser);
            } else {
                Toast.makeText(requireContext(), "Không được để trống tên", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showEditUsernameDialog() {
        if (currentUser == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Sửa Username");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(currentUser.username);
        input.setHint("Điền username");
        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newUsername = input.getText().toString().trim();
            if (Validator.isNonEmpty(newUsername)) {
                currentUser.username = newUsername;
                updateProfile(currentUser);
            } else {
                Toast.makeText(requireContext(), "Username không được trống", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Đổi mật khẩu");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        EditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        builder.setPositiveButton("Đổi", null);
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String currentPassword = etCurrentPassword.getText().toString();
                String newPassword = etNewPassword.getText().toString();
                String confirmPassword = etConfirmPassword.getText().toString();

                if (!Validator.isValidPassword(newPassword)) {
                    etNewPassword.setError("Mật khẩu phải có ít nhất 6 chữ cái");
                    etNewPassword.requestFocus();
                    return;
                }

                if (!Validator.passwordsMatch(newPassword, confirmPassword)) {
                    etConfirmPassword.setError("Mật khẩu không khớp");
                    etConfirmPassword.requestFocus();
                    return;
                }

                dialog.dismiss();
                changePassword(currentPassword, newPassword);
            });
        });

        dialog.show();
    }

    // ==================== API Methods ====================

    /**
     * UpdateProfile API - Cập nhật thông tin profile
     * TODO: Replace with actual API call when backend is ready
     */
    private void updateProfile(Account account) {
        setLoading(true);

        // Currently using Firebase repository, replace with API call:
        // Example: apiService.updateProfile(account).thenAccept(...)
        userRepo.update(account)
                .thenAccept(updatedUser -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (updatedUser != null) {
                                currentUser = updatedUser;
                                bindUserInfo(updatedUser);
                                checkEmailVerification(updatedUser.email);
                                Toast.makeText(requireContext(), "Cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Cập nhật hồ sơ thất bại", Toast.LENGTH_SHORT).show();
                            }
                            setLoading(false);
                        });
                    }
                })
                .exceptionally(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Lỗi khi cập nhật hồ sơ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            setLoading(false);
                        });
                    }
                    return null;
                });
    }

    /**
     * ChangePassword API - Đổi mật khẩu
     * TODO: Replace with actual API call when backend is ready
     */
    private void changePassword(String currentPassword, String newPassword) {
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Người dùng chưa được tải", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verify current password
        if (!currentPassword.equals(currentUser.password)) {
            Toast.makeText(requireContext(), "Sai mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        // Currently using Firebase repository, replace with API call:
        // Example: apiService.changePassword(currentUser.id, currentPassword, newPassword).thenAccept(...)
        currentUser.password = newPassword;
        userRepo.update(currentUser)
                .thenAccept(updatedUser -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (updatedUser != null) {
                                currentUser = updatedUser;
                                Toast.makeText(requireContext(), "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Đổi mật khẩu thất bại", Toast.LENGTH_SHORT).show();
                            }
                            setLoading(false);
                        });
                    }
                })
                .exceptionally(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Lỗi khi đổi mật khẩu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            setLoading(false);
                        });
                    }
                    return null;
                });
    }

    // ==================== Email Verification & OTP ====================

    private void handleVerifyEmail() {
        if (currentUser == null || currentUser.email == null) return;
        if (currentUser.isEmailConfirmed) {
            Toast.makeText(requireContext(), "Email đã xác minh", Toast.LENGTH_SHORT).show();
            return;
        }
        sendOTP(currentUser.email);
    }

    /**
     * Send OTP API - Gửi mã OTP đến email
     * TODO: Replace with actual API call when backend is ready
     */
    private void sendOTP(String email) {
        setLoading(true);
        otpService.sendOtpAsync(email)
                .thenAccept(response -> runOnUiThreadSafe(() -> {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_LONG).show();
                    setLoading(false);
                    showVerifyOtpDialog();
                }))
                .exceptionally(e -> { runOnUiThreadSafe(() -> {
                    Toast.makeText(requireContext(), "Gửi mã OTP thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    setLoading(false);
                }); return null; });
    }

    /**
     * Show OTP verification dialog
     */
    private void showVerifyOtpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Xác minh Email");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_verify_otp, null);
        builder.setView(dialogView);

        EditText etOtpCode = dialogView.findViewById(R.id.etOtpCode);
        TextView tvResendOtp = dialogView.findViewById(R.id.tvResendOtp);
        TextView tvOtpTimer = dialogView.findViewById(R.id.tvOtpTimer);
        TextView tvOtpMessage = dialogView.findViewById(R.id.tvOtpMessage);

        // Update message with email
        if (currentUser != null && currentUser.email != null) {
            tvOtpMessage.setText("Đã gửi 1 mã OTP đến email  " + currentUser.email + ". Hãy nhập OTP.");
        }

        // Start timer
        startOtpTimer(tvResendOtp, tvOtpTimer);

        // Resend OTP click listener
        tvResendOtp.setOnClickListener(v -> {
            if (currentUser != null && currentUser.email != null) {
                sendOTP(currentUser.email);
                startOtpTimer(tvResendOtp, tvOtpTimer);
            }
        });

        builder.setPositiveButton("Xác minh", null);
        builder.setNegativeButton("Hủy", (dialog, which) -> {
            cancelOtpTimer();
            dialog.cancel();
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String otpCode = etOtpCode.getText().toString().trim();

                if (otpCode.isEmpty()) {
                    etOtpCode.setError("Hãy nhập mã OTP");
                    etOtpCode.requestFocus();
                    return;
                }

                if (otpCode.length() != 6) {
                    etOtpCode.setError("Mã OTP có ít nhất 6 kí tự");
                    etOtpCode.requestFocus();
                    return;
                }

                dialog.dismiss();
                cancelOtpTimer();
                verifyOTP(otpCode);
            });
        });

        dialog.setOnDismissListener(d -> cancelOtpTimer());
        dialog.show();
    }

    /**
     * Verify OTP API - Xác thực mã OTP
     * TODO: Replace with actual API call when backend is ready
     */

    private void verifyOTP(String otpCode) {
        if (currentUser == null || currentUser.email == null) return;
        setLoading(true);
        otpService.verifyOtpAsync(currentUser.email, otpCode)
                .thenAccept(response -> runOnUiThreadSafe(() -> {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show();
                    if (response.Success) {
                        currentUser.isEmailConfirmed = true;
                        updateProfile(currentUser);
                    }
                    setLoading(false);
                }))
                .exceptionally(e -> { runOnUiThreadSafe(() -> {
                    Toast.makeText(requireContext(), "Xác minh OTP thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    setLoading(false);
                }); return null; });
    }

    /**
     * Start OTP resend timer
     */
    private void startOtpTimer(TextView tvResendOtp, TextView tvOtpTimer) {
        cancelOtpTimer(); // Cancel existing timer if any

        tvResendOtp.setEnabled(false);
        tvResendOtp.setAlpha(0.5f);

        otpTimer = new CountDownTimer(OTP_RESEND_INTERVAL, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvOtpTimer.setText("Gửi lại trong " + seconds + " giây");
            }

            @Override
            public void onFinish() {
                tvOtpTimer.setText("");
                tvResendOtp.setEnabled(true);
                tvResendOtp.setAlpha(1.0f);
            }
        };

        otpTimer.start();
    }

    /**
     * Cancel OTP timer
     */
    private void cancelOtpTimer() {
        if (otpTimer != null) {
            otpTimer.cancel();
            otpTimer = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancelOtpTimer();
        binding = null;
    }
    
    private void runOnUiThreadSafe(Runnable r) { if (getActivity() != null) getActivity().runOnUiThread(r); }
}