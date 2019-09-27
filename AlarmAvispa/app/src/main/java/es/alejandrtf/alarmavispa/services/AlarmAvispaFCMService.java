package es.alejandrtf.alarmavispa.services;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;


import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import es.alejandrtf.alarmavispa.NavGraphDirections;
import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.activities.MainActivity;

public class AlarmAvispaFCMService extends FirebaseMessagingService {
    private NavController navController;
    private String latitude="", longitude="";

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            // it has payload
            latitude = remoteMessage.getData().get("latitude");
            longitude = remoteMessage.getData().get("longitude");
        }

        if (remoteMessage.getNotification() != null) {
            Log.i("MIAPLI", remoteMessage.getNotification().getBody());

            MainActivity.getCurrentContext().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    navController = Navigation.findNavController(MainActivity.getCurrentContext(), R.id.my_nav_host_fragment);

                    AlertDialog ad = new AlertDialog.Builder(MainActivity.getCurrentContext()).create();
                    ad.setTitle(remoteMessage.getNotification().getTitle());
                    ad.setMessage(remoteMessage.getNotification().getBody());
                    ad.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.go),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    dialog.dismiss();
                                    //navigate to the MapSightingFragment  passing as argument the latitude and longitude of the new registered sighting
                                    //this navigation is using "safeArgs". See https://developer.android.com/guide/navigation/navigation-pass-data#Safe-args
                                    NavDirections actionGlobalToMapSightingFragmentDest = NavGraphDirections.actionGlobalToMapSightingFragmentDest(latitude, longitude);
                                    navController.navigate(actionGlobalToMapSightingFragmentDest);

                                }
                            });
                    ad.setIcon(R.drawable.small_logo);
                    ad.show();
                }
            });

        }


    }
}
