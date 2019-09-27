package es.alejandrtf.alarmavispa.storage.interfaces;

import com.google.firebase.firestore.Query;

import java.util.List;
import java.util.Map;

import es.alejandrtf.alarmavispa.models.Sighting;

public interface SightingAsync {
    interface OnCompletedGetListener{
        void onCompletedGet(Sighting sighting);
    }

    interface OnCompletedSizeListener{
        void onCompletedSize(long size);
    }

    interface OnCompletedAddSightingListener{
        void onSuccessfulAddSighting(String sightingId);
        void onFailureAddSighting(Exception e);
    }

    interface OnCompleteAllGetListener{
        void onCompleteAllGet(Map<String,Sighting> sightingList);
    }

    interface OnCompleteGetSightingsMonthYearListener{
        void onCompleteGetSightingsMonthYear(List<Sighting>sightingList);
    }

    void getSighting(String id, OnCompletedGetListener listener);
    void add(Sighting sighting,String sightingId, OnCompletedAddSightingListener listener);
    void delete(String id);
    void update(String id, Sighting sighting);
    void size(OnCompletedSizeListener listener);
    Query queryGetAllSightingOrdered(String sortFieldName);
    void startListening(Query query, OnCompleteAllGetListener listener);
    void stopListening();
    Map<String,Sighting> getAllSightingOrdered(); //return query results

    void getSightingsMonthAndYear(String year, int numberMonth, OnCompleteGetSightingsMonthYearListener listener);

}
