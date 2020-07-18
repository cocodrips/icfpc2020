#ifndef EXPR_H_
#define EXPR_H_

#include <cassert>
#include <iostream>
#include <memory>
#include <vector>
#include <unordered_map>
#include "token.h"

class Expr;
std::ostream& operator<<(std::ostream& os, const Expr& expr);
std::ostream& operator<<(std::ostream& os, const std::shared_ptr<Expr>& expr);

class Def {
public:
    Def(const Token& token, std::shared_ptr<Expr> expr)
        : token_(token), expr_(expr) {
            assert(token_.tag == Tag::kVariable || token_.tag == Tag::kGalaxy);
        }
    Number name() const { return token_.num; }
    std::shared_ptr<Expr> expr() const { return expr_; }

private:
    Token token_;
    std::shared_ptr<Expr> expr_;
};

std::ostream& operator<<(std::ostream& os, const Def& def) {
    os << ":" << def.name() << " = " << def.expr();
    return os;
}

class Env {
public:
    void set_last_expr(std::shared_ptr<Expr> last_expr) { last_expr_ = last_expr; }
    std::shared_ptr<Expr> last_expr() const {
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

    std::shared_ptr<Expr> GetGlobal(Number name) {
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
    std::shared_ptr<Expr> last_expr_;
};

class Expr {
public:
    Expr(const Token& token) : token_(token), reduced_(false) {}
    const Tag tag() const { return token_.tag; }
    const Token& token() const { return token_; }
    virtual bool built() const { return true; }
    bool reduced() { return reduced_; }

    virtual std::shared_ptr<Expr> Reduce(Env& env) {
        return nullptr;
    }

    virtual std::shared_ptr<Expr> Clone(std::unordered_map<int, int>& id_map) {
        return nullptr;
    }

    virtual std::shared_ptr<Expr> Apply(int param_id, std::shared_ptr<Expr> arg) {
        return nullptr;
    }

    virtual void Output(std::ostream& os) const {
        os << "Expr";
    }

protected:
    Token token_;
    bool reduced_;
};

std::shared_ptr<Expr> ReduceIfPossible(std::shared_ptr<Expr> expr, Env& env) {
    if (expr->reduced()) {
        return expr;
    }
    std::shared_ptr<Expr> reduced = expr->Reduce(env);
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

    std::shared_ptr<Expr> Clone(std::unordered_map<int, int>& id_map) override {
        return std::make_shared<FuncParam>(token_, id_map[id_]);
    }

    std::shared_ptr<Expr> Apply(int param_id, std::shared_ptr<Expr> arg) override {
        return param_id == id_ ? arg : nullptr;
    }

protected:
    int id_;
};

class Func : public Expr {
public:
    Func(const Token& token, std::shared_ptr<FuncParam> param, std::shared_ptr<Expr> expr)
        : Expr(token), param_id_(param->id()), expr_(expr) {}
    int param_id() const { return param_id_; }
    std::shared_ptr<Expr> expr() const { return expr_; }

    void Output(std::ostream& os) const override {
        os << "λx" << param_id_ << ".";
        expr_->Output(os);
    }

    std::shared_ptr<Expr> Clone(std::unordered_map<int, int>& id_map) override {
        std::shared_ptr<FuncParam> new_param = std::make_shared<FuncParam>(token_);
        id_map[param_id_] = new_param->id();
        std::shared_ptr<Expr> new_expr = expr_->Clone(id_map);
        if (!new_expr) {
            return nullptr;
        }
        return std::make_shared<Func>(token_, new_param, new_expr);
    }

    std::shared_ptr<Expr> Apply(int param_id, std::shared_ptr<Expr> arg) override {
        std::shared_ptr<Expr> new_expr = expr_->Apply(param_id, arg);
        if (!new_expr) {
            return nullptr;
        }
        std::shared_ptr<FuncParam> new_param = std::make_shared<FuncParam>(token_, param_id_);
        return std::make_shared<Func>(token_, new_param, new_expr);
    }

    std::shared_ptr<Expr> Apply(std::shared_ptr<Expr> arg) {
        std::unordered_map<int, int> id_map;
        std::shared_ptr<Expr> newArg = arg->Clone(id_map);
        return expr_->Apply(param_id_, newArg ? newArg : arg);
    }

protected:
    int param_id_;
    std::shared_ptr<Expr> expr_;
};

class ApplyExpr : public Expr {
public:
    ApplyExpr(const Token& token) : Expr(token) {}
    ApplyExpr(const Token& token, std::shared_ptr<Expr> func, std::shared_ptr<Expr> arg)
        : Expr(token), func_(func), arg_(arg) {}

    std::shared_ptr<Expr> func() const { return func_; }
    void set_func(std::shared_ptr<Expr> func) { func_ = func; }
    std::shared_ptr<Expr> arg() const { return arg_; }
    void set_arg(std::shared_ptr<Expr> arg) { arg_ = arg; }
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

    std::shared_ptr<Expr> Reduce(Env& env) override {
        func_ = ReduceIfPossible(func_, env);
        arg_ = ReduceIfPossible(arg_, env);
        Func* func = dynamic_cast<Func*>(func_.get());
        if (!func) {
            std::clog << "Not a function: " << func_ << std::endl;
            return nullptr;
        }
        std::shared_ptr<Expr> applied = func->Apply(arg_);
        return ReduceIfPossible(applied, env);
    }

    std::shared_ptr<Expr> Clone(std::unordered_map<int, int>& id_map) override {
        std::shared_ptr<Expr> new_func = func_->Clone(id_map);
        std::shared_ptr<Expr> new_arg = arg_->Clone(id_map);
        if (!new_func && !new_arg) {
            return nullptr;
        }
        return std::make_shared<ApplyExpr>(
            token_, new_func ? new_func : func_, new_arg ? new_arg : arg_);
    }

    std::shared_ptr<Expr> Apply(int param_id, std::shared_ptr<Expr> arg) override {
        std::shared_ptr<Expr> new_func = func_->Apply(param_id, arg);
        std::shared_ptr<Expr> new_arg = arg_->Apply(param_id, arg);
        if (!new_func && !new_arg) {
            return nullptr;
        }
        return std::make_shared<ApplyExpr>(
            token_, new_func ? new_func : func_, new_arg ? new_arg : arg_);
    }

protected:
    std::shared_ptr<Expr> func_;
    std::shared_ptr<Expr> arg_;
};

class GlobalVar : public Expr {
public:
    GlobalVar(const Token& token) : Expr(token) {}
    Number name() const { return token_.num; }
    void Output(std::ostream& os) const override {
        os << ":" << name();
    }
    std::shared_ptr<Expr> Reduce(Env& env) override {
        std::shared_ptr<Expr> value = env.GetGlobal(name());
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
    BinaryExpr(const Token& token, std::shared_ptr<Expr> left, std::shared_ptr<Expr> right)
        : Expr(token), left_(left), right_(right) {}
    std::shared_ptr<Expr> left() const { return left_; }
    std::shared_ptr<Expr> right() const { return right_; }
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

    std::shared_ptr<Expr> Reduce(Env& env) override {
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

    std::shared_ptr<Expr> Clone(std::unordered_map<int, int>& id_map) override {
        std::shared_ptr<Expr> new_left = left_->Clone(id_map);
        std::shared_ptr<Expr> new_right = right_->Clone(id_map);
        if (!new_left && !new_right) {
            return nullptr;
        }
        return std::make_shared<BinaryExpr>(
            token_, new_left ? new_left : left_, new_right ? new_right : right_);
    }

    std::shared_ptr<Expr> Apply(int param_id, std::shared_ptr<Expr> arg) override {
        std::shared_ptr<Expr> new_left = left_->Apply(param_id, arg);
        std::shared_ptr<Expr> new_right = right_->Apply(param_id, arg);
        if (!new_left && !new_right) {
            return nullptr;
        }
        return std::make_shared<BinaryExpr>(
            token_, new_left ? new_left : left_, new_right ? new_right : right_);
    }

protected:
    std::shared_ptr<Expr> left_;
    std::shared_ptr<Expr> right_;
};

std::ostream& operator<<(std::ostream& os, const Expr& expr) {
    expr.Output(os);
    return os;
}

std::ostream& operator<<(std::ostream& os, const std::shared_ptr<Expr>& expr) {
    expr->Output(os);
    return os;
}

std::ostream& operator<<(std::ostream& os, const Env& def) {
    def.Output(os);
    return os;
}

#endif  // EXPR_H_
