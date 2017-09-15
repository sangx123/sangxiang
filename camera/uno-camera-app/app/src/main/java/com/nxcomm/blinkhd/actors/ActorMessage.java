package com.nxcomm.blinkhd.actors;

import com.hubble.util.ListChild;

import java.util.Map;

/**
 * Created by brennan on 2015-07-20.
 */
public class ActorMessage {

  // Getters
  public static class GetAdaptiveQuality {
    public ListChild listChild;

    public GetAdaptiveQuality(ListChild listChild) {
      this.listChild = listChild;
    }
  }

  public static class GetBrightness {
    public ListChild listChild;

    public GetBrightness(ListChild listChild) {
      this.listChild = listChild;
    }
  }

  public static class GetCeilingMount {
    public ListChild listChild;

    public GetCeilingMount(ListChild listChild) {
      this.listChild = listChild;
    }
  }

  public static class GetNightLight {
    public ListChild listChild;

    public GetNightLight(ListChild listChild) {
      this.listChild = listChild;
    }
  }

  public static class GetContrast {
    public ListChild listChild;

    public GetContrast(ListChild listChild) {
      this.listChild = listChild;
    }
  }

  public static class GetCameraBattery {
    public ListChild listChild;

    public GetCameraBattery(ListChild listChild) {
      this.listChild = listChild;
    }
  }

  public static class GetLEDFlicker {
    public ListChild listChild;

    public GetLEDFlicker(ListChild listChild) {
      this.listChild = listChild;
    }
  }

  public static class GetNightVision {
    public ListChild listChild;
    public boolean useCommandIR;

    public GetNightVision(ListChild listChild, boolean useCommandIR) {
      this.listChild = listChild;
      this.useCommandIR = useCommandIR;
    }
  }

  public static class GetSOCVersion {
    public ListChild listChild;

    public GetSOCVersion(ListChild listChild) {
      this.listChild = listChild;
    }
  }

  public static class GetOverlayDate {
    public ListChild listChild;

    public GetOverlayDate(ListChild listChild) {
      this.listChild = listChild;
    }
  }

  public static class GetNotificationSettings {
    public ListChild motionDetection;
    public ListChild soundDetection;
    public ListChild temperature;

    public ListChild motionDetection1;
    public ListChild soundDetection1;
    public ListChild temperature1;

    public GetNotificationSettings(ListChild motionDetection, ListChild soundDetection, ListChild temperature) {
      this.motionDetection = motionDetection;
      this.soundDetection = soundDetection;
      this.temperature = temperature;
    }


  }

  public static class GetLensCorrection {
    public ListChild listChild;

    public GetLensCorrection(ListChild lensCorrection) {
      this.listChild = lensCorrection;
    }
  }
  public static class SetLensCorrection {
    public ListChild listChild;
    public int value;

    public SetLensCorrection(ListChild listChild, int value) {
      this.listChild = listChild;
      this.value = value;
    }
  }

  public static class GetVideoRecordingDuration
  {
    public ListChild listChild;

    public GetVideoRecordingDuration(ListChild lensCorrection)
    {
      this.listChild = lensCorrection;
    }
  }
  public static class SetVideoRecordingDuration
  {
    public ListChild listChild;
    public int value;

    public SetVideoRecordingDuration(ListChild listChild, int value,int currentValue)
    {
      this.listChild = listChild;
      this.listChild.intValue = value;
      this.listChild.oldIntValue = currentValue;
      this.value = value;
    }
  }

  public static class GetMCUVersion
  {
    public ListChild listChild;

    public GetMCUVersion(ListChild mcuVersion)
    {
      this.listChild = mcuVersion;
    }
  }


  public static class GetSetting2Settings {
    /**
     * all setting2 keys have to be handled. include no-supported keys.
     */
    public String[] settings;
    /**
     * determine which setting2 key is corresponding with which listView item
     */
    private Map<String, ListChild> listChildMap;

    public GetSetting2Settings(String[] settings, Map<String, ListChild> listChildMap) {
      this.settings = settings;
      this.listChildMap = listChildMap;
    }

    public ListChild getListChildBySettingCode(String settingCode) {
      if (listChildMap == null || !listChildMap.containsKey(settingCode)) {
        return null;
      }
      return listChildMap.get(settingCode);
    }
  }

