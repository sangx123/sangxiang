package com.sensor.helper;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

public class ActionListener implements IMqttActionListener {

    private final static String TAG = ActionListener.class.getName();
    private ActionStateStatus action;
    private Context context;
    public static boolean connect;
    private IWatcherMqttPublishAction publishAction;

    public enum ActionStateStatus {
        CONNECTING, DISCONNECTING, SUBSCRIBE, UNSUBSCRIBE, PUBLISH
    }

    public ActionListener(Context context, ActionStateStatus action) {
        this.context = context;
        this.action = action;
    }

    public ActionListener(Context context, ActionStateStatus action, IWatcherMqttPublishAction publishAction) {
        this.context = context;
        this.action = action;
        this.publishAction = publishAction;
    }

    /**
     * Determine the type of callback that completed successfully.
     * @param token The MQTT Token for the completed action.
     */
    @Override
    public void onSuccess(IMqttToken token) {
        Log.d(TAG, "onSuccess()");
        switch (action) {
            case CONNECTING:
                handleConnectSuccess();
                break;

            case SUBSCRIBE:
                handleSubscribeSuccess();
                break;

            case PUBLISH:
                publishAction.onCompletedMqttSendColor();
                handlePublishSuccess();
                break;

            case DISCONNECTING:
                handleDisconnectSuccess();
                break;

            default:
                break;
        }
    }

    /**
     * Determine the type of callback that failed.
     * @param token The MQTT Token for the completed action.
     * @param throwable The exception corresponding to the failure.
     */
    @Override
    public void onFailure(IMqttToken token, Throwable throwable) {
        Log.e(TAG, "onFailure()");
        switch (action) {
            case CONNECTING:
                handleConnectFailure(throwable);
                break;

            case SUBSCRIBE:
                handleSubscribeFailure(throwable);
                break;

            case PUBLISH:
                publishAction.onFailMqttSendColor();
                handlePublishFailure(throwable);
                break;

            case DISCONNECTING:
                handleDisconnectFailure(throwable);
                break;

            default:
                break;
        }
    }

    /**
     * Called on successful connection to the MQTT broker.
     */
    private void handleConnectSuccess() {
        Log.d(TAG, "handleConnectSuccess()");
    }

    /**
     * Called on successful subscription to the MQTT topic.
     */
    private void handleSubscribeSuccess() {
        Log.d(TAG, "handleSubscribeSuccess()");
    }

    /**
     * Called on successful publish to the MQTT topic.
     */
    private void handlePublishSuccess() {
        Log.d(TAG, "handlePublishSuccess()");
    }

    /**
     * Called on successful disconnect from the MQTT server.
     */
    private void handleDisconnectSuccess() {
        Log.d(TAG, "handleDisconnectSuccess()");
    }

    /**
     * Called on failure to connect to the MQTT server.
     * @param throwable The exception corresponding to the failure.
     */
    private void handleConnectFailure(Throwable throwable) {
        Log.e(TAG, "handleConnectFailure()");
        Log.e(TAG, "handleConnectFailure() - Failed with exception", throwable.getCause());
        throwable.printStackTrace();
    }

    /**
     * Called on failure to subscribe to the MQTT topic.
     * @param throwable The exception corresponding to the failure.
     */
    private void handleSubscribeFailure(Throwable throwable) {
        Log.e(TAG, "handleSubscribeFailure()");
        Log.e(TAG, "handleSubscribeFailure() - Failed with exception", throwable.getCause());
    }

    /**
     * Called on failure to publish to the MQTT topic.
     * @param throwable The exception corresponding to the failure.
     */
    private void handlePublishFailure(Throwable throwable) {
        Log.e(TAG, ".handlePublishFailure() entered");
        Log.e(TAG, ".handlePublishFailure() - Failed with exception", throwable.getCause());
    }

    /**
     * Called on failure to disconnect from the MQTT server.
     * @param throwable The exception corresponding to the failure.
     */
    private void handleDisconnectFailure(Throwable throwable) {
        Log.e(TAG, "handleDisconnectFailure()");
        Log.e(TAG, "handleDisconnectFailure() - Failed with exception", throwable.getCause());
    }

}
