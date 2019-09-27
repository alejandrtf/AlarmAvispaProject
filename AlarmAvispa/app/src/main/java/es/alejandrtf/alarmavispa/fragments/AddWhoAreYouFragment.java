package es.alejandrtf.alarmavispa.fragments;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.models.Sighting;
import es.alejandrtf.alarmavispa.models.SightingType;
import es.alejandrtf.alarmavispa.models.State;
import es.alejandrtf.alarmavispa.models.UserModel;
import es.alejandrtf.alarmavispa.storage.classes.NumberNotificationsYearMunicipalityFirestore;
import es.alejandrtf.alarmavispa.storage.classes.SightingFirestore;
import es.alejandrtf.alarmavispa.storage.classes.Users;
import es.alejandrtf.alarmavispa.storage.interfaces.NumberNotificationsYearMunicipalityAsync;
import es.alejandrtf.alarmavispa.storage.interfaces.SightingAsync;
import es.alejandrtf.alarmavispa.utilities.Constants;
import es.alejandrtf.alarmavispa.utilities.Contacts;
import es.alejandrtf.alarmavispa.utilities.Internet;
import es.alejandrtf.alarmavispa.utilities.Utils;

import static es.alejandrtf.alarmavispa.utilities.Constants.REQUEST_PERMISSION_READ_CONTACT;
import static es.alejandrtf.alarmavispa.utilities.Constants.REQUEST_PERMISSION_WRITE_CONTACT;
import static es.alejandrtf.alarmavispa.utilities.Permissions.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE;
import static es.alejandrtf.alarmavispa.utilities.Permissions.hasDeviceContactsPermission;
import static es.alejandrtf.alarmavispa.utilities.Permissions.hasDeviceStoragePermission;
import static es.alejandrtf.alarmavispa.utilities.Permissions.requestPermissionFragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddWhoAreYouFragment extends Fragment {
    private static final String TAG = "AddWhoAreYouFragment";

    // Layout variables
    private TextInputLayout layoutFullName, layoutPhone;
    private TextInputEditText etFullName, etPhone;
    private CheckBox cbAcceptPrivayPolicy;
    private MaterialButton btSendSighting;

    //Cloud Storage variables
    private StorageReference mStorageRef;
    static UploadTask uploadTask = null; //tarea para subir archivos a Firebase Storage
    StorageReference imagenRef; //referencia al archivo que quiero subir
    List<String> downloadUri; //
    private List<String> urisImagesToSendWhatsapp; // there are uris of the device, not firebase
    private ProgressDialog mProgressDialog;

    //Firebase Firestore variables
    private SightingAsync sightings; //interface
    private NumberNotificationsYearMunicipalityAsync numberNotificationsYearMunicipality; //interface
    private long newNumberNotificationsForThatMunicipality;

    private Gson gson;
    private Sighting currentSighting;
    private String currentSightingCode;

    //whatsapp variables
    private Sighting sightingToWhatsapp;
    private String codeToWhatsapp;
    private List<String> photosToWhatsapp;

    public AddWhoAreYouFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gson = new Gson();
        //get reference to Cloud Storage
        mStorageRef = FirebaseStorage.getInstance().getReference();
        // get the helpers to make CRUD operations in Cloud Firestore with nodes: Sighting and NumberNotificationsYearMunicipalityAsync
        sightings = new SightingFirestore(getContext());
        numberNotificationsYearMunicipality = new NumberNotificationsYearMunicipalityFirestore(getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_who_are_you, container, false);
        setReferencesViewsToXML(v);
        initUI();
        return v;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String previousScreenInformationSighting = AddWhoAreYouFragmentArgs.fromBundle(getArguments()).getInformationPrevious();
        //deserialize. This object contains the information of previous screen
        currentSighting = gson.fromJson(previousScreenInformationSighting, Sighting.class);
        setUPLinkPrivacyPolicyUI(view);
        setActionsToButtons();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //check if I  already have an user logged in order to catch his name and phone in that case.
        if (Users.isUserLogged(getContext())) {
            Users.getUserInformationFromDB(Users.getUserIdOfUserLoggedFromPreferences(getContext()),
                    new Users.OnCompletedGetListener() {
                        @Override
                        public void onCompletedGet(UserModel user) {
                            etFullName.setText(user.getName() + " " + user.getSurName());
                            etPhone.setText(user.getPhone());
                            currentSighting.setCreatorId(Users.getUserIdOfUserLoggedFromPreferences(getContext()));

                        }
                    });
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    ///////////  METHOD COLLECTS THE RESULT OF REQUESTING PERMISSIONS /////////
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_READ_CONTACT:
            case REQUEST_PERMISSION_WRITE_CONTACT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission was granted");
                    sendByWhatsapp(currentSighting, currentSighting.getPhoto(), currentSightingCode);
                } else {
                    //permission denied
                    Toast.makeText(getContext(), getString(R.string.error_permission_read_contact), Toast.LENGTH_LONG).show();
                }
                return;
            case REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission was granted");
                    sendByWhatsapp(currentSighting, currentSighting.getPhoto(), currentSightingCode);
                } else {
                    //permission denied
                    Toast.makeText(getContext(), getString(R.string.error_permission_write_external_storage), Toast.LENGTH_LONG).show();
                }
                return;
        }
    }


    //region CUSTOM METHODS

    /**
     * Gets  XML references for JAVA View objects
     */
    private void setReferencesViewsToXML(View v) {
        layoutFullName = v.findViewById(R.id.layoutFullNameAddWhoAreYou);
        layoutPhone = v.findViewById(R.id.layoutPhoneAddWhoAreYou);
        etFullName = v.findViewById(R.id.etFullNameAddWhoAreYou);
        etPhone = v.findViewById(R.id.etPhoneAddWhoAreYou);
        cbAcceptPrivayPolicy = v.findViewById(R.id.cbAcceptPrivayPolicyAddWhoAreYou);
        btSendSighting = v.findViewById(R.id.btSendAddWhoAreYouFragment);
    }


    /**
     * Method configures the link to open the privacy policy alert dialog
     */
    private void setUPLinkPrivacyPolicyUI(View v) {
        final String token = "#";
        String text = getString(R.string.InformationPrivacyPolicyAddWhoAreYou, token, token);

        int start = text.indexOf(token);
        //we do -1 since we are about to remove the tokens afterwards so it shifts
        int end = text.indexOf(token, start + 1) - 1;

        // remove token from original text
        text = text.replaceAll(token, "");

        // create spannable
        SpannableString ss = new SpannableString(text);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                showWebViewAlertDialogPrivacy();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };

        ss.setSpan(clickableSpan, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        TextView tv = ((TextView) v.findViewById(R.id.tvInformationPrivacyPolicy));
        tv.setText(ss);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setHighlightColor(Color.TRANSPARENT);

    }

    /**
     * Set up to each button its actions
     */
    private void setActionsToButtons() {
        btSendSighting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (verifiedFields()) {
                    if (cbAcceptPrivayPolicy.isChecked()) {
                        //policy accepted
                        if (!Users.isUserLogged(getContext())) {
                            currentSighting.setUser(etFullName.getText().toString());
                            currentSighting.setPhone(etPhone.getText().toString());
                        }
                        currentSighting.setSightingDate(System.currentTimeMillis());
                        currentSighting.setState(State.REPORTED.getLocalizedName(getContext()));

                        if (Internet.isInternetConectionActive(getContext())) {

                            showProgressDialog(getString(R.string.title_dialog_sending_sighting));

                            Calendar c = Calendar.getInstance();
                            c.setTimeInMillis(currentSighting.getSightingDate());
                            int year = c.get(Calendar.YEAR);

                            currentSightingCode = "V" + String.valueOf(year) + currentSighting.getMunicipality().substring(0, 3).toUpperCase(); //only the first three characters
                            // check if there are already notifications for that municipality in this year
                            numberNotificationsYearMunicipality.getNumberNotificationsYearMunicipality(String.valueOf(year), currentSighting.getMunicipality(),
                                    new NumberNotificationsYearMunicipalityAsync.OnCompletedGetNumberNotificationsListener() {
                                        @Override
                                        public void onCompletedGetNumberNotifications(long totalNotitications) {
                                            newNumberNotificationsForThatMunicipality = totalNotitications + 1;
                                            // recalculate the new code for my sighting based on the number of notification got.
                                            currentSightingCode += String.format(Locale.getDefault(), "%04d", newNumberNotificationsForThatMunicipality);
                                            //I must upload all images to Cloud storage and wait for all successfully
                                            uploadAllImagesSighting(currentSighting, currentSightingCode);
                                        }

                                        @Override
                                        public void onNotExists() {
                                            newNumberNotificationsForThatMunicipality = 1;
                                            // recalculate the new code for my sighting based on the number of notification got.
                                            currentSightingCode += String.format(Locale.getDefault(), "%04d", newNumberNotificationsForThatMunicipality);
                                            //I must upload all images to Cloud storage and wait for all successfully
                                            uploadAllImagesSighting(currentSighting, currentSightingCode);
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            currentSightingCode = null;
                                        }
                                    });


                        } else {
                            // there is not Internet
                            showMessageDialog(getString(R.string.error_conection_internet_title), getString(R.string.error_conection_internet_message));
                        }
                    } else {
                        Toast toast = Toast.makeText(getContext(), getString(R.string.error_privacy_policy_not_accepted), Toast.LENGTH_LONG);
                        toast.getView().setBackgroundColor(getResources().getColor(R.color.colorAccent));
                        ((TextView) toast.getView().findViewById(android.R.id.message)).setTextColor(Color.WHITE);
                        toast.show();
                    }
                } else {
                    Toast toast = Toast.makeText(getContext(), getString(R.string.error_requiered_field_without_fill), Toast.LENGTH_LONG);
                    toast.getView().setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    ((TextView) toast.getView().findViewById(android.R.id.message)).setTextColor(Color.WHITE);
                    toast.show();
                }
            }
        });

    }


    /**
     * Method initializes the UI
     */
    private void initUI() {
        // Set up required fields
        layoutFullName.setHint(layoutFullName.getHint() + " " + getString(R.string.asterisk));
        layoutPhone.setHint(layoutPhone.getHint() + " " + getString(R.string.asterisk));
        btSendSighting.setEnabled(true);
    }


    /**
     * Method verifies that the required fields are not empty
     */
    private boolean verifiedFields() {
        if (!etFullName.getText().toString().isEmpty() && !etPhone.getText().toString().isEmpty())
            return true;
        else
            return false;

    }


    /**
     * Method uploads all the images of the specified sighting to Cloud Storage and expects all
     * of them to be unloaded.
     *
     * @param sighting - the Sighting object containing all information
     * @param code     - code for that sighting and that will be used as the name of the storage
     *                 folder for your images
     */
    private void uploadAllImagesSighting(Sighting sighting, final String code) {
        List<Task<Uri>> taskArrayList = new ArrayList<>();
        try {

            // I create a new task for uploading each photo
            for (String uriString : sighting.getPhoto()) {
                taskArrayList.add(uploadImageTask(Uri.parse(uriString), code));
            }
        } catch (Exception e) {
            hideProgressDialog();
            showMessageDialog(getString(R.string.error_uploading_photos_title) + ":" + e.getLocalizedMessage(), getString(R.string.error_uploading_photos_message));

        }
        //whenAllSuccess method is executed when all tasks in taskArrayList have finished successfully
        Tasks.whenAllSuccess(taskArrayList).addOnCompleteListener(new OnCompleteListener<List<Object>>() {
            @Override
            public void onComplete(@NonNull Task<List<Object>> task) {
                downloadUri = new ArrayList<>();
                //task will contain a List with the download uris of my photos
                for (Object o : task.getResult()) {
                    downloadUri.add(((Uri) o).toString());
                }

                // here, currentSighting.getPhoto() contains the local uris of the photos (taken or chosen from the gallery).
                // I need those uris to later send them by whatsapp
                urisImagesToSendWhatsapp = currentSighting.getPhoto();
                // update sighting with download uris from firebase
                currentSighting.setPhoto(downloadUri);

                // upload the currentsighting to Firebase Firestore
                sightings.add(currentSighting, code, new SightingAsync.OnCompletedAddSightingListener() {
                    @Override
                    public void onSuccessfulAddSighting(String sightingId) {
                        // I must add in the collection called "numberNotificationsYearMunicipality" the number of notification that is
                        // that notification for that year and that municipality
                        String year = code.substring(1, 5);
                        numberNotificationsYearMunicipality.addNotification(year, currentSighting.getMunicipality(),
                                newNumberNotificationsForThatMunicipality,
                                new NumberNotificationsYearMunicipalityAsync.OnCompletedAddNotificationListener() {
                                    @Override
                                    public void onSuccessfulAddNotification(long numberSightings) {
                                        sendByWhatsapp(currentSighting, urisImagesToSendWhatsapp, code);
                                        //END OF THE SIGHTING NOTICE PROCESS
                                    }

                                    @Override
                                    public void onFailureAddNotification(Exception e) {
                                        hideProgressDialog();
                                        showMessageDialog(getString(R.string.error_getting_number_notifications_title) + ":" + e.getLocalizedMessage(), getString(R.string.error_getting_number_notifications_message));

                                    }
                                });
                    }

                    @Override
                    public void onFailureAddSighting(Exception e) {
                        hideProgressDialog();
                        showMessageDialog(getString(R.string.error_adding_sighting_title) + ":" + e.getLocalizedMessage(), getString(R.string.error_adding_sighting_message));

                    }
                });

            }
        });


    }


    /**
     * Method uploads an image represented by the specified uri and return its corresponding download uri
     * It is made using two tasks: firstly, uploadTask makes the uploading, and then it is continued with
     * another task that makes the process of getting the urls of download for each image uploaded.
     *
     * @param fileUri      - uri of image you want upload
     * @param sightingCode - code for that sighting and that will be used as the name of the storage
     *                     folder for your images
     * @return uri necessary to download that image later
     */
    private Task<Uri> uploadImageTask(Uri fileUri, String sightingCode) {
        String fileName = getFileNameFromUri(fileUri);
        final StorageReference imageRef = mStorageRef.child(Constants.IMAGES_FOLDER_NAME).child(sightingCode).child(fileName);
        UploadTask uploadTask = imageRef.putFile(fileUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressDialog();
                showMessageDialog(getString(R.string.error_uploading_photos_title), getString(R.string.error_uploading_photos_message));

            }
        });


        return uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return imageRef.getDownloadUrl();
            }
        });
    }


    /**
     * Method shows a dialog with the specified title and message
     *
     * @param title   - title of the dialog
     * @param message - text of the dialog
     */
    private void showMessageDialog(String title, String message) {
        AlertDialog ad = new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .create();
        ad.show();
    }


    /**
     * Method shows a dialog with the specified title and message and a positive button
     *
     * @param title   - title of the dialog
     * @param message - text of the dialog
     */
    private void showMessageDialogNotWhatsapp(String title, String message, final Sighting sighting, List<String>urisImagesToSendEmail, final String sightingCode) {
        AlertDialog ad = new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // sending by email
                        sendSightingByEmail(sighting, urisImagesToSendWhatsapp, sightingCode);
                        dialogInterface.dismiss();
                    }
                })
                .create();
        ad.show();
    }


    /**
     * Method show a progress dialog to notify to the user that is working
     *
     * @param caption title for the dialog
     */
    private void showProgressDialog(String caption) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.setMessage(caption);
        mProgressDialog.show();
    }

    /**
     * Method hide the progress dialog
     */
    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }


    /**
     * Method gets the name of file specified by its uri
     *
     * @param fileUri - uri of file which name you want
     * @return the name of that file
     */
    private String getFileNameFromUri(Uri fileUri) {
        String path;
        Cursor cursor = getContext().getContentResolver().query(fileUri, null, null, null, null);
        if (cursor == null) {//source is a classic file path, no content:// scheme
            path = fileUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            if (index != -1) {
                path = cursor.getString(index);
            } else
                path = fileUri.getPath();
            cursor.close();

        }
        String filename = path.substring(path.lastIndexOf("/") + 1);
        String fileWOExtension; //filename without extension
        if (filename.indexOf(".") > 0) {
            //has extension
            fileWOExtension = filename.substring(0, filename.lastIndexOf("."));
        } else {
            fileWOExtension = filename;
        }
        return fileWOExtension;
    }


    /**
     * Method gets the real path of file specified by its uri
     *
     * @param fileUri - uri of file which path you want
     * @return the path of that file
     */
    private String getRealPathFromUri(Uri fileUri) {
        String path;
        Cursor cursor = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        try {
            cursor = getContext().getContentResolver().query(fileUri, projection, null, null, null);
            if (cursor == null) {//source is a classic file path, no content:// scheme
                path = fileUri.getPath();
            } else {
                cursor.moveToFirst();
                int index = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
                if (index != -1) {
                    path = cursor.getString(index);
                } else
                    path = fileUri.getPath();


            }
            return path;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    /**
     * Method sends by Whatsapp all information about the sighting
     * The number used is the "avisap" number: 610 255 111
     *
     * @param sighting                 Sighting object with all information
     * @param urisImagesToSendWhatsapp uris of the images of the sighting
     * @param sightingCode             code corresponding to that sighting in the database
     */
    private void sendByWhatsapp(Sighting sighting, List<String> urisImagesToSendWhatsapp, String sightingCode) {
        hideProgressDialog();// dialog of uploading photos
        sightingToWhatsapp = sighting;
        codeToWhatsapp = sightingCode;
        photosToWhatsapp = urisImagesToSendWhatsapp;

        //check if I have permission to read contacts. If I haven't got I request it.
        if (!hasDeviceContactsPermission(getContext(), Manifest.permission.READ_CONTACTS)) {
            requestPermissionFragment(Manifest.permission.READ_CONTACTS, getString(R.string.justificationPermissionReadContact),
                    REQUEST_PERMISSION_READ_CONTACT, this);
        } else {
            //check if there is already the official contact for whatsapp in the user's contact list
            if (!Contacts.contactExists(getContext(), getString(R.string.whatsapp_number_official))) {
                createNewContact();
            } else {
                boolean isWhatsappInstalled = Utils.isAppInstalled(getContext(), getString(R.string.whatsapp_package_name));
                if (!isWhatsappInstalled) {
                    showMessageDialogNotWhatsapp(getString(R.string.title_dialog_not_installed_whatsapp),
                            getString(R.string.message_dialog_not_installed_whatsapp),
                            sighting,urisImagesToSendWhatsapp,sightingCode);

                    //deactivate send sighting button to avoid errors
                    // btSendSighting.setEnabled(false);

                } else {
                    showHelpDialogContinueSending(sighting, urisImagesToSendWhatsapp, sightingCode);
                }


            }
        }


        //Toast.makeText(getContext(), "Enviando por whatsapp", Toast.LENGTH_SHORT).show();

    }


    /**
     * Method  creates a new contact for the official whatsapp number in the user's contact list
     */
    private void createNewContact() {
        //check if I have permission to write contacts. If I haven't got I request it.
        if (!hasDeviceContactsPermission(getContext(), Manifest.permission.WRITE_CONTACTS)) {
            requestPermissionFragment(Manifest.permission.WRITE_CONTACTS, getString(R.string.justificationPermissionWriteContact),
                    REQUEST_PERMISSION_WRITE_CONTACT, this);
        } else {
            showNewCreationContactDialog();

        }
    }


    /**
     * Method shows a dialog to inform the user that a new contact will be created
     * with the name "AlarmAvispa" and the oficial phone number used as whatsapp (610255111).
     * This dialog expect a response from the user accepting or rejecting.
     */
    private void showNewCreationContactDialog() {
        AlertDialog ad = new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.title_dialog_new_creation_contact))
                .setMessage(getString(R.string.message_dialog_new_creation_contact))
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        insertNewContactAutomatically(getString(R.string.app_name), getString(R.string.whatsapp_number_official));
                        showMessageDialogContactCreated(getString(R.string.title_dialog_created_contact),
                                getString(R.string.message_dialog_created_contact) + getString(R.string.accept));


                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getContext(), getString(R.string.error_rejecting_creation_new_contact), Toast.LENGTH_LONG).show();
                        dialogInterface.dismiss();
                    }
                })
                .create();
        ad.show();
    }


    /**
     * Method shows a dialog to inform the user how to continue with the process of sending.
     * If the user clicks positive button the message will be send by whatsapp; otherwise no.
     *
     * @param sighting                 Sighting object with all information to send
     * @param urisImagesToSendWhatsapp List<String> containing the images of sighting
     * @param sightingCode             the code that identifies this sighting in the database
     */
    private void showHelpDialogContinueSending(final Sighting sighting, final List<String> urisImagesToSendWhatsapp, final String sightingCode) {
        AlertDialog ad = new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.title_dialog_help_continue_sending))
                .setIcon(R.drawable.small_logo)
                .setCancelable(false)

                .setMessage(getString(R.string.message_dialog_help_continue_sending))
                .setPositiveButton(R.string.send_by_whatsapp, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendSightingToWhatsapp(sighting, urisImagesToSendWhatsapp, sightingCode);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getContext(), getString(R.string.error_rejecting_sending_whatsapp), Toast.LENGTH_LONG).show();
                        dialogInterface.dismiss();
                    }
                })
                .create();

        ad.show();


    }


    /**
     * Method inserts  a new Contact with the specified name and number,
     * without showing the add contacts screen of the device.
     * Made using ContentResolver
     */
    private void insertNewContactAutomatically(String name, String phoneNumber) {
        // Get android phone contact content provider uri
        Uri addContactsUri = ContactsContract.Data.CONTENT_URI;

        // Add an empty contact in order to get the generated id for that contact (it's necessary
        // to have an id)
        long contactId = insertEmptyContact();

        // Add contact name
        addContactName(addContactsUri, contactId, name);

        // Add contact phone
        addContactPhone(addContactsUri, contactId, phoneNumber);

    }


    /**
     * Method add an empty contact and return its id
     *
     * @return
     */
    private long insertEmptyContact() {
        ContentValues contentValues = new ContentValues();
        Uri contactUri = getContext().getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, contentValues);
        // Get the id of the created contact
        return ContentUris.parseId(contactUri);
    }


    /**
     * Method add the name in the created contact with el specified id
     *
     * @param addContactsUri Contacts uri
     * @param contactId      id of the newly created contact
     * @param name           contact's name
     */
    private void addContactName(Uri addContactsUri, long contactId, String name) {
        ContentValues contentValues = new ContentValues();

        // Each contact must has an id to avoid java.lang.IllegalArgumentException: raw_contact_id is required error.
        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, contactId);

        //Each contact must have a mime type to avoid java.lang.IllegalArgumentException: mimetype is required error
        contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);

        // Put the name
        contentValues.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name);

        getContext().getContentResolver().insert(addContactsUri, contentValues);
    }


    /**
     * Method add the contact phone in the created contact with el specified id
     *
     * @param addContactsUri Contacts uri
     * @param contactId      id of the newly created contact
     * @param phone          contact's phone
     */
    private void addContactPhone(Uri addContactsUri, long contactId, String phone) {
        ContentValues contentValues = new ContentValues();

        // Each contact must has an id to avoid java.lang.IllegalArgumentException: raw_contact_id is required error.
        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, contactId);

        //Each contact must have a mime type to avoid java.lang.IllegalArgumentException: mimetype is required error
        contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);

        // Put the phone number
        contentValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone);
        contentValues.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);

        getContext().getContentResolver().insert(addContactsUri, contentValues);
    }


    /**
     * Method shows a dialog with the specified title and message and a positive button, when contact
     * is created
     *
     * @param title   - title of the dialog
     * @param message - text of the dialog
     */
    private void showMessageDialogContactCreated(String title, String message) {
        AlertDialog ad = new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        sendByWhatsapp(sightingToWhatsapp, photosToWhatsapp, codeToWhatsapp);
                    }
                })
                .create();
        ad.show();

    }


    /**
     * Method formats the information to adapt it to the whatsapp and finally sends it to
     * the number registered as official in the resources (strings.xml)
     * Whatsapp is sent by a shareIntent. It's necessary the contact exists previously.
     *
     * @param sighting                 the Sighting object that contains all information about this sighting
     * @param urisImagesToSendWhatsapp a List<String> that contains the uris of the photos as strings
     * @param sightingCode             the code corresponding to that sighting in the database
     */
    private void sendSightingToWhatsapp(Sighting sighting, List<String> urisImagesToSendWhatsapp, String sightingCode) {
        if (!hasDeviceStoragePermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissionFragment(Manifest.permission.WRITE_EXTERNAL_STORAGE, getString(R.string.justificationPermissionWriteExternaStorage),
                    REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE, this);
        } else {
            ArrayList<Uri> infoToSend; // object will be sent by whatsapp
            // add images
            infoToSend = addPhotosFromSighting(urisImagesToSendWhatsapp);
            // add the other sighting information creating a text file for that
            Uri mUri = getUriSightingInformationFile(currentSighting, sightingCode);
            if (mUri != null)
                infoToSend.add(mUri);
            getActivity().finish(); // go back to the main activity
            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.setType("*/*");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, infoToSend);
            intent.setPackage("com.whatsapp");
            startActivity(Intent.createChooser(intent, getString(R.string.share_by_whatsapp)));


        }


    }


    /**
     * Method formats the information to adapt it to be sent by email and finally sends it to
     * the email registered as official in the Constants class
     *
     * @param sighting                 the Sighting object that contains all information about this sighting
     * @param urisImagesToSendWhatsapp a List<String> that contains the uris of the photos as strings
     * @param sightingCode             the code corresponding to that sighting in the database
     */
    private void sendSightingByEmail(Sighting sighting, List<String> urisImagesToSendWhatsapp, String sightingCode) {
        if (!hasDeviceStoragePermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissionFragment(Manifest.permission.WRITE_EXTERNAL_STORAGE, getString(R.string.justificationPermissionWriteExternaStorage),
                    REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE, this);
        } else {
            ArrayList<Uri> infoToSend; // object will be sent by email
            // add images
            infoToSend = addPhotosFromSighting(urisImagesToSendWhatsapp);
            // add the other sighting information creating a text file for that
            Uri mUri = getUriSightingInformationFile(currentSighting, sightingCode);
            if (mUri != null)
                infoToSend.add(mUri);
            getActivity().finish(); // go back to the main activity
            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{Constants.OFFICIAL_EMAIL});
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.sighting_subject_email) + " (" + sightingCode + ")");
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.sighting_text_email));
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, infoToSend);
            startActivity(Intent.createChooser(intent, getString(R.string.share_by_email)));


        }


    }


    /**
     * Method gets a list of Uris from a list of String.
     * The strings represents the uris of the photos of the sighting
     *
     * @param urisImagesToSendWhatsapp the list of strings corresponding to the photos of the sighting
     * @return an ArrayList of Uri
     */
    private ArrayList<Uri> addPhotosFromSighting(List<String> urisImagesToSendWhatsapp) {
        ArrayList<Uri> uriList = new ArrayList<>();
        for (String uriStringImage :
                urisImagesToSendWhatsapp) {
            uriList.add(Uri.parse(uriStringImage));
        }
        return uriList;
    }


    /**
     * Method creates a text file with information of the sighting and return its uri
     *
     * @param sighting     Sighting object with all information
     * @param sightingCode code for that sighting in the database
     * @return Uri of text file that will be sent by whatsapp
     */
    private Uri getUriSightingInformationFile(Sighting sighting, String sightingCode) {


        try {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            String fileName = sightingCode + "_" + etFullName.getText().toString()
                    + getString(R.string.file_extension);
            File file = new File(path, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            Uri uri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".fileprovider", file);

            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true)); //true for append
            bw.write(getString(R.string.title_text_send_by_whatsapp));
            bw.newLine();
            bw.newLine();
            bw.write(getString(R.string.labelWhatYouSeen));
            bw.newLine();
            bw.write(sighting.getSightingType());
            bw.newLine();
            if (sighting.getSightingType() == SightingType.HORNET.getLocalizedName(getContext()))
                bw.write(getString(R.string.labelPlaceSeenWasp) + ": ");
            else
                bw.write(getString(R.string.labelPlaceSeenNest) + ": ");
            bw.newLine();
            bw.write(sighting.getPlace());
            bw.newLine();
            bw.write(getString(R.string.labelWhereAreYou));
            bw.newLine();
            bw.write(getString(R.string.latitude) + ": " + sighting.getLatitude());
            bw.newLine();
            bw.write(getString(R.string.longitude) + ": " + sighting.getLongitude());
            bw.newLine();
            bw.write(getString(R.string.locality) + ": " + sighting.getLocality());
            bw.newLine();
            bw.write(getString(R.string.municipality) + ": " + sighting.getMunicipality());
            bw.newLine();
            bw.write(getString(R.string.commentsLocation) + ": ");
            bw.newLine();
            bw.write(sighting.getComment());
            bw.newLine();
            bw.write(getString(R.string.labelHowIsTheNest));
            bw.newLine();
            bw.write(getString(R.string.labelNestType) + ": " + sighting.getNestType());
            bw.newLine();
            bw.write(getString(R.string.labelNestStand) + ": " + sighting.getNestStand());
            bw.newLine();
            bw.write(getString(R.string.nestHeight) + ": " + sighting.getHeight());
            bw.newLine();
            bw.write(getString(R.string.labelWaspAround) + ": " +
                    ((sighting.isWaspAround()) ? getString(R.string.yes) : getString(R.string.no)));
            bw.newLine();
            bw.write(getString(R.string.labelWhoAreYou));
            bw.newLine();
            bw.write(getString(R.string.fullName) + ": " + etFullName.getText().toString());
            bw.newLine();
            bw.write(getString(R.string.hint_phone_profile) + ": " + etPhone.getText().toString());
            bw.newLine();
            // get date from milliseconds
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            Date date = new Date(sighting.getSightingDate());
            bw.write(getString(R.string.sighting_date) + ": " + df.format(date));
            bw.newLine();
            bw.close();
            return uri;

        } catch (IOException e) {
            Toast.makeText(getContext(), "error fichero", Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    /**
     * Method shows an alert dialog that load a Privacy Policy Web inside using a webview
     */
    private void showWebViewAlertDialogPrivacy() {


        AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
        ad.setTitle(getString(R.string.privacy_policy));

        View alertDialogLayout = getLayoutInflater().inflate(R.layout.alertdialog_with_webview_layout, null);
        WebView wv = alertDialogLayout.findViewById(R.id.wvPrivacy);
        wv.getSettings().setBuiltInZoomControls(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            wv.setWebContentsDebuggingEnabled(true);
        wv.loadUrl(Constants.URL_PRIVACY_POLICY_WEB);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // open all links in my webview and avoid going out
                String url_filter = Constants.URL_PRIVACY_POLICY_WEB;
                if (!url.equals(url_filter))
                    view.loadUrl(url_filter);

                return true;
            }

            //show alertDialog is loading...
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (Internet.isInternetConectionActive(getContext())) {
                    mProgressDialog = new ProgressDialog(getContext());
                    mProgressDialog.setMessage(getString(R.string.message_loading));
                    mProgressDialog.setCancelable(true);
                    mProgressDialog.show();
                }
            }


            //hide alertdialog shown loading...
            @Override
            public void onPageFinished(WebView view, String url) {
                if (mProgressDialog != null)
                    mProgressDialog.dismiss();
            }


            // catching errors
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext())
                        .setTitle(getString(R.string.error_loading_privacy_web_title))
                        .setMessage(getString(R.string.error_loading_privacy_web_message) + ": " + description)
                        .setPositiveButton(getString(R.string.accept), null);
                builder.show();
            }
        });

        ad.setView(wv);
        ad.setCancelable(false);
        ad.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        ad.create().show();
    }
    //endregion

}
