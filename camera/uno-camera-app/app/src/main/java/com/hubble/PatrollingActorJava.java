package com.hubble;

import android.os.Handler;

import com.actor.model.NextDevice;
import com.actor.model.Pause;
import com.actor.model.PreviousDevice;
import com.actor.model.ProgressUpdate;
import com.actor.model.Resume;
import com.actor.model.StartPatrolling;
import com.google.common.util.concurrent.SettableFuture;
import com.hubble.actors.Actor;
import com.hubble.devcomm.Device;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;



/**
 * Created by sonikas on 17/08/16.
 */
public abstract class PatrollingActorJava extends Actor {

    List<Device> devices;
    int delaySeconds;


    public PatrollingActorJava(List<Device> devices, int delaySeconds) {
        this.devices = devices;
        this.delaySeconds = delaySeconds;

    }

    public PatrollingActorJava() {
    }

    Handler mHandler=new Handler();
    Runnable mRunnable=new Runnable() {
        @Override
        public void run() {
            send(new ProgressUpdate());
        }
    };

    private AtomicBoolean shouldPause = new AtomicBoolean(false);
    private int currentDeviceIndex = 0;
    private long progressInMillis = 0;
    @Nullable
    @Override
    public Object receive(Object m) {
        if(m instanceof StartPatrolling){
            progressInMillis = 0;
            currentDeviceIndex = 0;
            this.send (new ProgressUpdate());
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    onStartPatrolling(devices.get(0));
                }
            }) ;
        }else if(m instanceof ProgressUpdate){
            if ( !shouldPause.get() ) {
                 if(   progressInMillis < delaySeconds * 1000 ) {
                        progressInMillis += 100;
                        //Log.d("PatrollingActor", "progress is " + progressInMillis)
                        mHandler.removeCallbacks(mRunnable);
                        mHandler.postDelayed(mRunnable, 100);
                        runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                onUpdateProgress(progressInMillis);
                            }
                        }) ;
                    }
                    else{
                        next();
                    }
            }
        }else if(m instanceof Pause){
            shouldPause.set(true);
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    onPause();
                }
            });
        }else if(m instanceof Resume){
            shouldPause.set(false);
            this.send(new ProgressUpdate());
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    onResume();
                }
            });
        }else if(m instanceof NextDevice){
            incrementAsRing();
            progressInMillis = 0;
            //TODO futre used instead of promise
            final SettableFuture<Object> future=SettableFuture.create();
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    onNextDevice(devices.get(currentDeviceIndex), future);
                }
            });
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            this.send(new ProgressUpdate());
        }else if(m instanceof PreviousDevice){
            decrementAsRing();
            progressInMillis = 0;
            //TODO check future used instead of promise;
            final SettableFuture<Object> promise=SettableFuture.create();
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    onPreviousDevice(devices.get(currentDeviceIndex),promise);
                }
            });
            try {
                promise.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            this.send(new ProgressUpdate());
        }


        return null;
    }

    private void incrementAsRing() {
        if(currentDeviceIndex <devices.size()-1){
            currentDeviceIndex+=1;
        }else{
            currentDeviceIndex=0;
        }

    }

    private void decrementAsRing() {
        if(currentDeviceIndex>0){
            currentDeviceIndex-=1;
        }else{
            currentDeviceIndex=devices.size()-1;
        }

    }



    public abstract void onStartPatrolling(Device device);
    public abstract void onUpdateProgress(long currentMillis);
    public abstract void onNextDevice(Device device, SettableFuture<Object> promise);
    public abstract void onPreviousDevice(Device device, SettableFuture<Object> promise);
    public abstract void onPause();
    public abstract void onResume();

    public void start() {
        this.send (new StartPatrolling(devices.get(currentDeviceIndex)));
    }

    public void next() {
        this.send(new NextDevice(devices.get(currentDeviceIndex)));
    }

    public void previous() {
        this.send(new PreviousDevice(devices.get(currentDeviceIndex)));
    }

    public void pause() {
        this.send(new Pause());
    }

    public void resume() {
        this.send(new Resume());
    }

}
