from dataclasses import dataclass


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
    size:int = 500
