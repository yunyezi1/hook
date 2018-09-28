#include "jni.h"
#include "android_runtime/AndroidRuntime.h"
//#include "android/log.h"
#include "stdio.h"
#include "stdlib.h"
#include "config.h"
#include <fcntl.h>
#include <sys/stat.h>
//#define log(a,b) __android_log_write(ANDROID_LOG_INFO,a,b); //
//#define log_(b) __android_log_write(ANDROID_LOG_INFO,"Inject",b); //

#define TAG "Inject"
static bool g_bAttatedT;
static JavaVM *g_JavaVM;

void initJVM()
{
	g_bAttatedT = false;
	g_JavaVM = android::AndroidRuntime::getJavaVM();
}

static JNIEnv *GetEnv()
{
	int status;
	JNIEnv *envnow = NULL;
	status = g_JavaVM->GetEnv((void **)&envnow, JNI_VERSION_1_4);
	if(status < 0)
	{
		status = g_JavaVM->AttachCurrentThread(&envnow, NULL);
		if(status < 0)
		{
			return NULL;
		}
		g_bAttatedT = true;
	}
	return envnow;
}

static void DetachCurrent()
{
	if(g_bAttatedT)
	{
		g_JavaVM->DetachCurrentThread();
	}
}

///java调用的jni方法
/*参数说明：
* path1: jar存储路径
* path2： 经过优化后的dex存存放路径
* className： 需要调用jar包中的类名
* methodName： 需要调用的类中的静态方法
* 注意：path1 和path2将会作为DexClassLoader构造函数中的第一、第二参数
*/
static void reflectLoadJar(JNIEnv *env, jstring path1, jstring path2, jstring className, jstring methodName) {
	//找到ClassLoader类
	jclass classloaderClass = env->FindClass("java/lang/ClassLoader");
	//找到ClassLoader类中的静态方法getSystemClassLoader
	jmethodID getsysloaderMethod = env->GetStaticMethodID(classloaderClass, "getSystemClassLoader","()Ljava/lang/ClassLoader;");
	//调用ClassLoader中的getSystemClassLoader方法，返回ClassLoader对象
	jobject loader =env->CallStaticObjectMethod(classloaderClass,getsysloaderMethod);

	//jar包存放位置
	jstring dexpath = path1;
	//优化后的jar包存放位置
	jstring dex_odex_path = path2;
	//找到DexClassLoader类
	jclass dexLoaderClass = env->FindClass("dalvik/system/DexClassLoader");
	//获取DexClassLoader的构造函数ID
	jmethodID initDexLoaderMethod =env->GetMethodID(dexLoaderClass, "<init>","(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;)V");
	//新建一个DexClassLoader对象
	jobject dexLoader =env->NewObject(dexLoaderClass,initDexLoaderMethod, dexpath, dex_odex_path, NULL, loader);

	//找到DexClassLoader中的方法findClass
	jmethodID findclassMethod = env->GetMethodID(dexLoaderClass,"findClass", "(Ljava/lang/String;)Ljava/lang/Class;");
	//如果返回空,那就找DexClassLoader的loadClass方法
	//说明：老版本的SDK中DexClassLoader有findClass方法，新版本SDK中是loadClass方法
	if(NULL==findclassMethod)
	{
		//
		findclassMethod = env->GetMethodID(dexLoaderClass,"loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
	}
	//存储需要调用的类
	jstring javaClassName = className;
	//调用DexClassLoader的loadClass方法，加载需要调用的类
	jclass javaClientClass=(jclass)env->CallObjectMethod(dexLoader,findclassMethod,javaClassName);

	//将jstring类型的方法名转换为utf8编码的字符串
	const char* func =env->GetStringUTFChars(methodName, NULL);
	//获取加载的类中的方法
	//
	jmethodID inject_method = env->GetStaticMethodID(javaClientClass, func, "()V");
	//调用加载的类中的静态方法
	env->CallStaticVoidMethod(javaClientClass,inject_method);
}

void cutdir(const char *path,char* buf){
	int len = strlen(path);
	int index = len;
	for(int i=len;i>=0;i--){
		if(*(path+i)=='/' || *(path+i)=='\\'){
			index = i;
			break;
		}
	}
	memcpy(buf,path,index);
}

int mkdirs(const char *sPathName)
{
	char DirName[256];
	strcpy(DirName, sPathName);
	int i,len=strlen(DirName);
	if(DirName[len-1]!='/')
		strcat(DirName,"/");
	len=strlen(DirName);
	for(i=1;i<len;i++)
	{
		if(DirName[i]=='/')
		{
			DirName[i]=0;
			if(access(DirName,0)!=0)
			{
				if(mkdir(DirName,0777)==-1)
				{
//                      perror("mkdir   error");
                      return -1;
				}
			}
			DirName[i]='/';
		}
	}
	return 0;
}

char* parasePath(char*arg,char*buf){
	if(arg == NULL)
		return arg;
	int len = strlen(arg);
	for(int i=0;i<len;i++){
		if(arg[i]=='@'){
			arg[i]='\0';
			if(NULL != buf)
				sprintf(buf,"/data/data/%s/files/cache",(char*)(arg+i+1));
			break;
		}
	}
	return arg;
}
extern "C" void InjectInterface(char*arg){
	char dir[255]={0};
	char c_dexPath[255]={0};
	if(arg == NULL)
		return;
	memcpy(dir,arg,strlen(arg));
	LOGI_(TAG,"*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*");
	LOGI_(TAG,"*-*-*-*-*-* Injected so *-*-*-*-*-*-*-*");
	LOGI_(TAG,"*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*");
	LOGI_(TAG,"param:%s",dir);
	initJVM();
	JNIEnv *jenv = GetEnv();
	if(jenv == NULL)
		return;
	char* c_jarPath = parasePath(dir,c_dexPath);
//	cutdir(arg,dir);
	LOGI_(TAG,"path1:%s;path2:%s",c_jarPath,c_dexPath);
	if(mkdirs(c_dexPath)<0){
		DetachCurrent();
		return;
	}
	const char* c_mainCls = "com.spread.inject.GlobalPatch";
	const char* c_mainMtd = "main";
	jstring jarPath = jenv->NewStringUTF(c_jarPath);
	jstring dexPath = jenv->NewStringUTF(c_dexPath);
	jstring mainClass = jenv->NewStringUTF(c_mainCls);
	jstring mainMethod = jenv->NewStringUTF(c_mainMtd);
	LOGI_(TAG,"env target:%ld",(long)jenv);
	reflectLoadJar(jenv,jarPath,dexPath,mainClass,mainMethod);
	LOGI_(TAG,"call reflect done!");
	DetachCurrent();
	LOGI_(TAG,"*-*-*-*-*-*-*- End -*-*-*-*-*-*-*-*-*-*");
}
