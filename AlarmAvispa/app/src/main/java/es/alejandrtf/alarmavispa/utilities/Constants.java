package es.alejandrtf.alarmavispa.utilities;

public class Constants {

    public static final String adminProfile = "ADMIN";
    public static final String userProfile = "USER";
    public static final String adminEmail = "admin_alarmavispa46@gmail.com";
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_IMAGE_GALLERY = 2;
    public static final int MAX_NUMBER_OF_PHOTOS_OF_SIGHTING = 6;
    public static final int COLUMNS_NUMBER_VERTICAL_ADD_PHOTOS_RECYCLERVIEW = 3;
    public static final int COLUMNS_NUMBER_HORIZONTAL_ADD_PHOTOS_RECYCLERVIEW = 6;

    //AddLocationFragment
    // Keys for storing activity state in the Bundle.
    public final static String KEY_REQUESTING_LOCATION_UPDATES = "requestionLocationUpdates";
    public final static String PREFERENCES_KEY_EDITING_LOCALITY = "editingLocality";
    public final static String PREFERENCES_KEY_EDITING_MUNICIPALITY = "editingMunicipality";

    /**
     * Constant used in the location settings dialog.
     */
    public static final int REQUEST_CHECK_SETTINGS = 1;


    //GEOCODER SERVICE
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "es.alejandrtf.alarmavispa";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
    public static final int MAX_NUMBER_RESULTS = 5;

    // CLOUD STORAGE
    public static final String IMAGES_FOLDER_NAME = "images";

    // FIREBASE FIRESTORE
    public static final String USERS_COLLECTION_NAME = "users";
    public static final String SIGHTING_COLLECTION_NAME = "sightings";
    public static final String NUMBER_NOTIFICATIONS_YEAR_MUNICIPALITY_COLLECTION_NAME = "numberNotificationsYearMunicipality";
    public static final String NUMBER_NOTIFICATIONS_YEAR_MUNICIPALITY_FIELD_NAME = "numberSightings";
    public static final String NAME_MUNICIPALITY_NOTIFICATIONS_YEAR_MUNICIPALITY_FIELD_NAME="municipality";
    public static final String SIGHTING_DATE_FIELD_NAME = "sightingDate";


    // AddWhoAreYouFragment
    public static final int REQUEST_PERMISSION_READ_CONTACT = 2;
    public static final int REQUEST_PERMISSION_WRITE_CONTACT = 3;
    public static final String OFFICIAL_EMAIL="info@avisap.es";

    // LOPD & PRIVACY
    public static final String URL_PRIVACY_POLICY_WEB = "https://alejandrtf.github.io/AlarmAvispa/privacy.html";
    public static final String URL_TERMS_CONDITIONS_WEB = "https://alejandrtf.github.io/AlarmAvispa/terms-conditions.html";
    public static final String URL_APPLICATION_WEB = "https://alejandrtf.github.io/AlarmAvispa/";

    // MapSightingFragment
    public static final String PREFERENCES_KEY_GO_MAPSIGHTINGFRAGMENT="go_mapSightingFragment";
    public static final float ASTURIAS_CENTRE_LATITUDE=43.3602900f;
    public static final float ASTURIAS_CENTRE_LONGITUDE=-5.8447600f;
    public static final int SIGHTING_MAP_ZOOM=9;

    // TrapFragment
    public static final String PREFERENCES_KEY_GO_TRAPFRAGMENT="go_trapFragment";

    // Contacts
    public static final String DEVELOPER_EMAIL_ADDRESS="alarmavispa@gmail.com";
}
