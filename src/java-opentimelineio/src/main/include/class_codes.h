#include <map>

#ifndef _CLASS_CODES_H_INCLUDED_
#define _CLASS_CODES_H_INCLUDED_

enum ClassCode {
    _OpenTimeErrorStatus,
    _Any,
    _OTIOErrorStatus,
    _SerializableObject,
    _SerializableObjectWithMetadata,
    _Composable,
};

extern std::map<std::string, ClassCode> stringToClassCode;

extern std::map<ClassCode, std::string> classCodeToString;

#endif