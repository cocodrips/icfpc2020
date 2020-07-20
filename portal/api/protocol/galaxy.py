import enum
import re
import sys


_NUMBER_RE = re.compile('^-?[0-9]+$')


class _Lambda(object):

    def __init__(self, name, body):
        self.name = name
        self.body = body

    def _invoke(self, arg):
        return self.body(arg)

    def __str__(self):
        return self.name


class _Special(enum.Enum):
    NIL = 'nil'
    AP = 'AP'

    def __str__(self):
        return self.value


_ENV = {
    'add'   : _Lambda('add', lambda x: _Lambda(f'(add {x})', lambda y: (_reduce(x) + _reduce(y)))),
    'mul'   : _Lambda('mul', lambda x: _Lambda(f'(mul {x})', lambda y: (_reduce(x) * _reduce(y)))),
    'div'   : _Lambda('div', lambda x: _Lambda(f'(div {x})', lambda y: (_reduce(x) // _reduce(y)))),
    'eq'    : _Lambda('eq', lambda x: _Lambda(f'(eq {x})', lambda y: (_reduce(x) == _reduce(y)))),
    'lt'    : _Lambda('lt', lambda x: _Lambda(f'(lt {x})', lambda y: (_reduce(x) < _reduce(y)))),
    'neg'   : _Lambda('neg', lambda x: -_reduce(x)),
    's'     : _Lambda('s', lambda f: _Lambda(f'(s {f})', lambda g: _Lambda(f'(s {f} {g})', lambda x: _Apply(_Apply(f, x), _Apply(g, x))))),
    'c'     : _Lambda('c', lambda f: _Lambda(f'(c {f})', lambda x: _Lambda(f'(c {f} {x})', lambda y: _Apply(_Apply(f, y), x)))),
    'b'     : _Lambda('b', lambda f: _Lambda(f'(b {f})', lambda g: _Lambda(f'(b {f} {g})', lambda x: _Apply(f, _Apply(g, x))))),
    'i'     : _Lambda('i', lambda x: x),
    't'     : True,
    'cons'  : _Lambda('cons', lambda x: _Lambda(f'(cons {x})', lambda y: _Cons(x, y))),
    'car'   : _Lambda('car', lambda x: _semi_reduce(x).car),
    'cdr'   : _Lambda('cdr', lambda x: _semi_reduce(x).cdr),
    'nil'   : _Special.NIL,
    'isnil' : _Lambda('isnil', lambda x: _reduce(x) == _Special.NIL),
}


class Error(Exception):
    pass


class _Reducible(object):

    def __init__(self):
        self._value = None

    @property
    def value(self):
        if not self._value:
            self._value = self._reduce()
        return self._value

    def _reduce(self):
        raise NotImplementedError()


class _Cons(object):

    def __init__(self, car, cdr):
        super().__init__()
        self.car = car
        self.cdr = cdr

    def __str__(self):
        return f'({self.car}, {self.cdr})'


class _Apply(_Reducible):

    def __init__(self, f, x):
        super().__init__()
        self.f = f
        self.x = x

    def _reduce(self):
        return _semi_reduce(_call(self.f, self.x))

    def __str__(self):
        return f'ap({self.f} {self.x})'


class _Expr(_Reducible):

    def __init__(self, lhs, rhs):
        super().__init__()
        self.lhs = lhs
        self.rhs = rhs

    def _reduce(self):
        stack = []

        for token in self.rhs:
            if   token == 'ap':
                stack.append(_Special.AP)
            elif token in _ENV:
                stack.append(_ENV[token])
            elif _NUMBER_RE.match(token):
                stack.append(int(token))
            else:
                raise Error(f'unknown token: {token!r}')

            while len(stack) >= 3:
                if stack[-1] == _Special.AP:
                    break
                if stack[-2] == _Special.AP:
                    break
                if stack[-3] == _Special.AP:
                    f, x = stack[-2:]
                    stack = stack[:-3]
                    stack.append(_Apply(f, x))
                else:
                    raise Error(f'invalid expression: {str(self)!r}; {stack!r}')

        if len(stack) != 1:
            raise Error(f'invalid expression: {str(self)!r}; {stack!r}')

        return _semi_reduce(stack[0])

    def __str__(self):
        return self.lhs


def _semi_reduce(x):
    return x.value if isinstance(x, _Reducible) else x


def _reduce(x):
    x = _semi_reduce(x)
    return (_reduce(x.car), _reduce(x.cdr)) if isinstance(x, _Cons) else x


def _call(f, x):
    f = _semi_reduce(f)

    if isinstance(f, _Cons):
        return _Apply(_Apply(x, f.car), f.cdr)
    if f == True:
        f = _Lambda('t', lambda x1: _Lambda(f'(t {x1})', lambda x2: x1))
    if f == False:
        f = _Lambda('f', lambda x1: _Lambda(f'(f {x1})', lambda x2: x2))
    if f == _Special.NIL:
        f = _Lambda('nil', lambda _: True)
    return f._invoke(x)


def _pretty_print(x):
    if x == _Special.NIL:
        return []
    if isinstance(x, tuple):
        car, cdr = x
        car = _pretty_print(car)
        cdr = _pretty_print(cdr)
        if isinstance(cdr, list):
            return [car] + cdr
        return (car, cdr)
    return x


def convert(arg):
    sys.setrecursionlimit(100000)
    with open("/app/api/protocol/galaxy.txt") as f:
        for line in f:
            lhs, _, *rhs = line.split()
            _ENV[lhs] = _Expr(lhs, rhs)
    return str(_pretty_print(_reduce(_Expr('<input>', arg))))


if __name__ == '__main__':
    main(sys.argv)
