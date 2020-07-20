import io


def _demod_expr(src):
    result = []

    while True:
        kind = src.read(2)
        if   kind == '00':
            return result
        elif kind == '01':
            return _with_cdr(result, +_demod_number(src))
        elif kind == '10':
            return _with_cdr(result, -_demod_number(src))
        elif kind == '11':
            result.append(_demod_expr(src))
        else:
            raise ValueError()


def _demod_number(src):
    size = 0
    while src.read(1) == '1':
        size += 4
    if size == 0:
        return 0
    return int(src.read(size), 2)


def _with_cdr(result, cdr):
    for car in reversed(result): cdr = (car, cdr)
    return cdr


def demodulate(bits):
    return _demod_expr(io.StringIO(bits))


if __name__ == '__main__':
    print(demodulate(input()))
