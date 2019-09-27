package es.alejandrtf.alarmavispa.storage.interfaces;


import java.util.List;

import es.alejandrtf.alarmavispa.models.MunicipalityNotifications;

public interface NumberNotificationsYearMunicipalityAsync {
    interface OnCompletedGetNumberNotificationsListener {
        void onCompletedGetNumberNotifications(long totalNotitications);

        void onNotExists();

        void onError(Exception e);
    }

    interface OnCompletedAddNotificationListener {
        void onSuccessfulAddNotification(long numberSightings);

        void onFailureAddNotification(Exception e);
    }

    interface OnCompleteGetMunicipalitiesListener{
        void onCompleteGetMunicipalities(List<MunicipalityNotifications>list);
        void onError(Exception e);
    }

    void getNumberNotificationsYearMunicipality(String year, String municipality, OnCompletedGetNumberNotificationsListener listener);

    void addNotification(String year, String municipality, long newNumberNotificationsForThatMunicipality, OnCompletedAddNotificationListener listener);

    void update(String year, String municipality, long oldTotal);

    void getMunicipalitiesInfoThatYear(String year,  OnCompleteGetMunicipalitiesListener listener);

}
