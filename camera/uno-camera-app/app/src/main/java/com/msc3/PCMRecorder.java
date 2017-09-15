package com.msc3;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PCMRecorder {
    private static final String      TAG             = "pcmrecorder";
    private              String      filename        = null;
    private              int         bufferSize      = 0;
    private              AudioRecord recorder        = null;
    private              Thread      recordingThread = null;
    private              boolean     isRecording     = false;

    private static final int RECORDER_SAMPLERATE     = 8000;
    private static final int RECORDER_CHANNELS       = AudioFormat.CHANNEL_IN_DEFAULT;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private Activity         mActivity;
    private CircularBuffer   audioBuffer;
    private AudioOutStreamer audioOut;
    private Thread           streamingThread;
    private int              iRead, iWrite;
    private boolean recordToFile;
    private Handler mHandler;
    private boolean finishFlushing = false;
    private boolean isInLocal      = false;

    public PCMRecorder(String ip, int cmd_port, String http_pass, int audio_port, Handler touchlistener) {

        audioOut = new AudioOutStreamer(this, ip, cmd_port, http_pass, audio_port, touchlistener);

        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        // // Log.d(TAG, "bufferSize =" + bufferSize);

        if (bufferSize > 0) {

            // 20130925: phung : need to buffer at least 2 sec of data
            int app_buffer_size = ((bufferSize * 4) > 2 * 16000) ? (bufferSize * 4) : 2 * 16000;

            audioBuffer = new CircularBuffer(app_buffer_size);
            // // Log.d(TAG, "app_buffer_size =" + app_buffer_size);

        }

        recordToFile = false;
        streamingThread = null;
        mHandler = touchlistener;
    }

    private PCMRecorder(String offlineFile) {
        filename = offlineFile;

        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        recordToFile = true;
        // // Log.d(TAG, "bufferSize =" + bufferSize);
        streamingThread = null;
    }

    public boolean isLocalMode() {
        return isInLocal;
    }

    public void setLocalMode(boolean isInLocal) {
        this.isInLocal = isInLocal;
        if (audioOut != null) {
            audioOut.setLocalMode(isInLocal);
        }
    }

    public void setActivity(Activity activity) {
        mActivity = activity;
    }

    public boolean isFinishFlushing() {
        return finishFlushing;
    }

    public void setFinishFlushing(boolean finishFlushing) {
        this.finishFlushing = finishFlushing;
    }

    public boolean startRecording() {
        finishFlushing = false;
        try {
            if (mActivity != null) {
                AudioManager audioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
                audioManager.setSpeakerphoneOn(true);
            }
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                // RECORDER_AUDIO_ENCODING, bufferSize*8);
                RECORDER_AUDIO_ENCODING, 16000);

            // // Log.d(TAG, "Audio talkback: start recording...");
            recorder.startRecording();


        }
        catch (IllegalArgumentException ie) {
            // // Log.e(TAG, Log.getStackTraceString(ie));
            recorder = null;
            return false;
        }
        catch (IllegalStateException ise) {
            // // Log.e(TAG, Log.getStackTraceString(ise));
            recorder = null;
            return false;
        }

        isRecording = true;

        if (recordToFile) {
            recordingThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    writeAudioDataToFile();
                }
            }, "AudioRecorder Thread");
            recordingThread.start();
        } else {
            if (audioBuffer != null) {
                audioBuffer.reset();
            }

            recordingThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    writeAudioDataToBuffer();
                }
            }, "AudioRecorder Thread");

            // audioOut.startStreaming();
            recordingThread.start();
            // streamingThread.start();

        }

        return isRecording;
    }

    public void setRelayAddr(String addr) {
        if (audioOut != null) {
            audioOut.setAddr(addr);
        }
    }

    public void setRelayPort(int port) {
        if (audioOut != null) {
            audioOut.setAudioPort(port);
        }
    }

    public void setSessionKey(String skey) {
        if (audioOut != null) {
            audioOut.setSessionKey(skey);
        }
    }

    public void setStreamId(String streamId) {
        if (audioOut != null) {
            audioOut.setStreamId(streamId);
        }
    }

    public void startStreaming() {
        try {
            if (audioOut != null) {
                streamingThread = new Thread(audioOut, "AudioStreaming Thread");
                audioOut.startStreaming();
                if (streamingThread != null) {
                    streamingThread.start();
                } else {
                    // // Log.d(TAG, "Audio talkback: streaming thread already stopped");
                }
            }
        }
        catch (Exception ex) {
            stopRecording();
            stopStreaming();
        }
    }

    public void stopStreaming() {
        if (audioOut != null) {
            // // Log.d(TAG, "Audio talkback: stop streaming");
            audioOut.stopStreaming();

            try {
                if (streamingThread != null) {
                    streamingThread.join(2000);
                }
            }
            catch (InterruptedException e) {
                // // Log.e(TAG, Log.getStackTraceString(e));
            }
            streamingThread = null;
        }
    }

    /**
     * Stop Talk back - Stop recording thread first , then stop the audio
     * streaming thread If there is some recorded data left, audio streaming
     * thread will try to send out all of them before terminate -- this could
     * take some times. - Should not be called on UI thread because there is
     * some delay inside (at most 2sec)
     *
     * @return
     */
    public boolean stopRecording() {
        if (recorder != null) {
            isRecording = false;
            try {
                if (mActivity != null) {
                    AudioManager audioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setSpeakerphoneOn(false);
                }
                recorder.stop();
            }
            catch (IllegalStateException e1) {
                // // Log.e(TAG, Log.getStackTraceString(e1));
            }
            catch (NullPointerException ex) {
                ex.printStackTrace();
            }
            // // Log.d(TAG, "Audio talkback: stop recording...");

            if (recordToFile) {
                try {
                    recordingThread.join();
                }
                catch (InterruptedException e) {
                    // // Log.e(TAG, Log.getStackTraceString(e));
                }
                recordingThread = null;
            } else {
                try {
                    if (recordingThread != null) {
                        recordingThread.join(500);
                    }

                }
                catch (InterruptedException e) {
                    // // Log.e(TAG, Log.getStackTraceString(e));
                }
                recordingThread = null;

            }
        }

        return isRecording;
    }

    public void releaseRecorder() {
        if (recorder != null) {
            // // Log.d(TAG, "Release recorder");
            recorder.release();
            recorder = null;
        }
    }

    public int readFromAudioBuffer(byte[] data, int num_of_bytes) {
        int actual_read = -1;

        actual_read = audioBuffer.read(data, num_of_bytes);
        return actual_read;
    }

    private void writeAudioDataToBuffer() {
        int    read        = 0;
        byte[] temp_buffer = new byte[bufferSize * 4];
        // // Log.d(TAG, "start writeAudioDataToBuffer: " + System.currentTimeMillis());

        // while(isRecording && streamingThread.isAlive())
        while (recorder != null && (read = recorder.read(temp_buffer, 0, bufferSize * 4)) > 0) {
            // read = recorder.read(temp_buffer, 0, bufferSize);
            if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                // tryDoublingAmplitude(temp_buffer);
                audioBuffer.write(temp_buffer, read);
            }
        }

        finishFlushing = true;
        // if ( !streamingThread.isAlive())
        // {
        // mHandler.dispatchMessage(Message.obtain(mHandler,
        // ViewCameraActivity.MSG_PCM_RECORDER_ERR));
        // }
    }

    public void stop() {
        if (recorder != null) {
            recorder.stop();
        }
    }

    private void writeAudioDataToFile() {
        byte data[] = new byte[bufferSize];
        // String filename = getTempFilename();
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(filename);
        }
        catch (FileNotFoundException e) {

            // // Log.e(TAG, Log.getStackTraceString(e));
        }

        int read = 0;

        if (null != os) {
            while (isRecording) {
                read = recorder.read(data, 0, bufferSize);

                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        os.write(data);
                    }
                    catch (IOException e) {
                        // // Log.e(TAG, Log.getStackTraceString(e));
                    }
                }
            }

            try {
                os.close();
            }
            catch (IOException e) {
                // // Log.e(TAG, Log.getStackTraceString(e));
            }
        }
    }

    private void tryDoublingAmplitude(byte[] data) {
        int data_len = data.length;
        int one_sample, new_sample;

        for (int i = 0; i < data_len; i += 2) {

            // one_sample = data[i+1];
            // one_sample = ((one_sample <<8) | data[i]);
            //
            // one_sample = (one_sample <<1);
            //
            //
            // data[i] = (byte)(one_sample & 0x0F);
            // data[i+1] = (byte) ((one_sample >>8) & 0x0F);

            one_sample = data[i + 1];
            one_sample = ((one_sample << 8) | data[i]);

            new_sample = one_sample * one_sample;
            if (one_sample < 0) {
                new_sample *= (-1);
            }

            data[i] = (byte) (new_sample & 0x0F);
            data[i + 1] = (byte) ((new_sample >> 8) & 0x0F);

        }
    }

    public void setDeviceId(String deviceId) {
        if (audioOut != null) {
            audioOut.setRegistrationId(deviceId);
        }
    }

}
