package es.alejandrtf.alarmavispa.utilities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class Contacts {

    /**
     * Method checks if there is a specific contact in the contact list of the user's device
     *
     * @param context Context
     * @param number  number of phone of the contact you want look up
     * @return true if the contact exists; false otherwise
     */
    public static boolean contactExists(Context context, String number) {

        Uri lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        String[] mPhoneNumberProjection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor cur = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null, null);
        try {
            if (cur.moveToFirst()) {
                return true;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return false;
    }
}
