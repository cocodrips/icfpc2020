set -ex
for line in `cat baselines`
do
    curl http://104.197.240.151:28910/run\?attacker=$@\&defender=$line
    curl http://104.197.240.151:28910/run\?attacker=$line\&defender=$@
done
