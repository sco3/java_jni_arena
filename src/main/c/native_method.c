#include <jni.h>
#include <string.h>

JNIEXPORT void JNICALL Java_sco3_Main_passString(
    JNIEnv *env, 
    jobject obj, 
    jlong address,    
    jint length) {    

    unsigned char* ptr = (unsigned char*) address;
    
    printf ("Got: ");
    for (int i = 0; i < length; i++) {
		putc (ptr[i],stdout);
    }
    printf ("\n");
}