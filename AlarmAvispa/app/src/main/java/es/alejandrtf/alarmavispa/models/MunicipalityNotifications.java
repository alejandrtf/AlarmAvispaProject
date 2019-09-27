package es.alejandrtf.alarmavispa.models;

public class MunicipalityNotifications {
    private String municipality;
    private long numberSightings;

    public MunicipalityNotifications(String municipality, long numberSightings) {
        this.municipality = municipality;
        this.numberSightings = numberSightings;
    }

    public MunicipalityNotifications() {
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public long getNumberSightings() {
        return numberSightings;
    }

    public void setNumberSightings(long numberSightings) {
        this.numberSightings = numberSightings;
    }
}

