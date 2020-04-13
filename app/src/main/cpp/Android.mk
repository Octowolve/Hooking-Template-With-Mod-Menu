LOCAL_PATH := $(call my-dir)
MAIN_LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := wolve

LOCAL_CFLAGS := -Wno-error=format-security -fpermissive
LOCAL_CFLAGS += -fno-rtti -fno-exceptions

LOCAL_C_INCLUDES += $(MAIN_LOCAL_PATH)

LOCAL_SRC_FILES := main.cpp \
		   Patching/Patch.cpp \
		   Substrate/hde64.c \
           Substrate/SubstrateDebug.cpp \
           Substrate/SubstrateHook.cpp \
           Substrate/SubstratePosixMemory.cpp \
           X64Hook\And64InlineHook.cpp \

LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
