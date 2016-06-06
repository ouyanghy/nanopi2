#include <jni.h>
#include <android/log.h>
#include <linux/ioctl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>

#define LOGD(...) __android_log_print(3, "pwm-pca9685:", __VA_ARGS__)

#define PCA9685_PWM_IOCTL_CODE  0x80
#define PCA9685_PWM_REQUEST   	_IOWR(PCA9685_PWM_IOCTL_CODE, 11, unsigned int)
#define PCA9685_PWM_FREE  		_IOWR(PCA9685_PWM_IOCTL_CODE, 12, unsigned int)
#define PCA9685_PWM_CONFIG   	_IOWR(PCA9685_PWM_IOCTL_CODE, 13, unsigned int)
#define PCA9685_PWM_UNCONFIG   	_IOWR(PCA9685_PWM_IOCTL_CODE, 14, unsigned int)
#define PCA9685_PWM_HZ   		_IOWR(PCA9685_PWM_IOCTL_CODE, 15, unsigned int)

static int fd = -1;
JNIEXPORT jint JNICALL Java_com_oo_pwm_Pwm_open(JNIEnv * env, jobject jobj)
{
    LOGD(" open\n");//��ӡLog
    fd = open("/dev/pca9685", O_RDWR);
    if (fd < 0)
    	return -1;
    return 0;
}

JNIEXPORT jint JNICALL Java_com_oo_pwm_Pwm_close(JNIEnv * env, jobject jobj)
{
	return close(fd);

}

JNIEXPORT jint JNICALL Java_com_oo_pwm_Pwm_unconfig(JNIEnv * env, jobject jobj, jint channel)
{
    LOGD(" unconfig\n");
    return ioctl(fd, PCA9685_PWM_UNCONFIG, channel);
}


JNIEXPORT jint JNICALL Java_com_oo_pwm_Pwm_request(JNIEnv * env, jobject jobj)
{
    LOGD(" request\n");
    return ioctl(fd, PCA9685_PWM_REQUEST, 0);
}

JNIEXPORT jint JNICALL Java_com_oo_pwm_Pwm_free(JNIEnv * env, jobject jobj)
{
    LOGD(" free\n");
    return ioctl(fd, PCA9685_PWM_FREE, 0);
}

JNIEXPORT jint JNICALL Java_com_oo_pwm_Pwm_config(JNIEnv * env, jobject jobj, jint channel, jint on, jint off)
{
	unsigned long arg = channel << 24 | on << 12 | off;
    LOGD(" config,channel:%d, on:%d, off:%d\n", channel, on, off);
    return ioctl(fd, PCA9685_PWM_CONFIG, arg);
}

JNIEXPORT jint JNICALL Java_com_oo_pwm_Pwm_hz(JNIEnv * env, jobject jobj, jint hz)
{
    LOGD(" set hz\n");
    return ioctl(fd, PCA9685_PWM_HZ, hz);
}
