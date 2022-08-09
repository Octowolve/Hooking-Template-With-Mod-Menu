#pragma once
#include <jni.h>
#include <unistd.h>

const char* libName = "libil2cpp.so";

namespace utils{
    bool is_library_loaded(const char *lib) {
        char line[512] = {0};
        FILE *fp = fopen("/proc/self/maps", "rt");
        if (fp != NULL) {
            while (fgets(line, sizeof(line), fp)) {
                if (strstr(line, lib))
                    return true;
            }
            fclose(fp);
        }
        return false;
    }

    uintptr_t find_library(const char *library_name){
        FILE *fp = fopen("/proc/self/maps", "rt");
        if (fp != NULL)
        {
            char line[512], mod_name[64];
            std::uintptr_t base;
            while (fgets(line, sizeof(line), fp))
                if (std::sscanf(line, "%llx-%*llx %*s %*ld %*s %*d %s", &base, mod_name))
                    if (std::strstr(mod_name, library_name)){
                        fclose(fp);
                        return base;
                    }
        }
        return NULL;
    }

    uintptr_t get_absolute_address(uintptr_t relative_addr, const char* lib_name = "libil2cpp.so"){
        uintptr_t lib_base = find_library(lib_name);
        if (lib_base == 0 || lib_base == NULL)
            return 0;
        return (lib_base + relative_addr);
    }

    void make_toast(JNIEnv* env, jobject thiz, const char* text){
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
}