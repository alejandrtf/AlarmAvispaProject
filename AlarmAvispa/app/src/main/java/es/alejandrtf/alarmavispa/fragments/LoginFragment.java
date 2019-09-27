package es.alejandrtf.alarmavispa.fragments;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;


import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.activities.LoginActivityArgs;
import es.alejandrtf.alarmavispa.storage.classes.Users;
import es.alejandrtf.alarmavispa.utilities.Constants;
import es.alejandrtf.alarmavispa.utilities.Internet;


import static es.alejandrtf.alarmavispa.storage.classes.Users.saveUserPreferences;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {
    private final static String TAG = "LoginFragment";

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String email = "";
    private String password = "";
    private TextInputEditText etEmail, etPassword;
    private TextInputLayout layoutEmail, layoutPassword;
    private ProgressDialog dialog;
    private MaterialButton btSignin, btRegister;
    private TextView tvForgottenPassword;

    //email verification
    private final String TAG_VERIFICATION = "Verification";
    private boolean isAdminAccount = false;

    // variables for navigation control
    private String sourceFragmentName; // I need it to know from what fragment this has been reached


    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        setReferencesViewsToXML(v);

        return v;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //when I arrive from another fragment different of NotifiSighting (for example: MapSightingFragment,...),
        // in the argument, will be the name of the source fragment. I must to collect it
        //Use saveArgs to collect it
        try {
            sourceFragmentName = LoginActivityArgs.fromBundle(getActivity().getIntent().getExtras()).getFragmentOrigin();
        } catch (IllegalArgumentException e) {
            // I arrived here from initial screen, so there isn't argument in LoginActivityArgs
            sourceFragmentName = "main";
        }


        //create new progress dialog
        dialog = new ProgressDialog(getActivity());
        dialog.setTitle(getString(R.string.verifyingUser));
        dialog.setMessage(getString(R.string.pleaseWaiting));

        btSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSessionEmail();

            }
        });

        tvForgottenPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retrievePassword();
            }
        });

        btRegister.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_fragmentLogin_to_fragmentRegister, null));
    }


    //region CUSTOM METHODS

    /**
     * Gets  XML references for JAVA View objects
     */
    private void setReferencesViewsToXML(View v) {
        etEmail = v.findViewById(R.id.etEmail);
        etPassword = v.findViewById(R.id.etPassword);
        layoutEmail = v.findViewById(R.id.layoutEmail);
        layoutPassword = v.findViewById(R.id.layoutPassword);
        btSignin = v.findViewById(R.id.btnSignin);
        btRegister = v.findViewById(R.id.btnRegister);
        tvForgottenPassword = v.findViewById(R.id.tvLabelForgottenPassword);
    }


    /**
     * Checks if we already have an user or not
     */
    private void checkIfValidatedUser() {
        Log.i(TAG, "checkIfValidatedUser()");

        if (auth.getCurrentUser() != null) {
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            user.reload();
            String provider = "otro";
            try {
                provider = user.getProviders().get(0).trim();
            } catch (Exception e) {
                provider = "anonim";
            }

            if (provider.equals("password")) {
                //authenticated with email

                isAdminAccount = email.equals(Constants.adminEmail);

                //check if email is verified
                if (!isAdminAccount && !user.isEmailVerified()) {
                    //the admin account is not necessary to verify
                    Log.i(TAG, "checkIfValidatedUser()-> if(!user.isEmailVerified())");
                    user.reload();
                    //I have to send email of verification
                    user.sendEmailVerification();
                    //inform to user about this situation
                    showVerificationDialog(user);
                } else {
                    //account verified or it's admin account
                    //update start session in Firestore
                    Users.updateStartSession(user);
                    //save user in Preferences
                    saveUserPreferences(getContext(), user);

                    /*Toast.makeText(getContext(), "inicia sesión: " +
                            user.getDisplayName() + " - " + user.getEmail() + " - " +
                            provider, Toast.LENGTH_LONG).show();*/
                    Log.d(TAG_VERIFICATION, provider);

                    if (sourceFragmentName.equals(getString(R.string.mapSightingFragmentLabel))) {
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(Constants.PREFERENCES_KEY_GO_MAPSIGHTINGFRAGMENT, true).commit();
                    } else if (sourceFragmentName.equals(getString(R.string.trapFragmentLabel))) {
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(Constants.PREFERENCES_KEY_GO_TRAPFRAGMENT, true).commit();
                    }
                    getActivity().finish();


                }
            }


        }
    }


    /**
     * Method shows dialog  to inform that a confirmation email has been sent and waits the user click
     * the button to confirm that he's verified his email
     *
     * @param user - user which is logging
     */
    private void showVerificationDialog(final FirebaseUser user) {
        Log.i(TAG, "showVerificationDialog()");
        String message = getString(R.string.messageVerification) + user.getEmail();

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        //set custom title and message
        builder.setTitle(R.string.titleDialog)
                .setMessage(message);

        //set buttons
        builder.setPositiveButton(R.string.buttonVerification, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "showVerificationDialog()-> onClick PositiveButton pressed");

                //disconnect user
                FirebaseAuth.getInstance().signOut();
                //connect again that user in order to reload user (already verified)
                startSessionEmail();

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();


    }


    /**
     * Method is executed when button START SESSION is pressed
     */
    private void startSessionEmail() {
        Log.i(TAG, "startSessionEmail()");
        if (verifyFields()) {
            dialog.show();


            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                checkIfValidatedUser();
                            } else {

                                dialog.dismiss();
                                if (!Internet.isInternetConectionActive(getContext()))
                                    Toast.makeText(getContext(), R.string.error_conection_internet_message, Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(getContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

        }

    }


    /**
     * Method verifies  if the email and password fields are valid and in that case gets their values
     *
     * @return true if they are right;false in other case
     */
    private boolean verifyFields() {
        email = etEmail.getText().toString();
        password = etPassword.getText().toString();
        layoutEmail.setError("");
        layoutPassword.setError("");
        if (email.isEmpty()) {
            layoutEmail.setError(getString(R.string.error_email_empty));
        } else if (!email.matches(".+@.+[.].+")) {
            layoutEmail.setError(getString(R.string.error_invalid_email));
        } else if (password.isEmpty()) {
            layoutPassword.setError(getString(R.string.error_password_empty));
        } else if (password.length() < 6) {
            layoutPassword.setError(getString(R.string.error_password_invalid_lenght));
        } else if (!password.matches(".*[0-9].*")) {
            layoutPassword.setError(getString(R.string.error_password_without_number));
        } else if (!password.matches(".*[A-Z].*")) {
            layoutPassword.setError(getString(R.string.error_password_without_capital_letter));
        } else {
            return true;
        }
        return false;
    }


    /**
     * Method sends an email to the user's account to reset his password
     */
    private void retrievePassword() {
        email = etEmail.getText().toString();
        layoutEmail.setError("");
        if (email.isEmpty()) {
            layoutEmail.setError(getString(R.string.error_email_empty));
        } else if (!email.matches(".+@.+[.].+")) {
            layoutEmail.setError(getString(R.string.error_invalid_email));
        } else {
            dialog.show();
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            dialog.dismiss();
                            if (task.isSuccessful()) {
                                showDialog(getString(R.string.verify_email_reset_password));
                            } else {
                                showDialog(getString(R.string.error_send_email_reset_password));
                            }
                        }
                    });
        }

    }


    /**
     * Method shows an information dialog with the state of user's reset password process
     *
     * @param message - Text shown in the dialog
     */
    private void showDialog(String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        //set custom title and message
        builder.setTitle(R.string.title_dialog_reset_password)
                .setMessage(message);

        //set buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();


    }
    //end region

}
