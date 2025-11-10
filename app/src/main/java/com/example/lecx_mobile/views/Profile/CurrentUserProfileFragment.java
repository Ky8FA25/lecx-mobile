//package com.example.lecx_mobile.views.Profile;
//
//import android.os.Bundle;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//
//import com.example.lecx_mobile.R;
//import com.example.lecx_mobile.models.Account;
//import com.example.lecx_mobile.repositories.implementations.AccountRepository;
//import com.example.lecx_mobile.repositories.interfaces.IAccountRepository;
//import com.example.lecx_mobile.utils.Prefs;
//
//public class CurrentUserProfileFragment extends Fragment {
//
//    private FragmentProfileBinding binding;
//
//    // ✅ Khởi tạo Repository trực tiếp (Giả định UserRepository implements IUserRepository)
//    private final IAccountRepository userRepo = new AccountRepository();
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        // Tên binding cần khớp với tên file layout của bạn (fragment_profile.xml)
//        binding = FragmentProfileBinding.inflate(inflater, container, false);
//
//        int userId = Prefs.getUserId(requireContext());
//
//        // ✅ Bắt đầu load data
//        loadUserInfo(userId);
//
//        // Xử lý Logout (giữ nguyên)
//        binding.btnLogout.setOnClickListener(v -> handleLogout());
//
//        return binding.getRoot();
//    }
//
//    /**
//     * ✅ Phương thức load data, gọi thẳng hàm getByIdAsync từ Repository.
//     */
//    private void loadUserInfo(int userId) {
//        setLoading(true);
//
//        // ✅ GỌI REPOSITORY TRỰC TIẾP DÙNG ID
//        userRepo.getById(userId)
//                .thenAccept(user -> {
//                    // Cập nhật UI trên Main Thread
//                    if (getActivity() != null) {
//                        getActivity().runOnUiThread(() -> {
//                            if (user == null) {
//                                Toast.makeText(requireContext(), "User not found or session expired", Toast.LENGTH_SHORT).show();
//                            } else {
//                                bindUserInfo(user);
//                            }
//                            setLoading(false);
//                        });
//                    }
//                })
//                .exceptionally(e -> {
//                    // Xử lý lỗi kết nối/server trên Main Thread
//                    if (getActivity() != null) {
//                        getActivity().runOnUiThread(() -> {
//                            Toast.makeText(requireContext(), "Error loading profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                            setLoading(false);
//                        });
//                    }
//                    return null;
//                });
//    }
//
//    private void bindUserInfo(Account user) {
//        if (user == null) return;
//
//        binding.tvFullName.setText(user.fullName);
//        binding.tvEmail.setText(user.email);
//
//        Glide.with(this)
//                .load(user.avatarUrl)
//                .placeholder(R.drawable.ic_user_placeholder)
//                .error(R.drawable.ic_user_placeholder)
//                .circleCrop()
//                .into(binding.ivAvatar);
//
//        // BỎ logic Orders/RecyclerView
//        // Giữ lại để tránh lỗi nếu các View này tồn tại trong layout
//        // binding.rvOrders.setVisibility(View.GONE);
//        // binding.tvEmptyOrders.setVisibility(View.VISIBLE);
//    }
//
//    private void setLoading(boolean loading) {
//        binding.btnLogout.setEnabled(!loading);
//        // Có thể thêm logic ẩn/hiện ProgressBar ở đây
//    }
//
//    private void handleLogout() {
//        Prefs.clearSession(requireContext());
//        Toast.makeText(requireContext(), "Logout successful", Toast.LENGTH_SHORT).show();
//
//        Intent intent = new Intent(requireContext(), SignInActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        binding = null;
//    }
//}