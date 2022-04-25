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

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

public class SignInActivity extends AppCompatActivity {
    private static final String LOG_TAG = SignInActivity.class.getSimpleName();

    private EditText etEmail, etPassword;
    private TextView btnResetPassword, btnSignIn, btnSignInWithoutReg,btnSignUp;
    private ImageView ivLogo;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

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
        etPassword = (EditText) findViewById(R.id.input_password_et);

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

        btnSignInWithoutReg = (TextView) findViewById(R.id.btn_sign_in_without_reg);
        btnSignInWithoutReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignInWithoutReg();
            }
        });

        btnSignUp = (TextView) findViewById(R.id.btn_sign_up);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSingUp();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
        }
    }

    private void onResetPassword() {
        startActivity(new Intent(SignInActivity.this, ResetPasswordActivity.class));
    }

    private void onSignIn() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInputs(email, password))
        {
            return;
        }

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.INVISIBLE);

                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                        } else {
                            progressBar.setVisibility(View.INVISIBLE);

                            //Log.w("ERROR", "signInWithEmail:failure", task.getException());
                            if (task.getException().toString().contains("FirebaseNetworkException")) {
                                Toast.makeText(getApplicationContext(), R.string.er_network_error, Toast.LENGTH_SHORT).show();
                            } else if (task.getException().toString().contains("FirebaseAuthInvalidCredentialsException")) {
                                Toast.makeText(getApplicationContext(), R.string.er_invalid_password, Toast.LENGTH_SHORT).show();
                            } else if (task.getException().toString().contains("FirebaseAuthInvalidUserException")) {
                                Toast.makeText(getApplicationContext(), R.string.er_no_user_record, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.er_unknown_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void onSignInWithoutReg() {
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        firebaseAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.INVISIBLE);
                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                            //Log.w("ERROR", "signInAnonymously:failure", task.getException());
                            if (task.getException().toString().contains("FirebaseNetworkException")) {
                                Toast.makeText(getApplicationContext(), R.string.er_network_error, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.er_unknown_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void onSingUp() {
        startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
    }

    private boolean validateInputs(String email, String password) {
        boolean valid = true;

        etEmail.setError(null);
        etPassword.setError(null);

        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getText(R.string.er_fill_field));
            valid = false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getText(R.string.er_fill_field));
            valid = false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && !TextUtils.isEmpty(email)) {
            etEmail.setError(getText(R.string.er_incorrect_email));
            valid = false;
        }

        if (password.length() < 6 && !TextUtils.isEmpty(password)){
            etPassword.setError(getText(R.string.er_min_password_length));
            valid = false;
        }

        return valid;
    }
}
