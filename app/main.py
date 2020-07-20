import requests
import sys
from dataclasses import dataclass

import demodulate
import modulate

_API_KEY = '95052afa4bf54914a26622eea251b536'
_JOIN = 2
_START = 3
_COMMANDS = 4


@dataclass
class Command:
    commands: []


@dataclass
class Ship:
    role: int
    ship_id: int
    position: (int, int)
    velocity: (int, int)
    x4: [int]
    x5: int
    x6: int
    x7: int


@dataclass
class ShipAndCommand:
    ship: Ship
    command: Command


@dataclass
class GameInfo:
    space_info: [int]


@dataclass
class GameState:
    game_info: GameInfo
    ship_and_commands: [ShipAndCommand]
    size: int = 500


def parse(data):
    res, stage, static_game_info, game_state = data
    x0, role, x2, x3, x4 = static_game_info
    ship_and_command_obj: [ShipAndCommand] = []

    if game_state:
        game_tick, x1, ship_and_commands = game_state
        for ship, commands in ship_and_commands:
            ship_and_command_obj.append(ShipAndCommand(Ship(*ship), Command(commands)))

    return GameState(GameInfo(x3), ship_and_command_obj)


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
    res = client.send(_START, [200, 1, 0, 100])

    while True:
        print(parse(res))
        client.send(_COMMANDS, [])


if __name__ == '__main__':
    main(sys.argv)
