import urllib.request
import json
import time

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

def call_protocol(protocol, state, bit):
    url = "http://localhost:8080/protocol/" + protocol
    params = {
    'state': state,
    'value': bit,
    }

    req = urllib.request.Request('{}?{}'.format(url, urllib.parse.urlencode(params)))
    with urllib.request.urlopen(req) as res:
        body = res.read().decode('utf-8')
    body_d = json.loads(body)
    return body_d["flag"], body_d["state"], body_d["value"]

def interact(protocol, state, value, max_index):
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
        flag, new_state, data = call_protocol(protocol, state, value)
        new_log={}
        new_log["index"]=index
        new_log["query"]=data
        if flag == 0:
            log.append(new_log)
            return json.dumps({
                "output": data,
                "log": log,
                })
        ret, code = send_query(data)
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
