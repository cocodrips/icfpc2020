import sys


def apapnize(e):
  ty = type(e)
  if ty == tuple:
    if len(e) != 2:
      raise ValueError('innput contais tuple of size not 2')
    return 'ap ap cons {} {}'.format(apapnize(e[0]), apapnize(e[1]))
  elif ty == list:
    if len(e) == 0:
      return 'nil'
    return apapnize((e[0], e[1:]))
  elif ty == int:
    return e
  else:
    raise ValueError('input contains unknown expression')


def main():
  line = sys.stdin.readline()

  nil = []
  parsed = eval(line)

  print(apapnize(parsed))


if __name__ == '__main__':
  main()
