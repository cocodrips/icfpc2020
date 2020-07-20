import requests
import sys

import demodulate
import modulate


class Error(Exception):
    pass


class Client(object):

    def __init__(self, url):
        self.url = url

    def send(self, req):
        raw_req = modulate.modulate(req)
        print(f'--> {req!r}; {raw_req}')
        http_res = requests.post(self.url, data=raw_req)
        http_res.raise_for_status()
        raw_res = http_res.text.strip()
        res = demodulate.demodulate(raw_res)
        print(f'<-- {res!r}; {raw_res}')
        if res == [0]: raise Error('request error')
        return res


def main(argv):
    server_url, player_key = argv[1:]
    player_key = int(player_key)

    print((server_url, player_key))

    client = Client(f'{server_url}/aliens/send')

    client.send([2, player_key, []])
    client.send([3, player_key, [442, 1, 0, 1]])
    while True: client.send([4, player_key, []])


if __name__ == '__main__':
    main(sys.argv)
