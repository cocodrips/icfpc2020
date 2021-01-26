import schedule
import time
from container_manager import coctl as cm 
import shutil

remains=[]
def job():
    global remains
    cl = cm.command_list()
    new_remains=[]
    for c in cl:
        cn =c.name
        if cn in remains:
            print("{} is deleteing".format(cn))
            cm.command_rm(cn)
            with open("tmp/"+cn, 'a') as f:
                f.write("1,timeout")
            shutil.move("tmp/"+cn, "logs/"+cn)
        else:
            if c.status == "exited":
                print("{} is exited. deleteing".format(cn))
                with open("tmp/"+cn, 'a') as f:
                    f.write("1 {}".format(c.logs()))
                shutil.move("tmp/"+cn, "logs/"+cn+".txt")
            else:
                print("{} is add to list".format(cn))
                new_remains.append(cn)
    remains=new_remains
def clean():
    cm.command_clean()

schedule.every(2).minutes.do(job)

schedule.every(30).minutes.do(clean)
while True:
    schedule.run_pending()
    time.sleep(1)
