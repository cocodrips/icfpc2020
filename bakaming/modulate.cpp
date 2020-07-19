#include <iostream>
#include <string>
#include <stack>
#include <cctype>
#include <cmath>

class Modulator {
  public:
    void alert (int pointer) {
      std::cerr<<"Invalid Character is found at "<<pointer<<std::endl;
      std::cerr<<output<<std::endl;
      return;
    }

    void add_sign_header(const char c,int pointer) {
      if (c == '-') {
        output += "10";
        pointer++;
      } else if (std::isdigit(c)) {
        output += "01";
      } else {
        alert(pointer);
      }
    }

    void read_digit(int& pointer, long long& num) {
      while(pointer < (int)input.size() && std::isdigit(input[pointer])) {
        num *= 10;
        num += (long long)(input[pointer] - '0');
        pointer++;
      }
    }
    
    void get_msb(const int number, int& msb) {
      for (int i=63;i>=0;i--) {
         if (number & (1LL<<i)) {
           msb = i+1;
           break;
         }
      }
    }

    void write_cbit_header(const int cbit) {
      for (int i=0;i<cbit;i++) {
        output += "1";
      }
      output += "0";
    }

    bool write_digit(int& pointer) {
      if (pointer >= (int)input.size()) {
        return false;
      }
      add_sign_header(input[pointer], pointer);
      long long number = 0LL;
      read_digit(pointer, number);
      int msb = 0;
      get_msb(number, msb);
      int cbit = msb % 4;
      cbit = (msb - msb%4) / 4 + 1;
      write_cbit_header(cbit);
      for (int i=cbit*4-1;i>=0;i--) {
        if (number & (1LL<<i)) {
          output += "1";
        } else {
          output += "0";
        }
      }
      return true;
    }

    bool check_write_list(int& pointer) {
      if (stack.empty()) {
        return false;
      }
      int nils = stack.top() - 3;
      if (nils > 0) {
        output += "00";
        stack.pop();
        pointer++;
      } else {
        return false;
      }
      return true;
    }

    bool check_cons(int& pointer) {
      if (stack.empty()) {
        return false;
      }
      if (stack.top() == 2) {
        stack.pop();
        pointer++;
      } else {
        return false;
      }
      return true;
    }
    
    bool check_push(int& pointer) {
        if (stack.empty()) {
          return false;
        }
        if (stack.top() == 2) {
          return false;
        }
        if (stack.top() == 1) {
          stack.pop();
          stack.push(2);
        } else if (stack.top() >= 3) {
          output += "11";
          int top = stack.top();
          stack.pop();
          top++;
          stack.push(top);
        }
        pointer++;
        return true;
    }

    bool modulate() {
      int pointer = 0;
      while (pointer < (int)input.size()) {
        switch (input[pointer]) {
          case '(':
            output += "11";
            stack.push(1);
            pointer++;
            break;
          case '[':
            pointer++;
            if (pointer >= (int)input.size()) {
              alert(pointer);
              return false;
            }
            if (input[pointer] == ']') {
              output += "nil";
            } else {
              output += "11";
              stack.push(3);
            }
            break;
          case ']':
            if (!check_write_list(pointer)) {
              alert(pointer);
              return false;
            }
            break;
          case ')':
            if (!check_cons(pointer)) {
              alert(pointer);
              return false;
            }
            break;
          case ',':
            if (!check_push(pointer)) {
              alert(pointer);
              return false;
            }
            break;
          default:
            if (input.substr(pointer, 3) == "nil") {
              output += "00";
              pointer += 3;
            }
            if (input[pointer] == '-' || std::isdigit(input[pointer])) {
              if (!write_digit(pointer)) {
                alert(pointer);
                return false;
              }
            }
        }
      }
      return true;
    }
    std::string input;
    std::string output;
    std::stack<int> stack;
};

Modulator modulator;

int main() {
  std::string token;
  while (std::cin>>token) {
    modulator.input += token;
  }
  std::cout<<modulator.input<<std::endl;
  if(modulator.modulate()) {
    std::cout<<modulator.output<<std::endl;
  }
  return 0;
}
