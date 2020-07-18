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

// TODO: Inherit UnaryExpr.
class If0Expr : public Expr {
public:
    If0Expr(const Token& token, ExprPtr expr) : Expr(token), expr_(expr) {}
    void Output(std::ostream& os) const override {
        os << token_ << "(";
        expr_->Output(os);
        os << ")";
    }

    ExprPtr Reduce(Env& env) override {
        expr_ = ReduceIfPossible(expr_, env);
        NumberExpr* num_expr = dynamic_cast<NumberExpr*>(expr_.get());
        if (!num_expr) {
            std::clog << "Not a number: " << expr_ << std::endl;
            return nullptr;
        }
        Token dummyToken = token_;
        dummyToken.tag = num_expr->value() == 0 ? Tag::kTrue : Tag::kFalse;
        return std::make_shared<BoolExpr>(dummyToken);
    }
    
    ExprPtr Clone(std::unordered_map<int, int>& id_map) override {
        ExprPtr new_expr = expr_->Clone(id_map);
        if (!new_expr) {
            return nullptr;
        }
        return std::make_shared<If0Expr>(token_, new_expr ? new_expr : expr_);
    }

    ExprPtr Apply(int param_id, ExprPtr arg) override {
        ExprPtr new_expr = expr_->Apply(param_id, arg);
        if (!new_expr) {
            return nullptr;
        }
        return std::make_shared<If0Expr>(token_, new_expr ? new_expr : expr_);
    }

protected:
    ExprPtr expr_;
};

class IfExpr : public Expr {
public:
    IfExpr(const Token& token, ExprPtr cond, ExprPtr true_expr, ExprPtr false_expr)
        : Expr(token), cond_(cond), true_expr_(true_expr), false_expr_(false_expr) {}

    void Output(std::ostream& os) const override {
        os << "if(";
        cond_->Output(os);
        os << ", ";
        true_expr_->Output(os);
        os << ", ";
        false_expr_->Output(os);
        os << ")";
    }

    ExprPtr Reduce(Env& env) override {
        cond_ = ReduceIfPossible(cond_, env);
        BoolExpr* cond_bool = dynamic_cast<BoolExpr*>(cond_.get());
        if (!cond_bool) {
            std::clog << "Not a boolean: " << cond_ << std::endl;
            return nullptr;
        }
        if (cond_bool->value()) {
            true_expr_ = ReduceIfPossible(true_expr_, env);
            return true_expr_;
        } else {
            false_expr_ = ReduceIfPossible(false_expr_, env);
            return false_expr_;
        }
    }

    ExprPtr Clone(std::unordered_map<int, int>& id_map) override {
        ExprPtr new_cond = cond_->Clone(id_map);
        ExprPtr new_true = true_expr_->Clone(id_map);
        ExprPtr new_false = false_expr_->Clone(id_map);
        if (!new_cond && !new_true && !new_false) {
            return nullptr;
        }
        return std::make_shared<IfExpr>(
            token_,
            new_cond ? new_cond : cond_,
            new_true ? new_true : true_expr_,
            new_false ? new_false : false_expr_);
    }

    ExprPtr Apply(int param_id, ExprPtr arg) override {
        ExprPtr new_cond = cond_->Apply(param_id, arg);
        ExprPtr new_true = true_expr_->Apply(param_id, arg);
        ExprPtr new_false = false_expr_->Apply(param_id, arg);
        if (!new_cond && !new_true && !new_false) {
            return nullptr;
        }
        return std::make_shared<IfExpr>(
            token_,
            new_cond ? new_cond : cond_,
            new_true ? new_true : true_expr_,
            new_false ? new_false : false_expr_);
    }

protected:
    ExprPtr cond_;
    ExprPtr true_expr_;
    ExprPtr false_expr_;
};

#endif  // LOGIC_H_
