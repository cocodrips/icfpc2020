#ifndef LOGIC_H_
#define LOGIC_H_

#include <cassert>
#include <iostream>
#include <memory>
#include <vector>
#include "token.h"
#include "expr.h"

class BoolExpr : public Expr {
public:
    BoolExpr(const Token& token) : Expr(token) {
        reduced_ = true;
    }
    bool value() const { return token_.tag == Tag::kTrue; }
    void Output(std::ostream& os) const override {
        os << value();
    }
};

#endif  // LOGIC_H_
