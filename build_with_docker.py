import os
import subprocess


def run_command(command):
    command_list = command.split(" ")
    command_list = [
        item.strip()
        for item in command_list
        if len(item.strip()) > 0
    ]
    print("Run Command: ", command_list)
    with subprocess.Popen(command_list, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, bufsize=1,
                          universal_newlines=True) as process:
        for line in process.stdout:
            print(line, end='')  # 实时打印输出

    exit_code = process.wait()

    return exit_code == 0


image_name = "group-center-builder"
print("=" * 20)
print(f"Build Jar({image_name}) using Docker")
print("=" * 20)

# Build Image
print("=" * 20)
print("== Build Image")
if not run_command(f"docker build -t {image_name} -f ./Docker/Dockerfile-Build ."):
    print("Build Image Failed")
    exit(1)

# Check Build Scripts
print("=" * 20)
print("== Check Build Scripts")
if os.path.exists("./build.py"):
    print("Build Scripts Exists")
else:
    print("Build Scripts Not Exists")
    exit(1)

# Run Containers
print("=" * 20)
print("== Run Containers")
# Get Work Dir
# work_dir = os.getcwd()
# py_file_path = os.path.abspath(__file__)
# work_dir = os.path.dirname(py_file_path)
# print("Work Dir: ", work_dir)
# if not run_command(f"docker run --rm -v {work_dir}:/usr/local/group-center {image_name}"):
if not run_command(f"docker run --rm -v .:/usr/local/group-center {image_name}"):
    print("Run Containers Failed")
    exit(1)

# Remove Image
print("=" * 20)
print("== Remove Image")
run_command(f"docker rmi {image_name}")

# Done
print("=" * 20)
print("== Done")
print("=" * 20)
