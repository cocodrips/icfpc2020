from api.thirdparty.modulate import modulate_from_string
from api.thirdparty.demodulate import demodulate as  demodulate_from_string


def demodulate(value):
    if not value:
        return ""
    return demodulate_from_string(value)

def modulate(value):
    if not value:
        return ""
    return modulate_from_string(value)