  public static class GetPark {
    public GetPark() {
    }
  }

  public static class GetSlaveFirmware {
    public ListChild slaveFirmware;

    public GetSlaveFirmware(ListChild slaveFirmware) {
      this.slaveFirmware = slaveFirmware;
    }
  }

  public static class GetStatusLED {
    public ListChild listChild;

    public GetStatusLED(ListChild listChild) {
      this.listChild = listChild;
    }
  }

  public static class GetTimeZone {
    public ListChild timezone;

    public GetTimeZone(ListChild timezone) {
      this.timezone = timezone;
    }
  }

  public static class GetVolume {
    public ListChild listChild;

    public GetVolume(ListChild listChild) {
      this.listChild = listChild;
    }
  }

  // Setters
  public static class SetAdaptiveQuality {
    public ListChild listChild;
    public boolean isAdaptive;
    public String resolutionValue;
    public String bitrateValue;

    public SetAdaptiveQuality(ListChild listChild, boolean isAdaptive, String resolutionValue, String bitrateValue) {
      this.listChild = listChild;
      this.isAdaptive = isAdaptive;
      this.resolutionValue = resolutionValue;
      this.bitrateValue = bitrateValue;
    }
  }

  public static class SetBrightness {
    public ListChild listChild;
    public int brightnessLevel;

    public SetBrightness(ListChild listChild, int brightnessLevel) {
      this.listChild = listChild;
      this.brightnessLevel = brightnessLevel;
    }
  }

  public static class SetCeilingMount {
    public ListChild listChild;
    public boolean orientation;

    public SetCeilingMount(ListChild listChild, boolean orientation) {
      this.listChild = listChild;
      this.orientation = orientation;
    }
  }

  public static class SetNightLight {
    public ListChild listChild;
    public int mode;

    public SetNightLight(ListChild listChild, int mode) {
      this.listChild = listChild;
      this.mode = mode;
    }
  }

  public static class SetOverlayDate {
    public ListChild listChild;
    public boolean on;

    public SetOverlayDate(ListChild listChild, boolean on) {
      this.listChild = listChild;
      this.on = on;
    }
  }

    public static class GetViewMode {
        public ListChild vm;
        public ListChild qos;

        public GetViewMode(ListChild vm, ListChild qos) {
            this.vm = vm;
            this.qos = qos;
        }
    }

    public static class SetViewMode {
        public ListChild vm;
        public ListChild qos;

        public SetViewMode(ListChild vm, ListChild qos) {
            this.vm = vm;
            this.qos = qos;
        }
    }


    public static class SetQualityOfService {
        public ListChild listChild;
        public int position;

        public SetQualityOfService(ListChild listChild, int position) {
            this.listChild = listChild;
            this.position = position;
        }
    }

    public static class GetQualityOfService {
        public ListChild listChild;
        public int viewMode;

        public GetQualityOfService(ListChild listChild, int viewMode) {
            this.listChild = listChild;
            this.viewMode = viewMode;
        }
    }

  public static class SetContrast {
    public ListChild listChild;
    public int contrastLevel;

    public SetContrast(ListChild listChild, int contrastLevel) {
      this.listChild = listChild;
      this.contrastLevel = contrastLevel;
    }
  }

  public static class SetLEDFlicker {
    public ListChild listChild;
    public int ledHz;

    public SetLEDFlicker(ListChild listChild, int ledHz) {
      this.listChild = listChild;
      this.ledHz = ledHz;
    }
  }

  public static class SetMotionSource {
    public ListChild listChild;
    public int motionSource;

    public SetMotionSource(ListChild listChild,  int motionSource) {
      this.listChild = listChild;
      this.motionSource = motionSource;
    }
  }

  public static class SetMotionDetection {
    public ListChild listChild;
    public boolean motionDetectionEnabled;
    public int motionDetectionLevel;

    public SetMotionDetection(ListChild listChild, boolean motionDetectionEnabled, int motionDetectionLevel) {
      this.listChild = listChild;
      this.motionDetectionEnabled = motionDetectionEnabled;
      this.motionDetectionLevel = motionDetectionLevel;
    }
  }

  public static class SetMotionSentivity{
    public ListChild listChild;
    public boolean motionDetectionEnabled;
    public int motionDetectionLevel;

