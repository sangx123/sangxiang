package com.hubble.registration.models;

import java.util.BitSet;


public class NameAndSecurity extends ApScanBase {
  private String capability;
  private boolean isChecked;
  private int level;
  private boolean showSecurity;
  private boolean hideMac;

  public String name;

  /*
   * Can be following values:
   * "OPEN", "WEP", "WAP", "WAP2"
   */
  public String security;
  public String BSSID;
  public int frequency;

  public NameAndSecurity(String network_name_no_quote, String s, String bssid) {
    super("wifi");
    name = network_name_no_quote;
    capability = s;
    security = getScanResultSecurity(capability);
    BSSID = bssid;
    isChecked = false;
    showSecurity = true;
    hideMac = false;
    frequency = -1;
  }

  public int getFrequency() {
    return frequency;
  }

  public void setFrequency(int frequency) {
    this.frequency = frequency;
  }

  public boolean isHideMac() {
    return hideMac;
  }

  public void setHideMac(boolean hideMac) {
    this.hideMac = hideMac;
  }

  public boolean isShowSecurity() {
    return showSecurity;
  }

  public void setShowSecurity(boolean showSecurity) {
    this.showSecurity = showSecurity;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public int getLevel() {
    return level;
  }

  public void setChecked(boolean ch) {
    isChecked = ch;
  }

  public boolean isChecked() {
    return isChecked;
  }

  public String toString() {
    String mac = "\n\t" + BSSID;
    if (hideMac) {
      mac = "";
    }
    String security_ = "(" + security + ")";

    if (!showSecurity) {
      security_ = "";
    }
    String freq = " (" + frequency + " MHZ)";
    if (frequency == -1) {
      freq = "";
    }

    return name + security_ + freq + mac;
  }

  private String getScanResultSecurity(String cap) {
    final String[] securityModes = {"WEP", "WPA", "WPA2"};
    for (int i = securityModes.length - 1; i >= 0; i--) {
      if (cap.contains(securityModes[i])) {
        return securityModes[i];
      }
    }

    return "OPEN";
  }

	/* Parse capability String for security mode */
  /* AuthAlgorithms ,
   * GroupCiphers,
	 * KeyManagement, 
	 * PairwiseCiphers
	 * Protocols
	 * */

  public BitSet getAuthAlgorithm() {
    BitSet bs = new BitSet(3);
    bs.clear();

    String[] modes = {"OPEN", "SHARED", "LEAP"};
    for (int i = modes.length - 1; i >= 0; i--) {
      if (capability.contains(modes[i])) {
        bs.set(i);
      }
    }

    if (capability.contains("WEP")) {
      bs.set(0);
      bs.set(1);
    }
    return bs;
  }

  public BitSet getGroupCiphers() {
    BitSet bs = new BitSet(4);
    bs.clear();
    //String[] modes = { "WEP40", "WEP104","TKIP","CCMP"};
    String[] modes = {"WEP", "WEP", "TKIP", "CCMP"};
    for (int i = modes.length - 1; i >= 0; i--) {
      if (capability.contains(modes[i])) {
        bs.set(i);
      }
    }
    return bs;
  }


  public BitSet getProtocols() {
    BitSet bs = new BitSet(2);
    bs.clear();
    String[] modes = {"WPA", "RSN"};
    for (int i = modes.length - 1; i >= 0; i--) {
      if (capability.contains(modes[i])) {
        bs.set(i);
      }
    }

    if (capability.contains("WPA")) {
      bs.set(1);//add "RSN"
    }

    return bs;
  }

  public BitSet getPairWiseCiphers() {
    BitSet bs = new BitSet(3);
    bs.clear();
    String[] modes = {"NONE", "TKIP", "CCMP"};
    for (int i = modes.length - 1; i >= 0; i--) {
      if (capability.contains(modes[i])) {
        bs.set(i);
      }
    }

    return bs;
  }


  public BitSet getKeyManagement() {
    BitSet bs = new BitSet(4);
    bs.clear();
    String[] modes = {"NONE", "PSK", "EAP", "IEEE8021X"};
    for (int i = modes.length - 1; i >= 0; i--) {
      if (capability.contains(modes[i])) {
        bs.set(i);
      }
    }


    if ((capability.contains("WEP")) || (security.equals("OPEN"))) {
      bs.set(0);
    }

    return bs;
  }

  public String log() {
    StringBuilder sb = new StringBuilder();
    sb.append("------------------------");
    sb.append("capability = " + capability + "\n");
    sb.append("level = " + level + "\n");
    sb.append("showSecurity = " + showSecurity + "\n");
    sb.append("hideMac = " + hideMac + "\n");
    sb.append("name = " + name + "\n");
    sb.append("security = " + security + "\n");
    sb.append("BSSID = " + BSSID + "\n");
    sb.append("frequency = " + frequency);
    return sb.toString();
  }

}