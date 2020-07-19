import requests
import sys
import urllib
import subprocess
import dataclasses
from typing import List
from dataclasses import field
import json

def send_query(url, bit, is_real):
    #if is_real:
    #    url = 'https://icfpc2020-api.testkontur.ru/aliens/send'
    #else:
    #    url = 'https://dummy-server-prtffs3u5a-uc.a.run.app/aliens/send'
    data = bit
    #params = {
    #'apiKey': "95052afa4bf54914a26622eea251b536",
    #}
    headers = {
        'accept': '*/*',
        'Content-Type': 'text/plain',
    }
    req = urllib.request.Request(url+"/aliens/send", data.encode('utf-8'), headers)
    with urllib.request.urlopen(req) as res:
        body = res.read().decode('utf-8')
        code = res.getcode()
        #info = res.info()
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
    

def main():
    server_url = sys.argv[1]
    player_key = sys.argv[2]

    #Environment Investigation
    #print("$free -mh")
    #p = subprocess.Popen("free -mh", shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    #stdout_data, stderr_data = p.communicate()
    #print("{}".format(stdout_data.decode('utf-8')))
    #print("$cat /proc/cpuinfo")
    #p = subprocess.Popen("cat /proc/cpuinfo", shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    #stdout_data, stderr_data = p.communicate()
    #print("{}".format(stdout_data.decode('utf-8')))

    log={}
    ev = EventLogger(server_url=server_url, player_key=player_key)

    mod_join = modulate("2 "+player_key)[:-2]+"00"
    res_join, code = send_query(server_url, mod_join, False)
    ev.event_logging("join", mod_join, res_join, code)
    
    mod_start = modulate("3 "+player_key+" 0 0 0 0")
    res_start, code = send_query(server_url, mod_join, False)
    ev.event_logging("start", mod_start, res_start, code)

    mod_command = modulate("4 "+player_key)[:-2]+"1111011000001101100000110110000011011000000000"
    res_command, code = send_query(server_url, mod_join, False)
    ev.event_logging("command", mod_command, res_command, code)
    ev.print_logs()

if __name__ == '__main__':
    main()
