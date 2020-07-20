import io


def _demod_expr(r):
    result = []

    while True:
        kind = r.read(2)
        if   kind == '00':
            return result
        elif kind == '01':
            return _with_cdr(result, +_demod_number(r))
        elif kind == '10':
            return _with_cdr(result, -_demod_number(r))
        elif kind == '11':
            result.append(_demod_expr(r))
        else:
            raise ValueError()


def _demod_number(r):
    size = 0
    while r.read(1) == '1':
        size += 4
    if size == 0:
        return 0
    return int(r.read(size), 2)


def _with_cdr(result, cdr):
    for car in reversed(result): cdr = (car, cdr)
    return cdr


def demodulate(bits):
    return _demod_expr(io.StringIO(bits))


if __name__ == '__main__':
    print(demodulate(input()))
