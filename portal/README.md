## Menu
- [visualizer](/visualizer)
- [demodulator](/demodulator)

## API

#### [/demodulate?value=\<bin>](/demodulate?value=)
- GET `http://host/demodulator?value=11110110000110100110110000`
- Response: `( cons ( cons 1 -6 ) ( cons nil nil ) )`    

#### [/interact-dummy?protocol=\<string>&state=\<binary>&value=\<bin>&max_index=\<int>](/interact-dummy?portal=dummy&state=1&value=)
- GET `http://host/interact-dummy?protocol=dummy\&state=0\&value=11011000011101000\&max_index=1`
- Responce: simulate real alians api

#### [/interact?protocol=\<string>&state=\<binary>&value=\<bin>&max_index=\<int>](/interact?portal=dummy&state=1&value=)
- GET `http://host/interact?protocol=dummy\&state=0\&value=11011000011101000\&max_index=1`
- Response: `{"output": "invalid", "log": [{"index": 0, "query": "11011000011101000", "code": 200, "return": "11011000011111110101101111111111111111100010110001001110110100100100001100111110101011011111111110101000001111011000011101111111111111111100011110110000101001001111010000000001000111000000111101000111101000000"}, {"index": 1, "query": "11011000011111110101101111111111111111100010110001001110110100100100001100111110101011011111111110101000001111011000011101111111111111111100011110110000101001001111010000000001000111000000111101000111101000000", "code": 200, "return": "1101000"}]}`    