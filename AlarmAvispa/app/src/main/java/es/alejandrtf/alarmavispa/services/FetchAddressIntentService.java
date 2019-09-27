package es.alejandrtf.alarmavispa.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.utilities.Constants;


public class FetchAddressIntentService extends IntentService {
    private final String TAG = "FetchAddressIService";

    protected ResultReceiver receiver;
    protected String locality;

    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }
        String errorMessage = "";

        // Get the receiver to collect the response
        receiver = intent.getParcelableExtra(Constants.RECEIVER);
        if (receiver == null)
            return;

        // Get the location passed to this service through an extra
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);
        if (location == null) {
            errorMessage = getString(R.string.location_data_extra_not_available);
            Log.e(TAG, errorMessage);
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        }
        //it's ok
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    //get only one result
                    Constants.MAX_NUMBER_RESULTS);

        } catch (IOException ioException) {
            // Catch network or other I/O problems
            errorMessage = getString(R.string.service_not_available);
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = getString(R.string.invalid_lat_long_used);
            Log.e(TAG, errorMessage + ". " +
                            "Latitude = " + location.getLatitude() +
                            "Longitude = " + location.getLongitude(),
                    illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        } else {
            // Address found
            // I go through all the addresses looking for the first one that contains locality
            for (Address address :
                    addresses) {

                if (address.getLocality() != null && address.getLocality().length() > 0) {
                    locality = address.getLocality();
                    break;
                }

            }


            Log.i(TAG, getString(R.string.address_found));
            deliverResultToReceiver(Constants.SUCCESS_RESULT, locality);
        }

    }


    /**
     * Method that sends the results back to a ResultReceiver in the activity/fragment that
     * started the service
     *
     * @param resultCode the resultCode that indicates success or failure on the geocoding request
     * @param message    if operation was success will contain the address; in the case of a failure will contain
     *                   some text describing the reason for the failure
     */
    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        receiver.send(resultCode, bundle);
    }
}
