/* //device/libs/android_runtime/com_media_ffmpeg_FFMpegPlayer.cpp
**
** Copyright 2007, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

#define TAG "FFMpegPlayer-JNI"

#include <android/log.h>
#include <android/bitmap.h>
#include "jniUtils.h"
#include "methods.h"
#include "output.h"

#include <mediaplayer.h>

struct fields_t {
    jfieldID    context;
    jmethodID   post_event;
    jmethodID	get_next_clip;

};
static fields_t fields;

/* Global thingy */
static MediaPlayer * mediaPlayer ;

static const char* const kClassPathName = "com/media/ffmpeg/FFMpegPlayer";
const char* STR_MEDIA_PLAYBACK_COMPLETE = "complete";
const char* STR_MEDIA_PLAYBACK_IN_PROGRESS = "in_progress";

// ----------------------------------------------------------------------------
// ref-counted object for callbacks
class JNIFFmpegMediaPlayerListener: public MediaPlayerListener
{
public:
    JNIFFmpegMediaPlayerListener(JNIEnv* env, jobject thiz, jobject weak_thiz);
    ~JNIFFmpegMediaPlayerListener();
    void notify(int msg, int ext1, int ext2);
    int getNextClip(char**);
private:
    JNIFFmpegMediaPlayerListener();
    jclass      mClass;     // Reference to MediaPlayer class
    jobject     mObject;    // Weak ref to MediaPlayer Java object to call on
};

JNIFFmpegMediaPlayerListener::JNIFFmpegMediaPlayerListener(JNIEnv* env, jobject thiz, jobject weak_thiz)
{
    // Hold onto the MediaPlayer class for use in calling the static method
    // that posts events to the application thread.
    jclass clazz = env->GetObjectClass(thiz);
    if (clazz == NULL) {
        jniThrowException(env, "java/lang/Exception", kClassPathName);
        return;
    }
    mClass = (jclass)env->NewGlobalRef(clazz);

#if 0
    // We use a weak reference so the MediaPlayer object can be garbage collected.
    // The reference is only used as a proxy for callbacks.
    mObject  = env->NewGlobalRef(weak_thiz);
#else
    mObject = (jobject) env->NewGlobalRef(thiz);
#endif
}

JNIFFmpegMediaPlayerListener::~JNIFFmpegMediaPlayerListener()
{
    // remove global references
    JNIEnv *env = getJNIEnv();
    env->DeleteGlobalRef(mObject);
    env->DeleteGlobalRef(mClass);
}

void JNIFFmpegMediaPlayerListener::notify(int msg, int ext1, int ext2)
{
    JNIEnv *env = getJNIEnv();
    //env->CallStaticVoidMethod(mClass, fields.post_event, mObject, msg, ext1, ext2, 0);
    env->CallVoidMethod(mObject, fields.post_event, mObject, msg, ext1, ext2, 0);
}

int JNIFFmpegMediaPlayerListener::getNextClip(char** url)
{
	int result = MEDIA_PLAYBACK_STATUS_IN_PROGRESS;
	JNIEnv *env = getJNIEnv();

	jstring ret = (jstring) env->CallObjectMethod(mObject, fields.get_next_clip);
	const char *url_ret = env->GetStringUTFChars(ret, NULL);
	__android_log_print(ANDROID_LOG_INFO, TAG, "Get next clip: %s\n", url_ret);
	int ret_code = strcmp(url_ret, STR_MEDIA_PLAYBACK_COMPLETE);
	if (ret_code == 0)
	{
		env->DeleteLocalRef(ret);
		return MEDIA_PLAYBACK_STATUS_COMPLETE;
	}

	ret_code = strcmp(url_ret, STR_MEDIA_PLAYBACK_IN_PROGRESS);
	if (ret_code == 0)
	{
		env->DeleteLocalRef(ret);
		return MEDIA_PLAYBACK_STATUS_IN_PROGRESS;
	}

	if (url_ret != NULL)
	{
		*url = (char *) malloc(strlen(url_ret) * sizeof(char));
		if (*url != NULL)
		{
			strcpy(*url, url_ret);
			result = MEDIA_PLAYBACK_STATUS_STARTED;
		}
	}
	else
	{
		result = MEDIA_PLAYBACK_STATUS_IN_PROGRESS;
	}
	env->DeleteLocalRef(ret);

	return result;
}

