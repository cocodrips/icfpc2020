def apapnize(expr):
  if isinstance(expr, int):
    return str(expr)

  if isinstance(expr, tuple):
    if len(expr) == 2:
      return f'ap ap cons {apapnize(expr[0])} {apapnize(expr[1])}'
    else:
      raise ValueError(f'invalid expression: {expr!r}')

  if isinstance(expr, list):
    return ''.join(f'ap ap cons {apapnize(x)} ' for x in expr) + 'nil'

  raise ValueError(f'invalid expression: {expr!r}')


def apapnize_from_string(s):
  return apapnize(eval(s.replace('nil', '[]')))


if __name__ == '__main__':
  print(apapnize_from_string(input()))
