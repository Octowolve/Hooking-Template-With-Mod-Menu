#include "Patch.h"
#include "Memory.h"

extern "C" {
Patch *Patch::Setup(void* _target, uint32_t data)
{
	size_t target = (size_t)_target & (~1);
	size_t size = 0;
	if (data < INT_MAX) {
		size = sizeof(unsigned short);
		data = __swap16(data);
	} else {
		size = sizeof(int);
		data = __swap32(data);
	}

	return new Patch((void*)target, (char *)&data, size);
}

Patch *Patch::Setup(void* _target, char *data, size_t len){
	return new Patch(_target, data, len);
}

Patch::Patch(void* addr, char *data, size_t len)
: _t_addr(addr), _patchSize(len) {
	uint8_t *orig = new uint8_t[len];
	Memory::Read(addr, orig, len);

	this->_patchBytes.assign(data, data + len);
	this->_origBytes.assign(orig, orig + len);

	delete[] orig;
}

Patch::~Patch()
{

}

void Patch::Apply()
{
	Memory::Write(_t_addr, _patchBytes.data(), _patchSize);
}

void Patch::Reset()
{
	Memory::Write(_t_addr, _origBytes.data(), _patchSize);
}
}