// ----------------------------------------------------------------------------

static MediaPlayer* getMediaPlayer(JNIEnv* env, jobject thiz)
{
#if 0
    return (MediaPlayer*)env->GetIntField(thiz, fields.context);
#else
    return mediaPlayer;
#endif
}

static MediaPlayer* setMediaPlayer(JNIEnv* env, jobject thiz, MediaPlayer* player)
{
    MediaPlayer* old = (MediaPlayer*)env->GetIntField(thiz, fields.context);
    if (old != NULL) {
		__android_log_print(ANDROID_LOG_INFO, TAG, "freeing old mediaplayer object");
		free(old);
	}
    env->SetIntField(thiz, fields.context, (int)player);
    return old;
}

// If exception is NULL and opStatus is not OK, this method sends an error
// event to the client application; otherwise, if exception is not NULL and
// opStatus is not OK, this method throws the given exception to the client
// application.
static void process_media_player_call(JNIEnv *env, jobject thiz, status_t opStatus, const char* exception, const char *message)
{
    if (exception == NULL) {  // Don't throw exception. Instead, send an event.
		/*
        if (opStatus != (status_t) OK) {
            sp<MediaPlayer> mp = getMediaPlayer(env, thiz);
            if (mp != 0) mp->notify(MEDIA_ERROR, opStatus, 0);
        }
		*/
    } else {  // Throw exception!
        if ( opStatus == (status_t) INVALID_OPERATION ) {
            jniThrowException(env, "java/lang/IllegalStateException", "Native line 01" );
        } else if ( opStatus != (status_t) OK ) {
            if (strlen(message) > 230) {
               // if the message is too long, don't bother displaying the status code
               jniThrowException( env, exception, message);
            } else {
               char msg[256];
                // append the status code to the message
               sprintf(msg, "%s: status=0x%X", message, opStatus);
               jniThrowException( env, exception, msg);
            }
        }
    }
}

static void
    com_media_ffmpeg_FFMpegPlayer_setDataSourceAndHeaders(
        JNIEnv *env, jobject thiz, jstring path, jobject headers) {

#if 0
    MediaPlayer* mp = getMediaPlayer(env, thiz);
    if (mp == NULL ) {
        jniThrowException(env, "java/lang/IllegalStateException", "player is null");
        return;
    }
#else

    if (mediaPlayer == NULL ) {

        jniThrowException(env, "java/lang/IllegalArgumentException", "player is null");
        return;
    }

    MediaPlayer * mp = mediaPlayer;


#endif
    if (path == NULL) {
        jniThrowException(env, "java/lang/IllegalArgumentException", "path is null");
        return;
    }

    const char *pathStr = env->GetStringUTFChars(path, NULL);
    if (pathStr == NULL) {  // Out of memory
        jniThrowException(env, "java/lang/RuntimeException", "Out of memory");
        return;
    }

    __android_log_print(ANDROID_LOG_INFO, TAG, "setDataSource: path %s", pathStr);
    status_t opStatus = mp->setDataSource(pathStr);

    __android_log_print(ANDROID_LOG_INFO, TAG, "setDataSource: opstatus: %d", opStatus );

    // Make sure that local ref is released before a potential exception
    env->ReleaseStringUTFChars(path, pathStr);

    process_media_player_call(
            env, thiz, opStatus, "java/io/IOException",
            "setDataSource failed." );
}

static void
com_media_ffmpeg_FFMpegPlayer_setDataSource(JNIEnv *env, jobject thiz, jstring path)
{
	com_media_ffmpeg_FFMpegPlayer_setDataSourceAndHeaders(env, thiz, path, 0);
}

