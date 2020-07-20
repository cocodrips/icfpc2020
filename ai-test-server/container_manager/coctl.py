import docker
import pathlib
import os
import argparse
import os.path
import json

def command_run(image_name, player_key):
    client = docker.from_env()
    client.images.pull(image_name)
    c = client.containers.run(
        name=player_key,
        image=image_name,
        #tty=True,
        detach=True,
        network_mode="host",
        command="http://localhost:28910 "+player_key,
    )

def command_list(args):
    client = docker.from_env()
    return client.containers.list(all=True)

def command_logs(args):
    client = docker.from_env()
    try:
        c = client.containers.get(prefix+args.task)
    except docker.errors.NotFound:
        print("{} is not found".format(args.task))
        return

    if args.follow:
        for l in c.logs(stream=True):
            print(l.strip().decode("UTF-8"))
    else:
        log_l = str(c.logs().decode("UTF-8")).split("\n")
        for l in log_l:
            print(l)

def command_rm(player_key):
    client = docker.from_env()
    try:
        c = client.containers.get(player_key)
    except docker.errors.NotFound:
        return
    c.remove(force=True)
    return

def command_clean(args):
    client = docker.from_env()
    client.containers.prune()
    client.images.prune(filters={'dangling': False})
    client.networks.prune()
    client.volumes.prune()