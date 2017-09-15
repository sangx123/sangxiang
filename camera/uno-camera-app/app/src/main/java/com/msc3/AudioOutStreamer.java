package com.msc3;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.blinkhd.TalkbackFragment;
import com.hubble.HubbleApplication;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.devcomm.impl.hubble.P2pCommunicationManager;
import com.hubble.framework.service.p2p.P2pService;
import com.hubble.tls.LocalDevice;
import com.nxcomm.jstun_android.P2pClient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import base.hubble.PublicDefineGlob;

public class AudioOutStreamer implements Runnable {
    private static final String TAG                        = "mbp";
    public static final  int    UNMUTE_CAMERA_AUDIO_FAILED = 0xde000007;

    private Socket      audioOutSock;
    private String      addr;
    private String      http_pass;
    private int         cmd_port;
    private int         audio_port;
    private PCMRecorder recorder;
    private boolean     streaming;
    private boolean     hasSetDeviceAudioOn;
    private boolean isInLocal   = false;
    private String  session_key = null;
    private String  stream_id   = null;

    //DBG
    private String           filename;
    private FileOutputStream os;
    private boolean debug_stream_to_file = false;
    private String  registrationId;
    private String appUuid;
    private Handler mHandler;

    /* @ip should be in the form x.y.z.t */
    public AudioOutStreamer(PCMRecorder recorder, String ip, int cmd_port, String http_pass, int audio_port, Handler errorCallback) {
    /* keep it for future reference */
        addr = ip;
        this.cmd_port = cmd_port;
        this.audio_port = audio_port;
        audioOutSock = null;
        this.recorder = recorder;
        streaming = false;
        hasSetDeviceAudioOn = false;
        appUuid = AppUUID.getAppUuid();
        this.http_pass = http_pass;
        if (http_pass == null) {
            this.http_pass = "";
        }

        mHandler = errorCallback;

        if (debug_stream_to_file) {
            String path = Environment.getExternalStorageDirectory().getPath() + File.separator;
            filename = path + "talkback.pcm";
            // // Log.d("mbp", "Talkback data is " + filename);
        }
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public int getAudioPort() {
        return audio_port;
    }

    public void setAudioPort(int audio_port) {
        this.audio_port = audio_port;
    }

    public void setLocalMode(boolean isInLocal) {
        this.isInLocal = isInLocal;
    }

    public boolean isLocalMode() {
        return isInLocal;
    }

    public void setSessionKey(String session_key) {
        this.session_key = session_key;
    }

    public void setStreamId(String stream_id) {
        this.stream_id = stream_id;
    }

    public void startStreaming() {
        streaming = true;
    }

    public void stopStreaming() {
        streaming = false;
    }

    public void run() {
        // run_udp();

        if (debug_stream_to_file) {
            initWriteTofile();
        }

        run_tcp();
    }

//    private int tryConnect() {
//        URL               url;
//        HttpURLConnection conn;
//
//        String usr_pass = String.format("%s:%s", PublicDefineGlob.DEFAULT_BASIC_AUTH_USR, http_pass);
//
//        try {
//            if (!isInLocal) {
//                if (P2pCommunicationManager.getInstance().isP2pCommunicationAvailable()) {
//                    Log.d(TAG, "P2p streaming is available, enable talkback via p2p");
//                    mHandler.sendMessage(Message.obtain(mHandler, TalkbackFragment.MSG_AUDIO_STREAM_HANDSHAKE_SUCCESS));
//                } else {
//                    Log.d(TAG, "Start handshaking...");
//                    if (!doHandShake()) {
//                        Log.d(TAG, "Remote talkback handshake failed");
//                        mHandler.sendMessage(Message.obtain(mHandler, TalkbackFragment.MSG_AUDIO_STREAM_HANDSHAKE_FAILED));
//                        return -1;
//                    } else {
//                        Log.d(TAG, "Remote talkback handshake success");
//                        mHandler.sendMessage(Message.obtain(mHandler, TalkbackFragment.MSG_AUDIO_STREAM_HANDSHAKE_SUCCESS));
//                    }
//                }
//            } else {
//                Log.d(TAG, "Sending talkback on cmd: " + PublicDefineGlob.SET_DEVICE_AUDIO_ON);
//                url = new URL("http://" + addr + ":" + cmd_port + PublicDefineGlob.HTTP_CMD_PART + PublicDefineGlob.SET_DEVICE_AUDIO_ON);
//                conn = (HttpURLConnection) url.openConnection();
//                conn.addRequestProperty("Authorization", "Basic " + Base64.encodeToString(usr_pass.getBytes("UTF-8"), Base64.NO_WRAP));
//                conn.connect();
//                conn.getContentType();
//                // // // Log.d(TAG,"responseCode:"+ responseCode);
//                audioOutSock = new Socket();
//                audioOutSock.connect(new InetSocketAddress(addr, audio_port), 10000);
//
//                hasSetDeviceAudioOn = true;
//                Log.d(TAG, "Sending talkback on DONE");
//                mHandler.sendMessage(Message.obtain(mHandler, TalkbackFragment.MSG_START_LOCAL_TALKBACK_SUCCESS));
//            }
//        }
//        catch (IOException e) {
//            Log.e(TAG, Log.getStackTraceString(e));
//            Log.d(TAG, "Start local talkback failed.");
//            mHandler.sendMessage(Message.obtain(mHandler, TalkbackFragment.MSG_START_LOCAL_TALKBACK_FAILED));
//            return -1;
//        }
//
//        return 0;
//    }

    private int tryConnect() {
        if (shouldUseP2PTalkback()) {
            Log.d(TAG, "Force local talkback via p2p");
            // Check P2P client whether it's ready
            if (P2pCommunicationManager.getInstance().isP2pCommunicationAvailable()) {
                TalkbackState talkbackState = checkTalkbackStatus();
                Log.d(TAG, "P2p streaming is available, enable talkback via p2p, busy? " + talkbackState);
                if (talkbackState == TalkbackState.BUSY) {
                    mHandler.sendMessage(Message.obtain(mHandler, TalkbackFragment.MSG_START_P2P_TALKBACK_BUSY));
                    return -1;
                } else if (talkbackState == TalkbackState.READY) {
                    if (isInLocal) {
                        mHandler.sendMessage(Message.obtain(mHandler, TalkbackFragment.MSG_AUDIO_STREAM_HANDSHAKE_SUCCESS));
                        mHandler.sendMessage(Message.obtain(mHandler, TalkbackFragment.MSG_START_LOCAL_TALKBACK_SUCCESS));
                    } else {
                        mHandler.sendMessage(Message.obtain(mHandler, TalkbackFragment.MSG_START_P2P_TALKBACK_SUCCESS));
                    }
                } else {
                    mHandler.sendMessage(Message.obtain(mHandler, TalkbackFragment.MSG_START_P2P_TALKBACK_FAILED));
                    return -1;
                }
            } else {
                Log.d(TAG, "P2p streaming is not available, can't start talkback via p2p. Try with other method");
                Integer ret = tryConnectForNoForceTalkbackViaP2p();
                if (ret != null) return ret;
            }
        } else {
            Integer ret = tryConnectForNoForceTalkbackViaP2p();
            if (ret != null) return ret;
        }

        return 0;
    }

    @Nullable
    private Integer tryConnectForNoForceTalkbackViaP2p() {
        try {
            if (!isInLocal) {
                if (P2pCommunicationManager.getInstance().isP2pCommunicationAvailable()) {
                    Log.d(TAG, "P2p streaming is available, enable talkback via p2p");
                    mHandler.sendMessage(Message.obtain(mHandler, TalkbackFragment.MSG_AUDIO_STREAM_HANDSHAKE_SUCCESS));
                } else {
                    Log.d(TAG, "Start handshaking...");
                    if (!doHandShake()) {
                        Log.d(TAG, "Remote talkback handshake failed");
                        mHandler.sendMessage(Message.obtain(mHandler, TalkbackFragment.MSG_AUDIO_STREAM_HANDSHAKE_FAILED));
                        return -1;
                    } else {
                        Log.d(TAG, "Remote talkback handshake success");
                        mHandler.sendMessage(Message.obtain(mHandler, TalkbackFragment.MSG_AUDIO_STREAM_HANDSHAKE_SUCCESS));
                    }
                }
            } else {
                Log.d(TAG, "Sending talkback on cmd: " + PublicDefineGlob.SET_DEVICE_AUDIO_ON);
                String talkbackOnCommand = getExtendedTalkbackCommand(PublicDefineGlob.SET_DEVICE_AUDIO_ON, appUuid);
                String res = getLocalDevice().sendCommandAndGetResponse(PublicDefineGlob.BM_HTTP_CMD_PART + talkbackOnCommand);
                Log.d(TAG, "Sending talkback on DONE: " + res);
                int ret;
                if ("audio_out1: 0".equals(res)) {
                    ret = 0;
                    audioOutSock = new Socket();
                    audioOutSock.connect(new InetSocketAddress(addr, audio_port), 10000);
                    hasSetDeviceAudioOn = true;
                    mHandler.sendMessage(Message.obtain(mHandler, TalkbackFragment.MSG_START_LOCAL_TALKBACK_SUCCESS));
                } else {
                    ret = -1;
                    hasSetDeviceAudioOn = false;
                    mHandler.sendMessage(Message.obtain(mHandler, TalkbackFragment.MSG_START_LOCAL_TALKBACK_FAILED));
                }
                return ret;
            }

        }
        catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            Log.d(TAG, "Start local talkback failed.");
            mHandler.sendMessage(Message.obtain(mHandler, TalkbackFragment.MSG_START_LOCAL_TALKBACK_FAILED));
            return -1;
        }

        return null;
    }

