#ifndef EXPR_H_
#define EXPR_H_

#include <cassert>
#include <memory>
#include <vector>
#include <unordered_map>
#include "token.h"

class Env;

class Expr {
public:
    Expr(const Token& token) : token_(token) {}
    const Tag tag() const { return token_.tag; }
    const Token& token() const { return token_; }
    virtual bool built() const { return true; }
    virtual std::shared_ptr<Expr> reduce(Env& env) {
        return nullptr;
    }
    virtual void Output(std::ostream& os) const {
        os << "Expr";
    }

protected:
    Token token_;
};

std::ostream& operator<<(std::ostream& os, const Expr& expr) {
    expr.Output(os);
    return os;
}

std::ostream& operator<<(std::ostream& os, const std::shared_ptr<Expr>& expr) {
    expr->Output(os);
    return os;
}

int func_param_id = 0;

class FuncParam : public Expr {
public:
    FuncParam(const Token& token) : Expr(token) {
        id_ = ++func_param_id;
    }
    int id() const { return id_; }
    void Output(std::ostream& os) const override {
        os << "$" << id_;
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
        os << "$" << param_id_ << " -> (";
        expr_->Output(os);
        os << ")";
    }

protected:
    int param_id_;
    std::shared_ptr<Expr> expr_;
};

class Apply : public Expr {
public:
    Apply(const Token& token) : Expr(token) {}
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
};

class NumberExpr : public Expr {
public:
    NumberExpr(const Token& token) : Expr(token) {}
    Number value() const { return token_.num; }
    void Output(std::ostream& os) const override {
        os << value();
    }
};

class BoolExpr : public Expr {
public:
    BoolExpr(const Token& token) : Expr(token) {}
    bool value() const { return token_.tag == Tag::kTrue; }
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
    std::shared_ptr<Expr> reduce();
    void Output(std::ostream& os) const override {
        os << token_ << "(";
        left_->Output(os);
        os << ", ";
        right_->Output(os);
        os << ")";
    }

protected:
    std::shared_ptr<Expr> left_;
    std::shared_ptr<Expr> right_;
};

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

std::ostream& operator<<(std::ostream& os, const Env& def) {
    def.Output(os);
    return os;
}

#endif  // EXPR_H_
