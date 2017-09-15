package com.sensor.helper;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.hubble.registration.PublicDefine;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

public class MqttHandler implements MqttCallback {

    private final static String TAG = MqttHandler.class.getName();
    private static MqttHandler instance;
    private MqttAndroidClient client;
    private Context context;
    private IWatcherMqttPublishAction publishAction;
    private String clientId;

    private MqttHandler(Context context) {
        this.context = context;
        this.client = null;
    }

    /**
     * @param context The application context for the object.
     * @return The MqttHandler object for the application.
     */
    public static MqttHandler getInstance(Context context) {
        Log.d(TAG, ".getInstance() entered");
        if (instance == null) {
            instance = new MqttHandler(context);
        }
        return instance;
    }

    public void setPublishAction(IWatcherMqttPublishAction publishAction){
        this.publishAction = publishAction;
    }

    /**
     * Connect MqttAndroidClient to the MQTT server
     */
    public void connect(String id) {
        Log.d(TAG, "MqrttHandler - connect");
        clientId = id;

        if (!isMqttConnected()) {
            String serverHost = PublicDefine.MQTT_HOST;
            String serverPort = PublicDefine.MQTT_PORT;
            String clientId = id;

            Log.d(TAG, "initMqttConnection() - Host name: " + serverHost + ", Port: " + serverPort
                    + ", client id: " + clientId);

            String connectionUri = "tcp://" + serverHost + ":" + serverPort;
            if (client != null) {
                client.unregisterResources();
                client = null;
            }
            client = new MqttAndroidClient(context, connectionUri, clientId);
            client.setCallback(this);

            // create ActionListener to handle connection results
            ActionListener listener = new ActionListener(context, ActionListener.ActionStateStatus.CONNECTING);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            try {
                client.connect(options, context, listener);
            } catch (MqttException e) {
                Log.e(TAG, "Exception caught while attempting to connect to server", e.getCause());
            }
        }
    }

    /**
     * Disconnect MqttAndroidClient from the MQTT server
     */
    public void disconnect() {
        Log.d(TAG, "MqttHandler - disconnect()");

        // check if client is actually connected
        if (isMqttConnected()) {
            ActionListener listener = new ActionListener(context, ActionListener.ActionStateStatus.DISCONNECTING);
            try {
                client.disconnect(context, listener);
            } catch (MqttException e) {
                Log.e(TAG, "Exception caught while attempting to disconnect from server", e.getCause());
            }
        }
    }

    /**
     * Subscribe MqttAndroidClient to a topic
     *
     * @param topic to subscribe to
     * @param qos   to subscribe with
     */
    public void subscribe(String topic, int qos) {
        Log.d(TAG, "MqttHandler - subscribe()");

        // check if client is connected
        if (isMqttConnected()) {
            try {
                // create ActionListener to handle subscription results
                ActionListener listener = new ActionListener(context, ActionListener.ActionStateStatus.SUBSCRIBE);
                Log.d(TAG, "subscribe() - Subscribing to: " + topic + ", with QoS: " + qos);
                client.subscribe(topic, qos, context, listener);
            } catch (MqttException e) {
                Log.e(TAG, "Exception caught while attempting to subscribe to topic " + topic, e.getCause());
            }
        } else {
            connectionLost(null);
        }
    }

    /**
     * Unsubscribe MqttAndroidClient from a topic
     *
     * @param topic to unsubscribe from
     */
    public void unsubscribe(String topic) {
        Log.d(TAG, "MqttHandler - unsubscribe()");

        // check if client is connected
        if (isMqttConnected()) {
            try {
                // create ActionListener to handle unsubscription results
                ActionListener listener = new ActionListener(context, ActionListener.ActionStateStatus.UNSUBSCRIBE);
                client.unsubscribe(topic, context, listener);
            } catch (MqttException e) {
                Log.e(TAG, "Exception caught while attempting to unsubscribe from topic " + topic, e.getCause());
            }
        } else {
            connectionLost(null);
        }
    }

    /**
     * Publish message to a topic
     *
     * @param topic    to publish the message to
     * @param message  JSON object representation as a string
     * @param retained true if retained flag is requred
     * @param qos      quality of service (0, 1, 2)
     */
    public void publish(final String topic, final String message, final boolean retained, final int qos) {
        Log.d(TAG, "MqttHandler - publish()");

        // check if client is connected
        if (isMqttConnected()) {
            // create a new MqttMessage from the message string
            MqttMessage mqttMsg = new MqttMessage(message.getBytes());
            // set retained flag
            mqttMsg.setRetained(retained);
            // set quality of service
            mqttMsg.setQos(qos);
            try {
                // create ActionListener to handle message published results
                ActionListener listener = new ActionListener(context, ActionListener.ActionStateStatus.PUBLISH, publishAction);
                Log.d(TAG, "publish() - Publishing " + message + " to: " + topic + ", with QoS: " + qos + " with retained flag set to " + retained);
                client.publish(topic, mqttMsg, context, listener);

            } catch (MqttPersistenceException e) {
                Log.e(TAG, "MqttPersistenceException caught while attempting to publish a message", e.getCause());
            } catch (MqttException e) {
                Log.e(TAG, "MqttException caught while attempting to publish a message", e.getCause());
            }
        } else {
            Log.e(TAG, "Mqtt - connectlost");
            connectionLost(null);
            connect(clientId);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    publish(topic, message, retained, qos);
                }
            }, 2500);
        }
    }

    /**
     * Handle loss of connection from the MQTT server.
     * @param throwable
     */
    @Override
    public void connectionLost(Throwable throwable) {
        Log.e(TAG, "MqttHandler - connectionLost()");

        if (throwable != null) {
            throwable.printStackTrace();
        }
    }

    /**
     * Process incoming messages to the MQTT client.
     *
     * @param topic       The topic the message was received on.
     * @param mqttMessage The message that was received
     * @throws Exception  Exception that is thrown if the message is to be rejected.
     */
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        Log.d(TAG, "MqttHandler - messageArrived()");
    }

    /**
     * Handle notification that message delivery completed successfully.
     *
     * @param iMqttDeliveryToken The token corresponding to the message which was delivered.
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        Log.d(TAG, "MqttHandler - deliveryComplete()");
    }

    /**
     * Checks if the MQTT client has an active connection
     *
     * @return True if client is connected, false if not.
     */
    private boolean isMqttConnected() {
        Log.d(TAG, "MqttHandler - isMqttConnected()");
        boolean connected = false;
        try {
            if ((client != null) && (client.isConnected())) {
                connected = true;
            }
        } catch (Exception e) {
            // swallowing the exception as it means the client is not connected
        }
        Log.d(TAG, "MqttHandler - isMqttConnected() - returning " + connected);
        return connected;
    }
}
