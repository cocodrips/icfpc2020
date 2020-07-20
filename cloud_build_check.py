import shutil

with open(".platform") as f:
    p = f.read()
    shutil.copyfile("dockerfiles/dockerfiles/{}/Dockerfile".format(p), "Dockerfile")
