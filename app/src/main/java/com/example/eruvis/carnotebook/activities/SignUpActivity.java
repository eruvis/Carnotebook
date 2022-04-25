package com.example.eruvis.carnotebook.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eruvis.carnotebook.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private static final String LOG_TAG = SignUpActivity.class.getSimpleName();

    private EditText etEmail, etPassword, etConfirmPassword;
    private TextView btnSignUp, btnSignIn;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private ImageView ivLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);

        ivLogo = (ImageView) findViewById(R.id.iv_logo);
        KeyboardVisibilityEvent.setEventListener(this, new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                final float scale = isOpen ? 0.75f : 1f;
                ViewCompat.animate(ivLogo)
                        .scaleX(scale)
                        .scaleY(scale)
                        .setDuration(300)
                        .start();
                if (!isOpen) getCurrentFocus().clearFocus();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        etEmail = (EditText) findViewById(R.id.input_email_et);
        etPassword = (EditText) findViewById(R.id.input_password_et);
        etConfirmPassword = (EditText) findViewById(R.id.input_confirm_password_et);

        btnSignUp = (TextView) findViewById(R.id.btn_sign_up);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignUp();
            }
        });

        btnSignIn = (TextView) findViewById(R.id.btn_sign_in);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignIn();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
        }
    }

    private void onSignIn() {
        startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
    }

    private void onSignUp() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!validateInputs(email, password, confirmPassword)) {
            return;
        }

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.INVISIBLE);
                            addUserFields();
                            finish();
                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                            if (task.getException().toString().contains("FirebaseNetworkException")) {
                                Toast.makeText(getApplicationContext(), R.string.er_network_error, Toast.LENGTH_SHORT).show();
                            } else if (task.getException().toString().contains("FirebaseAuthInvalidCredentialsException")) {
                                etEmail.setError(getText(R.string.er_incorrect_email));
                            } else if (task.getException().toString().contains("FirebaseAuthUserCollisionException")) {
                                etEmail.setError(getText(R.string.er_email_already_in_use));
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.er_unknown_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });


    }

    private void addUserFields() {
        firebaseUser = firebaseAuth.getCurrentUser();
        Map<String, Object> user = new HashMap<>();
        user.put("email", firebaseUser.getEmail());
        final DocumentReference dbUser;
        dbUser = db
                .collection("users")
                .document(firebaseUser.getUid());
        dbUser.set(user);
    }

    private boolean validateInputs(String email, String password, String confirmPassword) {
        boolean valid = true;

        etEmail.setError(null);
        etPassword.setError(null);
        etConfirmPassword.setError(null);

        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getText(R.string.er_fill_field));
            valid = false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getText(R.string.er_fill_field));
            valid = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError(getText(R.string.er_fill_field));
            valid = false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && !TextUtils.isEmpty(email)) {
            etEmail.setError(getText(R.string.er_incorrect_email));
            valid = false;
        }

        if (password.length() < 6 && !TextUtils.isEmpty(password)) {
            etPassword.setError(getText(R.string.er_min_password_length));
            valid = false;
        }

        if (!password.equals(confirmPassword) && !(password.length() < 6)) {
            etPassword.setError(getText(R.string.er_password_not_math));
            etConfirmPassword.setError(getText(R.string.er_password_not_math));
            valid = false;
        }

        return valid;
    }
}
