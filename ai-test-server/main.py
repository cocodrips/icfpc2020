from container_manager import coctl as cm 
from fastapi import Body, FastAPI
import subprocess
import urllib
import shutil

app = FastAPI()

def modulate(text):
    args = "./modem mod".split()
    proc = subprocess.Popen(args, stdin=subprocess.PIPE, stdout=subprocess.PIPE, text=True)
    stdout_value, stderr_value = proc.communicate(text)
    return stdout_value.strip()

def demodulate(text):
    args = "./modem dem".split()
    proc = subprocess.Popen(args, stdin=subprocess.PIPE, stdout=subprocess.PIPE, text=True)
    stdout_value, stderr_value = proc.communicate(text)
    return stdout_value.strip()

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
        return "invalid param attcker {}, defender {}".format(attacker, defender)
    body, code = send_query("11011000011101000")
    if code != 200:
        return "fail to create game"
    else:
        demod_res =  demodulate(body)
        sp_res = demod_res.replace("(","").replace(")","").split(",")
        print(sp_res)
        attack_id = sp_res[2]
        defend_id = sp_res[5]

    cm.command_run("gcr.io/icfpc2020-bokuyaba/client:"+attacker, attack_id)
    cm.command_run("gcr.io/icfpc2020-bokuyaba/client:"+defender, defend_id)
    return "run scceeded attcker: {} - {} defender: {} - {}".format(attacker, attack_id, defender, defend_id)

@app.post("/aliens/send")
def send(body=Body(...)):
    player_key = demodulate(str(body)).replace("(","").replace(")","").split(",")[1]
    res_body, code = send_query(str(body))
    demod_res =  demodulate(res_body)
    print(demod_res)
    with open("tmp/"+player_key, 'a') as f:
        f.write(demod_res+"\n")
    game_stage =  demod_res.replace("(","").replace(")","").split(",")[1]
    if game_stage=="2":
        shutil.move("tmp/"+player_key, "logs/"+player_key)
        cm.command_rm(player_key)
    return res_body
