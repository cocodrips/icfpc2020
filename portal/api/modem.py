import subprocess


def demodulate(value):
    if not value:
        return ""
    args = "demodulator"
    proc = subprocess.Popen(args, stdin=subprocess.PIPE, stdout=subprocess.PIPE, text=True)
    stdout_value, stderr_value = proc.communicate(value)
    return stdout_value

def modulate(value):
    if not value:
        return ""
    args = "modulator"
    proc = subprocess.Popen(args, stdin=subprocess.PIPE, stdout=subprocess.PIPE, text=True)
    stdout_value, stderr_value = proc.communicate(value)
    return stdout_value
