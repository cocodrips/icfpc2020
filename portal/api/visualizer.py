import numpy as np
from dataclasses import dataclass


class Picture():
    def __init__(self, index, points):
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
        return 200 / self.height()


class Pictures():
    def __init__(self, pictures: [Picture]):
        self.pictures = pictures
        self._width, self._height = None, None


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
        return 200 / self.height


def visualize(raw_data):
    """
    :return: (width, height, data)
    """
    if raw_data is None:
        return Pictures([])
    flag, state, image_data = eval(raw_data)

    pictures = []
    for i, image in enumerate(image_data):

        points = np.array(image)
        pictures.append(Picture(i, points))

    return Pictures(pictures)


if __name__ == '__main__':
    print(visualize())
