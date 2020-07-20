#ifndef FLAG_H_
#define FLAG_H_

#include <iostream>

bool FLAG_log_tokens = false;
bool FLAG_log_verbose = false;
bool FLAG_log_warning = false;
bool FLAG_abort_on_warning = false;

void ParseFlags(int argc, char* argv[]) {
    if (argc < 2) {
        return;
    }
    char* c = argv[1];
    while (*c) {
        if (*c == 't') {
            std::clog << "FLAG_log_tokens = true" << std::endl;
            FLAG_log_tokens = true;
        }
        if (*c == 'v') {
            std::clog << "FLAG_verbose_log = true" << std::endl;
            FLAG_log_verbose = true;
        }
        if (*c == 'w') {
            std::clog << "FLAG_warning_log = true" << std::endl;
            FLAG_log_warning = true;
        }
        if (*c == 'a') {
            std::clog << "FLAG_abort_on_warning = true" << std::endl;
            FLAG_abort_on_warning = true;
        }
        c++;
    }
}

#endif  // FLAG_H_
