package com.teamvh.orbital.athena;

/**
 * Created by Ron on 13-Jul-15.
 */
public class EmergencyTrackData {

    String address;

    String dateTime;

    String longitude;

    String latitude;

    String country;

    String locality;

    public EmergencyTrackData(String address, String dateTime, String latitude, String longitude, String country, String locality) {
        this.address = address;
        this.dateTime = dateTime;
        this.longitude = longitude;
        this.latitude = latitude;
        this.country = country;
        this.locality = locality;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }
}
