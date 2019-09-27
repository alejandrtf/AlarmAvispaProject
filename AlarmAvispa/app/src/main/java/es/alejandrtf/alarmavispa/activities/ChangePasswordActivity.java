package es.alejandrtf.alarmavispa.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.alejandrtf.alarmavispa.R;

public class ChangePasswordActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextInputEditText etOldPassword, etNewPassword, etRetypeNewPassword;
    private TextInputLayout layoutOldPassword, layoutNewPassword, layoutRetypeNewPassword;
    private MaterialButton btCancelChange, btUpdateChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_change_password);

        toolbar = findViewById(R.id.toolbarChangePassword);
        setSupportActionBar(toolbar);

        setReferencesViewsToXML();
        setActionsToButtons();
    }


    //Setting Up the back button
    @Override
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp();
    }


    //region CUSTOM METHODS

    /**
     * Gets  XML references for JAVA View objects
     */
    private void setReferencesViewsToXML() {
        layoutOldPassword = findViewById(R.id.layoutOldPassword);
        layoutNewPassword = findViewById(R.id.layoutNewPassword);
        layoutRetypeNewPassword = findViewById(R.id.layoutRetypeNewPassword);
        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etRetypeNewPassword = findViewById(R.id.etRetypeNewPassword);
        btCancelChange = findViewById(R.id.btCancelChangePassword);
        btUpdateChange = findViewById(R.id.btChangePassword);
    }


    /**
     * Set up to each button its actions
     */
    public void setActionsToButtons() {
        btCancelChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btUpdateChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (verifyPassword()) {
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    final String email = user.getEmail();
                    AuthCredential credential = EmailAuthProvider.getCredential(email, etOldPassword.getText().toString());

                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user.updatePassword(etNewPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(ChangePasswordActivity.this, getString(R.string.errorChangingPassword), Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(ChangePasswordActivity.this, getString(R.string.successChangingPassword), Toast.LENGTH_SHORT).show();
                                            // finish activity after 1,5 seconds
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    finish();
                                                }
                                            },1500);
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(ChangePasswordActivity.this, getString(R.string.errorAuthenticationFailed), Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                }

            }

        });
    }


    /**
     * Method verifies  if the password fields are valid and in that case gets their values
     *
     * @return true if they are right;false otherwise
     */
    private boolean verifyPassword() {
        //current password
        if (verifyField(etOldPassword, layoutOldPassword)) {
            if (verifyField(etNewPassword, layoutNewPassword)) {
                if(etNewPassword.getText().toString().equals(etOldPassword.getText().toString())){
                    layoutNewPassword.setError(getString(R.string.errorOldNewPasswordMatch));
                    return false;
                }else if (verifyField(etRetypeNewPassword, layoutRetypeNewPassword)) {
                    if (etNewPassword.getText().toString().equals(etRetypeNewPassword.getText().toString()))
                        return true;
                    else {
                        layoutRetypeNewPassword.setError(getString(R.string.passwordNoMatch));
                        return false;
                    }
                } else return false;
            } else return false;
        } else return false;
    }


    /**
     * Method checks if an editText field is a valid password
     *
     * @param editText - TextInputEditText to check
     * @param layout   - TextInputLayout where error message is written
     * @return true if it's right; false otherwise
     */
    private boolean verifyField(TextInputEditText editText, TextInputLayout layout) {
        String password = editText.getText().toString();
        layout.setError("");
        if (password.isEmpty()) {
            layout.setError(getString(R.string.error_password_empty));
        } else if (password.length() < 6) {
            layout.setError(getString(R.string.error_password_invalid_lenght));
        } else if (!password.matches(".*[0-9].*")) {
            layout.setError(getString(R.string.error_password_without_number));
        } else if (!password.matches(".*[A-Z].*")) {
            layout.setError(getString(R.string.error_password_without_capital_letter));
        } else {
            return true;
        }
        return false;
    }
    //endregion
}
