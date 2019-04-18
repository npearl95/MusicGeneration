package edu.temple.musicgen;

import android.app.Application;

public class UserInfo extends Application {

    private static UserInfo instance;

    // Global variable
    private String userName;
    private String profileEmail;
    private String profileID;

    // Restrict the constructor from being instantiated
    private UserInfo(){}

    public void setUserName(String userName){
     this.userName=userName;
    }
    public String getUserName(){
        return this.userName;
    }

    public void setProfileEmail(String profileEmail){
        this.profileEmail=profileEmail;
    }
    public String getProfileEmail(){
        return this.profileEmail;
    }
    public void setProfileID(String profileID){
        this.profileID=profileID;
    }
    public String getProfileID(){
        return this.profileID;
    }

    public static synchronized UserInfo getInstance(){
        if(instance==null){
            instance=new UserInfo();
        }
        return instance;
    }
}