static void
com_media_ffmpeg_FFMpegPlayer_setVideoSurface(JNIEnv *env, jobject thiz, jobject jsurface)
{
    MediaPlayer* mp = getMediaPlayer(env, thiz);
    if (mp == NULL ) {
        jniThrowException(env, "java/lang/IllegalStateException", NULL);
        return;
    }
	if (jsurface == NULL ) {
        jniThrowException(env, "java/lang/IllegalStateException", NULL);
        return;
    }
	process_media_player_call( env, thiz, mp->setVideoSurface(env, jsurface),
							  "java/io/IOException", "Set video surface failed.");
}

static void
com_media_ffmpeg_FFMpegPlayer_prepare(JNIEnv *env, jobject thiz)
{
    MediaPlayer* mp = getMediaPlayer(env, thiz);
    if (mp == NULL ) {
        jniThrowException(env, "java/lang/IllegalStateException", NULL);
        return;
    }
    process_media_player_call( env, thiz, mp->prepare(env, thiz), "java/io/IOException", "Prepare failed." );
}

static void
com_media_ffmpeg_FFMpegPlayer_start(JNIEnv *env, jobject thiz)
{
    MediaPlayer* mp = getMediaPlayer(env, thiz);
    if (mp == NULL ) {
        jniThrowException(env, "java/lang/IllegalStateException", NULL);
        return;
    }
    process_media_player_call( env, thiz, mp->start(), NULL, NULL );
}

static void
com_media_ffmpeg_FFMpegPlayer_stop(JNIEnv *env, jobject thiz)
{
    MediaPlayer* mp = getMediaPlayer(env, thiz);
    if (mp == NULL ) {
        jniThrowException(env, "java/lang/IllegalStateException", NULL);
        return;
    }
    process_media_player_call( env, thiz, mp->stop(), NULL, NULL );
}

static void
com_media_ffmpeg_FFMpegPlayer_pause(JNIEnv *env, jobject thiz)
{
    MediaPlayer* mp = getMediaPlayer(env, thiz);
    if (mp == NULL ) {
        jniThrowException(env, "java/lang/IllegalStateException", NULL);
        return;
    }
    process_media_player_call( env, thiz, mp->pause(), NULL, NULL );
}

static jboolean
com_media_ffmpeg_FFMpegPlayer_isPlaying(JNIEnv *env, jobject thiz)
{
    MediaPlayer* mp = getMediaPlayer(env, thiz);
    if (mp == NULL ) {
        jniThrowException(env, "java/lang/IllegalStateException", NULL);
        return false;
    }
    const jboolean is_playing = mp->isPlaying();
    return is_playing;
}

static jboolean
com_media_ffmpeg_FFMpegPlayer_isRecording(JNIEnv *env, jobject thiz)
{
    MediaPlayer* mp = getMediaPlayer(env, thiz);
    if (mp == NULL ) {
        jniThrowException(env, "java/lang/IllegalStateException", NULL);
        return false;
    }
    const jboolean is_recording = mp->isRecording();
    return is_recording;
}

static void
com_media_ffmpeg_FFMpegPlayer_seekTo(JNIEnv *env, jobject thiz, int msec)
{
    MediaPlayer* mp = getMediaPlayer(env, thiz);
    if (mp == NULL ) {
        jniThrowException(env, "java/lang/IllegalStateException", NULL);
        return;
    }
    process_media_player_call( env, thiz, mp->seekTo(msec), NULL, NULL );
}

static int
com_media_ffmpeg_FFMpegPlayer_getVideoWidth(JNIEnv *env, jobject thiz)
{
    MediaPlayer* mp = getMediaPlayer(env, thiz);
    if (mp == NULL ) {
        jniThrowException(env, "java/lang/IllegalStateException", NULL);
        return 0;
    }
    int w;
    if (0 != mp->getVideoWidth(&w)) {
        w = 0;
    }
    return w;
}

