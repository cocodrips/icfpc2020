import collections
import sys

import requests

import demodulate
import modulate


_API_KEY = '95052afa4bf54914a26622eea251b536'
_JOIN = 2
_START = 3
_COMMANDS = 4

_ACCELERATE = 0
_DETONATE = 1
_SHOOT = 2
_CLONE = 3


Point = collections.namedtuple('Point', ['x', 'y'])


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


def _sgn(x):
    return (x > 0) - (x < 0)


def main(argv):
    server_url, player_key = argv[1:]
    player_key = int(player_key)

    print((server_url, player_key))

    client = Client(f'{server_url}/aliens/send?apiKey={_API_KEY}',
                    player_key)

    client.send(_JOIN, [])

    resp = client.send(_START, [200, 0, 0, 10])
    while True:
        _, stage, info, state = resp
        _, myrole, *unused = info
        tick, _, ships = state

        commands = []
        for ship, _ in ships:
            role, id, pos, vec, *unused = ship
            if role != myrole:
                pass
            pos = Point(*pos)
            vec = Point(*vec)
            print(pos, vec)
            if (abs(pos.x) <= 72) and (abs(pos.y) <= 72):
                ax = -_sgn(pos.x)
                ay = -_sgn(pos.y)
                commands.append([_ACCELERATE, id, (ax, ay)])
                continue
            if abs(pos.x) > abs(pos.y):
                ax = -_sgn(pos.x) if _sgn(pos.x) != _sgn(vec.x) else 0
                ay = -_sgn(pos.y) if vec.y == 0 else 0
                if (ax != 0) or (ay != 0):
                    commands.append([_ACCELERATE, id, (ax, ay)])
                continue
            if abs(pos.x) < abs(pos.y):
                ay = -_sgn(pos.y) if _sgn(pos.y) != _sgn(vec.y) else 0
                ax = -_sgn(pos.x) if vec.x == 0 else 0
                if (ax != 0) or (ay != 0):
                    commands.append([_ACCELERATE, id, (ax, ay)])
                continue

        resp = client.send(_COMMANDS, commands)


if __name__ == '__main__':
    main(sys.argv)