    public SetMotionSentivity(ListChild listChild, int motionDetectionLevel) {
      this.listChild = listChild;
      this.motionDetectionLevel = motionDetectionLevel;
    }
  }

  public static class SetMotionNotification {
    public ListChild listChild;
    public boolean motionDetectionEnabled;


    public SetMotionNotification(ListChild listChild, boolean motionDetectionEnabled) {
      this.listChild = listChild;
      this.motionDetectionEnabled = motionDetectionEnabled;

    }
  }

  public static class SetMotionDetectionVda {
    public ListChild listChild;
    public int position;
    public int motionDetectionLevel;
    public int prevPosition;

    public SetMotionDetectionVda(ListChild listChild, int position, int motionDetectionLevel, int prevPosition) {
      this.listChild = listChild;
      this.position = position;
      this.motionDetectionLevel = motionDetectionLevel;
      this.prevPosition = prevPosition;
    }
  }

  public static class SetNightVision {
    public ListChild listChild;
    public int nightVisionMode;
    public int nightVisionIntensity;

    public SetNightVision(ListChild listChild, int nightVisionMode, int nightVisionIntensity) {
      this.listChild = listChild;
      this.nightVisionMode = nightVisionMode;
      this.nightVisionIntensity = nightVisionIntensity;
    }
  }

  public static class SetNightVisionHubble {
    public ListChild listChild;
    public int nightVisionMode;
    public boolean useCommandIR;

    public SetNightVisionHubble(ListChild listChild, int nightVisionMode, boolean useCommandIR) {
      this.listChild = listChild;
      this.nightVisionMode = nightVisionMode;
      this.useCommandIR = useCommandIR;
    }
  }

  public static class SetPark {
    public ListChild listChild;
    public boolean isEnabled;
    public int parkTimer;

    public SetPark(ListChild listChild, boolean isEnabled, int parkTimer) {
      this.listChild = listChild;
      this.isEnabled = isEnabled;
      this.parkTimer = parkTimer;
    }
  }

  public static class SetSoundDetection {
    public ListChild listChild;
    public boolean soundDetectionEnabled;

    public SetSoundDetection(ListChild listChild, boolean soundDetectionEnabled) {
      this.listChild = listChild;
      this.soundDetectionEnabled = soundDetectionEnabled;

    }
  }

  public static class SetSoundThreshold {
    public ListChild listChild;

    public int soundDetectionThreshold;

    public SetSoundThreshold(ListChild listChild, int soundDetectionThreshold) {
      this.listChild = listChild;

      this.soundDetectionThreshold = soundDetectionThreshold;
    }
  }

  public static class SetStatusLED {
    public ListChild listChild;
    public boolean ledOn;

    public SetStatusLED(ListChild listChild, boolean ledOn) {
      this.listChild = listChild;
      this.ledOn = ledOn;
    }
  }

  public static class SetTemperatureDetection {
    public ListChild listChild;
    public boolean lowEnabled;
    public boolean highEnabled;
   /* public int lowThreshold;
    public int highThreshold;*/

    public SetTemperatureDetection(ListChild listChild, boolean isEnabled) {
      this.listChild = listChild;
      this.lowEnabled = isEnabled;
      this.highEnabled = isEnabled;
     /* this.lowThreshold = lowThreshold;
      this.highThreshold = highThreshold;*/
    }
  }

  public static class SetLowTemperatureThreshold {
    public ListChild listChild;
   public int lowThreshold;

    public SetLowTemperatureThreshold(ListChild listChild, int lowThreshold) {
      this.listChild = listChild;
      this.lowThreshold = lowThreshold;
    }
  }

  public static class SetHighTemperatureThreshold {
    public ListChild listChild;
    public int highThreshold;

    public SetHighTemperatureThreshold(ListChild listChild, int highThreshold) {
      this.listChild = listChild;
      this.highThreshold = highThreshold;
    }
  }


  public static class SetTimeZone {
    public ListChild listChild;
    public String timezone;

    public SetTimeZone(ListChild listChild, String timezone) {
      this.listChild = listChild;
      this.timezone = timezone;
    }
  }

  public static class SetVolume {
    public ListChild listChild;
    public int volumeLevel;

    public SetVolume(ListChild listChild, int volumeLevel) {
      this.listChild = listChild;
      this.volumeLevel = volumeLevel;
    }
  }
}
