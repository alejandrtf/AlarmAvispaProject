package es.alejandrtf.alarmavispa.models;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Pojo used in Firestore to save/read a sighting
 */
public class Sighting {
    //fields necessary to notify sighting
    private List<String> photo;
    private String nestType; // got from NestType enum
    private String nestStand; //got from NestStand enum
    private float height; //in meters (to the nest)
    private String locality; //town, city,...
    private String municipality;
    private String place; // neighborhood, forest, road, courtyard
    private float latitude, longitude;
    private String comment;
    private String creatorId; //user notifies this sighting. Only it's used when user is registered
    private String user; //name+surname. Only it's used when user is not registered
    private String phone; //user's phone. Only it's used when user is not registered
    private long sightingDate;
    private String sightingType; //hornet or nest
    private boolean waspAround; //if there was wasp around nest or not
    //fields necessary to admin

    private String reportMethod; //alarmAvisp, 112, whatsapp, phone, avisap,...
    private String reportDate;
    private String state; //solved, pending verification, reported, pending resolution, not-intervention
    private String verificator;
    private String verificationState; //active, removed,inactive, not velutina, not localized
    private boolean faceCheck; //if the verification process was face to face
    private long verificationDate;
    private String verificationComments;
    private String inteventionType; //not intevention, biocide injected, biocide sprayed
    private String explosiveSerialNumber;
    private String resolutionProposal;
    private String exterminator;
    private long exterminationDate;
    private String exterminationComments;

    public Sighting() {

    }

    public Sighting(List<String> photo, String sightingType, String place) {
        this.photo = photo;
        this.sightingType = sightingType;
        this.place = place;
    }


    public Sighting(List<String> photo, String nestType, String nestStand, float height, String locality, String place, float latitude, float longitude, String comment,
                    String creatorId, String user, String phone, long sightingDate, String sightingType, String municipality, boolean waspAround) {
        this.photo = photo;
        this.nestType = nestType;
        this.nestStand = nestStand;
        this.height = height;
        this.locality = locality;
        this.place = place;
        this.latitude = latitude;
        this.longitude = longitude;
        this.comment = comment;
        this.creatorId = creatorId;
        this.user = user;
        this.phone = phone;
        this.sightingDate = sightingDate;
        this.sightingType = sightingType;
        this.municipality = municipality;
        this.waspAround = waspAround;
    }

    public List<String> getPhoto() {
        return photo;
    }

    public void setPhoto(List<String> photo) {
        this.photo = photo;
    }

    public String getNestType() {
        return nestType;
    }

    public void setNestType(String nestType) {
        this.nestType = nestType;
    }

    public String getNestStand() {
        return nestStand;
    }

    public void setNestStand(String nestStand) {
        this.nestStand = nestStand;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getSightingDate() {
        return sightingDate;
    }

    public void setSightingDate(long sightingDate) {
        this.sightingDate = sightingDate;
    }

    public String getSightingType() {
        return sightingType;
    }

    public void setSightingType(String sightingType) {
        this.sightingType = sightingType;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getReportMethod() {
        return reportMethod;
    }

    public void setReportMethod(String reportMethod) {
        this.reportMethod = reportMethod;
    }

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getVerificator() {
        return verificator;
    }

    public void setVerificator(String verificator) {
        this.verificator = verificator;
    }

    public String getVerificationState() {
        return verificationState;
    }

    public void setVerificationState(String verificationState) {
        this.verificationState = verificationState;
    }

    public boolean isFaceCheck() {
        return faceCheck;
    }

    public void setFaceCheck(boolean faceCheck) {
        this.faceCheck = faceCheck;
    }

    public long getVerificationDate() {
        return verificationDate;
    }

    public void setVerificationDate(long verificationDate) {
        this.verificationDate = verificationDate;
    }

    public String getVerificationComments() {
        return verificationComments;
    }

    public void setVerificationComments(String verificationComments) {
        this.verificationComments = verificationComments;
    }

    public String getInteventionType() {
        return inteventionType;
    }

    public void setInteventionType(String inteventionType) {
        this.inteventionType = inteventionType;
    }

    public String getExplosiveSerialNumber() {
        return explosiveSerialNumber;
    }

    public void setExplosiveSerialNumber(String explosiveSerialNumber) {
        this.explosiveSerialNumber = explosiveSerialNumber;
    }

    public String getResolutionProposal() {
        return resolutionProposal;
    }

    public void setResolutionProposal(String resolutionProposal) {
        this.resolutionProposal = resolutionProposal;
    }

    public String getExterminator() {
        return exterminator;
    }

    public void setExterminator(String exterminator) {
        this.exterminator = exterminator;
    }

    public long getExterminationDate() {
        return exterminationDate;
    }

    public void setExterminationDate(long exterminationDate) {
        this.exterminationDate = exterminationDate;
    }

    public String getExterminationComments() {
        return exterminationComments;
    }

    public void setExterminationComments(String exterminationComments) {
        this.exterminationComments = exterminationComments;
    }

    public boolean isWaspAround() {
        return waspAround;
    }

    public void setWaspAround(boolean waspAround) {
        this.waspAround = waspAround;
    }

    public String getYearFromDate(long sightingDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("YYYY");
        Date date = new Date(sightingDate);
        return formatter.format(date);
    }

    @NonNull
    @Override
    public String toString() {
        String code="V"+getYearFromDate(sightingDate)+municipality.substring(0,3);
        String result=code+"nesttype: "+nestType+" - "+"user: "+user+" - "+ "phone:"+phone;
        return result;
    }
}
