import sys
from yuizumi.galaxy import _ENV, _Expr, _pretty_print, _reduce


init = "ap ap galaxy nil ap ap cons 0 0".split()
def main(argv):
    sys.setrecursionlimit(100000)
    with open('utils/galaxy.txt', 'r') as f:
        for line in f:
            lhs, _, *rhs = line.split()
            _ENV[lhs] = _Expr(lhs, rhs)

    data = init
    for i in range(10):
        data = _pretty_print(_reduce(_Expr('<input>', data)))

if __name__ == '__main__':
    main(sys.argv)
