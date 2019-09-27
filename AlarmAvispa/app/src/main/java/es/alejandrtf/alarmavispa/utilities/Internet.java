package es.alejandrtf.alarmavispa.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Internet {

    public static boolean isInternetConectionActive(Context context){
        ConnectivityManager cm=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork =cm.getActiveNetworkInfo();
        boolean isConnected=activeNetwork!=null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
}
