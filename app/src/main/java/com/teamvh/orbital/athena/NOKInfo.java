package com.teamvh.orbital.athena;

/**
 * Created by YANG on 5/22/2015.
 */
public class NOKInfo {
    // Database Information
    //Table name

    //variables
    private int _id;
    private String _nokname;
    private String _nokemail;
    private int _nokphone;

    //constructor
    public NOKInfo() {

    }

    public NOKInfo(int id, String nokname , String nokemail, int nokphone) {
        this._id = id;
        this._nokname = nokname;
        this._nokemail = nokemail;
        this._nokphone = nokphone;
    }

    public NOKInfo(String nokname , String nokemail, int nokphone) {
        this._nokname = nokname;
        this._nokemail = nokemail;
        this._nokphone = nokphone;
    }

    public NOKInfo(String nokname, int nokphone) {
        this._nokname = nokname;
        this._nokphone = nokphone;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_nokname() {
        return _nokname;
    }

    public void set_nokname(String _nokname) {
        this._nokname = _nokname;
    }

    public String get_nokemail() {
        return _nokemail;
    }

    public void set_nokemail(String _nokemail) {
        this._nokemail = _nokemail;
    }

    public int get_nokphone() {
        return _nokphone;
    }

    public void set_nokphone(int _nokphone) {
        this._nokphone = _nokphone;
    }










}
