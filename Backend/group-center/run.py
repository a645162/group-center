import os

# FILE_ENV_PATH=./Config/Program/FileEnv.toml
os.environ['FILE_ENV_PATH'] = "./Config/Program/FileEnv.toml"

os.system("java -jar ./build/libs/group-center-docker.jar")
