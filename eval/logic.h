#ifndef LOGIC_H_
#define LOGIC_H_

#include <cassert>
#include <iostream>
#include <memory>
#include <vector>
#include "token.h"
#include "expr.h"

class IfExpr;

class BoolExpr : public Expr {
public:
    BoolExpr(const Token& token) : Expr(token) {
        reduced_ = true;
    }
    bool value() const { return token_.tag == Tag::kTrue; }

    void Output(std::ostream& os) const override {
        os << (value() ? "true" : "false");
    }

    ExprPtr Apply(ExprPtr arg) override {
        ExprPtr cond = std::make_shared<BoolExpr>(token_);
        std::unordered_map<int, int> id_map;
        ExprPtr new_arg = arg->Clone(id_map);
        auto right = std::make_shared<FuncParam>(token_);
        auto if_expr = std::make_shared<IfExpr>(token_, cond, new_arg ? new_arg : arg, right);
        return std::make_shared<Func>(token_, right, if_expr);
    }
};

// TODO: Inherit BinaryExpr.
class CompExpr : public Expr {
public:
    CompExpr(const Token& token, ExprPtr left, ExprPtr right)
        : Expr(token), left_(left), right_(right) {}
    ExprPtr left() const { return left_; }
    ExprPtr right() const { return right_; }
    void Output(std::ostream& os) const override {
        os << token_ << "(";
        left_->Output(os);
        os << ", ";
        right_->Output(os);
        os << ")";
    }

    bool eval(Number left, Number right) {
        switch (token_.tag) {
            case Tag::kLt: return left < right;
            case Tag::kEq: return left == right;
        }
        std::clog << "Unsupported comparison: " << token_ << std::endl;
        std::abort();
    }

    ExprPtr Reduce(Env& env) override {
        left_ = ReduceIfPossible(left_, env);
        right_ = ReduceIfPossible(right_, env);
        NumberExpr* leftNum = dynamic_cast<NumberExpr*>(left_.get());
        NumberExpr* rightNum = dynamic_cast<NumberExpr*>(right_.get());
        if (!leftNum) {
            std::clog << "Not a number: " << left_ << std::endl;
            return nullptr;
        }
        if (!rightNum) {
            std::clog << "Not a number: " << right_ << std::endl;
            return nullptr;
        }
        Token dummyToken = token_;
        dummyToken.tag = eval(leftNum->value(), rightNum->value()) ? Tag::kTrue : Tag::kFalse;
        dummyToken.num = 0;
        return std::make_shared<BoolExpr>(dummyToken);
    }

    ExprPtr Clone(std::unordered_map<int, int>& id_map) override {
        ExprPtr new_left = left_->Clone(id_map);
        ExprPtr new_right = right_->Clone(id_map);
        if (!new_left && !new_right) {
            return nullptr;
        }
        return std::make_shared<CompExpr>(
            token_, new_left ? new_left : left_, new_right ? new_right : right_);
    }

    ExprPtr Apply(int param_id, ExprPtr arg) override {
        ExprPtr new_left = left_->Apply(param_id, arg);
        ExprPtr new_right = right_->Apply(param_id, arg);
        if (!new_left && !new_right) {
            return nullptr;
        }
        return std::make_shared<CompExpr>(
            token_, new_left ? new_left : left_, new_right ? new_right : right_);
    }

protected:
    ExprPtr left_;
    ExprPtr right_;
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
