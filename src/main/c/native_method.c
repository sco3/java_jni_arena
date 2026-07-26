#include <jni.h>
#include <string.h>
#include <stdlib.h>

JNIEXPORT void JNICALL Java_sco3_Main_passString(
    JNIEnv *env, 
    jobject obj, 
    jlong address,    
    jint length
) {    

    unsigned char* ptr = (unsigned char*) address;
    
    printf ("Got: ");
    for (int i = 0; i < length; i++) {
		if (ptr[i] == 0) {
			putc ('\\',stdout);
			putc ('0',stdout);
		} else {
		    putc (ptr[i],stdout);
		}
    }
    printf ("\n");
}


JNIEXPORT double JNICALL Java_sco3_Main_parseDouble(
    JNIEnv *env, 
    jobject obj, 
    jlong address,    
    jint length,
	jlong error_address
) {
	double result = 0;    

    char* ptr = (char*) address;
	long* e_ptr = (long*) error_address;
	
	char *stop;
	result = strtod(ptr, &stop);
	if (*stop) {
	   //printf ("error\n");
	   long pos = stop - ptr;
	   *e_ptr = pos;
	} else {
		//printf ("ok\n");
		*e_ptr = -1;
	}
	//printf ("%.16g\n", result);
	return result;
}


