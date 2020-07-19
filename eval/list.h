#ifndef LIST_H_
#define LIST_H_

#include <cassert>
#include <iostream>
#include <memory>
#include <vector>
#include "token.h"
#include "expr.h"

class NilExpr : public Expr {
public:
    NilExpr(const Token& token) : Expr(token) {
        reduced_ = true;
    }

    void Output(std::ostream& os) const override {
        os << token_;
    }
};

class ConsExpr : public Expr {
public:
    ConsExpr(const Token& token, ExprPtr car, ExprPtr cdr)
        : Expr(token), car_(car), cdr_(cdr) {
        reduced_ = car->reduced() && cdr->reduced();
    }
    ExprPtr car() const { return car_; }
    ExprPtr cdr() const { return cdr_; }

    void Output(std::ostream& os) const override {
        os << "(" << token_ << " ";
        car_->Output(os);
        os << " ";
        cdr_->Output(os);
        os << ")";
    }

    ExprPtr Clone(std::unordered_map<int, int>& id_map) override {
        if (reduced_) {
            return nullptr;
        }
        ExprPtr new_car = car_->Clone(id_map);
        ExprPtr new_cdr = cdr_->Clone(id_map);
        if (!new_car && !new_cdr) {
            return nullptr;
        }
        return std::make_shared<ConsExpr>(
            token_, new_car ? new_car : car_, new_cdr ? new_cdr : cdr_);
    }

    ExprPtr Apply(int param_id, ExprPtr arg) override {
        if (reduced_) {
            return nullptr;
        }
        ExprPtr new_car = car_->Apply(param_id, arg);
        ExprPtr new_cdr = cdr_->Apply(param_id, arg);
        if (!new_car && !new_cdr) {
            return nullptr;
        }
        return std::make_shared<ConsExpr>(
            token_, new_car ? new_car : car_, new_cdr ? new_cdr : cdr_);
    }

    ExprPtr Apply(ExprPtr arg) override {
        // ap ap ap cons x0 x1 x2 = ap ap x2 x0 x1
        std::unordered_map<int, int> id_map;
        ExprPtr new_arg = arg->Clone(id_map);
        return std::make_shared<ApplyExpr>(token_,
            std::make_shared<ApplyExpr>(token_, new_arg ? new_arg : arg, car()), cdr());
    }

protected:
    ExprPtr car_;
    ExprPtr cdr_;
};

// TODO: Inherit UnaryExpr.
class CarCdrExpr : public Expr {
public:
    CarCdrExpr(const Token& token, ExprPtr expr) : Expr(token), expr_(expr) {}
    void Output(std::ostream& os) const override {
        os << token_ << "(";
        expr_->Output(os);
        os << ")";
    }

    Number eval(Number value) {
        switch (token_.tag) {
            case Tag::kNeg: return -value;
            case Tag::kPwr2: return 1LL << value;
            case Tag::kIdentity: return value;
            case Tag::kInc: return value + 1;
            case Tag::kDec: return value - 1;
        }
        std::clog << "Unsupported unary op: " << token_ << std::endl;
        std::abort();
    }

    ExprPtr Reduce(Env& env) override {
        expr_ = ReduceIfPossible(expr_, env);
        std::shared_ptr<ConsExpr> cons_expr = std::dynamic_pointer_cast<ConsExpr>(expr_);
        if (!cons_expr) {
            std::clog << "Not a cons: " << expr_ << std::endl;
            return nullptr;
        }
        if (token_.tag == Tag::kCar) {
            return cons_expr->car();
        } else if (token_.tag == Tag::kCdr) {
            return cons_expr->cdr();
        }
    }

    ExprPtr Clone(std::unordered_map<int, int>& id_map) override {
        ExprPtr new_expr = expr_->Clone(id_map);
        if (!new_expr) {
            return nullptr;
        }
        return std::make_shared<CarCdrExpr>(token_, new_expr ? new_expr : expr_);
    }

    ExprPtr Apply(int param_id, ExprPtr arg) override {
        ExprPtr new_expr = expr_->Apply(param_id, arg);
        if (!new_expr) {
            return nullptr;
        }
        return std::make_shared<CarCdrExpr>(token_, new_expr ? new_expr : expr_);
    }

protected:
    ExprPtr expr_;
};

#endif  // LIST_H_
