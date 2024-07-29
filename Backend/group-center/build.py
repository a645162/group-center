import os
import shutil

target_path = "./group-center-docker.jar"
jar_dir = "./build/libs"


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
    if os.path.exists(target_path):
        os.remove(target_path)
    if os.path.exists(jar_dir):
        shutil.rmtree(jar_dir)

    os.mkdir(jar_dir)

    if not do_command("./gradlew bootJar"):
        print("Build failed!!!")
        exit(1)

    jar_path = get_jar_path()
    if jar_path == "":
        print("Jar not found")
        return

    # Copy to ./group-center-docker.jar
    shutil.copy(jar_path, target_path)


if __name__ == '__main__':
    main()