static int
com_media_ffmpeg_FFMpegPlayer_getVideoHeight(JNIEnv *env, jobject thiz)
{
    MediaPlayer* mp = getMediaPlayer(env, thiz);
    if (mp == NULL ) {
        jniThrowException(env, "java/lang/IllegalStateException", NULL);
        return 0;
    }
    int h;
    if (0 != mp->getVideoHeight(&h)) {
        h = 0;
    }
    return h;
}


static int
com_media_ffmpeg_FFMpegPlayer_getCurrentPosition(JNIEnv *env, jobject thiz)
{
    MediaPlayer* mp = getMediaPlayer(env, thiz);
    if (mp == NULL ) {
        jniThrowException(env, "java/lang/IllegalStateException", NULL);
        return 0;
    }
    int msec;
    process_media_player_call( env, thiz, mp->getCurrentPosition(&msec), NULL, NULL );
    return msec;
}

static int
com_media_ffmpeg_FFMpegPlayer_getDuration(JNIEnv *env, jobject thiz)
{
    MediaPlayer* mp = getMediaPlayer(env, thiz);
    if (mp == NULL ) {
        jniThrowException(env, "java/lang/IllegalStateException", NULL);
        return 0;
    }
    int msec;
    process_media_player_call( env, thiz, mp->getDuration(&msec), NULL, NULL );
    return msec;
}

static void
com_media_ffmpeg_FFMpegPlayer_reset(JNIEnv *env, jobject thiz)
{
    MediaPlayer* mp = getMediaPlayer(env, thiz);
    if (mp == NULL ) {
        jniThrowException(env, "java/lang/IllegalStateException", NULL);
        return;
    }
    process_media_player_call( env, thiz, mp->reset(), NULL, NULL );
}

static void
com_media_ffmpeg_FFMpegPlayer_setAudioStreamType(JNIEnv *env, jobject thiz, int streamtype)
{
    MediaPlayer* mp = getMediaPlayer(env, thiz);
    if (mp == NULL ) {
        jniThrowException(env, "java/lang/IllegalStateException", NULL);
        return;
    }
    process_media_player_call( env, thiz, mp->setAudioStreamType(streamtype) , NULL, NULL );
}

static void com_media_ffmpeg_FFMpegPlayer_setPlayOptions (JNIEnv * env, jobject thiz, jint opts)
{
    MediaPlayer* mp = getMediaPlayer(env, thiz);
    if (mp == NULL ) {
        jniThrowException(env, "java/lang/IllegalStateException", NULL);
        return;
    }
    process_media_player_call( env, thiz, mp->setPlayOption(opts) , NULL, NULL );

}



// ----------------------------------------------------------------------------

static void
com_media_ffmpeg_FFMpegPlayer_native_init(JNIEnv *env)
{
	__android_log_print(ANDROID_LOG_INFO, TAG, "native_init");
    jclass clazz;
    clazz = env->FindClass("com/media/ffmpeg/FFMpegPlayer");
    if (clazz == NULL) {
        jniThrowException(env, "java/lang/RuntimeException", "Can't find android/media/MediaPlayer");
        return;
    }

    fields.get_next_clip = env->GetMethodID(clazz, "getNextUrl", "()Ljava/lang/String;");
    if (fields.get_next_clip == NULL) {
    	__android_log_print(ANDROID_LOG_INFO, TAG, "Can't find FFMpegMediaPlayer.getNextUrl");
    	jniThrowException(env, "java/lang/RuntimeException", "Can't find FFMpegMediaPlayer.getNextUrl");
    	return;
    }

    fields.context = env->GetFieldID(clazz, "mNativeContext", "I");
    if (fields.context == NULL) {
        jniThrowException(env, "java/lang/RuntimeException", "Can't find MediaPlayer.mNativeContext");
        return;
    }

#if 0
    fields.post_event = env->GetStaticMethodID(clazz, "postEventFromNative",
                                                   "(Ljava/lang/Object;IIILjava/lang/Object;)V");
    if (fields.post_event == NULL) {
        jniThrowException(env, "java/lang/RuntimeException", "Can't find FFMpegMediaPlayer.postEventFromNative");
        return;
    }
#else
    fields.post_event = env->GetMethodID(clazz, "postEventFromNative",
                                                   "(Ljava/lang/Object;IIILjava/lang/Object;)V");
    if (fields.post_event == NULL) {
        jniThrowException(env, "java/lang/RuntimeException", "Can't find FFMpegMediaPlayer.postEventFromNative");
        return;
    }
#endif



}

