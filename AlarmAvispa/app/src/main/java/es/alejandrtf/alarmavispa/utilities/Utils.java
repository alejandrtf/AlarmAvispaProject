package es.alejandrtf.alarmavispa.utilities;

import android.content.Context;
import android.content.pm.PackageManager;

public class Utils {


    /** Method checks if the app which packageName is specified is installed on the device or not.
     *
     * @param context
     * @param packageName name of the package for that application
     * @return true if it is installed; false otherwise
     */
    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
