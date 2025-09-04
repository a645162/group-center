import os
import re
import subprocess


def run_command(command):
    command_list = command.split(" ")
    command_list = [item.strip() for item in command_list if len(item.strip()) > 0]
    print("Run Command: ", command_list)
    with subprocess.Popen(
        command_list,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        bufsize=1,
        universal_newlines=True,
    ) as process:
        for line in process.stdout:
            print(line, end="")  # 实时打印输出

    exit_code = process.wait()

    return exit_code == 0


gradle_properties_path = r"gradle/wrapper/gradle-wrapper.properties"


def gradle_change_to_official():
    # Backup to .bak
    backup_path = gradle_properties_path + ".bak"

    # 创建备份
    if os.path.exists(gradle_properties_path):
        with open(gradle_properties_path, "r", encoding="utf-8") as f:
            content = f.read()

        with open(backup_path, "w", encoding="utf-8") as f:
            f.write(content)

        print(f"Backup created: {backup_path}")

        # 替换为官方源
        # 匹配 distributionUrl=https://任意内容/gradle/ 的模式
        pattern = r"distributionUrl=https\\?://[^/]+/gradle/"
        replacement = "distributionUrl=https\\://services.gradle.org/distributions/"

        updated_content = re.sub(pattern, replacement, content)

        with open(gradle_properties_path, "w", encoding="utf-8") as f:
            f.write(updated_content)

        print("Changed to official Gradle distribution")
    else:
        print(f"Warning: {gradle_properties_path} not found")


def restore_to_original():
    backup_path = gradle_properties_path + ".bak"

    if os.path.exists(backup_path):
        with open(backup_path, "r", encoding="utf-8") as f:
            content = f.read()

        with open(gradle_properties_path, "w", encoding="utf-8") as f:
            f.write(content)

        # 删除备份文件
        os.remove(backup_path)
        print("Restored to original Gradle distribution")
    else:
        print(f"Warning: Backup file {backup_path} not found")


image_name = "group-center-builder"
print("=" * 20)
print(f"Build Jar({image_name}) using Docker")
print("=" * 20)



# Build Image
print("=" * 20)
print("== Build Image")

ret: bool = not run_command(f"docker build -t {image_name} -f ./Docker/Dockerfile-Build .")

print("=" * 20)
print("== Check Build Image")
if ret:
    print("Build Image Failed")
    exit(1)
print("Build Image Succeeded")
print("=" * 20)

# Check Build Scripts
print("=" * 20)
print("== Check Build Scripts")
if os.path.exists("./build.py"):
    print("Build Scripts Exists")
else:
    print("Build Scripts Not Exists")
    exit(1)

print("=" * 20)
print("== Change Gradle to Official")
gradle_change_to_official()
print("=" * 20)

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

print("=" * 20)
print("== Restore Gradle to Original")
restore_to_original()
print("=" * 20)

# Remove Image
print("=" * 20)
print("== Remove Image")
run_command(f"docker rmi {image_name}")

# Done
print("=" * 20)
print("== Done")
print("=" * 20)
