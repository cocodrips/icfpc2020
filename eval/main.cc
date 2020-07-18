#include <algorithm>
#include <cstdlib>
#include <memory>
#include <iostream>
#include <iterator>
#include <sstream>
#include <string>
#include <vector>
#include "expr.h"
#include "logic.h"
#include "token.h"

bool IsNum(const std::string& str) {
    return std::all_of(std::begin(str), std::end(str), ::isdigit);
}

Token ParseToken(const std::string& token_str) {
    auto tag = STR_TO_TAG.find(token_str);
    if (tag != STR_TO_TAG.end()) {
        return { tag->second, 0L };
    } else if (token_str[0] == ':') {
        if (IsNum(token_str.substr(1))) {
            return { Tag::kVariable, stoll(token_str.substr(1)) };
        } else {
            std::clog << "Number is expected after colon: " << token_str << std::endl;
            std::abort();
        }
    } else if (token_str[0] == '-') {
        if (IsNum(token_str.substr(1))) {
            return { Tag::kNumber, -std::stoll(token_str.substr(1)) };
        } else {
            std::clog << "Number is expected after colon: " << token_str << std::endl;
            std::abort();
        }
    } else if (IsNum(token_str)) {
        return { Tag::kNumber, std::stoll(token_str) };
    } else {
        std::clog << "Unknown token: " << token_str << std::endl;
        std::abort();
    }
}

std::vector<std::vector<Token>> Tokenize(std::istream& in_stream) {
    std::vector<std::vector<Token>> result;
    std::string line_str;
    int line_num = 1;
    while (std::getline(in_stream, line_str)) {
        std::vector<Token> tokens;
        std::istringstream line_stream(line_str);
        std::string token_str;
        int index = 1;
        while (line_stream >> token_str) {
            Token token = ParseToken(token_str);
            token.line = line_num;
            token.index = index;
            tokens.push_back(token);
            index++;
	    }
        result.push_back(tokens);
        line_num++;
    }
    return result;
}

ExprPtr CreateExpr(const Token& token) {
    switch (token.tag) {
        case Tag::kNumber:
            return std::make_shared<NumberExpr>(token);
        case Tag::kVariable:
            return std::make_shared<GlobalVar>(token);
        case Tag::kApply:
            return std::make_shared<ApplyExpr>(token);
        case Tag::kAdd:
        case Tag::kDiv:
        case Tag::kMul: {
                auto left = std::make_shared<FuncParam>(token);
                auto right = std::make_shared<FuncParam>(token);
                auto binary = std::make_shared<BinaryExpr>(token, left, right);
                auto inner = std::make_shared<Func>(token, right, binary);
                auto outer = std::make_shared<Func>(token, left, inner);
                return outer;
        }
        case Tag::kNeg:
        case Tag::kInc:
        case Tag::kDec: {
                auto param = std::make_shared<FuncParam>(token);
                auto unary = std::make_shared<UnaryExpr>(token, param);
                return std::make_shared<Func>(token, param, unary);
            }
        case Tag::kTrue:
        case Tag::kFalse: {
                auto cond = std::make_shared<BoolExpr>(token);
                auto left = std::make_shared<FuncParam>(token);
                auto right = std::make_shared<FuncParam>(token);
                auto if_expr = std::make_shared<IfExpr>(token, cond, left, right);
                auto inner = std::make_shared<Func>(token, right, if_expr);
                auto outer = std::make_shared<Func>(token, left, inner);
                return outer;
        }
        case Tag::kIf0: {
                auto param = std::make_shared<FuncParam>(token);
                auto cond = std::make_shared<If0Expr>(token, param);
                auto left = std::make_shared<FuncParam>(token);
                auto right = std::make_shared<FuncParam>(token);
                auto if_expr = std::make_shared<IfExpr>(token, cond, left, right);
                return std::make_shared<Func>(token, param,
                    std::make_shared<Func>(token, left,
                    std::make_shared<Func>(token, right, if_expr)));
        }
        case Tag::kSCombinator: {
            // ap ap ap s x0 x1 x2 = ap ap x0 x2 ap x1 x2
            auto x0 = std::make_shared<FuncParam>(token);
            auto x1 = std::make_shared<FuncParam>(token);
            auto x2 = std::make_shared<FuncParam>(token);
            auto ap_x0_x2 = std::make_shared<ApplyExpr>(token, x0, x2);
            auto ap_x1_x2 = std::make_shared<ApplyExpr>(token, x1, x2);
            auto ap = std::make_shared<ApplyExpr>(token, ap_x0_x2, ap_x1_x2);
            return std::make_shared<Func>(token, x0,
                std::make_shared<Func>(token, x1,
                std::make_shared<Func>(token, x2, ap)));
        }
    }
    std::clog << "Unsupported token: " << token << " at line " << token.line << std::endl;
    std::abort();
}

bool ReduceStack(std::vector<ExprPtr>& stack) {
    if (stack.size() < 3) {
        return false;
    }
    ExprPtr func = stack[stack.size() - 2];
    ExprPtr arg = stack[stack.size() - 1];
    if (!func->built() || !arg->built()) {
        return false;
    }
    ApplyExpr* apply = dynamic_cast<ApplyExpr*>(stack[stack.size() - 3].get());
    if (!apply) {
        return false;
    }
    apply->set_func(func);
    apply->set_arg(arg);
    stack.pop_back();
    stack.pop_back();
    return true;
}

ExprPtr ParseExpr(const std::vector<Token>& tokens, int start) {
    std::vector<ExprPtr> stack;
    for (int i = start; i < tokens.size(); i++) {
        const Token& token = tokens[i];
        stack.push_back(CreateExpr(token));
        while(ReduceStack(stack));
    }
    if (stack.size() != 1) {
        std::clog << "Unexpected stack size after parsing: " << stack.size() << std::endl;
        std::abort();
    }
    return stack[0];
}

Env Parse(const std::vector<std::vector<Token>>& lines) {
    Env env;
    for (const std::vector<Token>& tokens : lines) {
        if (tokens.size() > 2 && tokens[1].tag == Tag::kDef) {
            Def def(tokens[0], ParseExpr(tokens, 2));
            env.AddDef(def);
        } else {
            env.set_last_expr(ParseExpr(tokens, 0));
        }
    }
    return env;
}

int main() {
    std::vector<std::vector<Token>> lines = Tokenize(std::cin);
    Env env = Parse(lines);
    std::clog << env << std::endl;
    ExprPtr reduced = env.last_expr()->Reduce(env);
    if (!reduced) {
        std::clog << "Failed to reduce." << std::endl;
        std::clog << env.last_expr() << std::endl;
        return 1;
    }
    std::cout << reduced << std::endl;
    return 0;
}
