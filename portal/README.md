## Menu
- [visualizer](/visualizer)
- [modem](/modem)

## API

#### [/demodulate?value=\<bin>](/demodulate?value=11110110000110100110110000)
- GET `http://host/demodulator?value=11110110000110100110110000`
- Response: `( cons ( cons 1 -6 ) ( cons nil nil ) )`    

#### [/modulate?value=\<\[int\]>](/modulate?value=1%2010)
- GET `http://host/demodulator?value=1%2010`
    - 1%2010: 1 10
- Response: `1101100001110110101000`    


#### [/interact-dummy?protocol=\<string>&state=\<binary>&value=\<bin>&max_index=\<int>](/interact-dummy?portal=dummy&state=1&value=)
- GET `http://host/interact-dummy?protocol=dummy\&state=0\&value=11011000011101000\&max_index=1`
- Responce: simulate real alians api

#### [/interact?protocol=\<string>&state=\<binary>&value=\<bin>&max_index=\<int>](/interact?portal=dummy&state=1&value=)
- GET `http://host/interact?protocol=dummy\&state=0\&value=11011000011101000\&max_index=1`
- Response: 
```
{
  "output": "invalid",
  "log": [
    {
      "index": 0,
      "query": "11011000011101000",
      "code": 200,
      "return": "110110000111111..."
    },
    {
      "index": 1,
      "query": "1101100001111111...",
      "code": 200,
      "return": "1101000"
    }
  ]
}
```    
