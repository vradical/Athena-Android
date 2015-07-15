package com.teamvh.orbital.athena;

/**
 * Created by Ron on 14-Jul-15.
 */
public class CountryData {

    private String countryName;
    private String countryCode;
    private String countryPhone;
    private String policeNum;
    private String hospitalNum;
    private String addNum;

    public CountryData(String countryName, String policeNum, String hospitalNum, String addNum, String countryCode, String countryPhone) {
        this.countryName = countryName;
        this.countryCode = countryCode;
        this.countryPhone = countryPhone;
        this.policeNum = policeNum;
        this.hospitalNum = hospitalNum;
        this.addNum = addNum;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryPhone() {
        return countryPhone;
    }

    public void setCountryPhone(String countryPhone) {
        this.countryPhone = countryPhone;
    }

    public String getPoliceNum() {
        return policeNum;
    }

    public void setPoliceNum(String policeNum) {
        this.policeNum = policeNum;
    }

    public String getHospitalNum() {
        return hospitalNum;
    }

    public void setHospitalNum(String hospitalNum) {
        this.hospitalNum = hospitalNum;
    }

    public String getAddNum() {
        return addNum;
    }

    public void setAddNum(String addNum) {
        this.addNum = addNum;
    }
}
