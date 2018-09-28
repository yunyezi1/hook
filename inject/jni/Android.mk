LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE:= so

LOCAL_SRC_FILES := so.cpp

LOCAL_LDLIBS+= 

LOCAL_CFLAGS    := -I./jni/include/ -I./jni/dalvik/vm/ -I./jni/dalvik -DHAVE_LITTLE_ENDIAN

LOCAL_LDFLAGS	:=	-L./jni/lib/  -L$(SYSROOT)/usr/lib -llog -ldvm -landroid_runtime

LOCAL_SHARED_LIBRARIES :=
include $(BUILD_SHARED_LIBRARY)
#==================================================================

include $(CLEAR_VARS)

LOCAL_MODULE:= inject

LOCAL_SRC_FILES := inject.c shellcode.s

#使用Android的Log日志系统
LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog -lc
#LOCAL_FORCE_STATIC_EXECUTABLE := true  
LOCAL_SHARED_LIBRARIES := liblog libcutils
LOCAL_CFLAGS :=  

include $(BUILD_EXECUTABLE)