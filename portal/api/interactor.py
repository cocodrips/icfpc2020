import urllib.request
import json
import time
from api.thirdparty.apapnize import apapnize_from_string


def send_query(bit, is_real):
    if is_real:
        url = 'https://icfpc2020-api.testkontur.ru/aliens/send'
    else:
        url = 'https://dummy-server-prtffs3u5a-uc.a.run.app/aliens/send'
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

def call_protocol(protocol, state, value):
    url = "http://localhost:8080/protocol/" + protocol
    
    if state == "nil":
        state_a = state
    else:
        state_a = apapnize_from_string(state)
    value_a = apapnize_from_string(value)
    params = {
        'value': "ap_ap_galaxy_"+state_a.replace(" ", "_")+"_"+value_a.replace(" ", "_"),
    }
    req = urllib.request.Request('{}?{}'.format(url, urllib.parse.urlencode(params)))
    with urllib.request.urlopen(req) as res:
        body = res.read().decode('utf-8')
    #body_d = json.loads(body)
    return body.replace(" ", "")

def interact(protocol, state, value, max_index, is_real):
    index=0
    log=[]
    if not protocol or not value or not state:
        return "invalid query protocol {}, value {}, state {}".format(
                protocol,
                value,
                state,
                )
    if not max_index:
        max_index=10
    else:
        max_index=int(max_index)
    while True:
        data = call_protocol(protocol, state, value)
        return data
        new_log={}
        new_log["index"]=index
        new_log["query"]=data
        if flag == 0:
            log.append(new_log)
            return json.dumps({
                "output": data,
                "log": log,
                })
        ret, code = send_query(data, is_real)
        new_log["code"] = code
        new_log["return"] = ret
        if ret == "1101000":
            log.append(new_log)
            return json.dumps({
                "output": "invalid",
                "log": log,
                })
        time.sleep(0.4)
        index += 1
        log.append(new_log)
        state = new_state
        value = ret
        if index > max_index:
            return json.dumps({
                "output": "max index exceeded",
                "log": log,
                })
