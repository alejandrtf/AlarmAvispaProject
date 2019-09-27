package es.alejandrtf.alarmavispa.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.models.UserModel;
import es.alejandrtf.alarmavispa.storage.classes.Users;
import es.alejandrtf.alarmavispa.utilities.Constants;

public class ContactActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private MaterialButton btSend;
    private TextInputEditText etName, etPhone, etSubject, etMessage;
    private TextInputLayout nameLayout, phoneLayout, subjectLayout, messageLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_contact);

        toolbar = findViewById(R.id.toolbarContact);
        setSupportActionBar(toolbar);

        setReferencesViewsToXML();
        initUI();
        setActionsToButtons();
    }


    //region CUSTOM METHODS

    /**
     * Gets  XML references for JAVA View objects
     */
    private void setReferencesViewsToXML() {
        nameLayout = findViewById(R.id.layoutNameContact);
        phoneLayout = findViewById(R.id.layoutPhoneContact);
        subjectLayout = findViewById(R.id.layoutSubjectContact);
        messageLayout = findViewById(R.id.layoutMessageContact);
        etName = findViewById(R.id.etNameContact);
        etPhone = findViewById(R.id.etPhoneContact);
        etSubject = findViewById(R.id.etSubjectContact);
        etMessage = findViewById(R.id.etMessageContact);
        btSend = findViewById(R.id.btSendMessageContact);
    }


    /**
     * Method initializes the UI
     */
    private void initUI() {
        // Set up required fields
        nameLayout.setHint(nameLayout.getHint() + " " + getString(R.string.asterisk));
        phoneLayout.setHint(phoneLayout.getHint() + " " + getString(R.string.asterisk));
        subjectLayout.setHint(subjectLayout.getHint() + " " + getString(R.string.asterisk));
        messageLayout.setHint(messageLayout.getHint() + " " + getString(R.string.asterisk));

        //check if I  already have an user logged in order to catch his name and phone in that case.
        if (Users.isUserLogged(this)) {
            Users.getUserInformationFromDB(Users.getUserIdOfUserLoggedFromPreferences(this),
                    new Users.OnCompletedGetListener() {
                        @Override
                        public void onCompletedGet(UserModel user) {
                            etName.setText(user.getName() + " " + user.getSurName());
                            etPhone.setText(user.getPhone());
                        }
                    });
        }
    }

    /**
     * Set up to each button its actions
     */
    public void setActionsToButtons() {
        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (verifyFields()) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{Constants.DEVELOPER_EMAIL_ADDRESS});
                    intent.putExtra(Intent.EXTRA_SUBJECT, etSubject.getText().toString());
                    intent.putExtra(Intent.EXTRA_TEXT, etPhone.getText().toString() + "\n" +
                            etMessage.getText().toString());
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                    finish();


                }
            }
        });
    }

    /**
     * Method verifies  if all fields are filled in
     *
     * @return true if they are right;false in otherwise
     */
    private boolean verifyFields() {
        String name = etName.getText().toString();
        String phone = etPhone.getText().toString();
        String subject = etSubject.getText().toString();
        String message = etMessage.getText().toString();

        nameLayout.setError("");
        phoneLayout.setError("");
        subjectLayout.setError("");
        messageLayout.setError("");


        if (name.isEmpty()) {
            nameLayout.setError(getString(R.string.error_name_empty));
        } else if (phone.isEmpty()) {
            phoneLayout.setError(getString(R.string.error_phone_empty));
        } else if (subject.isEmpty()) {
            subjectLayout.setError(getString(R.string.error_subject_empty));
        } else if (message.isEmpty()) {
            messageLayout.setError(getString(R.string.error_message_empty));
        } else {

            return true;
        }
        return false;
    }
    //endregion

}
