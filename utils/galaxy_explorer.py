import sys
from yuizumi.galaxy import _ENV, _Expr, _pretty_print, _reduce

query = "ap ap galaxy {state} ap ap cons {x} {y}"
# [0, [0], 0, []]
state = "ap ap cons {} ap ap cons ap ap cons {} nil ap ap cons {} ap ap cons nil nil"


def main(argv):
    sys.setrecursionlimit(100000)
    with open('utils/galaxy.txt', 'r') as f:
        for line in f:
            lhs, _, *rhs = line.split()
            _ENV[lhs] = _Expr(lhs, rhs)

    data = query
    image_set = set()
    point = (0, 0)
    state_exp = 'nil'

    for i in range(2):
        flag, state, images = _pretty_print(_reduce(_Expr('<input>',
                                                          query.format('nil',
                                                           point[0], point[1]).split())))
        for image in images:
            for point in image:
                image_set.add(point)
        state = state_exp.format(state[0], state[1], state[2])
        print(data)


if __name__ == '__main__':
    main(sys.argv)
