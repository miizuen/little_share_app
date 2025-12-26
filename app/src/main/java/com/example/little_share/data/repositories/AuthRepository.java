package com.example.little_share.data.repositories;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private final FirebaseAuth auth;

    public AuthRepository() {
        this.auth = FirebaseAuth.getInstance();
    }

    // ========== REGISTER ONLY AUTH ===========
    public void register(String email, String password, OnAuthListener listener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null) {
                        listener.onSuccess(user.getUid());
                    } else {
                        listener.onFailure("Không thể tạo tài khoản");
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // ========== LOGIN ===========
    public void login(String email, String password, OnAuthListener listener) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        listener.onSuccess(user.getUid());
                    } else {
                        listener.onFailure("Không thể đăng nhập");
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // ========== DELETE AUTH ACCOUNT ===========
    public void deleteAccount(OnSimpleListener listener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            listener.onFailure("Chưa đăng nhập");
            return;
        }

        user.delete()
                .addOnSuccessListener(a -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // ===== Callback interfaces =====
    public interface OnAuthListener {
        void onSuccess(String userId);
        void onFailure(String error);
    }

    public interface OnSimpleListener {
        void onSuccess();
        void onFailure(String error);
    }
}
