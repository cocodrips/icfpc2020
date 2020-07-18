#include <algorithm>
#include <cstdlib>
#include <iostream>
#include <iterator>
#include <sstream>
#include <string>
#include <vector>
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

int main() {
    std::vector<std::vector<Token>> tokens = Tokenize(std::cin);
    for (std::vector<Token>& line : tokens) {
        for (Token& token : line) {
            std::cout << token << " ";
        }
        std::cout << std::endl;
    }
    return 0;
}