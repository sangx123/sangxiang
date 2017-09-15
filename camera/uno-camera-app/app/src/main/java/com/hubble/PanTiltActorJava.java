package com.hubble;

/**
 * Created by sonikas on 28/07/16.
 */

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import com.actor.model.CheckMovePresetPoint;
import com.actor.model.CheckStatusCentering;
import com.actor.model.Direction;
import com.actor.model.FinishCenter;
import com.actor.model.PanTiltFailure;
import com.actor.model.PanTiltSuccess;
import com.actor.model.ProcessQueue;
import com.actor.model.QueuePanTilt;
import com.actor.model.SendFBStop;
import com.actor.model.SendFBStopAsync;
import com.actor.model.SendLRStop;
import com.actor.model.SendLRStopAsync;
import com.actor.model.SendPanTilt;
import com.actor.model.SendPanTiltContinuously;
import com.actor.model.SendPanTiltPreset;
import com.actor.model.SetPanTiltCenterH;
import com.actor.model.SetStop;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.actors.Actor;
import com.hubble.command.CameraCommandUtils;
import com.hubble.devcomm.Device;
import com.hubble.registration.PublicDefine;
import com.hubble.ui.PanTiltFragment;
import com.hubble.ui.PanTiltTouchHandler;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

import jet.runtime.typeinfo.JetValueParameter;


import java.util.concurrent.LinkedBlockingQueue;

public abstract class PanTiltActorJava extends Actor{

    private static final String TAG="PanTiltActorJava";
    Device device;
    long queueTime;
    long sendStopAfter;

    AtomicBoolean running = new AtomicBoolean();
    LinkedBlockingQueue<QueuePanTilt> queue = new LinkedBlockingQueue<QueuePanTilt>();
    AtomicBoolean mStop = new AtomicBoolean();
    PanTiltTouchHandler panTiltTouchHandler;


    public PanTiltActorJava() {
    }

    public PanTiltActorJava(Device device, long queueTime, long sendStopAfter) {
        this.device = device;
        this.queueTime = queueTime;
        this.sendStopAfter = sendStopAfter;
    }

    public LinkedBlockingQueue<QueuePanTilt> getQueue() {
        return queue;
    }

    public void setPanTiltTouchHandler(PanTiltTouchHandler panTiltTouchHandler) {
        this.panTiltTouchHandler = panTiltTouchHandler;
    }

