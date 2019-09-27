package es.alejandrtf.alarmavispa.fragments;


import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.activities.AddSightingActivityArgs;
import es.alejandrtf.alarmavispa.adapters.PhotoAdapter;
import es.alejandrtf.alarmavispa.models.Sighting;
import es.alejandrtf.alarmavispa.models.SightingType;
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
public class AddPhotosFragment extends Fragment {
    private final static String TAG = "AddPhotosFragment";

    private FloatingActionButton fabCamera, fabGallery;
    private MaterialButton btNext;
    private RadioGroup rgSightingType;
    private TextInputEditText etPlace;
    private TextView tvLabelPlaceSeen;
    private RecyclerView rvPhotoGrid;
    private List<String> photoUriList = new ArrayList<String>(); // store the uri of each photo in string format
    private PhotoAdapter photoAdapter;
    private int column_numbers = Constants.COLUMNS_NUMBER_VERTICAL_ADD_PHOTOS_RECYCLERVIEW;
    private Uri fileUri;
    //serialization to pass Sighting object to another fragment
    private Gson gson;


    public AddPhotosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gson = new Gson();


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setActionsToButtons();


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_photos, container, false);
        setReferencesViewsToXML(v);
        return v;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //the first time, the Activity will contain la uri of the image file. I must to collect it
        //Use saveArgs to collect it
        String fileUriString = AddSightingActivityArgs.fromBundle(getActivity().getIntent().getExtras()).getPhotoFileUri();

        if (!fileUriString.isEmpty() && photoUriList.isEmpty()) {

            photoUriList.add(fileUriString);


        }

        //check if activity is dead. For example when device is rotate
        if (savedInstanceState != null) {
            photoUriList = (ArrayList<String>) savedInstanceState.get("photos");
        }

        //set up recyclerview
        photoAdapter = new PhotoAdapter(getContext(), photoUriList);
        rvPhotoGrid.setAdapter(photoAdapter);
        //check phone orientation in order to adapt the number of recycler grid columns
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

            column_numbers = Constants.COLUMNS_NUMBER_HORIZONTAL_ADD_PHOTOS_RECYCLERVIEW;
        }
        rvPhotoGrid.setLayoutManager(new GridLayoutManager(getContext(), column_numbers));
    }


    /**
     * Method is executed when returning from taking the photo
     *
     * @param requestCode request code to which it's answering
     * @param resultCode  operation result
     * @param data        data returns by the camera
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //add photo to photolist
            if (fileUri != null) {
                photoUriList.add(fileUri.toString());
            }
            //notify the changes to the adapter
            rvPhotoGrid.getAdapter().notifyDataSetChanged();


        } else if (requestCode == Constants.REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {
            //add photo to photolist
            if (data != null) {
                Uri imagenSeleccionadaUri = data.getData();
                photoUriList.add(imagenSeleccionadaUri.toString());
                //notify the changes to the adapter
                rvPhotoGrid.getAdapter().notifyDataSetChanged();
            }

        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("photos", (ArrayList) photoUriList);
    }

    @Override
    public void onResume() {
        super.onResume();

        //reset the preference about editing locality and municipality
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.PREFERENCES_KEY_EDITING_LOCALITY, false);
        editor.putBoolean(Constants.PREFERENCES_KEY_EDITING_MUNICIPALITY, false);
        editor.commit();
    }


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
    }

//region CUSTOM METHODS

    /**
     * Gets  XML references for JAVA View objects
     */
    private void setReferencesViewsToXML(View v) {
        fabCamera = v.findViewById(R.id.fab_camera_add_photo_fragment);
        fabGallery = v.findViewById(R.id.fab_gallery_add_photo_fragment);
        rvPhotoGrid = v.findViewById(R.id.rvPhotoGrid);
        btNext = v.findViewById(R.id.btNextAddPhotosFragment);
        rgSightingType = v.findViewById(R.id.rgWhatYouSeen);
        etPlace = v.findViewById(R.id.etPlaceAddPhotos);
        tvLabelPlaceSeen = v.findViewById(R.id.tvLabelPlaceSeen);
    }


    /**
     * Set up to each button its actions
     */
    public void setActionsToButtons() {
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhotoButtonAction();
            }
        });


        fabGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (photoUriList.size() < Constants.MAX_NUMBER_OF_PHOTOS_OF_SIGHTING)
                    openDeviceGallery();
                else {
                    Toast.makeText(getContext(), getContext().getString(R.string.error_limit_allowed_photos), Toast.LENGTH_LONG).show();

                }
            }
        });


        rgSightingType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int idSelected) {
                if (idSelected == R.id.rbNest)
                    tvLabelPlaceSeen.setText(getString(R.string.labelPlaceSeenNest));
                else {
                    tvLabelPlaceSeen.setText(getString(R.string.labelPlaceSeenWasp));
                }

            }
        });


        btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check which radiobutton is checked
                int idChecked = rgSightingType.getCheckedRadioButtonId();
                String sightingType;
                if (idChecked == R.id.rbWasp) {
                    sightingType = SightingType.HORNET.getLocalizedName(getContext());
                } else {
                    sightingType = SightingType.NEST.getLocalizedName(getContext());
                }
                //creates a new sighting with the information of this fragment
                Sighting sighting = new Sighting(photoUriList, sightingType, etPlace.getText().toString());
                //serialize the Sighting object
                String sigtingObjectString = gson.toJson(sighting);
                //navigate to the AddLocationFragment passing the current info about the sighting
                AddPhotosFragmentDirections.ActionAddPhotosFragmentToAddLocationFragment action = AddPhotosFragmentDirections.actionAddPhotosFragmentToAddLocationFragment(sigtingObjectString);
                Navigation.findNavController(view).navigate(action);

            }
        });


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
    private void openDeviceGallery() {
        Intent iOpenGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(iOpenGallery, Constants.REQUEST_IMAGE_GALLERY);
    }


    /**
     * Method is executed when button CAMERA is pressed.
     * Checks permissions to take photo and takes it if all permission is granted
     */
    private void takePhotoButtonAction() {
        if (photoUriList.size() < Constants.MAX_NUMBER_OF_PHOTOS_OF_SIGHTING) {
            //check if I have permission to use the camera. If I haven't got I request it.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    if (checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        takePhoto();
                    } else {
                        //I request it
                        requestPermissionFragment(Manifest.permission.WRITE_EXTERNAL_STORAGE, getString(R.string.justificationPermissionWriteExternaStorage),
                                REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE, AddPhotosFragment.this);
                    }
                } else {
                    //I request it
                    requestPermissionFragment(Manifest.permission.CAMERA, getString(R.string.justificationPermissionCamera),
                            REQUEST_PERMISSION_CAMERA, AddPhotosFragment.this);
                }
            } else {
                takePhoto();
            }

        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.error_limit_allowed_photos), Toast.LENGTH_LONG).show();
        }
    }

//endregion
}
