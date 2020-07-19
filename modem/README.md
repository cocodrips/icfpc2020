# modem usage
- Usage: ./a.out {mod, dem, modem, demod} < input.txt

# 文法

- modulator
(x,y) : cons x y  
[x,y,z] : list = (x,(y,(z,nil)))  
[] : nil  
nil : nil  
do not use spaces

- demodulator
11XY : ( cons X Y )  
00 : nil  
number : read https://message-from-space.readthedocs.io/en/latest/message13.html
