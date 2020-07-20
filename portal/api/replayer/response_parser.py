from api.replayer.game_items import *

def parse(raw_data):
    data = eval(raw_data.replace('nil', '[]'))

    res, stage, static_game_info, game_state = data
    x0, role, x2, x3, x4 = static_game_info
    ship_and_command_obj: [ShipAndCommand] = []

    if game_state:
        game_tick, x1, ship_and_commands = game_state
        for ship, commands in ship_and_commands:
            ship_and_command_obj.append(ShipAndCommand(Ship(*ship), Command(commands)))

    return GameState(GameInfo(x3), ship_and_command_obj)


if __name__ == '__main__':
    raw_data = '[1,1,[256,0,[512,1,64],[16,128],[97,47,11,1]],[0,[16,128],[[[1,0,(-29,48),(0,0),[97,47,11,1],0,64,1],[0,[1,1]]],[[0,1,(29,-48),(0,0),[64,64,10,1],0,64,1],nil]]]]'
    game_state = parse(raw_data)
    print(game_state)
