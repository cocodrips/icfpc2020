#include <iostream>
#include <string>
#include <sstream>
#include <vector>
#include <stack>
#include <map>


class Tokens {
  public:
    std::vector<std::string> tokens;

    void push(std::string& in) {
      tokens.push_back(in);
    }

    std::string at(int i) {
      return tokens[i];
    }

    size_t size() {
      return tokens.size();
    }

    void show() {
      for (int i=0;i<(int)tokens.size();i++) {
        std::cout<<tokens[i]<<' ';
      } std::cout<<std::endl;
    }

    bool is_digit(std::string& token) {
      for (auto i : token) {
        if (i < '0' || '9' < i) return false;
      }
      return true;
    }

    int id(int i) {
      if (tokens[i] == "ap") return 0;
      if (tokens[i] == "i") return 1;
      if (tokens[i] == "inc") return 2;
      if (tokens[i] == "dec") return 3;
      if (tokens[i] == "add") return 4;
      if (tokens[i] == "mul") return 5;
      if (tokens[i] == "div") return 6;
      if (tokens[i] == "eq") return 7;
      if (tokens[i] == "lt") return 8;
      if (tokens[i] == "mod") return 9;
      if (tokens[i] == "dem") return 10;
      if (tokens[i] == "send") return 11;
      if (tokens[i] == "neg") return 12;
      if (tokens[i] == "s") return 13;
      if (tokens[i] == "c") return 14;
      if (tokens[i] == "b") return 15;
      if (tokens[i] == "t") return 16;
      if (tokens[i] == "f") return 17;
      if (tokens[i] == "pwr2") return 18;
      if (tokens[i] == "cons") return 19;
      if (tokens[i] == "car") return 20;
      if (tokens[i] == "cdr") return 21;
      if (tokens[i] == "nil") return 22;
      if (tokens[i] == "isnil") return 23;
      if (tokens[i] == "vec") return 24;
      if (tokens[i] == "draw") return 25;
      if (tokens[i] == "checkerboard") return 26;
      if (tokens[i] == "multipledraw") return 27;
      if (tokens[i] == "if0") return 28;
      if (tokens[i] == "interact") return 29;

      if (is_digit(tokens[i])) return -1; //number
      return -10; //valuable
    }
};

// Node
class Node {
  public:
    std::string value = "";
    int id = -100;
    Node* parent = nullptr;
    Node* function = nullptr;
    Node* args = nullptr;

    void show() {
      std::cerr<<"node value is: "<<value<<" ";
      if (parent != nullptr) {
        std::cerr<<"parent is: "<<(parent->value)<<std::endl;
      } else {
        std::cerr<<std::endl;
      }
      if (function != nullptr) {
        Tokens token;
        token.push(function->value);
        if (token.id(0) != -10){
          std::cerr<<"This node has function"<<std::endl;
          function->show();
        }
      }
      if (args != nullptr) {
        Tokens token;
        token.push(args->value);
        if (token.id(0) != -10){
          std::cerr<<"This node has args"<<std::endl;
          args->show();
        }
      }
    }
};

class Tree {
  public:
    std::map<std::string, Node*> function_map;

    Node* get_new_root_node(std::string& name) {
      Node* root = new Node;
      function_map[name] = root;
      return root;
    }
    Node* get_root_node(const std::string name) {
      if (function_map.find(name) == function_map.end()) {
        return nullptr;
      }
      return function_map[name];
    }
    Node* get_new_node(Node* parent, const std::string name) {
      Node* node = new Node;
      node->parent = parent;
      node->value = name;
      return node;
    }
};

Tree tree;
    
class Parser {
  public:
    Node* parse(Tokens& tokens, int& place, Node* parent) {
      /*std::cerr<<"Now parsing: "<<tokens.at(place)<<std::endl;
      if (parent != nullpt) {
        std::cerr<<" parent: "<<(parent->value)<<std::endl;
      }*/

      if (place >= (int)tokens.size()) return nullptr;
      Node* now = tree.get_new_node(parent, tokens.at(place));;
      now->id = tokens.id(place);
      if (now->id == -1) return now;
      if (now->id == -10) {
        now->function = tree.get_root_node(now->value);
        return now;
      }
      if (now->id == 0) {
        if (place > (int)tokens.size()-2) {
          std::cerr<<"Invalid argument at place: "<<place<<std::endl;
          return nullptr;
        }
        place++;
        //std::cerr<<"goto function"<<std::endl;
        now->function = parse(tokens, place, now);
        //std::cerr<<"come back from function"<<std::endl;
        if (place > (int)tokens.size()-1) {
          std::cerr<<"Invalid argument at place: "<<place<<std::endl;
          return nullptr;
        }
        place++;
        //std::cerr<<"goto args"<<std::endl;
        now->args = parse(tokens, place, now);
        //std::cerr<<"come back from args"<<std::endl;
      }
      if (now->id > 0) {
        return now;
      }
      return now;
    }
};

Parser parser;

class Result {
  public:
    long long id = -100;
    long long num_value;
    bool nil = false;

    long long stol(std::string number){
      long long ret = 0LL;
      for (int i=0;i<(int)number.size();i++){
        ret *= 10;
        ret += (long long)(number[i]-'0');
      }
      return ret;
    }

    void show() {
      if (id == -100) {
        std::cerr<<"Invalid result"<<std::endl;
      }
      if (id == 0) { // number
        std::cout<<" "<<num_value<<" ";
      }
      if (id == 1) { // nil
        std::cout<<" nil ";
      };
    } 
};

class Executer {
  public:
    std::stack<Node*> stack;
    Result execute(Node* node) {
      std::cerr<<"eval value:"<<(node->value)<<" id:"<<(node->id)<<std::endl;

      if (node->value == "ap") {
        return execute(node->function);
      }
      if (node->value == "i") {
        return execute(node->parent->args);
      }
      if (node->value == "nil") {
        Result ret;
        ret.id = 1;
        ret.nil = true;
        return ret;
      }
      if (node->id == -1) {
        Result ret;
        ret.id = 0; // number
        ret.num_value = ret.stol(node->value);
        return ret;
      }
      if (node->id == -10) {
        return execute(node->function);
      }
      return Result();
    }
};

Executer executer;

int main() {
  std::string line;
  int line_number = 1;
  while(std::getline(std::cin, line)) {
    std::stringstream ss; ss << line;
    std::string valuable; ss >> valuable;
    std::string eq_exp; ss >> eq_exp;
    if (eq_exp != "=") {
      std::cerr<<"Line "<<line_number<<" is not definition"<<std::endl;
      return 0;
    }
    Tokens tokens;
    std::string token;
    while (ss >> token) {
      tokens.push(token);
    }
    Node* root = tree.get_new_root_node(valuable);;
    root->value = valuable;
    root->id = -10;
    int place = 0;
    root->function =
        parser.parse(tokens, place, root);
    line_number++;
    //root->show();
    //std::cout<<"line: "<<line_number<<std::endl;
    //tokens.show();
  }
  //for (auto it = tree.function_map.begin();it != tree.function_map.end();it++) {
    //std::cout<<(it->first)<<std::endl;
    //std::cout<<(it->second->value)<<std::endl;
    //it->second->show();
  //}
  executer.execute(tree.get_root_node("galaxy")).show();
  return 0;
}
