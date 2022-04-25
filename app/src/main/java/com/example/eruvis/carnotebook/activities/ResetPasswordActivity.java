package com.example.eruvis.carnotebook.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.firebase.auth.FirebaseAuth;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

public class ResetPasswordActivity extends AppCompatActivity {
    private static final String LOG_TAG = ResetPasswordActivity.class.getSimpleName();

    private EditText etEmail;
    private TextView btnResetPassword, btnSignIn;
    private FirebaseAuth firebaseAuth;
    private ImageView ivLogo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        ivLogo = (ImageView) findViewById(R.id.iv_logo);
        KeyboardVisibilityEvent.setEventListener(this, new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                final float scale =isOpen ? 0.75f : 1f;
                ViewCompat.animate(ivLogo)
                        .scaleX(scale)
                        .scaleY(scale)
                        .setDuration(300)
                        .start();
                if (!isOpen) getCurrentFocus().clearFocus();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();

        etEmail = (EditText) findViewById(R.id.input_email_et);

        btnResetPassword = (TextView) findViewById(R.id.btn_reset_password);
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResetPassword();
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
            startActivity(new Intent(ResetPasswordActivity.this, MainActivity.class));
        }
    }

    private void onResetPassword() {
        String email = etEmail.getText().toString().trim();

        if (!validateInputs(email))
        {
            return;
        }

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.INVISIBLE);

                            Toast.makeText(getApplicationContext(), R.string.suc_password_reset, Toast.LENGTH_SHORT).show();
                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                            //Log.w("ERROR", "signInWithEmail:failure", task.getException());
                            if (task.getException().toString().contains("FirebaseNetworkException")) {
                                Toast.makeText(getApplicationContext(), R.string.er_network_error, Toast.LENGTH_SHORT).show();
                            } else if (task.getException().toString().contains("FirebaseAuthInvalidCredentialsException")) {
                                etEmail.setError(getText(R.string.er_incorrect_email));
                            } else if (task.getException().toString().contains("FirebaseAuthInvalidUserException")) {
                                Toast.makeText(getApplicationContext(), R.string.er_no_user_record, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.er_unknown_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void onSignIn() {
        startActivity(new Intent(ResetPasswordActivity.this, SignInActivity.class));
    }

    private boolean validateInputs(String email) {
        boolean valid = true;

        etEmail.setError(null);

        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getText(R.string.er_fill_field));
            valid = false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && !TextUtils.isEmpty(email)) {
            etEmail.setError(getText(R.string.er_incorrect_email));
            valid = false;
        }

        return valid;
    }
}
