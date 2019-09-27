package es.alejandrtf.alarmavispa.fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Map;
import java.util.Set;

import es.alejandrtf.alarmavispa.NavGraphDirections;
import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.models.Sighting;
import es.alejandrtf.alarmavispa.storage.classes.SightingFirestore;
import es.alejandrtf.alarmavispa.storage.classes.Users;
import es.alejandrtf.alarmavispa.storage.interfaces.SightingAsync;
import es.alejandrtf.alarmavispa.utilities.Constants;
import es.alejandrtf.alarmavispa.views.CustomMarkerInfoWindowGoogleMap;

import static androidx.core.content.ContextCompat.checkSelfPermission;
import static es.alejandrtf.alarmavispa.utilities.GoogleMaps.getIconToAddInMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapSightingFragment extends Fragment implements SightingAsync.OnCompleteAllGetListener,
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {
    private NavController navController;
    private CustomMarkerInfoWindowGoogleMap customInfoWindow;
    private String longitudeString = "";
    private String latitudeString = ""; // to centre map on the new registered sighting
    private float latitudeCentre, longitudeCentre;

    //Firebase Firestore variables
    private SightingAsync sightings; //interface
    // Google Maps variables
    private GoogleMap mMap;

    private Map<String, Sighting> sightingMap;

    public MapSightingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get the helpers to make CRUD operations in Cloud Firestore with nodes: Sighting and NumberNotificationsYearMunicipalityAsync
        sightings = new SightingFirestore(getContext());
    }


    @Override
    public void onStart() {
        super.onStart();
        //starts to listen to changes from firestore collection or query.
        // Begins to perform the requested query and will return it through the specified listener
        // (asynchronously)

        sightings.startListening(sightings.queryGetAllSightingOrdered(Constants.SIGHTING_DATE_FIELD_NAME), this);


    }


    @Override
    public void onStop() {
        super.onStop();
        //stop to listen to changes from firestore collection
        sightings.stopListening();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_map_sighting, container, false);
        setHasOptionsMenu(true);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapShowSighting);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(getContext(), getString(R.string.error_loading_google_map), Toast.LENGTH_SHORT).show();
        }
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //check if user is logged or not. It's required to be logged for this option
        if (!Users.isUserLogged(getContext())) {
            // send user to login screen

            navController = Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment);

            //  navigate to the LoginActivity (to the fragment: LoginFragment) passing as argument
            // the fragment identifier from which you are navigating in order to go back after.
            //This navigation is using "safeArgs". See https://developer.android.com/guide/navigation/navigation-pass-data#Safe-args
            NavDirections actionGlobalToLoginActivityDest = NavGraphDirections.actionGlobalToLoginActivityDest(navController.getCurrentDestination().getLabel().toString());
            navController.navigate(actionGlobalToLoginActivityDest);
        } else {
            // when a notification for new registered sighting arrives, the latitude and longitude of that new registered sighting
            // will be received in the save arguments. I must to collect it

            //Use saveArgs to collect it
            if (getArguments().size() > 0) {
                latitudeString = MapSightingFragmentArgs.fromBundle(getArguments()).getLatitudeCentreMap();
                longitudeString = MapSightingFragmentArgs.fromBundle(getArguments()).getLongitudeCentreMap();
            }
            if (!latitudeString.isEmpty() && !longitudeString.isEmpty()) {
                // there is a new registered sighting
                latitudeCentre = Float.parseFloat(latitudeString);
                longitudeCentre = Float.parseFloat(longitudeString);
            } else {
                latitudeCentre = Constants.ASTURIAS_CENTRE_LATITUDE;
                longitudeCentre = Constants.ASTURIAS_CENTRE_LONGITUDE;
            }
        }


    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.map_sighting_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        navController = Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment);

        switch (item.getItemId()) {
            case R.id.chartByMunicipalitiesCurrentYearDest:
                navController.navigate(R.id.action_mapSightingFragment_to_chartByMunicipalitiesCurrentYearDest);
                return true;
            case R.id.chartByMunicipalitiesCurrentMonthDest:
                navController.navigate(R.id.action_mapSightingFragment_to_chartByMunicipalitiesCurrentMonthDest);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }





    /**
     * Method of the OnCompleteAllGetListener that is called when the result of the query are ready to use it
     *
     * @param sightingList
     */
    @Override
    public void onCompleteAllGet(Map<String, Sighting> sightingList) {
        this.sightingMap = sightingList;
        if (mMap != null) {
            showSightings(mMap);
        }
    }


    //region GOOGLE MAP METHODS
    //////////////////////////////////////////////////////////////////////////////////////
    //////////   GOOGLE MAPS METHODS ////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //location permission is granted
                setUpMap(mMap);
            }
        } else {
            //location permission is granted
            setUpMap(mMap);

        }
    }


    /**
     * Method is called when the user clicks on the marker in the map
     *
     * @param marker that is clicked
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        customInfoWindow = new CustomMarkerInfoWindowGoogleMap(getContext());
        mMap.setInfoWindowAdapter(customInfoWindow);
        marker.showInfoWindow();
        return true;
    }


    /**
     * Method is called when the user clicks on the info window which is shown by clicking on the marker
     *
     * @param marker the marker associated with that info window
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        customInfoWindow.collapseExpandSightingInformationCardView();
        if (marker.isInfoWindowShown()) {
            marker.hideInfoWindow();
            marker.showInfoWindow();
        }
    }


    //endregion


    //region CUSTOM METHODS

    /**
     * Method set up the map
     *
     * @param map map you want to set up
     */
    @SuppressLint("MissingPermission")
    private void setUpMap(GoogleMap map) {
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(true);
        centreMapOnAsturias(mMap);

    }


    /**
     * Method puts the map in the centre of Asturias with a certain zoom
     *
     * @param map the map you want to centre
     */
    private void centreMapOnAsturias(final GoogleMap map) {
        LatLng myLocation = new LatLng(latitudeCentre, longitudeCentre); //Oviedo coordinates
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                Constants.SIGHTING_MAP_ZOOM));


    }


    /**
     * Method shows the sightings on the map
     *
     * @param map the map where sightings will be shown
     */
    private void showSightings(GoogleMap map) {
        if (sightingMap != null && sightingMap.size() > 0) {
            Set<String> keys = sightingMap.keySet();
            for (String key : keys) {
                //Place marker for that sighting
                Marker mMarker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(sightingMap.get(key).getLatitude(), sightingMap.get(key).getLongitude()))
                        .title(key)
                        .icon(getIconToAddInMap(getContext(), sightingMap.get(key))));
                mMarker.setTag(sightingMap.get(key));


            }
        }

    }


    //endregion
}
