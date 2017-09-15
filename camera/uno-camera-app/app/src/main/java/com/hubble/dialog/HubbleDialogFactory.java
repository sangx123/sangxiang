package com.hubble.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

/**
 * Created by hoang on 10/5/15.
 */
public class HubbleDialogFactory {
    public static Dialog createAlertDialog(Context context, CharSequence msg,
                                           String posTxt, DialogInterface.OnClickListener onPosClickListener,
                                           String negTxt, DialogInterface.OnClickListener onNegClickListener,
                                           boolean isCancelable, boolean isCanceledOnTouchOutside) {
        AlertDialog alertDialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setCancelable(isCancelable);
        if (posTxt != null) {
            if (onPosClickListener != null) {
                builder.setPositiveButton(posTxt, onPosClickListener);
            } else {
                // set default onClickListener
                builder.setPositiveButton(posTxt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
        }
        if (negTxt != null) {
            if (onNegClickListener != null) {
                builder.setNegativeButton(negTxt, onNegClickListener);
            } else {
                // set default onClickListener
                builder.setNegativeButton(negTxt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
        }
        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside);
        return alertDialog;
    }

    public static Dialog createAlertDialog(Context context, CharSequence msg, View view,
                                           String posTxt, DialogInterface.OnClickListener onPosClickListener,
                                           String negTxt, DialogInterface.OnClickListener onNegClickListener,
                                           boolean isCancelable, boolean isCanceledOnTouchOutside) {
        AlertDialog alertDialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (msg != null) {
            builder.setMessage(msg);
        }
        if (view != null) {
            builder.setView(view);
        }
        builder.setCancelable(isCancelable);
        if (posTxt != null) {
            if (onPosClickListener != null) {
                builder.setPositiveButton(posTxt, onPosClickListener);
            } else {
                // set default onClickListener
                builder.setPositiveButton(posTxt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
        }
        if (negTxt != null) {
            if (onNegClickListener != null) {
                builder.setNegativeButton(negTxt, onNegClickListener);
            } else {
                // set default onClickListener
                builder.setNegativeButton(negTxt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
        }
        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside);
        return alertDialog;
    }

    public static Dialog createFullScreenAlertDialog(Context context, CharSequence msg, View view,
                                           String posTxt, DialogInterface.OnClickListener onPosClickListener,
                                           String negTxt, DialogInterface.OnClickListener onNegClickListener,
                                           boolean isCancelable, boolean isCanceledOnTouchOutside) {
        AlertDialog alertDialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        if (msg != null) {
            builder.setMessage(msg);
        }
        if (view != null) {
            builder.setView(view);
        }
        builder.setCancelable(isCancelable);
        if (posTxt != null) {
            if (onPosClickListener != null) {
                builder.setPositiveButton(posTxt, onPosClickListener);
            } else {
                // set default onClickListener
                builder.setPositiveButton(posTxt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
        }
        if (negTxt != null) {
            if (onNegClickListener != null) {
                builder.setNegativeButton(negTxt, onNegClickListener);
            } else {
                // set default onClickListener
                builder.setNegativeButton(negTxt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
        }
        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside);
        return alertDialog;
    }

    public static ProgressDialog createProgressDialog(Context context, CharSequence msg,
                                              boolean isCancelable, boolean isCanceledOnTouchOutside) {
        ProgressDialog progressDialog = null;
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(msg);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(isCancelable);
        progressDialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside);
        return progressDialog;
    }
}
