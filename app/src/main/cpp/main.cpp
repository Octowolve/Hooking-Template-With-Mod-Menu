#include <pthread.h>
#include <jni.h>
#include <memory.h>
#include <dlfcn.h>
#include <cstdio>
#include <cstdlib>

#include "Includes/Logger.h"
#include "Patching/Patch.h"
#import "Includes/Utils.h"

#if defined(__aarch64__)
#include "X64Hook/And64InlineHook.hpp"
#else
#include "Substrate/CydiaSubstrate.h"
#endif

bool exampleBooleanForToggle;
int seekbarValueExample;
const char* spinnerExampleString;

struct Patches{
    Patch *miniMap;
}patch;

bool GameManagerLateUpdateHookInitialized = false;
const char* libName = "libil2cpp.so";

void octo_hook(void *orig_fcn, void* new_fcn, void **orig_fcn_ptr)
{
#if defined(__aarch64__)
    A64HookFunction(orig_fcn, new_fcn, orig_fcn_ptr);
#else
    MSHookFunction(orig_fcn, new_fcn, orig_fcn_ptr);
#endif
}


void(*old_GameManager_LateUpdate)(void *instance);
void GameManager_LateUpdate(void *instance) {
    //Check if instance is NULL to prevent crashes!  If the instance object is NULL,
    //this is what the call to update would look like in C++:
    //NULL.Update(); and dat doesnt make sense right?
    //Also check if our example boolean is true so the hack will work then. if not it just returns the old method
    if(instance != NULL) {
        if(!GameManagerLateUpdateHookInitialized){
            //Check if this hook initialized. If so log
            GameManagerLateUpdateHookInitialized = true;
            LOGI("GameManager_LateUpdate hooked");
        }
        //Your code here
    }
    old_GameManager_LateUpdate(instance);
}


// we will run our patches in a new thread so our while loop doesn't block process main thread
void* hack_thread(void*) {
    LOGI("I have been loaded. Mwuahahahaha");
    // loop until our target library is found
    do {
        sleep(1);
    } while (!isLibraryLoaded(libName));
    LOGI("I found the il2cpp lib. Address is: %p", (void*)findLibrary(libName));
    LOGI("Hooking GameManager_LateUpdate");
    octo_hook((void*)getAbsoluteAddress(libName, 0x7000DD0), (void*)GameManager_LateUpdate, (void**)&old_GameManager_LateUpdate);
    patch.miniMap = Patch::Setup((void*)getAbsoluteAddress(libName, 0xF09D64), (char*)"\x01\x00\xa0\xe3\x1e\xff\x2f\xe1", 8);
    return NULL;
}

extern "C"
JNIEXPORT jobjectArray JNICALL Java_com_dark_force_NativeLibrary_getListFT(JNIEnv *env, jclass jobj){
    jobjectArray ret;
    int i;
    const char *features[]= {"Example Toggle", "SeekBar_Slider_0_500", "Spinner_TestSpinner_weaponsList", "Spacing_Who the fuck knows", "EditText_Test_this is an example hint"};
    int Total_Feature = (sizeof features / sizeof features[0]); //Now you dont have to manually update the number everytime
    ret= (jobjectArray)env->NewObjectArray(Total_Feature,
                                           env->FindClass("java/lang/String"),
                                           env->NewStringUTF(""));

    for(i=0;i<Total_Feature;i++) {
        env->SetObjectArrayElement(
                ret,i,env->NewStringUTF(features[i]));
    }
    return(ret);
}


extern "C"
JNIEXPORT void JNICALL Java_com_dark_force_NativeLibrary_changeToggle(JNIEnv *env, jclass thisObj, jint number) {
    int i = (int) number;
    switch (i) {
        case 0:
            exampleBooleanForToggle = !exampleBooleanForToggle;
            if (exampleBooleanForToggle) {
                patch.miniMap->Apply();
            } else {
                patch.miniMap->Reset();
            }
            break;
        default:
            break;
    }
    return;
}


extern "C"
JNIEXPORT void JNICALL Java_com_dark_force_NativeLibrary_init(JNIEnv * env, jclass obj, jobject thiz){
    pthread_t ptid;
    pthread_create(&ptid, NULL, hack_thread, NULL);

    //Add our toast in here so it wont be easy to change by simply editing the smali and cant
    //be cut out because this method is needed to start the hack (I'm smart)
    jstring jstr = env->NewStringUTF("No u"); //Edit this text to your desired toast message!
    jclass toast = env->FindClass("android/widget/Toast");
    jmethodID methodMakeText =
            env->GetStaticMethodID(
                    toast,
                    "makeText",
                    "(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;");
    if (methodMakeText == NULL) {
        LOGE("toast.makeText not Found");
        return;
    }
    //The last int is the length on how long the toast should be displayed
    //0 = Short, 1 = Long
    jobject toastobj = env->CallStaticObjectMethod(toast, methodMakeText,
                                                      thiz, jstr, 0);

    jmethodID methodShow = env->GetMethodID(toast, "show", "()V");
    if (methodShow == NULL) {
        LOGE("toast.show not Found");
        return;
    }
    env->CallVoidMethod(toastobj, methodShow);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_dark_force_NativeLibrary_changeSeekBar(JNIEnv *env, jclass clazz, jint i, jint seekbarValue) {
    int li = (int) i;
    switch (li) {
        case 2:
            seekbarValueExample = seekbarValue;
            break;
        default:
            break;
    }
    return;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_dark_force_NativeLibrary_changeSpinner(JNIEnv *env, jclass clazz, jint i, jstring value) {
    int li = (int) i;
    switch (li) {
        case 3:
            spinnerExampleString = env->GetStringUTFChars(value, 0);
            break;
        default:
            break;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_dark_force_NativeLibrary_changeEditText(JNIEnv *env, jclass clazz, jint i, jstring value) {
    int li = (int) i;
    switch (li){
        default:
            break;
    }
    return;
}
