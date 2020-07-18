#ifndef EXPR_H_
#define EXPR_H_

#include <cassert>
#include <iostream>
#include <memory>
#include <vector>
#include <unordered_map>
#include "token.h"

class Expr;
typedef std::shared_ptr<Expr> ExprPtr;

std::ostream& operator<<(std::ostream& os, const Expr& expr);
std::ostream& operator<<(std::ostream& os, const ExprPtr& expr);

class Def {
public:
    Def(const Token& token, ExprPtr expr)
        : token_(token), expr_(expr) {
            assert(token_.tag == Tag::kVariable || token_.tag == Tag::kGalaxy);
        }
    Number name() const { return token_.num; }
    ExprPtr expr() const { return expr_; }

private:
    Token token_;
    ExprPtr expr_;
};

std::ostream& operator<<(std::ostream& os, const Def& def) {
    os << ":" << def.name() << " = " << def.expr();
    return os;
}

class Env {
public:
    void set_last_expr(ExprPtr last_expr) { last_expr_ = last_expr; }
    ExprPtr last_expr() const {
        if (last_expr_) {
            return last_expr_;
        } else if (!defs_.empty()) {
            return defs_.back().expr();
        }
        return nullptr;
    }

    void AddDef(const Def& def) {
        name_to_def_[def.name()] = defs_.size();
        defs_.push_back(def);
    }

    ExprPtr GetGlobal(Number name) {
        auto index = name_to_def_.find(name);
        if (index == name_to_def_.end()) {
            return nullptr;
        }
        return defs_[index->second].expr();
    }

    void Output(std::ostream& os) const {
        for (const Def& def : defs_) {
            os << def << std::endl;
        }
        if (last_expr()) {
            os << last_expr() << std::endl;
        }
    }

private:
    std::vector<Def> defs_;
    std::unordered_map<Number, size_t> name_to_def_;
    ExprPtr last_expr_;
};

class Expr {
public:
    Expr(const Token& token) : token_(token), reduced_(false) {}
    const Tag tag() const { return token_.tag; }
    const Token& token() const { return token_; }
    virtual bool built() const { return true; }
    bool reduced() { return reduced_; }

    virtual ExprPtr Reduce(Env& env) {
        return nullptr;
    }

    virtual ExprPtr Clone(std::unordered_map<int, int>& id_map) {
        return nullptr;
    }

    virtual ExprPtr Apply(int param_id, ExprPtr arg) {
        return nullptr;
    }

    virtual void Output(std::ostream& os) const {
        os << "Expr";
    }

protected:
    Token token_;
    bool reduced_;
};

ExprPtr ReduceIfPossible(ExprPtr expr, Env& env) {
    if (expr->reduced()) {
        return expr;
    }
    ExprPtr reduced = expr->Reduce(env);
    if (!reduced) {
        std::clog << "Not reduced: " << std::endl << expr << std::endl << std::endl;
        return expr;
    }
    std::clog << "Reduced:"<< std::endl;
    std::clog << expr << std::endl << reduced << std::endl << std::endl;
    return reduced;
}

int func_param_id = 0;

class FuncParam : public Expr {
public:
    FuncParam(const Token& token) : Expr(token) {
        id_ = ++func_param_id;
    }

    FuncParam(const Token& token, int id) : Expr(token), id_(id) {}

    int id() const { return id_; }

    void Output(std::ostream& os) const override {
        os << "x" << id_;
    }

    ExprPtr Clone(std::unordered_map<int, int>& id_map) override {
        return std::make_shared<FuncParam>(token_, id_map[id_]);
    }

    ExprPtr Apply(int param_id, ExprPtr arg) override {
        return param_id == id_ ? arg : nullptr;
    }

protected:
    int id_;
};

class Func : public Expr {
public:
    Func(const Token& token, std::shared_ptr<FuncParam> param, ExprPtr expr)
        : Expr(token), param_id_(param->id()), expr_(expr) {}
    int param_id() const { return param_id_; }
    ExprPtr expr() const { return expr_; }

    void Output(std::ostream& os) const override {
        os << "λx" << param_id_ << ".";
        expr_->Output(os);
    }

    ExprPtr Clone(std::unordered_map<int, int>& id_map) override {
        std::shared_ptr<FuncParam> new_param = std::make_shared<FuncParam>(token_);
        id_map[param_id_] = new_param->id();
        ExprPtr new_expr = expr_->Clone(id_map);
        if (!new_expr) {
            return nullptr;
        }
        return std::make_shared<Func>(token_, new_param, new_expr);
    }

    ExprPtr Apply(int param_id, ExprPtr arg) override {
        ExprPtr new_expr = expr_->Apply(param_id, arg);
        if (!new_expr) {
            return nullptr;
        }
        std::shared_ptr<FuncParam> new_param = std::make_shared<FuncParam>(token_, param_id_);
        return std::make_shared<Func>(token_, new_param, new_expr);
    }

