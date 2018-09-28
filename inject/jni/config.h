#include <android/log.h>
//是否编译日志系统模块，目前需要始终定义
#define LOG_MODULE

//日志系统自身的的调试输出控制, 如要调试，必须包含此头文件
#define DEBUG 1

#define LOG_MAX_FILE_COUNT 10

#define LEVEL_NONE			999
#define LEVEL_VERBOSE		0
#define LEVEL_DEBUG			1
#define LEVEL_INFO			2
#define LEVEL_WARN			3
#define LEVEL_ERROR			4


//日志级别，默认只写入错误日志
//PS：不定义则不写入任何日志
#define ROOT_LOG_LEVEL	LEVEL_ERROR
#include "log_for_android.h"
