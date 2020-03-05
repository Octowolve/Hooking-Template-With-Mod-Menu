#ifndef UTILS_H
#define UTILS_H
#include <jni.h>
#include <unistd.h>

typedef unsigned long DWORD;
static DWORD libBase;

DWORD findLibrary(const char *library);
DWORD getAbsoluteAddress(const char* libraryName, DWORD relativeAddr);
bool isLibraryLoaded(const char *libraryName);

DWORD findLibrary(const char *library) {
    char filename[0xFF] = {0},
            buffer[1024] = {0};
    FILE *fp = NULL;
    DWORD address = 0;

    sprintf( filename, "/proc/self/maps");

    fp = fopen( filename, "rt" );
    if( fp == NULL ){
        perror("fopen");
        goto done;
    }

    while( fgets( buffer, sizeof(buffer), fp ) ) {
        if( strstr( buffer, library ) ){
            address = (DWORD)strtoul( buffer, NULL, 16 );
            goto done;
        }
    }

    done:

    if(fp){
        fclose(fp);
    }

    return address;
}

DWORD getAbsoluteAddress(const char* libraryName, DWORD relativeAddr) {
    if(libBase == 0)
        libBase = findLibrary(libraryName);
    if (libBase != 0)
        return (reinterpret_cast<DWORD>(libBase + relativeAddr));
    else
        return 0;
}

bool isLibraryLoaded(const char *libraryName) {
    char line[512] = {0};
    FILE *fp = fopen("/proc/self/maps", "rt");
    if (fp != NULL) {
        while (fgets(line, sizeof(line), fp)) {
            if (strstr(line, libraryName))
                return true;
        }
        fclose(fp);
    }
    return false;
}
#endif