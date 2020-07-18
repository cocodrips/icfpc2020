import random


def visualize():
    """
    :return: (width, height, data)
    """
    data = []
    size = 100
    for i in range(5):
        data.append([random.randint(0, size), random.randint(0, size)])

    scale = 720 / size
    return size + 1, size + 1, scale, data


if __name__ == '__main__':
    print(visualize())
