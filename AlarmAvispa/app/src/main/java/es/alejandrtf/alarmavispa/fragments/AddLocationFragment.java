package es.alejandrtf.alarmavispa.fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.location.Location;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.util.List;

import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.models.Sighting;
import es.alejandrtf.alarmavispa.models.SightingType;
import es.alejandrtf.alarmavispa.services.FetchAddressIntentService;
import es.alejandrtf.alarmavispa.utilities.Constants;


import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.provider.SettingsSlicesContract.KEY_LOCATION;
import static androidx.core.content.ContextCompat.checkSelfPermission;
import static es.alejandrtf.alarmavispa.utilities.Constants.KEY_REQUESTING_LOCATION_UPDATES;
import static es.alejandrtf.alarmavispa.utilities.Constants.REQUEST_CHECK_SETTINGS;
import static es.alejandrtf.alarmavispa.utilities.GoogleMaps.getIconToAddInMap;
import static es.alejandrtf.alarmavispa.utilities.Permissions.REQUEST_PERMISSION_LOCATION;
import static es.alejandrtf.alarmavispa.utilities.Permissions.requestPermissionFragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddLocationFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "AddLocationFragment";

    // Layout variables
    private TextInputEditText etLatitude, etLongitude, etLocality, etMunicipality, etComments;
    private MaterialButton btNext;
    private TextInputLayout layoutLatitude, layoutLongitude, layoutMunicipality;
    private ImageButton ibEditLocality, ibEditMunicipality;

    private Sighting currentSighting;
    private Gson gson;
    //FusedLocationProviderClient and Google Maps variables
    private Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 20 secs */
    private long FASTEST_INTERVAL = 5 * 1000; /* 10 sec */
    private Marker mCurrLocationMarker;
    private GoogleMap mMap;
    private boolean mRequestingLocationUpdates;
    private LocationSettingsRequest mLocationSettingsRequest; //to check if the user has activated the location or not on his device
    private LocationSettingsRequest.Builder builder;
    // Used to get locality from latitude, longitude
    private LocalityResultReceiver resultReceiver;
    protected String localityReceived;


    //callback that collects a new location in each update
    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (mMap != null) {
                mRequestingLocationUpdates = true;
                List<Location> locationList = locationResult.getLocations();
                if (locationList.size() > 0) {
                    //The last location in the list is the newest
                    Location location = locationList.get(locationList.size() - 1);
                    Log.i(TAG, "Location: " + location.getLatitude() + "-" + location.getLongitude());
                    if (mLastLocation != null) {
                        if (!areEqual(mLastLocation, location)) {
                            mLastLocation = location;
                        }
                    } else {
                        mLastLocation = location;
                    }
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (mCurrLocationMarker != null && !areEqual(mLastLocation, location)) {
                        mCurrLocationMarker.remove();
                        //Place current location marker
                        mCurrLocationMarker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .icon(getIconToAddInMap(getContext(), currentSighting)));

                    } else if (mCurrLocationMarker == null) {
                        //Place current location marker
                        mCurrLocationMarker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .icon(getIconToAddInMap(getContext(), currentSighting)));

                    }

                    displayCoordinates(latLng);
                    // Request for getting address
                    startGeocodeIntentService();
                    //move map camera
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));

                }
            }
        }

    };


    public AddLocationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gson = new Gson();
        //location
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // Used to get locality by the Geocoder
        resultReceiver = new LocalityResultReceiver(new Handler());

        //check if I have permission to use location. If I haven't got I request it.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //location permission is granted
                //I check if the user has activated location in his settings
                buildLocationSettingsRequest();
                checkLocationSettings();
            } else {
                //I request it
                requestPermissionFragment(Manifest.permission.ACCESS_FINE_LOCATION, getString(R.string.justificationPermissionLocation),
                        REQUEST_PERMISSION_LOCATION, this);
            }
        } else {
            //location permission is granted
            //I check if the user has activated location in his settings
            buildLocationSettingsRequest();
            checkLocationSettings();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_location, container, false);
        setReferencesViewsToXML(v);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(getContext(), getString(R.string.error_loading_google_map), Toast.LENGTH_SHORT).show();
        }

        initRequiredFieldsUI();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String previousScreenInformationSighting = AddLocationFragmentArgs.fromBundle(getArguments()).getPreviousInformationSighting();
        //deserialize. This object contains the information of previous screen
        currentSighting = gson.fromJson(previousScreenInformationSighting, Sighting.class);
        setActionsToButtons();

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRequestingLocationUpdates = false;
        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mRequestingLocationUpdates)
            startLocationUpdates();


    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when fragment is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            mRequestingLocationUpdates = false;
        }
    }


    /* Get results from user dialog prompt to turn on location services for app
        Used when check if the user has activated or not location settings
        This method will be call when user accept or cancell the alertdialog that informs him
        that the location must be activated
    */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case RESULT_OK:
                    Log.i(TAG, "User agreed to make required location settings changes.");
                    startLocationUpdates();
                    mRequestingLocationUpdates = true;
                    break;
                case RESULT_CANCELED:

                    if (checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    //
                    mMap.setMyLocationEnabled(false);
                    mMap.getUiSettings().setMapToolbarEnabled(false);
                    Log.i(TAG, "User chose not to make required location settings changes.");
                    break;
            }
        }

    }


    //region GOOGLE MAP METHODS
    //////////////////////////////////////////////////////////////////////////////////////
    //////////   GOOGLE MAPS METHODS ////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //location permission is granted
                startLocationUpdates();
            }
        } else
            startLocationUpdates();

    }
    //endregion

    //region PERMISSION REQUESTION METHODS
    ///////////////////////////////////////////////////////////////////////////
    ///////////  METHOD COLLECTS THE RESULT OF REQUESTING PERMISSIONS /////////
    ///////////////////////////////////////////////////////////////////////////


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission was granted");
                    startLocationUpdates();

                } else {
                    //permission denied
                    Toast.makeText(getContext(), getString(R.string.error_permission_location_denied), Toast.LENGTH_LONG).show();


                }
                return;
        }
    }

    //endregion


    //region CUSTOM METHODS

    /**
     * Gets  XML references for JAVA View objects
     */
    private void setReferencesViewsToXML(View v) {
        etLatitude = v.findViewById(R.id.etLatitudeAddLocation);
        etLongitude = v.findViewById(R.id.etLongitudeAddLocation);
        etLocality = v.findViewById(R.id.etLocalityAddLocation);
        etLocality.setEnabled(false);
        etMunicipality = v.findViewById(R.id.etMunicipalityAddLocation);
        etMunicipality.setEnabled(false);
        etComments = v.findViewById(R.id.etCommentsAddLocation);
        btNext = v.findViewById(R.id.btNextAddLocationFragment);
        layoutLongitude = v.findViewById(R.id.layoutLongitudeAddLocationFragment);
        layoutLatitude = v.findViewById(R.id.layoutLatitudeAddLocationFragment);
        layoutMunicipality = v.findViewById(R.id.layoutMunicipalityAddLocationFragment);
        ibEditLocality = v.findViewById(R.id.btEditLocalityAddLocation);
        ibEditMunicipality = v.findViewById(R.id.btEditMunicipalityAddLocation);

    }


    /**
     * Method checks permissions of location and if they are granted, makes request location updates
     * If permission is not granted, it request the permission.
     */
    private void startLocationUpdates() {
        if (getActivity() != null) {

            //check if I have permission to use location. If I haven't got I request it.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //location permission is granted
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    if (mMap != null) {
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMapToolbarEnabled(true);
                    }

                } else {
                    //I request it
                    requestPermissionFragment(Manifest.permission.ACCESS_FINE_LOCATION, getString(R.string.justificationPermissionLocation),
                            REQUEST_PERMISSION_LOCATION, this);
                }
            } else {
                //location permission is granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                if (mMap != null) {
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMapToolbarEnabled(true);
                }

            }
        }
    }


    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        KEY_REQUESTING_LOCATION_UPDATES);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mLastLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }


        }
    }


    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has location settings activated.
     */
    private void buildLocationSettingsRequest() {
        builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
    }


    /**
     * Check if the device's location settings are adequate for the app's needs using the
     * {@link com.google.android.gms.location.SettingsClient#checkLocationSettings(LocationSettingsRequest)}
     * method, with the results provided through a {@code Task}.
     */
    private void checkLocationSettings() {
        SettingsClient client = LocationServices.getSettingsClient(getActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdates();
                mRequestingLocationUpdates = true;

            }
        });

        task.addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startIntentSenderForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        startIntentSenderForResult(resolvable.getResolution().getIntentSender(), REQUEST_CHECK_SETTINGS, null, 0, 0, 0, null);
                        /* IMPORTANT: I use startIntentSenderForResult() to show dialog which inform
                           the user about activating his location because I'm using Fragment instead of Activity.
                           The documentation of FusedLocationProviderClient shows another method to make this task.
                           Specifically, Google, in its documentation (https://developer.android.com/training/location/change-location-settings)
                           says that the method {@link com.google.android.gms.common.api.ResolvableApiException#startResolutionForResult(Activity, int)}must be used; but that method
                           only works for activities, since it expects the result in the onActivityResult of the Activity, not the fragment.
                         */
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                        Log.i(TAG, "Error open settings screen");
                    }
                }
            }
        });

    }


    /**
     * Method return true if location1 is equal to location2; false otherwise
     *
     * @param l1 location 1
     * @param l2 location 2
     * @return true if location 1 is equal to location 2;false otherwise
     */
    private boolean areEqual(Location l1, Location l2) {
        if (l1.getLongitude() == l2.getLongitude() && l1.getLatitude() == l2.getLatitude())
            return true;
        return false;
    }


    /**
     * Method starts a geocoding address lookup using an IntentService
     */
    protected void startGeocodeIntentService() {
        Intent intent = new Intent(getContext(), FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        getContext().startService(intent);
    }

    /**
     * Method shows the locality collected from latitude and longitude
     *
     * @
     */
    protected void displayLocalityReceived(String locality) {
        if (!locality.isEmpty())
            etLocality.setText(locality);

    }


    /**
     * Method shows the municipality collected from latitude and longitude
     *
     * @
     */
    protected void displayMunicipalityReceived(String municipality) {
        if (!municipality.isEmpty())
            etMunicipality.setText(municipality);

    }

    /**
     * Method shows the latitude and longitude in the UI
     *
     * @
     */
    protected void displayCoordinates(LatLng latLng) {
        etLatitude.setText(String.valueOf(latLng.latitude));
        etLongitude.setText(String.valueOf(latLng.longitude));
    }
    //endregion


    //region LocalityResultReceiver CLASS

    /**
     * ResultReceiver used to handle the response from FetchAddressIntentService.
     * The response will include a numeric result code (resultCode) as well as a message
     * containing the result data (resultData).
     * If the reverse geocoding process is successful, the resultData contains the locality.
     * In the case of a failure, the resultData contains a text describing the reason for the
     * failure.
     */
    class LocalityResultReceiver extends ResultReceiver {


        public LocalityResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == Constants.SUCCESS_RESULT) {
                if (resultData == null) {
                    return;
                }

                // Display the locality string
                // or an error message sent from the intent service.
                localityReceived = resultData.getString(Constants.RESULT_DATA_KEY);
                if (localityReceived == null)
                    localityReceived = "";
                if (!editedByUser(Constants.PREFERENCES_KEY_EDITING_LOCALITY))
                    displayLocalityReceived(localityReceived);
                if (!editedByUser(Constants.PREFERENCES_KEY_EDITING_MUNICIPALITY))
                    displayMunicipalityReceived(localityReceived);
            } else {
                etLocality.setText("");
                etMunicipality.setText("");
            }
        }
    }


    /**
     * Method initializes the UI to the required fields
     */
    private void initRequiredFieldsUI() {
        // Set up required fields
        layoutLatitude.setHint(layoutLatitude.getHint() + " " + getString(R.string.asterisk));
        layoutLongitude.setHint(layoutLongitude.getHint() + " " + getString(R.string.asterisk));
        layoutMunicipality.setHint(layoutMunicipality.getHint() + " " + getString(R.string.asterisk));
    }


    /**
     * Set up to each button its actions
     */
    private void setActionsToButtons() {
        btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //deactivated editText for the user
                etLocality.setEnabled(false);
                etMunicipality.setEnabled(false);

                if (verifiedFields()) {

                    // add the information contained in this screen to the current sighting
                    // obtained from the previous screen.
                    currentSighting.setLatitude(Float.parseFloat(etLatitude.getText().toString()));
                    currentSighting.setLongitude(Float.parseFloat(etLongitude.getText().toString()));
                    currentSighting.setLocality(etLocality.getText().toString());
                    currentSighting.setMunicipality(etMunicipality.getText().toString());
                    currentSighting.setComment(etComments.getText().toString());
                    //serialize the Sighting object
                    String sigtingObjectString = gson.toJson(currentSighting);

                    if (currentSighting.getSightingType().equals(SightingType.NEST.getLocalizedName(getContext()))) {
                        //IT IS A NEST SIGHTING
                        //navigate to the AddDescriptionNestFragment passing the current info about the sighting serialized
                        AddLocationFragmentDirections.ActionAddLocationFragmentToAddDescriptionNestFragment action = AddLocationFragmentDirections.actionAddLocationFragmentToAddDescriptionNestFragment(sigtingObjectString);
                        Navigation.findNavController(view).navigate(action);
                    } else {
                        // IT IS WASPS SIGHTING
                        //navigate to the AddWhoAreYouFragment passing the current info about the sighting serialized
                        AddLocationFragmentDirections.ActionAddLocationFragmentToAddWhoAreYouFragment action = AddLocationFragmentDirections.actionAddLocationFragmentToAddWhoAreYouFragment(sigtingObjectString);
                        Navigation.findNavController(view).navigate(action);
                    }


                    //reset the preference about editing locality and municipality
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(Constants.PREFERENCES_KEY_EDITING_LOCALITY, false);
                    editor.putBoolean(Constants.PREFERENCES_KEY_EDITING_MUNICIPALITY, false);
                    editor.commit();
                }

            }
        });


        ibEditLocality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(Constants.PREFERENCES_KEY_EDITING_LOCALITY, true);
                editor.commit();
                //deactivated municipality for user changes
                etMunicipality.setEnabled(false);
                //activated locality editText for user changes
                etLocality.setEnabled(true);
                etLocality.requestFocus();
            }
        });

        ibEditMunicipality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(Constants.PREFERENCES_KEY_EDITING_MUNICIPALITY, true);
                editor.commit();
                //deactivated locality for user changes
                etLocality.setEnabled(false);
                //activated municipality editText for user changes
                etMunicipality.setEnabled(true);
                etMunicipality.requestFocus();
            }
        });
    }


    /**
     * Method verifies that the required fields are not empty
     */
    private boolean verifiedFields() {
        layoutLatitude.setError("");
        layoutLongitude.setError("");
        layoutMunicipality.setError("");

        if (etLatitude.getText().toString().isEmpty() || etLongitude.getText().toString().isEmpty()) {
            layoutLatitude.setError(getString(R.string.error_invalid_latitude));
            layoutLongitude.setError(getString(R.string.error_invalid_longitude));
            AlertDialog ad = new AlertDialog.Builder(getContext()).create();
            ad.setTitle(R.string.error_requiered_field_without_fill);
            ad.setMessage(getString(R.string.error_gps_not_available_or_internet));
            ad.setCancelable(false);
            ad.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.close), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            ad.show();
            return false;
        } else if (etMunicipality.getText().toString().isEmpty()) {
            layoutMunicipality.setError(getString(R.string.error_invalid_municipality));
            return false;
        } else return true;




    }


    /**
     * Method checks if user edited the field which preference key is specified.
     * Returns true if it was edited; false in otherwise.
     *
     * @param preferenceKey preference key used to identify that field
     * @return true if it was edited by user; false in otherwise.
     */
    private boolean editedByUser(String preferenceKey) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        return prefs.getBoolean(preferenceKey, false);

    }

}
//endregion
