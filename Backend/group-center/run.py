import os


def main():
    # FILE_ENV_PATH=./Config/Program/FileEnv.toml
    os.environ['FILE_ENV_PATH'] = "./Config/Program/FileEnv.toml"

    os.system("java -jar ./group-center-docker.jar")


if __name__ == '__main__':
    main()