static void
com_media_ffmpeg_FFMpegPlayer_native_setup(JNIEnv *env, jobject thiz, jobject weak_this, jboolean forPlayback)
{
	__android_log_print(ANDROID_LOG_INFO, TAG, "native_setup");

    mediaPlayer = new MediaPlayer(forPlayback);
    //Create a global media player here
    if(mediaPlayer == NULL)
    {
        jniThrowException(env, "java/lang/RuntimeException", "Out of memory");
        return;
    }
    // create new listener and give it to MediaPlayer
    JNIFFmpegMediaPlayerListener* listener = new JNIFFmpegMediaPlayerListener(env, thiz, weak_this);
    mediaPlayer->setListener(listener);

    mediaPlayer->setAudioBufferCallBack(env, thiz);


#if 0
    MediaPlayer* mp = new MediaPlayer();
    if (mp == NULL) {
        jniThrowException(env, "java/lang/RuntimeException", "Out of memory");
        return;
    }
    // create new listener and give it to MediaPlayer
    JNIFFmpegMediaPlayerListener* listener = new JNIFFmpegMediaPlayerListener(env, thiz, weak_this);
    mp->setListener(listener);

    // Stow our new C++ MediaPlayer in an opaque field in the Java object.
    setMediaPlayer(env, thiz, mp);
#endif
}

static void
com_media_ffmpeg_FFMpegPlayer_release(JNIEnv *env, jobject thiz)
{
	/*
    MediaPlayer* mp = getMediaPlayer(env, thiz);
    if (mp != NULL) {
        // this prevents native callbacks after the object is released
        mp->setListener(0);
        mp->disconnect();
    }
	*/
	if (mediaPlayer != NULL)
	{
		delete(mediaPlayer);
		mediaPlayer = NULL;
		env->SetIntField(thiz, fields.context, (int)NULL);
	}
	else
	{
		__android_log_print(ANDROID_LOG_INFO, TAG, "Media Player null, not need to release...\n");
	}
}

static void
com_media_ffmpeg_FFMpegPlayer_native_finalize(JNIEnv *env, jobject thiz)
{
    __android_log_print(ANDROID_LOG_INFO, TAG, "native_finalize");
    com_media_ffmpeg_FFMpegPlayer_release(env, thiz);
}

static jint
com_media_ffmpeg_FFMpegPlayer_native_suspend_resume(
        JNIEnv *env, jobject thiz, jboolean isSuspend) {
    MediaPlayer* mp = getMediaPlayer(env, thiz);
    if (mp == NULL ) {
        jniThrowException(env, "java/lang/IllegalStateException", NULL);
        return UNKNOWN_ERROR;
    }

    return isSuspend ? mp->suspend() : mp->resume();
}

static jbyteArray
com_media_ffmpeg_FFMpegPlayer_native_getSnapShot(JNIEnv *env, jobject thiz)
{
	int len;
	void* buffer;
	mediaPlayer->getSnapShot(&buffer, &len);
	jbyteArray bytes;
	bytes = env->NewByteArray(len);
	if (bytes != NULL) {
		env->SetByteArrayRegion(bytes, 0, len, (jbyte*) buffer);
	}

	return bytes;
}
static void
com_media_ffmpeg_FFMpegPlayer_native_startRecord(JNIEnv * env, jobject thiz, jstring fileName)
{
    if (fileName == NULL)
    {
        jniThrowException(env, "java/lang/IllegalArgumentException", "fileName is NULL");
        return;
    }

    const char * pathStr   = env->GetStringUTFChars(fileName , NULL);
    if (pathStr == NULL) {  // Out of memory
        jniThrowException(env, "java/lang/RuntimeException", "Out of memory");
        return;
    }

    __android_log_print(ANDROID_LOG_INFO, TAG, "startRecording: path %s", pathStr);
    status_t opStatus = mediaPlayer->startRecord(pathStr);


    // Make sure that local ref is released before a potential exception
    env->ReleaseStringUTFChars(fileName, pathStr);

    process_media_player_call(
            env, thiz, opStatus, "java/io/IOException",
            "startRecord  failed." );
    return;
}


