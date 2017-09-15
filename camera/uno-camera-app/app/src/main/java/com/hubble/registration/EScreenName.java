package com.hubble.registration;

/**
 * Created by QuayChenh on 2/4/2016.
 */
public enum EScreenName {
    Login("Login"),
    Registration("Registration"),
    ForgotPassword("Forgot Password"),
    AddCamera("Add Camera"),
    DeviceSetup("Device Setup"),
    EnablePairingMode("Enable Pairing Mode"),
    Cameras("Camera List"),
    Timeline("Timeline"),
    Videos("Videos"),
    CameraSettings("Camera Settings"),
    Settings("Settings"),
    Account("Account"),
    Help("Help"),
    About("ToS"),
    LiveStreaming("Live Streaming"),
    Patrol("Patrol"),
    VideoPlayer("Video Player");

    private String _name;
    EScreenName(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }
}
