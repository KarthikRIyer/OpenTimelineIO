#include <exceptions.h>
#include <handle.h>
#include <io_opentimeline_opentimelineio_Timeline.h>
#include <opentimelineio/errorStatus.h>
#include <opentimelineio/timeline.h>
#include <opentimelineio/version.h>
#include <utilities.h>

/*
 * Class:     io_opentimeline_opentimelineio_Timeline
 * Method:    initialize
 * Signature: (Ljava/lang/String;Lio/opentimeline/opentime/RationalTime;Lio/opentimeline/opentimelineio/AnyDictionary;)V
 */
JNIEXPORT void JNICALL
Java_io_opentimeline_opentimelineio_Timeline_initialize(
    JNIEnv* env,
    jobject thisObj,
    jstring name,
    jobject globalStartTimeRationalTime,
    jobject metadataObj)
{
    if(name == nullptr || metadataObj == nullptr)
        throwNullPointerException(env, "");
    else
    {
        OTIO_NS::optional<opentime::RationalTime> globalStartTime =
            OTIO_NS::nullopt;
        if(globalStartTimeRationalTime != nullptr)
            globalStartTime =
                rationalTimeFromJObject(env, globalStartTimeRationalTime);
        std::string nameStr = env->GetStringUTFChars(name, 0);
        auto        metadataHandle =
            getHandle<OTIO_NS::AnyDictionary>(env, metadataObj);
        auto timeline =
            new OTIO_NS::Timeline(nameStr, globalStartTime, *metadataHandle);
        setHandle(env, thisObj, timeline);
    }
}

/*
 * Class:     io_opentimeline_opentimelineio_Timeline
 * Method:    getTracks
 * Signature: ()Lio/opentimeline/opentimelineio/Stack;
 */
JNIEXPORT jobject JNICALL
Java_io_opentimeline_opentimelineio_Timeline_getTracks(
    JNIEnv* env, jobject thisObj)
{
    auto thisHandle = getHandle<OTIO_NS::Timeline>(env, thisObj);
    auto result     = thisHandle->tracks();
    return stackFromNative(env, result);
}

/*
 * Class:     io_opentimeline_opentimelineio_Timeline
 * Method:    setTracks
 * Signature: (Lio/opentimeline/opentimelineio/Stack;)V
 */
JNIEXPORT void JNICALL
Java_io_opentimeline_opentimelineio_Timeline_setTracks(
    JNIEnv* env, jobject thisObj, jobject stackObj)
{
    auto thisHandle  = getHandle<OTIO_NS::Timeline>(env, thisObj);
    auto stackHandle = getHandle<OTIO_NS::Stack>(env, thisObj);
    thisHandle->set_tracks(stackHandle);
}

/*
 * Class:     io_opentimeline_opentimelineio_Timeline
 * Method:    getGlobalStartTime
 * Signature: ()Lio/opentimeline/opentime/RationalTime;
 */
JNIEXPORT jobject JNICALL
Java_io_opentimeline_opentimelineio_Timeline_getGlobalStartTime(
    JNIEnv* env, jobject thisObj)
{
    auto    thisHandle = getHandle<OTIO_NS::Timeline>(env, thisObj);
    auto    result     = thisHandle->global_start_time();
    jobject resultObj  = nullptr;
    if(result != OTIO_NS::nullopt)
        resultObj = rationalTimeToJObject(env, result.value());
    return resultObj;
}

/*
 * Class:     io_opentimeline_opentimelineio_Timeline
 * Method:    setGlobalStartTime
 * Signature: (Lio/opentimeline/opentime/RationalTime;)V
 */
JNIEXPORT void JNICALL
Java_io_opentimeline_opentimelineio_Timeline_setGlobalStartTime(
    JNIEnv* env, jobject thisObj, jobject globalStartTimeRationalTime)
{
    auto thisHandle = getHandle<OTIO_NS::Timeline>(env, thisObj);
    OTIO_NS::optional<OTIO_NS::RationalTime> globalStartTime = OTIO_NS::nullopt;
    if(globalStartTimeRationalTime != nullptr)
        globalStartTime =
            rationalTimeFromJObject(env, globalStartTimeRationalTime);
    thisHandle->set_global_start_time(globalStartTime);
}

/*
 * Class:     io_opentimeline_opentimelineio_Timeline
 * Method:    getDuration
 * Signature: (Lio/opentimeline/opentimelineio/ErrorStatus;)Lio/opentimeline/opentime/RationalTime;
 */
JNIEXPORT jobject JNICALL
Java_io_opentimeline_opentimelineio_Timeline_getDuration(
    JNIEnv* env, jobject thisObj, jobject errorStatusObj)
{
    auto thisHandle = getHandle<OTIO_NS::Timeline>(env, thisObj);
    auto errorStatusHandle =
        getHandle<OTIO_NS::ErrorStatus>(env, errorStatusObj);
    auto result = thisHandle->duration(errorStatusHandle);
    return rationalTimeToJObject(env, result);
}

/*
 * Class:     io_opentimeline_opentimelineio_Timeline
 * Method:    getRangeOfChild
 * Signature: (Lio/opentimeline/opentimelineio/Composable;Lio/opentimeline/opentimelineio/ErrorStatus;)Lio/opentimeline/opentime/TimeRange;
 */
JNIEXPORT jobject JNICALL
Java_io_opentimeline_opentimelineio_Timeline_getRangeOfChild(
    JNIEnv* env,
    jobject thisObj,
    jobject composableChild,
    jobject errorStatusObj)
{
    auto thisHandle  = getHandle<OTIO_NS::Timeline>(env, thisObj);
    auto childHandle = getHandle<OTIO_NS::Composable>(env, composableChild);
    auto errorStatusHandle =
        getHandle<OTIO_NS::ErrorStatus>(env, errorStatusObj);
    auto result = thisHandle->range_of_child(childHandle, errorStatusHandle);
    return timeRangeToJObject(env, result);
}

/*
 * Class:     io_opentimeline_opentimelineio_Timeline
 * Method:    getAudioTracksNative
 * Signature: ()[Lio/opentimeline/opentimelineio/Track;
 */
JNIEXPORT jobjectArray JNICALL
Java_io_opentimeline_opentimelineio_Timeline_getAudioTracksNative(
    JNIEnv* env, jobject thisObj)
{
    auto thisHandle = getHandle<OTIO_NS::Timeline>(env, thisObj);
    auto result     = thisHandle->audio_tracks();
    return trackVectorToArray(env, result);
}

/*
 * Class:     io_opentimeline_opentimelineio_Timeline
 * Method:    getVideoTracksNative
 * Signature: ()[Lio/opentimeline/opentimelineio/Track;
 */
JNIEXPORT jobjectArray JNICALL
Java_io_opentimeline_opentimelineio_Timeline_getVideoTracksNative(
    JNIEnv* env, jobject thisObj)
{
    auto thisHandle = getHandle<OTIO_NS::Timeline>(env, thisObj);
    auto result     = thisHandle->video_tracks();
    return trackVectorToArray(env, result);
}
