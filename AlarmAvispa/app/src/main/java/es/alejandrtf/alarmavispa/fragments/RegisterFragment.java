package es.alejandrtf.alarmavispa.fragments;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;


import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.activities.LoginActivityArgs;
import es.alejandrtf.alarmavispa.models.UserModel;
import es.alejandrtf.alarmavispa.utilities.Constants;
import es.alejandrtf.alarmavispa.utilities.Internet;

import static es.alejandrtf.alarmavispa.storage.classes.Users.saveUser;
import static es.alejandrtf.alarmavispa.storage.classes.Users.saveUserPreferences;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment {
    private final static String TAG = "LoginFragment";

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String email = "";
    private String password = "";
    private String retypePassword = "";
    private TextInputLayout emailLayout, passwordLayout, retypePasswordLayout, nameLayout, surnameLayout, phoneLayout;
    private TextInputEditText etEmail, etPassword, etRetypePassword, etName, etSurname, etIdCard, etPhone, etCity, etProvince;
    private MaterialButton btRegisterMe;
    private ProgressDialog dialog;
    private UserModel userToFirestore;

    //email verification
    private final String TAG_VERIFICATION = "Verification";

    // variables for navigation control
    private String sourceFragmentName; // I need it to know from what fragment this has been reached


    public RegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_register, container, false);
        setReferencesViewsToXML(v);
        initUI();
        return v;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //when I arrive from another fragment different of NotifiSighting (for example: MapSightingFragment,...),
        // in the argument, will be the name of the source fragment. I must to collect it

        try {
            //Use saveArgs to collect it
            sourceFragmentName = LoginActivityArgs.fromBundle(getActivity().getIntent().getExtras()).getFragmentOrigin();
        } catch (IllegalArgumentException e) {
            // I arrived here from initial screen, so there isn't argument in LoginActivityArgs
            sourceFragmentName = "main";
        }
        //create new progress dialog
        dialog = new ProgressDialog(getActivity());
        dialog.setTitle(getString(R.string.verifyingUser));
        dialog.setMessage(getString(R.string.pleaseWaiting));

        btRegisterMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerWithEmail();
            }
        });


    }


    //region CUSTOM METHODS

    /**
     * Gets  XML references for JAVA View objects
     */
    private void setReferencesViewsToXML(View v) {
        emailLayout = v.findViewById(R.id.layoutEmailRegister);
        passwordLayout = v.findViewById(R.id.layoutPasswordRegister);
        retypePasswordLayout = v.findViewById(R.id.layoutRetypePasswordRegister);
        nameLayout = v.findViewById(R.id.layoutNameRegister);
        surnameLayout = v.findViewById(R.id.layoutSurnameRegister);
        phoneLayout = v.findViewById(R.id.layoutPhoneRegister);
        etEmail = v.findViewById(R.id.etEmailRegister);
        etPassword = v.findViewById(R.id.etPasswordRegister);
        etRetypePassword = v.findViewById(R.id.etRetypePasswordRegister);
        etName = v.findViewById(R.id.etNameRegister);
        etSurname = v.findViewById(R.id.etSurnameRegister);
        etIdCard = v.findViewById(R.id.etIdCardRegister);
        etPhone = v.findViewById(R.id.etPhoneRegister);
        etCity = v.findViewById(R.id.etCityRegister);
        etProvince = v.findViewById(R.id.etProvinceRegister);
        btRegisterMe = v.findViewById(R.id.btRegisterMe);
    }


    /**
     * Method initializes the UI
     */
    private void initUI() {
        // Set up required fields
        emailLayout.setHint(emailLayout.getHint() + " " + getString(R.string.asterisk));
        passwordLayout.setHint(passwordLayout.getHint() + " " + getString(R.string.asterisk));
        retypePasswordLayout.setHint(retypePasswordLayout.getHint() + " " + getString(R.string.asterisk));
        nameLayout.setHint(nameLayout.getHint() + " " + getString(R.string.asterisk));
        surnameLayout.setHint(surnameLayout.getHint() + " " + getString(R.string.asterisk));
        phoneLayout.setHint(phoneLayout.getHint() + " " + getString(R.string.asterisk));

    }


    /**
     * Method is executed when button REGISTERME is pressed
     */
    private void registerWithEmail() {
        if (verifyFields()) {
            dialog.show();
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
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
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
            alertDialog.setTitle(R.string.error_fields_register);
            alertDialog.setMessage(getString(R.string.error_fields_register_message));
            alertDialog.setCancelable(false);
            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.close), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            alertDialog.show();
        }
    }


    /**
     * Method verifies  if the email and password fields are valid and in that case gets their values
     *
     * @return true if they are right;false in otherwise
     */
    private boolean verifyFields() {
        email = etEmail.getText().toString();
        password = etPassword.getText().toString();
        retypePassword = etRetypePassword.getText().toString();

        emailLayout.setError("");
        passwordLayout.setError("");
        retypePasswordLayout.setError("");
        nameLayout.setError("");
        surnameLayout.setError("");
        phoneLayout.setError("");


        if (email.isEmpty()) {
            emailLayout.setError(getString(R.string.error_email_empty));
        } else if (!email.matches(".+@.+[.].+")) {
            emailLayout.setError(getString(R.string.error_invalid_email));
        } else if (password.isEmpty()) {
            passwordLayout.setError(getString(R.string.error_password_empty));
        } else if (password.length() < 6) {
            passwordLayout.setError(getString(R.string.error_password_invalid_lenght));
        } else if (!password.matches(".*[0-9].*")) {
            passwordLayout.setError(getString(R.string.error_password_without_number));
        } else if (!password.matches(".*[A-Z].*")) {
            passwordLayout.setError(getString(R.string.error_password_without_capital_letter));
        } else if (retypePassword.isEmpty()) {
            retypePasswordLayout.setError(getString(R.string.error_password_empty));
        } else if (retypePassword.length() < 6) {
            retypePasswordLayout.setError(getString(R.string.error_password_invalid_lenght));
        } else if (!retypePassword.matches(".*[0-9].*")) {
            retypePasswordLayout.setError(getString(R.string.error_password_without_number));
        } else if (!retypePassword.matches(".*[A-Z].*")) {
            retypePasswordLayout.setError(getString(R.string.error_password_without_capital_letter));
        } else if (!password.equals(retypePassword)) {
            retypePasswordLayout.setError(getString(R.string.passwordNoMatch));
        } else if (etName.getText().toString().isEmpty()) {
            nameLayout.setError(getString(R.string.error_name_empty));
        } else if (etSurname.getText().toString().isEmpty()) {
            surnameLayout.setError(getString(R.string.error_surname_empty));
        } else if (etPhone.getText().toString().isEmpty()) {
            phoneLayout.setError(getString(R.string.error_phone_empty));
        } else {

            return true;
        }
        return false;
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

                //check if email is verified
                if (!user.isEmailVerified()) {
                    Log.i(TAG, "checkIfValidatedUser()-> if(!user.isEmailVerified())");
                    user.reload();
                    //I have to send email of verification
                    user.sendEmailVerification();
                    //inform to user about this situation
                    showVerificationDialog(user);
                } else {
                    dialog.dismiss();
                    //account already verified
                    saveUserInformation(user);

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
     * Method save the user's information in Firebase Firestore database, Firebase Authentication and
     * preferences.
     */
    private void saveUserInformation(FirebaseUser user) {
        //load data in UserModel object to save later in Firebase Firestore
        userToFirestore = new UserModel(etName.getText().toString(),
                etSurname.getText().toString(), user.getEmail(),
                etIdCard.getText().toString(),
                etPhone.getText().toString(),
                etCity.getText().toString(),
                etProvince.getText().toString(), System.currentTimeMillis(),
                Constants.userProfile);
        //save user in Firebase Firestore
        saveUser(getContext(), user.getUid(), userToFirestore);

        //update display name in FirebaseAuth
        UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                .setDisplayName(etName.getText().toString() + " " + etSurname.getText().toString())
                .build();
        user.updateProfile(profile);

        //save user in Preferences
        saveUserPreferences(getContext(), user);
    }


    /**
     * Method makes login from the user who is registering
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
                                Toast.makeText(getContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();

                            }
                        }
                    });

        }

    }

    //endregion

}
