package com.nxcomm.blinkhd.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.hubbleconnected.camera.R;

import java.util.regex.Pattern;

public class ChangePasswordDialog extends com.hubble.registration.ui.CommonDialog {
  private static final String PASSWORD_PATTERN = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z]).{8,30})";

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    // Get the layout inflater
    LayoutInflater inflater = getActivity().getLayoutInflater();

    contentView = inflater.inflate(R.layout.dialog_change_password, null);
    final EditText usrPwdField = (EditText) contentView.findViewById(R.id.txtNewPassword);
    final EditText usrConfirmPwdField = (EditText) contentView.findViewById(R.id.txtConfirmNewPassword);
    final ImageView ivPasswordStrength = (ImageView) contentView.findViewById(R.id.change_password_imageView_passwordStrength);
    final ImageView ivConfirmPasswordStrength = (ImageView) contentView.findViewById(R.id.change_password_imageView_confirmPasswordStrength);
    final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    usrPwdField.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        String currentPassword = s.toString();

        if (pattern.matcher(currentPassword).matches()) {
          ivPasswordStrength.setImageResource(R.drawable.setup_check_big);
          ivPasswordStrength.setTag(R.drawable.setup_check_big);
        } else {
          ivPasswordStrength.setImageResource(R.drawable.question_mark);
          ivPasswordStrength.setTag(R.drawable.question_mark);
        }

        if (currentPassword.equals(usrConfirmPwdField.getText().toString()) && ivPasswordStrength.getTag().equals(R.drawable.setup_check_big)) {
          ivConfirmPasswordStrength.setImageResource(R.drawable.setup_check_big);
        } else {
          ivConfirmPasswordStrength.setImageResource(R.drawable.question_mark);
        }
      }

      @Override
      public void afterTextChanged(Editable s) {

      }
    });
    ivPasswordStrength.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.password_requirement));
        builder
            .setMessage(getString(R.string.pass_requirements_toast))
            .setCancelable(true)
            .setIcon(R.drawable.ic_launcher)
            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
              }
            });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
      }
    });

    ivConfirmPasswordStrength.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.confirm_password));
        builder
            .setMessage(getString(R.string.confirm_password_text))
            .setCancelable(true)
            .setIcon(R.drawable.ic_launcher)
            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
              }
            });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
      }
    });

    usrConfirmPwdField.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        String currentPassword = usrPwdField.getText().toString();
        String currentConfirmPassword = s.toString();

        if (currentPassword.equals(currentConfirmPassword) && ivPasswordStrength.getTag().equals(R.drawable.setup_check_big)) {
          ivConfirmPasswordStrength.setImageResource(R.drawable.setup_check_big);
        } else {
          ivConfirmPasswordStrength.setImageResource(R.drawable.question_mark);
        }
      }

      @Override
      public void afterTextChanged(Editable s) {

      }
    });

    builder.setView(contentView);
    builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            if (commonDialogListener != null) {
              commonDialogListener.onDialogPositiveClick(ChangePasswordDialog.this);
            }
          }
        }
    ).setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            ChangePasswordDialog.this.getDialog().cancel();
          }
        }
    );
    return builder.create();
  }
}
