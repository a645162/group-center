import os
import shutil
import argparse

target_path = "./group-center-docker.jar"
jar_dir = "./build/libs"
cache_dir_list = ["./build", "./.gradle"]

target_path = os.path.abspath(target_path)
jar_dir = os.path.abspath(jar_dir)
cache_dir_list = [
    os.path.abspath(path)
    for path in cache_dir_list
]


def get_options():
    parser = argparse.ArgumentParser()

    parser.add_argument(
        "--clean-cache",
        help="Clean Cache directory",
        action="store_true"
    )

    return parser.parse_args()


def do_command(command: str) -> bool:
    # Check is Windows
    if os.name == "nt":
        while "/" in command:
            command = command.replace("/", "\\")

    print(f"Running command: {command}")
    ret = os.system(command)

    return ret == 0


def get_jar_path() -> str:
    # Walk Jar Directory
    for root, dirs, files in os.walk(jar_dir):
        for file in files:
            if file.endswith(".jar"):
                jar_file = os.path.join(root, file)
                return jar_file

    return ""


def main():
    opt = get_options()

    if os.path.exists(jar_dir):
        shutil.rmtree(jar_dir)

    if opt.clean_cache:
        print("Cleaning Cache")
        for cache_dir in cache_dir_list:
            if os.path.exists(cache_dir):
                shutil.rmtree(cache_dir)

    os.makedirs(jar_dir)

    if os.name != "nt":
        do_command("chmod +x ./gradlew")

    if not do_command("./gradlew --info bootJar"):
        print("Build failed!!!")
        exit(1)

    if opt.clean_cache:
        print("Cleaning Cache")
        for cache_dir in cache_dir_list:
            if os.path.exists(cache_dir):
                shutil.rmtree(cache_dir)

    jar_path = get_jar_path()
    if jar_path == "":
        print("Jar not found")
        return

    # Remove Old
    if os.path.exists(target_path):
        os.remove(target_path)

    # Copy to ./group-center-docker.jar
    shutil.copy(jar_path, target_path)


if __name__ == '__main__':
    main()