    @Nullable
    @Override
    public Object receive(final Object m) {
        if (m instanceof QueuePanTilt) {
            if (!running.get()) {
                this.send(((QueuePanTilt) m).message);
                running.set(true);
            } else {
                queue.add((QueuePanTilt) m);
            }
        } else if (m instanceof SendLRStop) {
            sendLRStop();
        } else if (m instanceof SendLRStopAsync) {
            sendLRStopAsync();
        } else if (m instanceof SendFBStop) {
            sendFBStop();
        } else if (m instanceof SendFBStopAsync) {
            sendFBStopAsync();
        } else if (m instanceof ProcessQueue) {
            processQueue();
        } else if (m instanceof SendPanTilt) {
            boolean success = false;
            if (device.getProfile().isVTechCamera()) {
                success = sendPanTilt(((SendPanTilt) m).direction);
            } else {
                // Hubble-related queue style. Currently not in use. We use soft-tap and press-and-hold now.
               /* sendPanTiltAsync((SendPanTilt) m);
                int count = 0;
                while (count < 5) {
                    performTimeout(queueTime);
                    sendPanTiltAsync((SendPanTilt) m);
                    sendPanTiltAsync((SendPanTilt) m);
                    count++;
                }*/
                success=sendPanTilt(((SendPanTilt)m).direction);
            }
            if (device.getProfile().isVTechCamera()) {
              /*
               * 20161124 HOANG VIC-1970
               * For Vtech camera, don't need to send stop command
               */
                if (queue.isEmpty()) {
                /*
                 * 20161124 HOANG VIC-1970
                 * This is the last pan-tilt command in queue, check whether user is long pressing
                 * If user is still press, keep sending command to camera every 500ms.
                 */
                    if (panTiltTouchHandler != null && panTiltTouchHandler.isPanTiltTouching()) {
                        Log.d(TAG, "pan-tilt touching -> send cmd continuously");
                        while (panTiltTouchHandler != null && panTiltTouchHandler.isPanTiltTouching()) {
                            performTimeout(500);
                            sendPanTiltDurationCmd(((SendPanTilt) m).direction, PanTiltFragment.DIRECTION_COMMAND_DURATION);
                        }
                        this.after(queueTime, new PanTiltSuccess((SendPanTilt) m));
                      /*
                       * 20161124 HOANG VIC-1970
                       * Just send stop command when user long pressed for stopping camera immediately
                       */
                        sendPanTiltStop(((SendPanTilt) m).direction);
                    } else {
                        if(success)
                            this.after(queueTime, new PanTiltSuccess((SendPanTilt)(m)));
                        else
                            this.send(new PanTiltFailure((SendPanTilt)(m)));
                    }

                } else {
                    if(success)
                        this.after(queueTime,new PanTiltSuccess((SendPanTilt)m));
                    else
                        this.send(new PanTiltFailure((SendPanTilt)m));

                }
            } else {
                performTimeout(sendStopAfter);
                onStop();

            }

        } else if (m instanceof SendPanTiltContinuously) {
            int success = 0;
            do {
                if (((SendPanTiltContinuously)m).direction == Direction.center || ((SendPanTiltContinuously)m).direction == Direction.centerH ) {
                    success = sendPanTiltCenter(((SendPanTiltContinuously)m).direction);
                } else {
                    sendPanTilt(((SendPanTiltContinuously)m).direction); // We want to use the blocking call here, so we wait for a response from the device.
                }
            } while (!mStop.get());
            performTimeout(1490);
            if (((SendPanTiltContinuously)m).direction == Direction.center) {
                if (success == -99) {
                    stopAsync(((SendPanTiltContinuously)m).direction);
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            onFailCenter();
                        }
                    });
               }else{
                    this.send(new CheckStatusCentering(Direction.center));
                }
            } else {
                stopAsync(((SendPanTiltContinuously)m).direction);
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        performTimeout(2500);
                        onStop();
                    }
                });

            }

        } else if (m instanceof SendPanTiltPreset) {
            int status = 0;
            try {
                status = sendCmdValue("move_preset", ((SendPanTiltPreset) m).value);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            final int finalStatus = status;
            switch (status) {

                case -1:
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            onPresetFail(finalStatus);
                        }
                    });
                    break;
                case -2:
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            onPresetFail(finalStatus);
                        }
                    });
                    break;
                case 0:
                    this.send(new CheckMovePresetPoint());
                    break;
                default:
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            onPresetFail(finalStatus);
                        }
                    });
            }
        } else if (m instanceof CheckMovePresetPoint) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int status = 0;
            try {
                status = sendCmdNotValue("motor_status");
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
            final int finalStatus = status;
            switch (status) {
                case -2:
                    this.send(new CheckMovePresetPoint());
                    break;
                case 0:
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            onPresetSuccess();
                            ;
                        }
                    });
                    break;
                default:
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            onPresetFail(finalStatus);
                        }
                    });
            }
        } else if (m instanceof CheckStatusCentering) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int status = 0;
            try {
                status = sendCmdPant("get_" + ((CheckStatusCentering) m).direction);
            } catch (ClassCastException e) {
            }
            switch (status) {
                case -2:
                    this.send(new CheckStatusCentering(((CheckStatusCentering) m).direction));
                    break;
                case 90:
                    if (((CheckStatusCentering) m).direction == Direction.center) {
                        try {
                            Thread.sleep(2000);
                            this.send(new SetPanTiltCenterH());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        this.send(new FinishCenter());
                    }
                    break;
                default:
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            onPanFailure(((CheckStatusCentering) m).direction);
                        }
                    });
            }

        }else if(m instanceof SetPanTiltCenterH){
            int succes = sendPanTiltCenter(Direction.centerH);
            if (succes == 0) {
                this.send(new CheckStatusCentering(Direction.centerH));
            }
        }else if(m instanceof FinishCenter){
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    onFinishCenter();
                }
            });
        }else if(m instanceof SetStop){
            mStop.set(true);
        }else if(m instanceof PanTiltSuccess){
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    onPanSuccess(((PanTiltSuccess)m).message.direction);
                }
            });
            if (!queue.isEmpty()) {
                this.send(new ProcessQueue());
            } else {
                running.set(false);
            }
        }else if(m instanceof PanTiltFailure){
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    onPanFailure(((PanTiltFailure)m).message.direction);
                }
            });
            if (!queue.isEmpty()) {
                this.send(new ProcessQueue());
            } else {
                running.set(false);
            }
        }
        return null;
    }

    public void clearQueue() {
        queue.clear();
    }

    public void offerQueue(LinkedBlockingQueue<QueuePanTilt> outsideQueue) {
        for (QueuePanTilt queuePanTilt : outsideQueue) {
            try {
                queue.put(queuePanTilt);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void processCompleteQueue() {
        this.send (new ProcessQueue());
    }

    public void processQueue() {
        if (!queue.isEmpty()) {
            QueuePanTilt command = null;
            try {
                command = queue.take();
                this.send( command.message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void performTimeout(long timeout) {
        long sTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - sTime < timeout) {
        }
    }

    private void sendPanTiltStop(Direction direction) {
        if (sendStopAfter > 0) {
            if(direction==Direction.left || direction==Direction.right){
                this.after(sendStopAfter,new SendLRStop());
                new SendLRStop();
                new SendLRStop();
            }
            if(direction == Direction.up || direction == Direction.down || direction == Direction.center || direction == Direction.centerH){
                this.after(sendStopAfter,new SendFBStop());
                new SendFBStop();
                new SendFBStop();
            }
        }
    }

    private boolean sendPanTiltDurationCmd(final Direction direction, long duration) {
        Log.d(TAG, "sendPanTiltDurationCmd: " + direction);
        String cmd = direction2DurationCmd(direction);
        boolean success = false;
        String response = CameraCommandUtils.sendCommandGetStringValue(device, cmd, duration+"", null);
        if (response != null) {
            if (response.equals(java.lang.String.valueOf(PanTiltFragment.PAN_TILT_BOUNDARY))) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        onPanBoundary(direction);
                    }
                });

            } else if (response.equals(java.lang.String.valueOf(PanTiltFragment.PAN_TILT_SUCCESS))) {
                success = true;}

        }
        return success;
    }

    private boolean sendPanTilt(final Direction direction) {
        String cmd = direction2Cmd(direction);
        boolean success = false;
        String duration=null;
        //todo duration cmd not working in 83. check again
        /*if (device.getProfile().doesSupportPanTiltDuration()) {
           duration= "2000";
        } else {
            duration=null;
        }*/
        String response = CameraCommandUtils.sendCommandGetStringValue(device, cmd, duration, null);
        if (response != null) {
            if (response.equals(java.lang.String.valueOf(PanTiltFragment.PAN_TILT_BOUNDARY))) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        onPanBoundary(direction);
                    }
                });
            } else if (response.equals(java.lang.String.valueOf(PanTiltFragment.PAN_TILT_SUCCESS))) {
                success = true;
            }
        }
        return success;
    }

    public int sendPanTiltCenter(Direction direction) {
        String cmd = direction2Cmd(direction);
        return sendCmdPant(cmd);
    }

    int socVersion = 0;

    private String direction2Cmd(Direction direction){
        /*if (socVersion == 0) {
            SecureConfig config = HubbleApplication.AppConfig;
            long lastTimeGetSoc = config.getLong(PublicDefine.PREFS_LAST_TIME_GET_SOC_VERSION, 0);
            if (System.currentTimeMillis() - lastTimeGetSoc > 6 * 3600 * 100) { // 6 hours
                Pair<String,Object> result = device.sendCommandGetValue("get_soc_version", null, null);
                if (result != null) {
                    try {
                        String strValue = (String)result.second;
                                socVersion = Integer.parseInt(strValue.replace("_", "").replace("v", ""));
                    } catch (Exception e) {
                        socVersion = 0;
                    }
                }
                if (socVersion != 0) { // request successful
                    config.putLong(PublicDefine.PREFS_LAST_TIME_GET_SOC_VERSION, System.currentTimeMillis());
                    config.putInt(PublicDefine.PREFS_SOC_VERSION, socVersion);
                }
            } else {
                socVersion = config.getInt(PublicDefine.PREFS_SOC_VERSION, 0);
            }
            Log.d(TAG, "soc version = " + socVersion);
        }*/
        Log.d(TAG, "direction2Cmd: " + direction + " model? " + device.getProfile().getModelId() + " isVtechCam? "
                + device.getProfile().isVTechCamera() + " supportDuration? " + device.getProfile().doesSupportPanTiltDuration());
        String cmd=null;
        if(device.getProfile().isVTechCamera()){
           if(direction==Direction.right)
               cmd="move_right_step";
            else if(direction == Direction.left)
               cmd="move_left_step";
            else if(direction==Direction.down)
               cmd="move_forward_step";
            else if(direction==Direction.up)
               cmd="move_backward_step";
            else if(direction==Direction.center)
               cmd="move_center";
            else if(direction==Direction.centerH)
               cmd="move_centerH";
        }
        //todo duration cmd not working on 83.check again
        /*else if(device.getProfile().doesSupportPanTiltDuration()) {
            if (direction == Direction.right)
                cmd = "move_right_duration";
            else if (direction == Direction.left)
                cmd = "move_left_duration";
            else if (direction == Direction.down)
                cmd = "move_forward_duration";
            else if (direction == Direction.up)
                cmd = "move_backward_duration";
            else if (direction == Direction.center)
                cmd = "move_center";
            else if (direction == Direction.centerH)
                cmd = "move_centerH";
            // keep flow of hubble but support duration
        } */else {
            if (direction == Direction.right)
                cmd = "move_right";
            else if (direction == Direction.left)
                cmd = "move_left";
            else if (direction == Direction.down)
                cmd = "move_forward";
            else if (direction == Direction.up)
                cmd = "move_backward";
            else if (direction == Direction.center)
                cmd = "move_center";
            else if (direction == Direction.centerH)
                cmd = "move_centerH";

        }
        Log.d(TAG, "pan tilt cmd = " + cmd);
        return cmd;

    }

    // only flow of vtech can reach to this method
    private String direction2DurationCmd(Direction direction){
        Log.d(TAG, "direction2DurationCmd: " + direction);
        String cmd=null;
        if(device.getProfile().doesSupportPanTiltDuration()) {
            if (direction == Direction.right)
                cmd = "move_right_duration";
            else if (direction == Direction.left)
                cmd = "move_left_duration";
            else if (direction == Direction.down)
                cmd = "move_forward_duration";
            else if (direction == Direction.up)
                cmd = "move_backward_duration";
            else if (direction == Direction.center)
                cmd = "move_center";
            else if (direction == Direction.centerH)
                cmd = "move_centerH";
            // keep flow of hubble but support duration
        } else {
            if (direction == Direction.right)
                cmd = "move_right";
            else if (direction == Direction.left)
                cmd = "move_left";
            else if (direction == Direction.down)
                cmd = "move_forward";
            else if (direction == Direction.up)
                cmd = "move_backward";
            else if (direction == Direction.center)
                cmd = "move_center";
            else if (direction == Direction.centerH)
                cmd = "move_centerH";

        }
        Log.d(TAG, "pan tilt duration cmd = " + cmd);
        return cmd;
    }

    private void sendPanTiltAsync(SendPanTilt m) {
        String cmd = direction2Cmd(m.direction);
        CameraCommandUtils.sendCommandGetFullResponseAsync(device, cmd, null, null, null);
    }

    private void sendPanTiltAsync(Direction direction) {
        String cmd = direction2Cmd(direction);
        CameraCommandUtils.sendCommandGetFullResponseAsync(device, cmd, null, null, null);
    }

    private boolean sendFBStop(){
        return sendStop("fb_stop");
    }
    private boolean sendLRStop(){
        return  sendStop("lr_stop");
    }

    private void sendFBStopAsync() {
        sendStopAsync("fb_stop");
    }

    private void sendLRStopAsync() {
        sendStopAsync("lr_stop");
    }

    private boolean sendStop(String stop){
        try {
            return CameraCommandUtils.sendCommandGetSuccess(device, stop, null, null);
        } catch ( Exception e) {
            return false;
        }
    }

    private void sendStopAsync(String stop) {
        try {
            CameraCommandUtils.sendCommandGetFullResponseAsync(device, stop, null, null, null);
        } catch (Exception e) {
        }
    }

    public void pan(Direction direction) {
        this.send (new SendPanTilt(direction));
    }

    public void panContinuously(Direction direction) {
        mStop.set(false);
        this.send(new SendPanTiltContinuously(direction));
    }

    public void setStop(boolean stop) {
        mStop.set(stop);
    }

    private void stopAsync(Direction direction) {
        switch(direction){
            case right:
                this.send(new SendLRStopAsync());
                break;
            case left:
                this.send(new SendLRStopAsync());
                break;
            case down:
                this.send(new SendFBStopAsync());
                break;
            case up:
                this.send(new SendFBStopAsync());
                break;
            case center:
                this.send(new SendFBStopAsync());
                break;
            case centerH:
                this.send(new SendFBStopAsync());
        }

    }

    public abstract void onPanSuccess(Direction direction);
    public abstract void onPanFailure(Direction direction);
    public abstract void onStop();
    public abstract void onFinishCenter();
    public abstract void onFailCenter();
    public abstract void onPresetSuccess();
    public abstract void onPresetFail(int code);
    public abstract void onPanBoundary(Direction direction);



    private int sendCmdPant(String cmd) {
         try {
            return (int) device.sendCommandGetValue(cmd, null, null).second;
        } catch(Exception e) {
            e.printStackTrace();
             return -99;
        }
    }

    public void sendPanPreset(String value) {
        this.send(new SendPanTiltPreset(value));
    }

    private int sendCmdValue(String cmd,String value) {
        int status = 0;
        try {
            String cmdValue = CameraCommandUtils.sendCommandGetStringValue(device, cmd, value, null);
            if (cmdValue != null) {
                if (cmdValue.equals(".")) {
                    status = 0;
                } else {
                    status = java.lang.Integer.parseInt(cmdValue);
                }
            } else {
                status = -99;
            }
            return status;
        } catch(Exception e) {
            e.printStackTrace();
            return -99;
        }
    }

    private int sendCmdNotValue(String cmd){
        int status = 0;
        try {
            String cmdValue = CameraCommandUtils.sendCommandGetStringValue(device, cmd, null, null);

            if (cmdValue.length() > 3) {
                if (cmdValue.startsWith("-2") || cmdValue.endsWith("-2")) {
                    status = -2;
                } else {
                    status = 0;
                }
            } else {
                status = Integer.parseInt(cmdValue);
            }

            return status;
        } catch(Exception e) {
            e.printStackTrace();
            return -5;
        }
    }
}
