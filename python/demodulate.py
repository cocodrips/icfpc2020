import sys


class Demodulator:

  def __init__(self, src):
    self.src = src
    self.pos = 0

  def read(self, n):
    if self.pos + n > len(self.src):
      raise ValueError(
          'input is incomplete: want {} digits from index {}'.format(
              n, self.pos))
    res = self.src[self.pos:self.pos + n]
    self.pos += n
    return res

  def find(self, pat):
    pat_pos = self.src.find(pat, self.pos)
    if pat_pos == -1:
      return -1
    return pat_pos - self.pos

  def demodulate(self):
    header = self.read(2)
    if header == '00':
      return []
    if header == '11':
      l = self.demodulate()
      r = self.demodulate()
      if type(r) == list:
        # TODO(tomokinat): Use append instead of prepend (and reverse all later) to avoid quadratic concatenation, if the performance really matters.
        r.insert(0, l)
        return r
      else:
        return (l, r)

    sign = +1 if header == '01' else -1

    size = self.find('0')
    if size == -1:
      raise ValueError("integer's size isn't 0 terminated")

    _ = self.read(size + 1)

    if (size == 0):
      return 0
    bits = self.read(4 * size)
    return sign * int(bits, 2)


def demodulate(src):
  demodulator = Demodulator(src)
  res = demodulator.demodulate()
  if demodulator.pos != len(src):
    print(demodulator.pos, len(src))
    raise ValueError('input contains extra suffix starting from index ' +
                     str(demodulator.pos))
  return res


def main():
  line = sys.stdin.readline().strip()
  parsed = demodulate(line)
  print(parsed)


if __name__ == '__main__':
  main()
