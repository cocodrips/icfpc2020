import sys


def modulate(e):
  ty = type(e)
  if ty == tuple:
    if len(e) != 2:
      raise ValueError('innput contais tuple of size not 2')
    return '11{}{}'.format(modulate(e[0]), modulate(e[1]))
  elif ty == list:
    if len(e) == 0:
      return '00'
    return modulate((e[0], e[1:]))
  elif ty == int:
    if e == 0:
      return '010'

    header = '01' if e > 0 else '10'
    e = abs(e)

    n_bits = e.bit_length()
    n_4bits = (n_bits + 3) // 4
    bits = '{:064b}'.format(e)

    return '{}{}0{}'.format(header, '1' * n_4bits, bits[64 - 4 * n_4bits:])
  else:
    raise ValueError('input contains unknown expression')


def main():
  line = sys.stdin.readline()

  nil = []
  parsed = eval(line)

  print(modulate(parsed))


if __name__ == '__main__':
  main()
