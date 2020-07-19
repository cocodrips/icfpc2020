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
    args = "./app/modem mod".split()
    proc = subprocess.Popen(args, stdin=subprocess.PIPE, stdout=subprocess.PIPE, text=True)
    stdout_value, stderr_value = proc.communicate(text)
    return stdout_value.strip()

def demodulate(text):
    args = "./app/modem dem".split()
    proc = subprocess.Popen(args, stdin=subprocess.PIPE, stdout=subprocess.PIPE, text=True)
    stdout_value, stderr_value = proc.communicate(text)
    return stdout_value.strip()

@dataclasses.dataclass
class EventLogger:
    server_url: str
    player_key: str
    event_index: int=0
    event_log: List[dict] = field(default_factory=list, compare=False)

    def event_logging(self, event_name, query, mod_query, responce, http_code, demod_res):
        self.event_index+=1
        self.event_log.append( {
                "event_index": self.event_index,
                "event_name": event_name,
                "query": query,
                "modulated_query": mod_query,
                "response": responce,
                "demodulated_responce": demod_res,
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
    
    query="(2,("+player_key+",(nil,nil)))"
    mod_query = modulate(query)
    res, code = send_query(server_url, mod_query, False)
    demod_res = demodulate(res)
    ev.event_logging("join", query, mod_query, res, code, demod_res)

    if "((446,(0,(0,(1,nil)))),nil)))))" in demod_res:
        cood="[446,0,0,1)"
    else:
        cood="[0,0,0,0]"
    
    query="[3,"+player_key+","+cood+"]"
    mod_query = modulate(query)
    res, code = send_query(server_url, mod_query, False)
    demod_res = demodulate(res)
    ev.event_logging("start", query, mod_query, res, code, demod_res)

    query="[4,"+player_key+",nil]]"
    mod_query = modulate(query)
    res, code = send_query(server_url, mod_query, False)
    demod_res = demodulate(res)
    ev.event_logging("command", query, mod_query, res, code, demod_res)

    ev.print_logs()

if __name__ == '__main__':
    main()
