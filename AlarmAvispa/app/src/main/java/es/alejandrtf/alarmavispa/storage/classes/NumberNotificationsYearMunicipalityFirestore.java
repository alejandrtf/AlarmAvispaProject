package es.alejandrtf.alarmavispa.storage.classes;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.models.MunicipalityNotifications;
import es.alejandrtf.alarmavispa.storage.interfaces.NumberNotificationsYearMunicipalityAsync;
import es.alejandrtf.alarmavispa.utilities.Constants;

import static es.alejandrtf.alarmavispa.utilities.Constants.NUMBER_NOTIFICATIONS_YEAR_MUNICIPALITY_COLLECTION_NAME;

public class NumberNotificationsYearMunicipalityFirestore implements NumberNotificationsYearMunicipalityAsync {
    private static final String TAG = "Firebase";
    private Context context;

    private CollectionReference numberNotificationsYearMunicipality;

    public NumberNotificationsYearMunicipalityFirestore(Context context) {
        this.context = context;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        numberNotificationsYearMunicipality = db.collection(NUMBER_NOTIFICATIONS_YEAR_MUNICIPALITY_COLLECTION_NAME);
    }


    /**
     * Method gets asynchronously  by the specified listener, the total number of notification
     * that have been during the year specified for that specified municipality.
     *
     * @param year         the year
     * @param municipality the municipality
     * @param listener     object implements the listener will get our data
     */
    @Override
    public void getNumberNotificationsYearMunicipality(String year, String municipality, final OnCompletedGetNumberNotificationsListener listener) {
        String id = year + municipality.substring(0, 3).toUpperCase(); //only the first three characters
        numberNotificationsYearMunicipality.document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().exists()) {
                        //there isn't notifications in that year for that municipality
                        listener.onNotExists();
                    } else {
                        long totalNumber = task.getResult().getLong(Constants.NUMBER_NOTIFICATIONS_YEAR_MUNICIPALITY_FIELD_NAME);
                        listener.onCompletedGetNumberNotifications(totalNumber);
                    }
                } else {
                    Log.e(TAG, context.getString(R.string.error_reading_firebase), task.getException());
                    listener.onError(task.getException());
                }
            }
        });
    }


    /**
     * Method gets asynchronously  by the specified listener, the information
     * store for that specified year about the municipalities which have got sightings
     *
     * @param year     the year
     * @param listener object implements the listener will get our data
     */
    @Override
    public void getMunicipalitiesInfoThatYear(String year, final OnCompleteGetMunicipalitiesListener listener) {
        int nextYear = Integer.parseInt(year) + 1;
        numberNotificationsYearMunicipality.whereGreaterThan(FieldPath.documentId(), year)
                .whereLessThan(FieldPath.documentId(), String.valueOf(nextYear))
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    listener.onCompleteGetMunicipalities(task.getResult().toObjects(MunicipalityNotifications.class));

                } else {
                    Log.e(TAG, context.getString(R.string.error_reading_firebase), task.getException());
                    listener.onError(task.getException());
                }
            }
        });
    }


    /**
     * Method adds o updates the number of notifications received for that year in that municipality
     * If there was none, it will add a new entry with the number 1;
     * otherwise, it will increase by one the number of notifications that already had.
     *
     * @param year                                      the year of notification
     * @param municipality                              the municipality
     * @param newNumberNotificationsForThatMunicipality the number de notifications already updated
     * @param listener                                  objects implements the listener that will be called when the process of recording this notification
     *                                                  *                   has concluded, both successfully and with error.
     */
    @Override
    public void addNotification(String year, String municipality,
                                final long newNumberNotificationsForThatMunicipality, final OnCompletedAddNotificationListener listener) {

        String code = year + municipality.substring(0, 3).toUpperCase(); //only the first three characters
        Map<String, Object> datos = new HashMap<>();
        datos.put(Constants.NUMBER_NOTIFICATIONS_YEAR_MUNICIPALITY_FIELD_NAME, newNumberNotificationsForThatMunicipality);
        datos.put(Constants.NAME_MUNICIPALITY_NOTIFICATIONS_YEAR_MUNICIPALITY_FIELD_NAME, municipality);
        numberNotificationsYearMunicipality.document(code).set(datos).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                listener.onSuccessfulAddNotification(newNumberNotificationsForThatMunicipality);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                listener.onFailureAddNotification(e);
            }
        });
    }

    @Override
    public void update(String year, String municipality, long oldTotal) {

    }
}
