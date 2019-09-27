package es.alejandrtf.alarmavispa.models;

public class UserModel {
    private String name;
    private String surName;
    private String email;
    private String idCard;
    private String phone;
    private String city;
    private String province;
    private long startSession;
    private String userProfile; //admin or user

    public UserModel() {
    }

    public UserModel(String name, String surName, String email, String idCard,
                     String phone, String city, String province, long startSession, String userProfile) {
        this.name = name;
        this.surName = surName;
        this.email = email;
        this.idCard = idCard;
        this.phone = phone;
        this.city = city;
        this.province = province;
        this.startSession = startSession;
        this.userProfile=userProfile;
    }

    public UserModel(String name, String surname, String email, String phone,long startSession,String userProfile) {
        this(name, surname, email, "", "", "", "", startSession,userProfile);
    }

    public UserModel(String name, String surname, String email,String phone,String userProfile) {
        this(name, surname, email,phone, System.currentTimeMillis(),userProfile);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getStartSession() {
        return startSession;
    }

    public void setStartSession(long startSession) {
        this.startSession = startSession;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }
}


