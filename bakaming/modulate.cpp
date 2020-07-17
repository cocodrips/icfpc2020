#include <iostream>
#include <string>
#include <stack>

std::string input;
std::stack<int> st;
int pt;

void modulate() {
  while (pt < (int)input.size()) {
    if (input[pt] == '0' && input[pt+1] == '0') { // nil
      std::cout<<"nil ";
      while (1) {
        if (!st.empty()) {
          int top = st.top();
          st.pop();
          top--;
          if (top == 1) {
            st.push(top);
            break;
          }
          else {
            std::cout<<") ";
          }
        } else {
          break;
        }
      }
      pt += 2;
    } else if (input[pt] == '1' && input[pt+1] == '1') { // cond
      std::cout<<"( cond ";
      st.push(2);
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
      std::cout<<num*dir<<" ";
      while (1) {
        if (!st.empty()) {
          int top = st.top();
          st.pop();
          top--;
          if (top == 1) {
            st.push(top);
            break;
          }
          else {
            std::cout<<") ";
          }
        } else {
          break;
        }
      }
    }
  }
}

int main() {
  std::cin>>input;
  modulate();
  std::cout<<std::endl;
  return 0;
}
