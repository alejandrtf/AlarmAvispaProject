package es.alejandrtf.alarmavispa.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.models.UserModel;
import es.alejandrtf.alarmavispa.storage.classes.Users;

public class MyProfileActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextInputLayout nameLayout, surnameLayout, phoneLayout;
    private TextInputEditText etEmail,etName,etSurname,etIdCard,etPhone,etCity,etProvince;
    private MaterialButton btCancel, btUpdateProfile;
    private UserModel userInformation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        toolbar = findViewById(R.id.toolbarMyProfile);
        setSupportActionBar(toolbar);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        setReferencesViewsToXML();
        initUI();
        setActionsToButtons();

        Users.getUserInformationFromDB(user.getUid(), new Users.OnCompletedGetListener() {
            @Override
            public void onCompletedGet(UserModel user) {
                if(user!=null){
                    userInformation=updateUserInformationUI(user);
                }
            }
        });

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
        nameLayout = findViewById(R.id.layoutNameProfile);
        surnameLayout = findViewById(R.id.layoutSurnameProfile);
        phoneLayout = findViewById(R.id.layoutPhoneProfile);
        etEmail=findViewById(R.id.etEmailProfile);
        etName=findViewById(R.id.etNameProfile);
        etSurname=findViewById(R.id.etSurnameProfile);
        etIdCard=findViewById(R.id.etIdCardProfile);
        etPhone=findViewById(R.id.etPhoneProfile);
        etCity=findViewById(R.id.etCityProfile);
        etProvince=findViewById(R.id.etProvinceProfile);
        btCancel = findViewById(R.id.btCancelProfile);
        btUpdateProfile=findViewById(R.id.btUpdateProfile);
    }


    /**Method initializes the UI
     *
     */
    private void initUI(){
        // Set up required fields
        nameLayout.setHint(nameLayout.getHint() + " " + getString(R.string.asterisk));
        surnameLayout.setHint(surnameLayout.getHint() + " " + getString(R.string.asterisk));
        phoneLayout.setHint(phoneLayout.getHint() + " " + getString(R.string.asterisk));

    }

    /** Method updates the user's information in the UI
     * @param userInformation - the UserModel object with user's information
     * @return UserModel object with user's information
     */
    private UserModel updateUserInformationUI(UserModel userInformation){
        // Load user information
        etEmail.setText(userInformation.getEmail());
        etName.setText(userInformation.getName());
        etSurname.setText(userInformation.getSurName());
        etIdCard.setText(userInformation.getIdCard());
        etPhone.setText(userInformation.getPhone());
        etCity.setText(userInformation.getCity());
        etProvince.setText(userInformation.getProvince());
        return userInformation;
    }



    /**
     * Set up to each button its actions
     */
    public void setActionsToButtons() {
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        btUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name=etName.getText().toString();
                String surName=etSurname.getText().toString();
                String phone=etPhone.getText().toString();

                if(!name.isEmpty())
                    userInformation.setName(name);
                if(!surName.isEmpty())
                    userInformation.setSurName(surName);
                userInformation.setIdCard(etIdCard.getText().toString());
                if(!phone.isEmpty())
                    userInformation.setPhone(phone);
                userInformation.setCity(etCity.getText().toString());
                userInformation.setProvince(etProvince.getText().toString());
                Users.updateUserProfile(userInformation);
                finish();

            }
        });
    }

    //endregion
}
