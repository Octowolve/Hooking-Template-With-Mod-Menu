#ifndef DARK_FORCE_MEMORY_H
#define DARK_FORCE_MEMORY_H

#include <memory.h>

namespace Memory
{
    inline bool Protect(void *addr, size_t length)
    {
        size_t pagesize = sysconf(_SC_PAGESIZE);
        uintptr_t start = (uintptr_t) addr;
        uintptr_t end = start + length;
        uintptr_t pagestart = start & -pagesize;
        return mprotect((void*)pagestart, end - pagestart, PROT_READ|PROT_EXEC) < 0;
    }

    inline bool UnProtect(void *addr, size_t length)
    {
        size_t pagesize = sysconf(_SC_PAGESIZE);
        uintptr_t start = (uintptr_t) addr;
        uintptr_t end = start + length;
        uintptr_t pagestart = start & -pagesize;
        return mprotect((void*)pagestart, end - pagestart, PROT_READ|PROT_WRITE|PROT_EXEC) < 0;
    }

    template<typename T1, typename T2> inline void Write(T1 memaddr, T2 bytes, size_t length)
    {
        if (UnProtect((void*) memaddr, length)) {
            return;
        }
        memcpy((void*) memaddr, (void*) bytes, length);
        Protect((void*) memaddr, length);
    }

    template<typename T1, typename T2> inline void Read(T1 memaddr, T2 dest, size_t length)
    {
        memcpy((void*) dest, (void*) memaddr, length);
    }
}
#endif //DARK_FORCE_MEMORY_H
