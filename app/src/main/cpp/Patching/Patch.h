#include <string>
#include <stdio.h>
#include <sys/mman.h>
#include <byteswap.h>
#include <unistd.h>
#include <vector>

#ifndef DARK_FORCE_PATCH_H
#define DARK_FORCE_PATCH_H
class Patch
{
public:
    static Patch *Setup(void* _target, char *data, size_t len);
    static Patch *Setup(void* _target, uint32_t data);

    virtual void Apply();
    virtual void Reset();

private:
    Patch() = default;
    Patch(void* _target, char *data, size_t len);
    ~Patch();

protected:
    void* _t_addr;
    size_t _patchSize;
    std::vector<uint8_t> _patchBytes;
    std::vector<uint8_t> _origBytes;
};
#endif //DARK_FORCE_PATCH_H
