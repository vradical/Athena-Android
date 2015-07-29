package com.teamvh.orbital.athena;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Ron on 29-Jul-15.
 */
public class SpecialZoneData {

    private String dz_id;
    private String dz_title;
    private String dz_info;
    private String zone_type;
    private LatLng coordinate;
    private String dateTime;

    public SpecialZoneData() {
        super();
    }
    public SpecialZoneData(String dz_id, String dz_title, String dz_info,
                           String zone_type, LatLng coordinate, String dateTime) {
        super();
        this.dz_id = dz_id;
        this.dz_title = dz_title;
        this.dz_info = dz_info;
        this.zone_type = zone_type;
        this.coordinate = coordinate;
        this.dateTime = dateTime;
    }
    public String getDz_id() {
        return dz_id;
    }
    public void setDz_id(String dz_id) {
        this.dz_id = dz_id;
    }
    public String getDz_title() {
        return dz_title;
    }
    public void setDz_title(String dz_title) {
        this.dz_title = dz_title;
    }
    public String getDz_info() {
        return dz_info;
    }
    public void setDz_info(String dz_info) {
        this.dz_info = dz_info;
    }
    public String getZone_type() {
        return zone_type;
    }
    public void setZone_type(String zone_type) {
        this.zone_type = zone_type;
    }

    public LatLng getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(LatLng coordinate) {
        this.coordinate = coordinate;
    }

    public String getDateTime() {
        return dateTime;
    }
    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

}
