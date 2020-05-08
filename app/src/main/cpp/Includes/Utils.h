#ifndef UTILS_H
#define UTILS_H
#include <jni.h>
#include <unistd.h>
#if defined(__aarch64__)
#include "X64Hook/And64InlineHook.hpp"
#else
#include "Substrate/CydiaSubstrate.h"
#endif

typedef unsigned long DWORD;
static DWORD libBase;
const char* libName = "libil2cpp.so";

DWORD findLibrary(const char *library);
DWORD getAbsoluteAddress(DWORD relativeAddr, const char *libraryName);
bool isLibraryLoaded(const char *libraryName);
void MakeToast(JNIEnv* env, jobject thiz, const char* text);
void octo_hook(void *orig_fcn, void* new_fcn, void **orig_fcn_ptr);

DWORD findLibrary(const char *library) {
    char filename[0xFF] = {0},
            buffer[1024] = {0};
    FILE *fp = NULL;
    DWORD address = 0;

    //sprintf( filename, "/proc/self/maps"); Buffer Overflow warning. shit is depreceated
    snprintf(filename, sizeof(filename) ,"/proc/self/maps");

    fp = fopen( filename, "rt" );
    if( fp == NULL ){
        perror("fopen");
        goto done;
    }

    while( fgets( buffer, sizeof(buffer), fp ) ) {
        if( strstr( buffer, library ) ){
            address = (DWORD)strtoul( buffer, NULL, 16 );
            goto done;
        }
    }

    done:

    if(fp){
        fclose(fp);
    }

    return address;
}

DWORD getAbsoluteAddress(DWORD relativeAddr, const char *libraryName = libName) {
    if (libBase == 0)
        libBase = findLibrary(libraryName);
    if (libBase != 0)
        return (reinterpret_cast<DWORD>(libBase + relativeAddr));
    else
        return 0;
}

bool isLibraryLoaded(const char *libraryName) {
    char line[512] = {0};
    FILE *fp = fopen("/proc/self/maps", "rt");
    if (fp != NULL) {
        while (fgets(line, sizeof(line), fp)) {
            if (strstr(line, libraryName))
                return true;
        }
        fclose(fp);
    }
    return false;
}

void MakeToast(JNIEnv* env, jobject thiz, const char* text){
    //Add our toast in here so it wont be easy to change by simply editing the smali and cant
    //be cut out because this method is needed to start the hack (I'm smart)
    jstring jstr = env->NewStringUTF(text); //Edit this text to your desired toast message!
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

void octo_hook(void *orig_fcn, void* new_fcn, void **orig_fcn_ptr)
{
#if defined(__aarch64__)
    A64HookFunction(orig_fcn, new_fcn, orig_fcn_ptr);
#else
    MSHookFunction(orig_fcn, new_fcn, orig_fcn_ptr);
#endif
}
#endif
