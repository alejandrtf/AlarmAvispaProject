package es.alejandrtf.alarmavispa.storage.classes;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.models.Sighting;
import es.alejandrtf.alarmavispa.storage.interfaces.SightingAsync;
import es.alejandrtf.alarmavispa.utilities.Constants;

import static es.alejandrtf.alarmavispa.utilities.Constants.SIGHTING_COLLECTION_NAME;

public class SightingFirestore implements SightingAsync, EventListener<QuerySnapshot> {
    private static final String TAG = "Firebase";
    private Context context;

    private CollectionReference sightings;
    private List<DocumentSnapshot> items; // for queries results
    private ListenerRegistration registration; // will contain the register of event listener to remove it after.
    private OnCompleteAllGetListener onCompleteAllGetListener; //used when getAllSightin is called

    public SightingFirestore(Context context) {
        this.context = context;
        items = new ArrayList<DocumentSnapshot>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        sightings = db.collection(SIGHTING_COLLECTION_NAME);
    }


    public Map<String, Sighting> getAllSightingOrdered() {
        Map<String, Sighting> map = new HashMap<>();

        for (int i = 0; i < items.size(); i++) {
            String key = items.get(i).getId();
            map.put(key, items.get(i).toObject(Sighting.class));

        }
        return map;
    }


    /**
     * Method gets asynchronously the sighting with id "id" by the specified listener
     *
     * @param id       the sighting id you want to get
     * @param listener object implements the listener will get our sighting
     */
    @Override
    public void getSighting(String id, final OnCompletedGetListener listener) {
        sightings.document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Sighting sighting = task.getResult().toObject(Sighting.class);
                    listener.onCompletedGet(sighting);
                } else {
                    Log.e(TAG, context.getString(R.string.error_reading_firebase), task.getException());
                    listener.onCompletedGet(null);
                }
            }
        });
    }


    /**
     * Method adds the specified sighting to the FirebaseFirestore database
     *
     * @param sighting   - the sighting you want to add to the database
     * @param sightingId - the id is made up of "V"+ the year + the first three letters of municipality + the number of sighting
     *                   for that municipality in that year.
     * @param listener   objects implements the listener that will be called when the process of recording the sighting
     *                   has concluded, both successfully and with error.
     */
    @Override
    public void add(Sighting sighting, final String sightingId, final OnCompletedAddSightingListener listener) {

        sightings.document(sightingId).set(sighting).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                listener.onSuccessfulAddSighting(sightingId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                listener.onFailureAddSighting(e);
            }
        });

    }


    /**
     * Method removes from FirebaseFirestore the sighting with specified id
     *
     * @param id - the sighting id you want remove
     */
    @Override
    public void delete(String id) {
        sightings.document(id).delete();

    }


    /**
     * Method updates FirebaseFirestore the sighting with specified id with
     *
     * @param id       - the sighting id you want update
     * @param sighting -the new data for that sighting
     */
    @Override
    public void update(String id, Sighting sighting) {
        sightings.document(id).set(sighting);
    }


    /**
     * Method returns asynchronously the total number of sightings saved in FirebaseFirestore.
     * A listener is used to return data
     *
     * @param listener - OnCompletedSizeListener object that will be the listener
     */
    @Override
    public void size(final OnCompletedSizeListener listener) {
        sightings.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    listener.onCompletedSize(task.getResult().size());
                } else {
                    Log.e(TAG, context.getString(R.string.error_reading_firebase), task.getException());
                    listener.onCompletedSize(-1);
                }
            }
        });
    }


    /**
     * Method returns the query that gets all sighting from Firestore ordered by specified sort field name.
     *
     * @param sortFieldName - field name used to order the query results
     */
    @Override
    public Query queryGetAllSightingOrdered(String sortFieldName) {
        Query query = FirebaseFirestore.getInstance()
                .collection(Constants.SIGHTING_COLLECTION_NAME)
                .orderBy(sortFieldName);
        return query;
    }


    // CONTROL OF LISTENERS FOR CHANGES IN COLLECTIONS FIRESTORE

    /**
     * Method activates the event listener
     *
     * @param query    the query on which the event listener will be activated
     * @param listener OnCompleteAllGetListener object that will be the listener which will be called when
     *                 updated data are ready
     */
    public void startListening(Query query, OnCompleteAllGetListener listener) {
        items = new ArrayList<DocumentSnapshot>();
        registration = query.addSnapshotListener(this);
        this.onCompleteAllGetListener = listener;
    }


    /**
     * Method deactivate the event listener
     */
    public void stopListening() {
        if (registration != null)
            registration.remove();
    }


    /**
     * Method of the interface EventListener that is called every time
     * there is any change in the query or collection.
     *
     * @param snapshots current snapshot of the query or collection (with the changes)
     * @param e         exception in case of error
     */
    @Override
    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
        if (e != null) {
            Toast.makeText(context, R.string.error_getting_query, Toast.LENGTH_LONG).show();
            return;
        }


        for (DocumentChange dc : snapshots.getDocumentChanges()) {
            int pos = dc.getNewIndex();
            int oldPos = dc.getOldIndex();
            switch (dc.getType()) {
                case ADDED:
                    items.add(pos, dc.getDocument());
                    break;
                case REMOVED:
                    items.remove(oldPos);
                    break;
                case MODIFIED:
                    items.remove(oldPos);
                    items.add(pos, dc.getDocument());
                    break;
                default:
                    Log.w(TAG, context.getString(R.string.unknown_change));
            }
        }

        onCompleteAllGetListener.onCompleteAllGet(getAllSightingOrdered());
    }


    /**
     * Method gets all sightings for the specified month and year
     *
     * @param year
     * @param numberMonth
     * @param listener    OnCompleteGetSightingsMonthYearListener object that will be the listener
     */
    @Override
    public void getSightingsMonthAndYear(final String year, int numberMonth, final OnCompleteGetSightingsMonthYearListener listener) {
        final List<Sighting>sightingList=new ArrayList<>();
        Date start = null;
        Date end = null;


        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        try {
            start = dateFormat.parse("1-" + numberMonth + "-" + year);
            end = dateFormat.parse("1-" + (numberMonth + 1) + "-" + year);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // convert dates to milliseconds
        Long startDate = start.getTime();
        Long endDate = end.getTime();


        sightings.whereGreaterThanOrEqualTo(Constants.SIGHTING_DATE_FIELD_NAME, startDate)
                .whereLessThan(Constants.SIGHTING_DATE_FIELD_NAME, endDate)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // I have got all sightings for that month, but in all years
                    // I must filter the year
                    for(DocumentSnapshot document:task.getResult()){
                        Sighting s=document.toObject(Sighting.class);
                        if(s.getYearFromDate(s.getSightingDate()).equals(year)){
                            sightingList.add(s);
                        }
                    }

                    listener.onCompleteGetSightingsMonthYear(sightingList);

                } else {
                    Log.e(TAG, context.getString(R.string.error_reading_firebase), task.getException());
                }
            }
        });
    }


}
