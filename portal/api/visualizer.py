import numpy as np
from dataclasses import dataclass


scale_base = 800
class Picture():
    def __init__(self, image_id, state, index, points):
        self.image_id = image_id
        self.state = state
        self.index = index
        self.points = points
        self.xmin, self.ymin, self.xmax, self.ymax = 0, 0, 0, 0

        if self.points.shape[0] > 0:
            self.xmin, self.ymin = points.min(axis=0)
            self.xmax, self.ymax = points.max(axis=0)

    @property
    def height(self):
        return self.ymax - self.ymin

    @property
    def width(self):
        return self.xmax - self.xmin

    @property
    def scale(self):
        return scale_base / self.height


class Pictures():
    def __init__(self, pictures: [Picture]):
        self.pictures = pictures
        self._width, self._height = None, None

    @property
    def image_ids(self):
        return sorted(list(set([p.image_id for p in self.pictures])))

    @property
    def xmin(self):
        return min([p.xmin for p in self.pictures])

    @property
    def xmax(self):
        return max([p.xmax for p in self.pictures])

    @property
    def ymin(self):
        return min([p.ymin for p in self.pictures])

    @property
    def ymax(self):
        return max([p.ymax for p in self.pictures])

    @property
    def height(self):
        if self._height is None:
            self._height = self.ymax - self.ymin + 1
        return self._height

    @property
    def width(self):
        if self._width is None:
            self._width = self.xmax - self.xmin + 1
        return self._width

    @property
    def scale(self):
        return scale_base / self.height


def visualize(raw_data):
    """
    :return: (width, height, data)
    """
    if raw_data is None:
        return Pictures([])

    pictures = []
    for line in raw_data.split('\n'):
        if not line:
            continue
        flag, state, image_data = eval(line.replace('nil', '[]'))
        if flag == 0:
            for i, image in enumerate(image_data):
                    points = np.array(image)
                    pictures.append(Picture(state[0], state[1][0], i, points))

    return Pictures(pictures)


if __name__ == '__main__':
    raw_data="""[0, [0, [1], 0, nil], [[(-1, -3), (0, -3), (1, -3), (2, -2), (-2, -1), (-1, -1), (0, -1), (3, -1), (-3, 0), (-1, 0), (1, 0), (3, 0), (-3, 1), (0, 1), (1, 1), (2, 1), (-2, 2), (-1, 3), (0, 3), (1, 3)], [(-7, -2), (-7, -3), (-8, -2)], []]]
[0, [1, [1], 0, []], [[(-3, -3), (-2, -3), (-1, -3), (0, -3), (1, -3), (2, -3), (3, -3), (-3, -2), (0, -2), (3, -2), (-3, -1), (0, -1), (3, -1), (-3, 0), (-2, 0), (-1, 0), (0, 0), (1, 0), (2, 0), (3, 0), (-3, 1), (0, 1), (3, 1), (-3, 2), (0, 2), (3, 2), (-3, 3), (-2, 3), (-1, 3), (0, 3), (1, 3), (2, 3), (3, 3)]]]
[0, [1, [2], 0, []], [[(0, -3), (-1, -3), (-2, -3), (-3, -3), (0, 0), (-1, 0), (-2, 0), (-3, 0), (-3, 0), (-3, -1), (-3, -2), (-3, -3), (0, -1), (0, -2), (0, -3)], [(1, -3), (2, -3), (3, -3), (3, -2), (3, -1), (1, 0), (2, 0), (3, 0)], [(0, 1), (3, 1), (0, 2), (3, 2), (0, 3), (1, 3), (2, 3), (3, 3)], [(-3, 1), (-3, 2), (-3, 3), (-2, 3), (-1, 3)]]]
"""
    print(visualize(raw_data))

