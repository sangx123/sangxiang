package com.nest.common;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.hubble.framework.service.Plugins.ThirdParty.Nest.NestPluginListener;
import com.hubble.framework.service.Plugins.ThirdParty.Nest.NestPluginManager;
import com.hubbleconnected.camera.R;
import com.nestlabs.sdk.GlobalUpdate;
import com.nestlabs.sdk.NestException;
import com.nestlabs.sdk.NestToken;

import static com.nest.common.Constants.CLIENT_ID;
import static com.nest.common.Constants.CLIENT_SECRET;
import static com.nest.common.Constants.REDIRECT_URL;

/**
 * Created by dasari on 09/01/17.
 */

public class GetStartedActivity extends AppCompatActivity {

    private static final String TAG = GetStartedActivity.class.getSimpleName();
    private static final int AUTH_TOKEN_REQUEST_CODE = 123;
    private NestPluginManager mNest;
    private NestToken mToken;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nest_get_started);
        mNest = NestPluginManager.getInstance();
        mToken = Settings.loadAuthToken(this);


        Button getStarted = (Button) findViewById(R.id.get_started);
        getStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNest.setConfig(CLIENT_ID, CLIENT_SECRET, REDIRECT_URL);
                mNest.launchAuthFlow(GetStartedActivity.this, AUTH_TOKEN_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (resultCode != RESULT_OK || requestCode != AUTH_TOKEN_REQUEST_CODE) {
            Log.e(TAG, "Finished with no result.");
            return;
        }

        mToken = NestPluginManager.getAccessTokenFromIntent(intent);
        if (mToken != null) {
            Settings.saveAuthToken(this, mToken);
            Log.v(TAG, "Token received successfully");
            setContentView(R.layout.nest_authorising_layout);
            authenticate(mToken);
        } else {
            Log.e(TAG, "Unable to resolve access token from payload.");
        }

    }

    /**
     * Authenticate with the Nest API and start listening for updates.
     *
     * @param token the token used to authenticate.
     */
    private void authenticate(NestToken token) {
        mNest.authWithToken(token, new NestPluginListener.AuthListener() {

            @Override
            public void onAuthSuccess() {
                Log.v(TAG, "Authentication succeeded.");
                setContentView(R.layout.nest_congratulations_layout);
                Button button = (Button) findViewById(R.id.congrats_get_started);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Launch nest dashboard
                        Intent intent = new Intent(GetStartedActivity.this, NestHomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
            }

            @Override
            public void onAuthFailure(NestException exception) {
                Log.e(TAG, "Authentication failed with error: " + exception.getMessage());
                Settings.saveAuthToken(GetStartedActivity.this, null);
                mNest.launchAuthFlow(GetStartedActivity.this, AUTH_TOKEN_REQUEST_CODE);
            }

            @Override
            public void onAuthRevoked() {
                Log.e(TAG, "Auth token was revoked!");
                Settings.saveAuthToken(GetStartedActivity.this, null);
                mNest.launchAuthFlow(GetStartedActivity.this, AUTH_TOKEN_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
