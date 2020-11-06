#include <pthread.h>
#include <jni.h>
#include <memory.h>
#include <dlfcn.h>
#include <cstdio>
#include <cstdlib>

#include "Includes/Logger.h"
#include "Patching/Patch.h"
#include "Includes/Utils.h"

bool exampleBooleanForToggle;
int seekbarValueExample;
const char* spinnerExampleString;

struct Patches{
    Patch *miniMap;
}patch;

bool GameManagerLateUpdateHookInitialized = false;

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
    octo_hook((void*)getAbsoluteAddress(0x7000DD0), (void*)GameManager_LateUpdate, (void**)&old_GameManager_LateUpdate);
    patch.miniMap = Patch::Setup((void*)getAbsoluteAddress(0xF09D64), (char*)"\x01\x00\xa0\xe3\x1e\xff\x2f\xe1", 8);
    return NULL;
}

jobjectArray getListFT(JNIEnv *env, jclass jobj){
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


void changeToggle(JNIEnv *env, jclass thisObj, jint number) {
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


void init(JNIEnv * env, jclass obj, jobject thiz){
    pthread_t ptid;
    pthread_create(&ptid, NULL, hack_thread, NULL);
    MakeToast(env, thiz, "Mod by Octowolve/Silence - https://piin.dev");
}

void changeSeekBar(JNIEnv *env, jclass clazz, jint i, jint seekbarValue) {
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

void changeSpinner(JNIEnv *env, jclass clazz, jint i, jstring value) {
    int li = (int) i;
    switch (li) {
        case 3:
            spinnerExampleString = env->GetStringUTFChars(value, 0);
            break;
        default:
            break;
    }
}

void changeEditText(JNIEnv *env, jclass clazz, jint i, jstring value){
    int li = (int) i;
    switch (li){
        default:
            break;
    }
    return;
}

extern "C"
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    // Find your class. JNI_OnLoad is called from the correct class loader context for this to work.
    jclass c = env->FindClass("com/dark/force/NativeLibrary");
    if (c == nullptr) return JNI_ERR;

    // Register your class' native methods.
    static const JNINativeMethod methods[] = {
            {"changeEditText", "(ILjava/lang/String;)V", reinterpret_cast<void*>(changeEditText)},
            {"changeSeekBar", "(II)V", reinterpret_cast<void*>(changeSeekBar)},
            {"changeSpinner", "(ILjava/lang/String;)V", reinterpret_cast<void*>(changeSpinner)},
            {"changeToggle", "(I)V", reinterpret_cast<void*>(changeToggle)},
            {"getListFT", "()[Ljava/lang/String;", reinterpret_cast<void*>(getListFT)},
            {"init", "(Lcom/dark/force/MenuService;)V", reinterpret_cast<void*>(init)},
    };
    int rc = env->RegisterNatives(c, methods, sizeof(methods)/sizeof(JNINativeMethod));
    if (rc != JNI_OK) return rc;

    return JNI_VERSION_1_6;
}
