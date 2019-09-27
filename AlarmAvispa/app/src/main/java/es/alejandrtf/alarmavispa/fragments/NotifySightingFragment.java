package es.alejandrtf.alarmavispa.fragments;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;

import es.alejandrtf.alarmavispa.NavGraphDirections;
import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.utilities.Constants;
import es.alejandrtf.alarmavispa.utilities.Images;

import static android.app.Activity.RESULT_OK;
import static androidx.core.content.ContextCompat.checkSelfPermission;
import static es.alejandrtf.alarmavispa.utilities.Permissions.REQUEST_PERMISSION_CAMERA;
import static es.alejandrtf.alarmavispa.utilities.Permissions.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE;
import static es.alejandrtf.alarmavispa.utilities.Permissions.requestPermissionFragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotifySightingFragment extends Fragment {
    private static final String TAG = "NotifySightingFragment";
    private ImageButton ibPhoto, ibGallery;
    //variables to take photo
    private Uri fileUri;

    NavController navController;


    public NotifySightingFragment() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_notify_sighting, container, false);
        setReferencesViewsToXML(v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);//region CUSTOM METHODS
        navController = Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment);
        setActionsToButtons();

    }

    @Override
    public void onResume() {
        super.onResume();
        // check if I must go back to MapSightingFragment because I arrived here from that fragment
        // For example, after login or registration process.
        boolean goMapSightingFragment = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(Constants.PREFERENCES_KEY_GO_MAPSIGHTINGFRAGMENT, false);
        if (goMapSightingFragment) {
            if (navController != null) {
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().remove(Constants.PREFERENCES_KEY_GO_MAPSIGHTINGFRAGMENT).commit();
                navController.navigate(R.id.action_notifySightingFragment_to_mapSightingFragment);
            }
        }else{
            boolean goTrapFragment=PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(Constants.PREFERENCES_KEY_GO_TRAPFRAGMENT, false);
            if(goTrapFragment) {
                if (navController != null) {

                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().remove(Constants.PREFERENCES_KEY_GO_TRAPFRAGMENT).commit();
                    navController.navigate(R.id.action_notifySightingFragment_to_trapFragment);
                }
            }
        }
    }


    //region ACTIVITY'S METHODS

    ///////////////////////////////////////////////////////////////////////////
    ///////////  METHOD COLLECTS THE RESULT OF REQUESTING PERMISSIONS /////////
    ///////////////////////////////////////////////////////////////////////////


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CAMERA:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permisos concedidos");
                    takePhotoButtonAction();

                } else {
                    Toast.makeText(getContext(), getString(R.string.justificationPermissionCamera), Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Permisos denegados");

                }
                break;
            case REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permisos concedidos");
                    takePhotoButtonAction();

                } else {
                    Toast.makeText(getContext(), getString((R.string.justificationPermissionWriteExternaStorage)), Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Permisos denegados");

                }
                break;


        }
        return;
    }


    /**
     * Method is executed when returning from taking the photo
     *
     * @param requestCode request code to which it's answering
     * @param resultCode  operation result
     * @param data        data returns by the camera
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            //navigate to the AddSightingActivity (to the fragment: AddPhotosFragment) passing as argument the file uri
            //this navigation is using "safeArgs". See https://developer.android.com/guide/navigation/navigation-pass-data#Safe-args
            NavDirections actionGlobalToAddPhotosActivityDest = NavGraphDirections.actionGlobalToAddPhotosActivityDest(fileUri.toString());
            navController.navigate(actionGlobalToAddPhotosActivityDest);

        } else if (requestCode == Constants.REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {
            if (data != null) {
                Uri imagenSeleccionadaUri = data.getData();
                //navigate to the AddSightingActivity (to the fragment: AddPhotosFragment) passing as argument the uri of the chosen file
                //this navigation is using "safeArgs". See https://developer.android.com/guide/navigation/navigation-pass-data#Safe-args
                NavDirections actionGlobalToAddPhotosActivityDest = NavGraphDirections.actionGlobalToAddPhotosActivityDest(imagenSeleccionadaUri.toString());
                navController.navigate(actionGlobalToAddPhotosActivityDest);
            }
        }
    }
    //endregion


    //region CUSTOM METHODS

    /**
     * Gets  XML references for JAVA View objects
     */
    private void setReferencesViewsToXML(View v) {
        ibGallery = v.findViewById(R.id.ibGallery);
        ibPhoto = v.findViewById(R.id.ibPhoto);
    }

    /**
     * Set up to each button its actions
     */
    public void setActionsToButtons() {
        ibPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhotoButtonAction();
            }
        });

        ibGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDeviceGalleryAction();
            }
        });

    }

    /**
     * Method is executed when button CAMERA is pressed.
     * Checks permissions to take photo and takes it if all permission is granted
     */
    private void takePhotoButtonAction() {
        //check if I have permission to use the camera. If I haven't got I request it.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                if (checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    takePhoto();
                } else {
                    //I request it
                    requestPermissionFragment(Manifest.permission.WRITE_EXTERNAL_STORAGE, getString(R.string.justificationPermissionWriteExternaStorage),
                            REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE, this);
                }
            } else {
                //I request it
                requestPermissionFragment(Manifest.permission.CAMERA, getString(R.string.justificationPermissionCamera),
                        REQUEST_PERMISSION_CAMERA, this);
            }
        } else {
            takePhoto();
        }
    }


    /**
     * Calls camera app to take the photo and and passes the file where it should be stored
     */
    private void takePhoto() {
        //create the Intent object that  launches the device's camera
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //check that there is a camera app in the device
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            // Get the uri of the file where I will save the photo
            File photoFile = Images.createOutputPictureFile(getContext());
            if (photoFile != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // I have to use FileProvider (file://URI get FileUriExposedException)
                    fileUri = FileProvider.getUriForFile(getContext(),
                            "es.alejandrtf.alarmavispa.fileprovider",
                            photoFile);
                } else {
                    fileUri = Uri.fromFile(photoFile);
                }
                //add that uri of file to the Intent object
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                //launches the intent in order to open de camera app
                startActivityForResult(takePictureIntent, Constants.REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    /**
     * Method show the gallery of the device to choose an image
     */
    private void openDeviceGalleryAction() {
        Intent iOpenGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(iOpenGallery, Constants.REQUEST_IMAGE_GALLERY);
    }


    //endregion
}

