def modulate(expr):
    if isinstance(expr, int):
        n = (abs(expr).bit_length() + 3) // 4
        return '{sign}{ones}0{body}'.format(
            sign=('10' if (expr < 0) else '01'),
            ones=('1' * n),
            body=(f'{abs(expr):064b}'[64-4*n:]),
        )

    if isinstance(expr, tuple):
        if len(expr) == 2:
            return f'11{modulate(expr[0])}{modulate(expr[1])}'
        else:
            raise ValueError(f'invalid expression: {expr!r}')

    if isinstance(expr, list):
        return ''.join(f'11{modulate(x)}' for x in expr) + '00'

    raise ValueError(f'invalid expression: {expr!r}')


if __name__ == '__main__':
    print(modulate(eval(input().replace('nil', '[]'))))
