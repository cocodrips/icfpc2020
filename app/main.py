import requests
import sys
import urllib
import subprocess
import dataclasses
from typing import List
from dataclasses import field
import json

def send_query(url, bit, is_real):
    data = bit
    headers = {
        'accept': '*/*',
        'Content-Type': 'text/plain',
    }
    req = urllib.request.Request(url+"/aliens/send", data.encode('utf-8'), headers)
    with urllib.request.urlopen(req) as res:
        body = res.read().decode('utf-8')
        code = res.getcode()
    return body.strip(), code

def modulate(text):
    args = "./app/modulator"
    proc = subprocess.Popen(args, stdin=subprocess.PIPE, stdout=subprocess.PIPE, text=True)
    stdout_value, stderr_value = proc.communicate(text)
    return stdout_value.strip()

@dataclasses.dataclass
class EventLogger:
    server_url: str
    player_key: str
    event_index: int=0
    event_log: List[dict] = field(default_factory=list, compare=False)

    def event_logging(self, event_name, query, responce, http_code):
        self.event_index+=1
        self.event_log.append( {
                "event_index": self.event_index,
                "event_name": event_name,
                "query": query,
                "response": responce,
                "http_code": http_code,
                })
    def print_logs(self):
        print(json.dumps({
            "server_url": self.server_url,
            "player_key": self.player_key,
            "event_logs": self.event_log,
            }, indent=2))
    
def gen_nil():
    return "00"

def gen_number(number):
    if number == 0:
        return "010";
    ret = ""
    if number < 0:
        ret += "10"
    else:
        ret += "01"
    ret += "11111111111111110"
    for i in range(64):
        if number & (1<<(63-i)) > 0:
            ret+="1"
        else:
            ret+="0"
    return ret

def main():
    server_url = sys.argv[1]
    player_key = sys.argv[2]

    log={}
    ev = EventLogger(server_url=server_url, player_key=player_key)

    mod_join = "110110001011"+ gen_number(int(player_key)) + "1111000000"
    res_join, code = send_query(server_url, mod_join, False)
    ev.event_logging("join", mod_join, res_join, code)
    
    mod_start = "110110001111" + gen_number(int(player_key)) + "11110101101011010110100000"
    res_start, code = send_query(server_url, mod_join, False)
    ev.event_logging("start", mod_start, res_start, code)

    mod_command = modulate("4 "+player_key)[:-2]+"1111011000001101100000110110000011011000000000"
    res_command, code = send_query(server_url, mod_join, False)
    ev.event_logging("command", mod_command, res_command, code)
    ev.print_logs()

if __name__ == '__main__':
    main()
