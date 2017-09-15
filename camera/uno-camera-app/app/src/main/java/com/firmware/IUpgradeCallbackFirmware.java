package com.firmware;

public interface IUpgradeCallbackFirmware {
  void onUpgradeSucceed ();

  void onUpgradeFail ();

  void onDownloadSucceed ();

  void onDownloadFailded ();
}