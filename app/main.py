import requests
import sys

import demodulate
import modulate


_API_KEY = '95052afa4bf54914a26622eea251b536'
_JOIN = 2
_START = 3
_COMMANDS = 4


class Client(object):

    def __init__(self, url, key):
        self.url = url
        self.key = key

    def send(self, cmd, arg):
        req = [cmd, self.key, arg]
        print(f'--> {req!r}')
        res = requests.post(self.url, data=modulate.modulate(req))
        res.raise_for_status()
        res = demodulate.demodulate(res.text.strip())
        print(f'<-- {res!r}')
        if res == [0]: sys.exit('request error')
        return res


def main(argv):
    server_url, player_key = argv[1:]
    player_key = int(player_key)

    print((server_url, player_key))

    client = Client(f'{server_url}/aliens/send?apiKey={_API_KEY}',
                    player_key)

    client.send(_JOIN, [])
    client.send(_START, [200, 1, 0, 100])
    while True:
        client.send(_COMMANDS, [[3,[0,0,0,1]]])


if __name__ == '__main__':
    main(sys.argv)
