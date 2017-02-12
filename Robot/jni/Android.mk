LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE:= libPwm
LOCAL_SRC_FILES:= pwm.c
LOCAL_LDLIBS :=-llog
include $(BUILD_SHARED_LIBRARY)