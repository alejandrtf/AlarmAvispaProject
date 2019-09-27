package es.alejandrtf.alarmavispa.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;


import androidx.fragment.app.Fragment;

import es.alejandrtf.alarmavispa.R;

import static androidx.core.content.ContextCompat.checkSelfPermission;

public class Permissions {
    public static final int REQUEST_PERMISSION_CAMERA = 0;
    public static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 1;
    public static final int REQUEST_PERMISSION_LOCATION = 2;


    /**
     * Request a permission to the user
     *
     * @param permission    permission requested
     * @param justification why that permission is necessary
     * @param requestCode   request code that identify this request
     * @param fragment      fragment will pick up the answer
     */
    public static void requestPermissionFragment(final String permission, String justification, final int requestCode, final Fragment fragment) {
        if (fragment.shouldShowRequestPermissionRationale(permission)) {
            new AlertDialog.Builder(fragment.getActivity())
                    .setTitle(fragment.getActivity().getString(R.string.requestPermission))
                    .setMessage(justification)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            fragment.requestPermissions(new String[]{permission}, requestCode);
                        }
                    })
                    .show();
        } else {
            fragment.requestPermissions(new String[]{permission}, requestCode);
        }
    }


    /**
     * Method checks if the device has the specified permission relative to the contacts
     *
     * @param permission the permission you want check
     * @return true if the device has permission; false otherwise
     */
    public static boolean hasDeviceContactsPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                //permission is granted
                return true;
            } else {
                return false;
            }
        } else {
            // permission is granted
            return true;
        }
    }




    /**
     * Method checks if the device has the specified permission relative to the storage
     *
     * @param permission the permission you want check
     * @return true if the device has permission; false otherwise
     */
    public static boolean hasDeviceStoragePermission(Context context,String permission){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                //permission is granted
                return true;
            } else {
                return false;
            }
        } else {
            // permission is granted
            return true;
        }
    }
}
