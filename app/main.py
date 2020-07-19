import requests
import sys
import urllib
import subprocess

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
    req = urllib.request.Request(url, data.encode('utf-8'), headers)
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

def main():
    server_url = sys.argv[1]
    player_key = sys.argv[2]
    print("$free -mh")
    p = subprocess.Popen("free -mh", shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    stdout_data, stderr_data = p.communicate()
    print("{}".format(stdout_data.decode('utf-8')))
    print("$cat /proc/cpuinfo")
    p = subprocess.Popen("cat /proc/cpuinfo", shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    stdout_data, stderr_data = p.communicate()
    print("{}".format(stdout_data.decode('utf-8')))
    print('ServerUrl: %s; PlayerKey: %s' % (server_url, player_key))
    mod_join = modulate("2 "+player_key)[:-2]+"110000"
    print("Join query: {}".format(mod_join))
    res, code = send_query(server_url, mod_join, False)
    print('Server response: {} code: {}'.format( res, code))

if __name__ == '__main__':
    main()
