#ifndef TOKEN_H_
#define TOKEN_H_

#include <iostream>
#include <string>
#include <map>

template <class K, class V>
std::map<V, K> invert(const std::map<K, V> map) {
    std::map<V, K> inverted;
    for (auto entry : map) {
        inverted[entry.second] = entry.first;
    }
    return inverted;
}

enum class Tag {
    kNumber,  // "123"
    kVariable,  // ":123"
    kApply,  // "ap"
    kDef,  // "="
    kInc,  // "inc"
    kDec,  // "dec"
    kAdd,  // "add"
    kMul,  // "mul"
    kDiv,  // "div"
    kEq,  // "eq"
    kLt,  // "lt"
    kMod,  // "mod"
    kDem,  // "dem"
    kSend,  // "send"
    kNeg,  // "neg"
    kTrue,  // "t", K combinator
    kFalse,  // "f"
    kSCombinator, // "s"
    kCCombinator, // "c"
    kBCombinator, // "b"
    kPwr2,  // "pwr2"
    kIdentity,  // "i"
    kCons,  // "cons"
    kCar,  // "car"
    kCdr,  // "cdr"
    kNil,  // "nil"
    kIsNil,  // "isnil"
    kListStart,  // "("
    kListSep,  // ","
    kListEnd,  // ")"
    kVec,  // "vec", cons
    kDraw,  // "draw"
    kCheckerboard,  // "checkerboard"
    kMultipleDraw,  // "multipledraw"
    kIf0,  // "if0"
    kModem,  // "modem"
    kInteract,  // "interact"
    kF38,  // "f38"
    kStatelessDraw,  // "statelessdraw"
    kStatefulDraw,  // "statefuldraw"
    kGalaxy,  // "galaxy"
};

std::map<Tag, std::string> TAG_TO_STR = {
    { Tag::kNumber, "number" },
    { Tag::kVariable, "varible" },
    { Tag::kApply, "ap" },
    { Tag::kDef, "=" },
    { Tag::kInc, "inc" },
    { Tag::kDec, "dec" },
    { Tag::kAdd, "add" },
    { Tag::kMul, "mul" },
    { Tag::kDiv, "div" },
    { Tag::kEq, "eq" },
    { Tag::kLt, "lt" },
    { Tag::kMod, "mod" },
    { Tag::kDem, "dem" },
    { Tag::kSend, "send" },
    { Tag::kNeg, "neg" },
    { Tag::kTrue, "t" },
    { Tag::kFalse, "f" },
    { Tag::kSCombinator, "s" },
    { Tag::kCCombinator,  "c" },
    { Tag::kBCombinator, "b" },
    { Tag::kPwr2, "pwr2" },
    { Tag::kIdentity, "i" },
    { Tag::kCons, "cons" },
    { Tag::kCar, "car" },
    { Tag::kCdr, "cdr" },
    { Tag::kNil, "nil" },
    { Tag::kIsNil, "isnil" },
    { Tag::kListStart, "(" },
    { Tag::kListSep, "," },
    { Tag::kListEnd, ")" },
    { Tag::kVec, "vec" },
    { Tag::kDraw, "draw" },
    { Tag::kCheckerboard, "checkerboard" },
    { Tag::kMultipleDraw, "multipledraw" },
    { Tag::kIf0, "if0" },
    { Tag::kModem, "modem" },
    { Tag::kInteract, "interact" },
    { Tag::kF38, "f38" },
    { Tag::kStatelessDraw, "statelessdraw" },
    { Tag::kStatefulDraw, "statefuldraw" },
    { Tag::kGalaxy, "galaxy" },
};

std::map<std::string, Tag> STR_TO_TAG = invert(TAG_TO_STR);

using Number = long long int;

struct Token {
    Tag tag;
    Number num;
    int line;
    int index;
};

std::ostream& operator<<(std::ostream& os, const Token& token) {
    if (token.tag == Tag::kNumber) {
        os << token.num;
    } else if (token.tag == Tag::kVariable) {
        os << ":" << token.num;
    } else {
        os << TAG_TO_STR[token.tag];
    }
    return os;
}

#endif  // TOKEN_H_
