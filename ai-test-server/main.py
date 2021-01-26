from container_manager import coctl as cm 
from fastapi import Body, FastAPI
import subprocess
import urllib
import shutil
from demodulate import demodulate
from modulate import modulate
import json
import os

app = FastAPI()

#def modulate(text):
#    args = "./modem mod".split()
#    proc = subprocess.Popen(args, stdin=subprocess.PIPE, stdout=subprocess.PIPE, text=True)
#    stdout_value, stderr_value = proc.communicate(text)
#    return stdout_value.strip()
#
#def demodulate(text):
#    args = "./modem dem".split()
#    proc = subprocess.Popen(args, stdin=subprocess.PIPE, stdout=subprocess.PIPE, text=True)
#    stdout_value, stderr_value = proc.communicate(text)
#    return stdout_value.strip()

def send_query(bit):
    url = 'https://icfpc2020-api.testkontur.ru/aliens/send'
    data = bit
    params = {
    'apiKey': "95052afa4bf54914a26622eea251b536",
    }
    headers = {
        'accept': '*/*',
        'Content-Type': 'text/plain',
    }

    req = urllib.request.Request('{}?{}'.format(url, urllib.parse.urlencode(params)), data.encode('utf-8'), headers)
    with urllib.request.urlopen(req) as res:
        body = res.read().decode('utf-8')
        code = res.getcode()
        #info = res.info()
    return body, code

@app.get('/run') # methodとendpointの指定
async def run(attacker: str = "", defender: str = ""):
    if attacker == "" or defender == "":
        return json.dumps({
            "status"   : "invalid request",
        })
    if os.path.exists("logs/encount_list.json"):
        with open('logs/encount_list.json', 'r') as f:
            el = json.load(f)
    else:
        el = []
    for e in el:
        if e["attacker"] == attacker and e["defender"] == defender:
            return json.dumps({
                "status"   : "existed",
                "attacker" : attacker,
                "defender" : defender,
                "attack_id" : e["attack_id"],
                "defend_id" : e["defend_id"],
            })

    body, code = send_query("11011000011101000")
    if code != 200:
        return json.dumps({
            "status"   : "failed to create game",
        })
    else:
        demod_res =  str(demodulate(body))
        sp_res = demod_res.replace(" ","").replace("(","").replace(")","").replace("[","").replace("]","").split(",")
        print(sp_res)
        attack_id = sp_res[2]
        defend_id = sp_res[4]

    cm.command_run("gcr.io/icfpc2020-bokuyaba/client:"+attacker, attack_id)
    cm.command_run("gcr.io/icfpc2020-bokuyaba/client:"+defender, defend_id)
    el.append({
       "attacker" : attacker,
       "defender" : defender,
       "attack_id" : attack_id,
       "defend_id" : defend_id,
    })
    with open('logs/encount_list.json', 'w') as f:
        json.dump(el, f, indent=2)
    return json.dumps({
        "status"   : "success",
        "attacker" : attacker,
        "defender" : defender,
        "attack_id" : attack_id,
        "defend_id" : defend_id,
    })

@app.post("/aliens/send")
def send(body=Body(...)):
    player_key = str(demodulate(str(body))).replace(" ","").replace("(","").replace(")","").replace("[","").replace("]","").split(",")[1]
    res_body, code = send_query(str(body))
    demod_res =  str(demodulate(res_body))
    print(demod_res)
    with open("tmp/"+player_key, 'a') as f:
        f.write("0 "+demod_res+"\n")
    game_stage =  demod_res.replace(" ","").replace("(","").replace(")","").replace("[","").replace("]","").split(",")[1]
    if game_stage=="2":
        shutil.move("tmp/"+player_key, "logs/"+player_key+".txt")
        cm.command_rm(player_key)
    return res_body
