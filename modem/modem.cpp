#include <iostream>
#include <string>
#include <stack>
#include <cctype>
#include <cmath>
#include <sstream>

class Modem {
  public:
    void init() {
      std::cin>>input;
    }
    void init(std::string in) {
      input = in;
    }
    void print() {
      std::cout<<output<<std::endl;
    }
    void print_input() {
      std::cout<<input<<std::endl;
    }
    std::string output_string() {
      return output;
    }
    void alert (int pointer) {
      std::cerr<<"Invalid Character: "<<pointer<<std::endl;
      std::cerr<<"Output State: "<<output<<std::endl;
      return;
    }
    std::string input;
    std::string output;
    std::stack<int> stack;
};

class Modulator : public Modem {
  public:
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
};

class Demodulator : public Modem {
  public:
    std::string str(long long num) {
      std::string ret = "";
      while(num != 0LL) {
        ret = std::string{(char)(num%10 + '0')} + ret;
        num /= 10LL;
      }
      return ret;
    }
    
    void demodulate() {
      int pt = 0;
      while (pt < (int)input.size()) {
        if (input[pt] == '0' && input[pt+1] == '0') { // nil
          output+="nil";
          while (1) {
            if (!stack.empty()) {
              int top = stack.top();
              stack.pop();
              top--;
              if (top == 1) {
                stack.push(top);
                output += ",";
                break;
              }
              else {
                output+=")";
              }
            } else {
              break;
            }
          }
          pt += 2;
        } else if (input[pt] == '1' && input[pt+1] == '1') { // cons
          output+="(";
          stack.push(2);
          pt += 2;
        } else { // number
          int dir = 0;
          if (input[pt] == '0') dir = 1;
          else dir = -1;
          pt += 2;
          int cnt_bits = 0;
          while (input[pt] == '1') {
            pt++;
            cnt_bits++;
          }
          pt++;
          cnt_bits *=4;
          long long num = 0;
          while (cnt_bits--) {
            num <<= 1;
            num += input[pt] == '0' ? 0 : 1;
            pt++;
          }
          if (dir == -1) output += "-";
          output += str(num);
          while (1) {
            if (!stack.empty()) {
              int top = stack.top();
              stack.pop();
              top--;
              if (top == 1) {
                output += ",";
                stack.push(top);
                break;
              }
              else {
                output+=")";
              }
            } else {
              break;
            }
          }
        }
      }
    }
};

Modulator modulator;
Demodulator demodulator;

void print_usage() {
  std::cerr<<"Usage: ./a.out {mod, dem, modem, demod} < input.txt"<<std::endl; 
}

int main(int argc, char *argv[]) {
  if (argc != 2) {
    print_usage();
    return 0;
  }
  std::string subcommand{argv[1]};

  if (subcommand == "mod") {
    modulator.init();
    modulator.modulate();
    modulator.print();
  } else if (subcommand == "dem") {
    demodulator.init();
    demodulator.demodulate();
    demodulator.print();
  } else if (subcommand == "modem") {
    modulator.init();
    modulator.modulate();
    demodulator.init(modulator.output_string());
    demodulator.demodulate();
    demodulator.print();
  } else if (subcommand == "demod") {
    demodulator.init();
    demodulator.demodulate();
    modulator.init(demodulator.output_string());
    modulator.modulate();
    modulator.print();
  } else {
    print_usage();
  }
  return 0;
}