static void
com_media_ffmpeg_FFMpegPlayer_native_stopRecord(JNIEnv *env, jobject thiz)
{

    __android_log_print(ANDROID_LOG_INFO, TAG, "stopRecording: ");

    status_t opStatus = mediaPlayer->stopRecord();

    return;
}

// ----------------------------------------------------------------------------




static JNINativeMethod gMethods[] = {
    {"setDataSource",       "(Ljava/lang/String;)V",            (void *)com_media_ffmpeg_FFMpegPlayer_setDataSource},
    {"_setVideoSurface",    "(Landroid/view/Surface;)V",        (void *)com_media_ffmpeg_FFMpegPlayer_setVideoSurface},
    {"_prepare",             "()V",                              (void *)com_media_ffmpeg_FFMpegPlayer_prepare},
    {"_start",              "()V",                              (void *)com_media_ffmpeg_FFMpegPlayer_start},
    {"_stop",               "()V",                              (void *)com_media_ffmpeg_FFMpegPlayer_stop},
    {"getVideoWidth",       "()I",                              (void *)com_media_ffmpeg_FFMpegPlayer_getVideoWidth},
    {"getVideoHeight",      "()I",                              (void *)com_media_ffmpeg_FFMpegPlayer_getVideoHeight},
    {"seekTo",              "(I)V",                             (void *)com_media_ffmpeg_FFMpegPlayer_seekTo},
    {"_pause",              "()V",                              (void *)com_media_ffmpeg_FFMpegPlayer_pause},
    {"isPlaying",           "()Z",                              (void *)com_media_ffmpeg_FFMpegPlayer_isPlaying},
    {"isRecording",           "()Z",                            (void *)com_media_ffmpeg_FFMpegPlayer_isRecording},
    {"getCurrentPosition",  "()I",                              (void *)com_media_ffmpeg_FFMpegPlayer_getCurrentPosition},
    {"getDuration",         "()I",                              (void *)com_media_ffmpeg_FFMpegPlayer_getDuration},
    {"_release",            "()V",                              (void *)com_media_ffmpeg_FFMpegPlayer_release},
    {"_reset",              "()V",                              (void *)com_media_ffmpeg_FFMpegPlayer_reset},
    {"setAudioStreamType",  "(I)V",                             (void *)com_media_ffmpeg_FFMpegPlayer_setAudioStreamType},
    {"native_init",         "()V",                              (void *)com_media_ffmpeg_FFMpegPlayer_native_init},
    {"native_setup",        "(Ljava/lang/Object;Z)V",            (void *)com_media_ffmpeg_FFMpegPlayer_native_setup},
    {"native_finalize",     "()V",                              (void *)com_media_ffmpeg_FFMpegPlayer_native_finalize},
    {"native_suspend_resume", "(Z)I",                           (void *)com_media_ffmpeg_FFMpegPlayer_native_suspend_resume},
    {"setPlayOption", "(I)V",                                  (void*) com_media_ffmpeg_FFMpegPlayer_setPlayOptions},
    {"native_getSnapShot", 		"()[B", 							(void*) com_media_ffmpeg_FFMpegPlayer_native_getSnapShot},
    {"native_startRecord", "(Ljava/lang/String;)V",             (void*) com_media_ffmpeg_FFMpegPlayer_native_startRecord},
    {"native_stopRecord", "()V",             (void*) com_media_ffmpeg_FFMpegPlayer_native_stopRecord}
};

int register_android_media_FFMpegPlayerAndroid(JNIEnv *env) {
	return jniRegisterNativeMethods(env, kClassPathName, gMethods, sizeof(gMethods) / sizeof(gMethods[0]));
}