    private TalkbackState checkTalkbackStatus() {
        TalkbackState talkbackState = TalkbackState.READY;
        String        response      = null;
        // 20170118 HOANG REL-289 Check whether P2P talk back ready
        int    retries           = 3;
        String talkbackOnCommand = getExtendedTalkbackCommand(PublicDefineGlob.SET_DEVICE_AUDIO_ON, appUuid);
        while (retries-- > 0) {
            if (isInLocal) {
                response = getLocalDevice().sendCommandAndGetResponse(PublicDefineGlob.BM_HTTP_CMD_PART + talkbackOnCommand);
            } else {
                response = P2pCommunicationManager.getInstance().sendCommand(talkbackOnCommand);
            }

            if (response != null) {
                talkbackState = parseTalkbackStatus(response);
                if (talkbackState == TalkbackState.BUSY || talkbackState == TalkbackState.READY)
                    break;
            } else {
                talkbackState = TalkbackState.FAILED;
            }

            try {
                Thread.sleep(3000);
            }
            catch (InterruptedException e) {
            }
        }
        return talkbackState;
    }

    public void run_tcp() {
        // OutputStream outStream= null;
        DataOutputStream outStream   = null;
        int              data_len    = 4 * 1024;
        byte[]           data_out    = new byte[data_len];
        int              actual_read = -1;

        Device      device            = DeviceSingleton.getInstance().getDeviceByRegId(registrationId);
        P2pClient currentP2pClient = P2pService.getP2pClient(registrationId);
        do {
      /* make sure we have some data before opening the connection */
            actual_read = recorder.readFromAudioBuffer(data_out, data_len);
            try {
                Thread.sleep(125);// 125
            }
            catch (InterruptedException e) {
                // // Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        while (actual_read == -1);

        if (tryConnect() != -1) {
            try {
                if (debug_stream_to_file) {
                    //DBG write pcm data to file
                    writeAudioDataToFile(data_out, 0, actual_read);
                }
                if (device.getProfile().shouldUseP2PTalkbackInLocal()) {
                    if (currentP2pClient != null && currentP2pClient.isValid()) {
                        Log.w(TAG, "Send talkback data " + data_out.length + ", actual read " + actual_read);
                        currentP2pClient.getRmcChannel().sendTalkbackData(data_out, 0, actual_read);
                    } else {
                        Log.e(TAG, "Can't talkback, P2P client is not available");
                        if (mHandler != null) {
                            mHandler.sendMessage(Message.obtain(mHandler,
                                TalkbackFragment.MSG_SEND_AUDIO_DATA_TO_SOCKET_FAILED));
                        }
                        streaming = false;
                    }
                } else {
                    // For local, don't use talkback via p2p
                    if (isInLocal == true) {
                        outStream = new DataOutputStream(audioOutSock.getOutputStream());
                        if (outStream != null) {
                            outStream.write(data_out, 0, actual_read);
                        }
                    } else {
                        if (P2pCommunicationManager.getInstance().isP2pCommunicationAvailable()) {
                            P2pCommunicationManager.getInstance().sendTalkbackData(data_out, 0, actual_read);
                        } else {
                            // outStream = new
                            // BufferedOutputStream(audioOutSock.getOutputStream());
                            outStream = new DataOutputStream(audioOutSock.getOutputStream());
                            if (outStream != null) {
                                outStream.write(data_out, 0, actual_read);
                            }
                        }
                    }
                }
            }
            catch (IOException e) {

                // // Log.e(TAG, Log.getStackTraceString(e));
            }

            while (streaming) {

                try {
                    actual_read = recorder.readFromAudioBuffer(data_out, data_len);
                    if (actual_read != -1) {
                        if (debug_stream_to_file) {
                            //DBG write pcm data to file
                            writeAudioDataToFile(data_out, 0, actual_read);
                        }
                        if (device.getProfile().shouldUseP2PTalkbackInLocal()) {
                            if (currentP2pClient != null && currentP2pClient.isValid()) {
                                currentP2pClient.getRmcChannel().sendTalkbackData(data_out, 0, actual_read);
                            } else {
                                Log.d(TAG, "Can't talkback, P2P client is not available");
                                if (mHandler != null) {
                                    mHandler.sendMessage(Message.obtain(mHandler,
                                        TalkbackFragment.MSG_SEND_AUDIO_DATA_TO_SOCKET_FAILED));
                                }
                                streaming = false;
                            }
                        } else {
                            // For local, don't use talkback via p2p
                            if (isInLocal == true) {
                                if (outStream != null) {
                                    outStream.write(data_out, 0, actual_read);
                                }
                            } else {
                                if (P2pCommunicationManager.getInstance().isP2pCommunicationAvailable()) {
                                    P2pCommunicationManager.getInstance().sendTalkbackData(data_out, 0, actual_read);
                                } else {
                                    if (outStream != null) {
                                        outStream.write(data_out, 0, actual_read);
                                    }
                                }
                            }
                        }
                    }
                }
                catch (SocketException e1) {
                    Log.e(TAG, Log.getStackTraceString(e1));
                    Log.d(TAG, "Send audio data to socket failed");
                    if (mHandler != null) {
                        mHandler.sendMessage(Message.obtain(mHandler,
                            TalkbackFragment.MSG_SEND_AUDIO_DATA_TO_SOCKET_FAILED));
                    }
                    streaming = false;
                }
                catch (IOException e1) {
                    Log.e(TAG, Log.getStackTraceString(e1));
                }

                try {
                    Thread.sleep(125);// 125
                }
                catch (InterruptedException e) {
                    // // Log.e(TAG, Log.getStackTraceString(e));
                }
            }

            if (!streaming) // stopped by releasing PTT button
            {
        /* try to push all the recorded data out */
                // // Log.d(TAG, "Pushing out last bit of audio");
                do {
                    try {
                        actual_read = recorder.readFromAudioBuffer(data_out, data_len);
                        if (actual_read != -1) {
                            if (debug_stream_to_file) {
                                //DBG write pcm data to file
                                writeAudioDataToFile(data_out, 0, actual_read);
                            }

                            // For local, don't use talkback via p2p
                            if (isInLocal == true) {
                                if (outStream != null) {
                                    outStream.write(data_out, 0, actual_read);
                                }
                            } else {
                                if (P2pCommunicationManager.getInstance().isP2pCommunicationAvailable()) {
                                    P2pCommunicationManager.getInstance().sendTalkbackData(data_out, 0, actual_read);
                                } else {
                                    if (outStream != null) {
                                        outStream.write(data_out, 0, actual_read);
                                    }
                                }
                            }
                        }
                    }
                    catch (IOException e1) {
                        // // Log.e(TAG, Log.getStackTraceString(e1));
                    }
                    // } while (actual_read != -1);
                }
                while (actual_read != -1);

                // // Log.d(TAG, "Finish flushing audio hardware buffer");
                recorder.releaseRecorder();
            }

            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e2) {

                // // Log.e(TAG, Log.getStackTraceString(e2));
            }

            // Can't disable talkback, camera audio may be muted, please
            // activate talkback again

            if (outStream != null) {
                try {
                    outStream.flush();
                    outStream.close();
                }
                catch (IOException e) {
                    // // Log.e(TAG, Log.getStackTraceString(e));
                }
            }

            if (audioOutSock != null) {
                try {
                    audioOutSock.close();
                }
                catch (IOException e1) {
                    // // Log.e(TAG, Log.getStackTraceString(e1));
                }
            }

            if (debug_stream_to_file) {
                stopWriteTofile();
            }

      /* send audio_out0 cmd */
            HttpURLConnection conn;
            String            usr_pass = String.format("%s:%s", PublicDefineGlob.DEFAULT_BASIC_AUTH_USR, http_pass);

            if (!isInLocal) {
                // Don't need to send stop talkback cmd
            } else {

                try {
                    Log.d(TAG, "Sending talkback off cmd: " + PublicDefineGlob.SET_DEVICE_AUDIO_OFF);
                    URL url = new URL("http://" + addr + ":" + cmd_port + PublicDefineGlob.HTTP_CMD_PART + PublicDefineGlob.SET_DEVICE_AUDIO_OFF);
                    //					// // Log.d(TAG, "AudioOff cmd: "+
                    //							"http://"+ addr+":" +cmd_port+
                    //							PublicDefine.HTTP_CMD_PART+
                    //							PublicDefine.SET_DEVICE_AUDIO_OFF);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.addRequestProperty("Authorization", "Basic " + Base64.encodeToString(usr_pass.getBytes("UTF-8"), Base64.NO_WRAP)
                    );
                    conn.connect();
                    conn.getContentType();
                    // // // Log.d(TAG,"responseCode:"+ responseCode);
                    Log.d(TAG, "Sending talkback off cmd DONE");
                    hasSetDeviceAudioOn = false;
                }
                catch (SocketTimeoutException ste) {
                    // // Log.e(TAG, Log.getStackTraceString(ste));
                    // Timeout ;
                    // // Log.d(TAG, "Socket Timeout -- send error now ");
                    mHandler.sendMessage(Message.obtain(mHandler, UNMUTE_CAMERA_AUDIO_FAILED));
                }
                catch (IOException e) {
                    // // Log.e(TAG, Log.getStackTraceString(e));
                }

            }

        } else {
            if (hasSetDeviceAudioOn) {
                HttpURLConnection conn;
                String            usr_pass = String.format("%s:%s", PublicDefineGlob.DEFAULT_BASIC_AUTH_USR, http_pass);
                try {
                    Log.d(TAG, "Sending talkback off cmd: " + PublicDefineGlob.SET_DEVICE_AUDIO_OFF);
                    URL url = new URL("http://" + addr + ":" + cmd_port + PublicDefineGlob.HTTP_CMD_PART + PublicDefineGlob.SET_DEVICE_AUDIO_OFF);
                    //					// // Log.d(TAG, "AudioOff cmd: "+
                    //							"http://"+ addr+":" +cmd_port+
                    //							PublicDefine.HTTP_CMD_PART+
                    //							PublicDefine.SET_DEVICE_AUDIO_OFF);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.addRequestProperty("Authorization", "Basic " + Base64.encodeToString(usr_pass.getBytes("UTF-8"), Base64.NO_WRAP)
                    );
                    conn.connect();
                    conn.getContentType();
                    Log.d(TAG, "Sending talkback off cmd DONE");
                    hasSetDeviceAudioOn = false;
                }
                catch (SocketTimeoutException ste) {
                    Log.e(TAG, Log.getStackTraceString(ste));
                    // Timeout ;
                    // // Log.d(TAG, "Socket Timeout -- send error now ");
                    mHandler.sendMessage(Message.obtain(mHandler, UNMUTE_CAMERA_AUDIO_FAILED));

                }
                catch (IOException e) {
                    // // Log.e(TAG, Log.getStackTraceString(e));
                }
            }

            recorder.stopRecording();
      /* try to push all the recorded data out */
            // // Log.d(TAG, "Pushing out last bit of audio");
            do {
                actual_read = recorder.readFromAudioBuffer(data_out, data_len);
            }
            while (actual_read != -1);

            // // Log.d(TAG, "Finish flushing audio hardware buffer");
            recorder.releaseRecorder();
        }

    }

    public boolean doHandShake() {
        boolean result                   = false;
        byte[]  handShakeSuccessResponse = {01, 07, 00, 00, 00, 00, 00};
        int     timeout                  = 10 * 1000;
        byte    ty                       = 1;
        byte    lengt                    = 79;
        byte[]  header                   = new byte[3];
        int     handshakestrlen;

        byte[] sendData = new byte[79];

        header[0] = ty;
        header[1] = lengt;
        header[2] = 0;
        String handshakeRequest = stream_id + session_key;

        audioOutSock = new Socket();
        try {
            SocketAddress serverAddr = new InetSocketAddress(InetAddress.getByName(addr), audio_port);

            audioOutSock.connect(serverAddr, timeout);
            audioOutSock.setSoTimeout(5000);

            BufferedOutputStream out = new BufferedOutputStream(audioOutSock.getOutputStream());
            BufferedInputStream  in  = new BufferedInputStream(audioOutSock.getInputStream());
            System.arraycopy(header, 0, sendData, 0, 3);
            System.arraycopy(handshakeRequest.getBytes(), 0, sendData, 3, 76);
            out.write(sendData, 0, sendData.length);
            out.flush();

            byte[] data = new byte[7];
            do {
                try {
                    handshakestrlen = in.read(data);
                    new String(data, 0, handshakestrlen, "UTF-8");
                }
                catch (Exception e) {
                    // // Log.e(TAG, Log.getStackTraceString(e));
                }
                break;
            }
            while (in.available() != 0);

            if (Arrays.equals(handShakeSuccessResponse, data)) {
                result = true;
            }
        }
        catch (Exception ex) {
            result = false;
            if (recorder != null) {
                recorder.stopRecording();
                recorder.stopStreaming();
            }
        }

        return result;
    }

    /**
     * use UDP **
     */
    public void run_udp() {
        int            data_len    = 2 * 1024;
        byte[]         data_out    = new byte[data_len];
        int            actual_read = -1;
        DatagramPacket packet      = null;
    /* Create new UDP-Socket */
        DatagramSocket socket = null;

    /*
     * Test addr = "192.168.2.102"; port = 51110;
     */
        URLConnection conn;
        URL           url;
        try {
            url = new URL("http://" + addr + PublicDefineGlob.HTTP_CMD_PART + PublicDefineGlob.SET_DEVICE_AUDIO_OFF);
            conn = url.openConnection();
            conn.getContentType();
        }
        catch (IOException e) {
            // // Log.e(TAG, Log.getStackTraceString(e));
        }

        try {
            socket = new DatagramSocket();
        }
        catch (SocketException e2) {

            // // Log.e(TAG, Log.getStackTraceString(e2));
        }

        do {

      /* make sure we have some data before opening the connection */
            actual_read = recorder.readFromAudioBuffer(data_out, data_len);
            try {
                Thread.sleep(125);// 125
            }
            catch (InterruptedException e) {
                // // Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        while (actual_read == -1);

        if (socket != null) {
            try {
        /*
         * Create UDP-packet with data & destination(url+port)
         */
                packet = new DatagramPacket(data_out, actual_read, InetAddress.getByName(addr), cmd_port);

          /* Send out the packet */
                socket.send(packet);
            }
            catch (IOException e) {

                // // Log.e(TAG, Log.getStackTraceString(e));
            }

            while (streaming) {

                try {
                    actual_read = recorder.readFromAudioBuffer(data_out, data_len);
                    if (actual_read != -1) {
                        assert packet != null;
                        packet.setData(data_out, 0, actual_read);
            /* Send out the packet */
                        socket.send(packet);
                    }
                }
                catch (SocketException e1) {
                    // // Log.e(TAG, Log.getStackTraceString(e1));
                    streaming = false;
                }
                catch (IOException e1) {
                    // // Log.e(TAG, Log.getStackTraceString(e1));
                }

                try {
                    Thread.sleep(125);// 125
                }
                catch (InterruptedException e) {
                    // // Log.e(TAG, Log.getStackTraceString(e));
                }
            }

      /* send audio_out0 cmd */
            try {
                url = new URL("http://" + addr + PublicDefineGlob.HTTP_CMD_PART + PublicDefineGlob.SET_DEVICE_AUDIO_ON);
                conn = url.openConnection();
                conn.getContentType();
            }
            catch (IOException e) {
                // // Log.e(TAG, Log.getStackTraceString(e));
            }

            socket.close();
        }

    }


    private void initWriteTofile() {
        // // Log.d("mbp", "Talkback file is: " + filename);
        os = null;
        try {
            os = new FileOutputStream(filename);
        }
        catch (FileNotFoundException e) {

            // // Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private void stopWriteTofile() {
        if (os != null) {
            try {
                os.close();
            }
            catch (IOException e) {
                // // Log.e(TAG, Log.getStackTraceString(e));
            }
        }
    }

    private void writeAudioDataToFile(byte[] pcm, int offset, int pcm_len) {

        if (os == null) {
            // // Log.e("mbp", "ERROR NO OUTPUT STREAM");
            return;
        }

        try {
            os.write(pcm, offset, pcm_len);
        }
        catch (IOException e) {
            // // Log.e("mbp", "ERROR WHILE WRITING");
            os = null;
        }

    }

    public boolean shouldUseP2PTalkback() {
        Device device = DeviceSingleton.getInstance().getDeviceByRegId(registrationId);
        if (device != null) {
            return device.getProfile().shouldUseP2PTalkbackInLocal();
        }
        return false;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getExtendedTalkbackCommand(String command, String streamName) {
        String result = command;
        Device device = DeviceSingleton.getInstance().getDeviceByRegId(registrationId);
        if (device != null && device.getProfile().shouldUseExtendedTalkbackCommand()) {
            result = result + "&value=" + streamName;
        }

        return result;
    }

    private LocalDevice getLocalDevice() {
        LocalDevice localDevice = DeviceSingleton.getInstance().getLocalDeviceByRegId(registrationId);
        Log.d(TAG, "Local device is null? " + (localDevice == null));
        if (localDevice == null) {
            localDevice = new LocalDevice(HubbleApplication.AppContext, addr, registrationId);
            localDevice.performTestBlock();
            DeviceSingleton.getInstance().addLocalDevice(registrationId, localDevice);
        }
        return localDevice;
    }

    public TalkbackState parseTalkbackStatus(String response) {
        Device device = DeviceSingleton.getInstance().getDeviceByRegId(registrationId);
        String[] resArr = parseCameraResposne(PublicDefineGlob.SET_DEVICE_AUDIO_ON, response);
        String result = "-1";
        if (resArr != null) {
            for (int i = 0; i < resArr.length; i++) {
                Log.w(TAG, "Matcher group " + i + ", value: " + resArr[i]);
            }

            if (device.getProfile().shouldUseExtendedTalkbackCommand()) {
                if (appUuid.equals(resArr[3])) {
                    Log.i(TAG, "Stream name matched. Proceed...");
                    result = resArr[1];
                } else {
                    Log.w(TAG, "Stream name not match, return busy");
                    result = "-3";
                }
            } else {
                result = resArr[1];
            }
        }
        // FAILED
        TalkbackState talkbackState = TalkbackState.FAILED;
        if (result.equals("-3")) {
            talkbackState = TalkbackState.BUSY;
        } else if (result.equals("0")) {
            talkbackState = TalkbackState.READY;
        }
        return talkbackState;
    }

    /**
     * Parse camera response string: command: value(&...)
     *
     * @param command  send command
     * @param response command response
     * @return null or String[4]
     */
    private String[] parseCameraResposne(String command, String response) {
        Pattern pattern = Pattern.compile("(" + command + ":\\s+)(-?\\d+)(&?)(.*)");
        Matcher m       = pattern.matcher(response);
        if (m.matches()) {
            String[] result = new String[4];
            result[0] = m.group(1);
            result[1] = m.group(2);
            result[2] = m.group(3);
            result[3] = m.group(4);
            return result;
        } else {
            return null;
        }
    }

    public enum TalkbackState {
        READY,
        BUSY,
        FAILED
    }
}