    ExprPtr Apply(ExprPtr arg) {
        std::unordered_map<int, int> id_map;
        ExprPtr newArg = arg->Clone(id_map);
        return expr_->Apply(param_id_, newArg ? newArg : arg);
    }

protected:
    int param_id_;
    ExprPtr expr_;
};

class ApplyExpr : public Expr {
public:
    ApplyExpr(const Token& token) : Expr(token) {}
    ApplyExpr(const Token& token, ExprPtr func, ExprPtr arg)
        : Expr(token), func_(func), arg_(arg) {}

    ExprPtr func() const { return func_; }
    void set_func(ExprPtr func) { func_ = func; }
    ExprPtr arg() const { return arg_; }
    void set_arg(ExprPtr arg) { arg_ = arg; }
    virtual bool built() const { return func_ && arg_; };

    void Output(std::ostream& os) const override {
        if (!built()) {
            os << "ap(null null)";
            return;
        }
        os << "ap(";
        func_->Output(os);
        os << ", ";
        arg_->Output(os);
        os << ")";
    }

    ExprPtr Reduce(Env& env) override {
        func_ = ReduceIfPossible(func_, env);
        arg_ = ReduceIfPossible(arg_, env);
        Func* func = dynamic_cast<Func*>(func_.get());
        if (!func) {
            std::clog << "Not a function: " << func_ << std::endl;
            return nullptr;
        }
        ExprPtr applied = func->Apply(arg_);
        return ReduceIfPossible(applied, env);
    }

    ExprPtr Clone(std::unordered_map<int, int>& id_map) override {
        ExprPtr new_func = func_->Clone(id_map);
        ExprPtr new_arg = arg_->Clone(id_map);
        if (!new_func && !new_arg) {
            return nullptr;
        }
        return std::make_shared<ApplyExpr>(
            token_, new_func ? new_func : func_, new_arg ? new_arg : arg_);
    }

    ExprPtr Apply(int param_id, ExprPtr arg) override {
        ExprPtr new_func = func_->Apply(param_id, arg);
        ExprPtr new_arg = arg_->Apply(param_id, arg);
        if (!new_func && !new_arg) {
            return nullptr;
        }
        return std::make_shared<ApplyExpr>(
            token_, new_func ? new_func : func_, new_arg ? new_arg : arg_);
    }

protected:
    ExprPtr func_;
    ExprPtr arg_;
};

class GlobalVar : public Expr {
public:
    GlobalVar(const Token& token) : Expr(token) {}
    Number name() const { return token_.num; }
    void Output(std::ostream& os) const override {
        os << ":" << name();
    }
    ExprPtr Reduce(Env& env) override {
        ExprPtr value = env.GetGlobal(name());
        return ReduceIfPossible(value, env);
    }
};

class NumberExpr : public Expr {
public:
    NumberExpr(const Token& token) : Expr(token) {
        reduced_ = true;
    }
    Number value() const { return token_.num; }
    void Output(std::ostream& os) const override {
        os << value();
    }
};

class BinaryExpr : public Expr {
public:
    BinaryExpr(const Token& token, ExprPtr left, ExprPtr right)
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

    Number eval(Number left, Number right) {
        switch (token_.tag) {
            case Tag::kAdd: return left + right;
            case Tag::kMul: return left * right;
        }
        std::clog << "Unsupported binary op: " << token_ << std::endl;
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
        dummyToken.tag = Tag::kNumber;
        dummyToken.num = eval(leftNum->value(), rightNum->value());
        return std::make_shared<NumberExpr>(dummyToken);
    }

    ExprPtr Clone(std::unordered_map<int, int>& id_map) override {
        ExprPtr new_left = left_->Clone(id_map);
        ExprPtr new_right = right_->Clone(id_map);
        if (!new_left && !new_right) {
            return nullptr;
        }
        return std::make_shared<BinaryExpr>(
            token_, new_left ? new_left : left_, new_right ? new_right : right_);
    }

    ExprPtr Apply(int param_id, ExprPtr arg) override {
        ExprPtr new_left = left_->Apply(param_id, arg);
        ExprPtr new_right = right_->Apply(param_id, arg);
        if (!new_left && !new_right) {
            return nullptr;
        }
        return std::make_shared<BinaryExpr>(
            token_, new_left ? new_left : left_, new_right ? new_right : right_);
    }

protected:
    ExprPtr left_;
    ExprPtr right_;
};

std::ostream& operator<<(std::ostream& os, const Expr& expr) {
    expr.Output(os);
    return os;
}

std::ostream& operator<<(std::ostream& os, const ExprPtr& expr) {
    expr->Output(os);
    return os;
}

std::ostream& operator<<(std::ostream& os, const Env& def) {
    def.Output(os);
    return os;
}

#endif  // EXPR_H_
