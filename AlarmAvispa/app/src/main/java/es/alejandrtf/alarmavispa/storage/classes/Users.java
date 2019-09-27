package es.alejandrtf.alarmavispa.storage.classes;

import android.content.Context;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.models.UserModel;

import static es.alejandrtf.alarmavispa.utilities.Constants.USERS_COLLECTION_NAME;

public class Users {

    public interface OnCompletedGetListener{
        void onCompletedGet(UserModel user);
    }

    /**
     * Method saves an user in Firebase Firestore
     *
     * @param context Context
     * @param userId  - the user id
     * @param user    - UserModel object with information about user
     */
    public static void saveUser(Context context, String userId, UserModel user) {
        //almacena en FirebaseFiresotre ese usuario
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(USERS_COLLECTION_NAME).document(userId).set(user);
    }


    /**
     * Method updates user's startSession field which FirebaseUser is specified
     *
     * @param user FirebaseUser object
     */
    public static void updateStartSession(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(USERS_COLLECTION_NAME).document(user.getUid()).update("startSession", System.currentTimeMillis());
    }



    /**
     * Method  gets asynchronously user's data  from database Firebase Firestore for the user with the id specified
     * Data are returned by the specified listener
     *
     * @param userId - the user id yout want to get
     * @param listener - OnCompletedGetListener object that implements the listener will get the user information
     */
    public static void getUserInformationFromDB(String userId, final OnCompletedGetListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(USERS_COLLECTION_NAME).document(userId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            UserModel userInformation = task.getResult().toObject(UserModel.class);
                            listener.onCompletedGet(userInformation);
                        }else{
                            Log.e("Firebase","Error reading",task.getException());
                            listener.onCompletedGet(null);
                        }
                    }
                });
    }


    /**
     * Method updates the user's profile in the database
     *
     * @param user - UserModel object with all information
     */
    public static void updateUserProfile(UserModel user) {
        FirebaseUser myCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(user.getName() + " " + user.getSurName())
                .build();

        myCurrentUser.updateProfile(profileUpdates);
        // update in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(USERS_COLLECTION_NAME).document(myCurrentUser.getUid()).set(user);


    }


    /**
     * Method saves an user in Preferences
     *
     * @param context Context
     * @param user    - FirebaseUser object with information about user
     */
    public static void saveUserPreferences(Context context, final FirebaseUser user) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString (context.getString(R.string.prefs_key_full_name),user.getDisplayName());
        editor.putString(context.getString(R.string.prefs_key_user_email), user.getEmail());
        editor.putString(context.getString(R.string.prefs_key_user_id), user.getUid());
        editor.commit();
    }


    /**
     * Method delete an user from Preferences
     *
     * @param context Context
     */
    public static void deleteUserFromPreferences(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        editor.remove(context.getString(R.string.prefs_key_full_name));
        editor.remove(context.getString(R.string.prefs_key_user_email));
        editor.remove(context.getString(R.string.prefs_key_user_id));
        editor.apply();
    }


    /**
     * Method checks if an user is logged in this application by using preferences
     *
     * @param context Context
     * @return true if there are any in the preferences; false otherwise
     */
    public static boolean isUserLogged(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.contains(context.getString(R.string.prefs_key_user_email));

    }



    /**
     * Method gets the user id of user that is logged from the preferences
     *
     * @param context Context
     * @return userId - the id of the user that is logged in
     */
    public static String getUserIdOfUserLoggedFromPreferences(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.prefs_key_user_id),"");
    }


}
