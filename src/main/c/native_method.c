#include <jni.h>
#include <string.h>
#include <stdlib.h>

#define FFC_IMPL
#include "ffc.h"

JNIEXPORT void JNICALL Java_sco3_Main_passString(
    JNIEnv *env, 
    jobject obj, 
    jlong address,    
    jint length
) {    

    unsigned char* ptr = (unsigned char*) address;
    
    printf ("C got: ");
    for (int i = 0; i < length; i++) {
		if (ptr[i] == 0) {
			putc ('\\',stdout);
			putc ('0',stdout);
		} else {
		    putc (ptr[i],stdout);
		}
    }
    printf ("\n");
	fflush(stdout);
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



JNIEXPORT double JNICALL Java_sco3_Main_parseDoubleFast(
    JNIEnv *env, 
    jobject obj, 
    jlong address,    
    jint length,
	jlong error_address
) {
	double result = 0;    

    char* ptr = (char*) address;
	long* e_ptr = (long*) error_address;
	
	
	
	ffc_result res = ffc_parse_double(length, ptr, &result);
	if (res.outcome != FFC_OUTCOME_OK || res.ptr != ptr + length) {
	   //printf ("error\n");
	   long pos = res.ptr - ptr;
	   *e_ptr = pos;
	} else {
		//printf ("ok\n");
		*e_ptr = -1;
	}
	//printf ("%.16g\n", result);
	return result;
}



