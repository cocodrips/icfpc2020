#!/usr/bin/env python
# -*- coding: utf-8 -*-
import sys

def apapnize(e):
    ty = type(e)
    if ty == tuple:
        if len(e) != 2:
            raise ValueError('input contains tuple of size not 2')
        return 'ap ap cons {} {}'.format(apapnize(e[0]), apapnize(e[1]))
    elif ty == list:
        if len(e) == 0:
            return 'nil'
        return 'ap ap cons {} {}'.format(apapnize(e[0]), apapnize(e[1:]))
    elif ty == int:
        return e
    else:
        raise ValueError('input contains unknown expression')


line = sys.stdin.readline()
parsed = eval(line.replace('nil', '[]'))
print(apapnize(parsed))

