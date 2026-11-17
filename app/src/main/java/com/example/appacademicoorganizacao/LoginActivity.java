package com.example.appacademicoorganizacao;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private static final long SIMULATED_LATENCY_MS = 700L;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private EditText etEmail;
    private EditText etPassword;
    private View boxEmail;
    private View boxPassword;
    private View errorBanner;
    private TextView errorMessage;
    private View btnLogin;
    private TextView tvLoginLabel;
    private ImageView ivToggle;

    private boolean passwordVisible = false;
    private boolean busy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // vai impedir print/gravacao de tela e miniatura nos "recentes".
        if (!AppFlags.isDebug(this)) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE);
        }

        setContentView(R.layout.activity_login);
        SystemBars.apply(this, findViewById(R.id.loginRoot),
                getResources().getBoolean(R.bool.auth_light_bars));

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        boxEmail = findViewById(R.id.boxEmail);
        boxPassword = findViewById(R.id.boxPassword);
        errorBanner = findViewById(R.id.errorBanner);
        errorMessage = findViewById(R.id.errorMessage);
        btnLogin = findViewById(R.id.btnLogin);
        tvLoginLabel = findViewById(R.id.tvLoginLabel);
        ivToggle = findViewById(R.id.ivTogglePassword);

        // Borda roxa no campo focado
        etEmail.setOnFocusChangeListener((v, hasFocus) -> boxEmail.setSelected(hasFocus));
        etPassword.setOnFocusChangeListener((v, hasFocus) -> boxPassword.setSelected(hasFocus));

        // Ao voltar a digitar, o aviso vermelho e as bordas de erro somem.
        TextWatcher clearOnType = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) {
                if (!busy && errorBanner.getVisibility() == View.VISIBLE) clearError();
            }
        };
        etEmail.addTextChangedListener(clearOnType);
        etPassword.addTextChangedListener(clearOnType);

        ivToggle.setOnClickListener(v -> togglePasswordVisibility());
        btnLogin.setOnClickListener(v -> attemptLogin());

        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });

        findViewById(R.id.tvForgot).setOnClickListener(v -> comingSoon());
        findViewById(R.id.tvCreateAccount).setOnClickListener(v -> comingSoon());

        // test para ver se as outras telas estão funcionando
        View demo = findViewById(R.id.demoStates);
        demo.setVisibility(AppFlags.isDebug(this) ? View.VISIBLE : View.GONE);
        findViewById(R.id.chipOffline).setOnClickListener(
                v -> startActivity(new Intent(this, OfflineActivity.class)));
        findViewById(R.id.chipServerDown).setOnClickListener(
                v -> startActivity(new Intent(this, ServerDownActivity.class)));
    }

    private void attemptLogin() {
        if (busy) return;
        hideKeyboard();
        clearError();

        String email = AuthManager.normalizeEmail(etEmail.getText().toString());
        String password = etPassword.getText().toString();

        // bloqueio temporario por excesso de tentativas
        int lockedFor = AuthManager.lockRemainingSeconds(this);
        if (lockedFor > 0) {
            showError(getString(R.string.error_locked, lockedFor), false, false);
            return;
        }

        // campos vazios
        if (email.isEmpty() || password.isEmpty()) {
            showError(getString(R.string.error_empty), email.isEmpty(), password.isEmpty());
            return;
        }

        // tamanho maximo e formato do e-mail
        if (email.length() > AuthManager.MAX_EMAIL_LENGTH
                || password.length() > AuthManager.MAX_PASSWORD_LENGTH) {
            showError(getString(R.string.error_input_too_long), true, true);
            return;
        }
        if (!AuthManager.isValidEmail(email)) {
            showError(getString(R.string.error_invalid_email), true, false);
            return;
        }

        // sem internet vai pra tela do jogo
        if (!NetworkUtils.isOnline(this)) {
            startActivity(new Intent(this, OfflineActivity.class));
            return;
        }

        // servidor fora do ar vai pra tela de manutenção
        if (ServerStatus.isServerDown()) {
            startActivity(new Intent(this, ServerDownActivity.class));
            return;
        }

        // autenticacao
        setBusy(true);
        handler.postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;
            AuthManager.Result result = AuthManager.authenticate(this, email, password);
            setBusy(false);

            switch (result) {
                case SUCCESS:
                    etPassword.setText("");
                    startActivity(new Intent(this, MainActivity.class));
                    break;
                case LOCKED:
                    etPassword.setText("");
                    showError(getString(R.string.error_locked,
                            Math.max(1, AuthManager.lockRemainingSeconds(this))), false, false);
                    break;
                default:
                    // mensagem generica q nao revela se o erro foi no e-mail ou na senha.
                    etPassword.setText("");
                    showError(getString(R.string.error_credentials), true, true);
                    break;
            }
        }, SIMULATED_LATENCY_MS);
    }

    private void setBusy(boolean value) {
        busy = value;
        btnLogin.setEnabled(!value);
        tvLoginLabel.setText(value ? R.string.login_button_loading : R.string.login_button);
    }

    private void showError(String message, boolean markEmail, boolean markPassword) {
        errorMessage.setText(message);
        errorBanner.setVisibility(View.VISIBLE);
        if (markEmail) boxEmail.setBackgroundResource(R.drawable.auth_bg_input_error);
        if (markPassword) boxPassword.setBackgroundResource(R.drawable.auth_bg_input_error);
    }

    private void clearError() {
        errorBanner.setVisibility(View.GONE);
        boxEmail.setBackgroundResource(R.drawable.auth_bg_input);
        boxPassword.setBackgroundResource(R.drawable.auth_bg_input);
        boxEmail.setSelected(etEmail.hasFocus());
        boxPassword.setSelected(etPassword.hasFocus());
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        int selection = etPassword.getSelectionStart();
        etPassword.setTransformationMethod(passwordVisible
                ? HideReturnsTransformationMethod.getInstance()
                : PasswordTransformationMethod.getInstance());
        ivToggle.setImageResource(passwordVisible
                ? R.drawable.auth_ic_visibility_off : R.drawable.auth_ic_visibility);
        if (selection >= 0) etPassword.setSelection(selection);
    }

    private void comingSoon() {
        Toast.makeText(this, R.string.login_coming_soon, Toast.LENGTH_SHORT).show();
    }

    private void hideKeyboard() {
        View focus = getCurrentFocus();
        if (focus == null) return;
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        etPassword.setText("");
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
