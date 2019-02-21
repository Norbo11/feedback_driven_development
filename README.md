curl -L https://repo.anaconda.com/miniconda/Miniconda3-latest-Linux-x86_64.sh --output Miniconda3.sh
./Miniconda3.sh

conda create -n playground_application python=3.6.5

git clone https://github.com/brendangregg/FlameGraph.git

git clone https://github.com/Norbo11/playground_application.git

curl -L https://github.com/uber/pyflame/archive/v1.6.7.tar.gz --output pyflame-1.6.7
tar xvzf pyflame-1.6.7

conda activate playground_environment


# Postgres setup

```
sudo -u postgres -i
mkdir ~/postgres_data
/usr/lib/postgresql/10/bin/pg_ctl -D ~/postgres_data -l postgres.log start
psql
CREATE DATABASE feedback_driven_development;
CREATE ROLE metric_backend WITH PASSWORD 'imperial';
\d
vim postgres_data/pg_hba.conf
```

Now add these lines as the first entry in the file:


```
local   feedback_driven_development metric_backend              md5
```

This allows you to connect to postgres using a local unix socket.


```
host    feedback_driven_development metric_backend 0.0.0.0/0    md5
```

This allows you to connect to postgres from any host. Now we actually need to make postgres listen on any host:

```
vim postgres_data/postgresql.conf
```

Change the `listen_addresses` line to `*` or `0.0.0.0` to make it listen on all IP interfaces.

```
/usr/lib/postgresql/10/bin/pg_ctl -D ~/postgres_data -l postgres.log restart
```

You might also have to open a 5432 ingress port on CloudStack VM to allow your machine to connect to the postgres database when developing locally








