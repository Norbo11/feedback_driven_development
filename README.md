curl -L https://repo.anaconda.com/miniconda/Miniconda3-latest-Linux-x86_64.sh --output Miniconda3.sh
./Miniconda3.sh

conda create -n playground_application python=3.6.5

git clone https://github.com/brendangregg/FlameGraph.git

git clone https://github.com/Norbo11/playground_application.git

curl -L https://github.com/uber/pyflame/archive/v1.6.7.tar.gz --output pyflame-1.6.7
tar xvzf pyflame-1.6.7

conda activate playground_environment
