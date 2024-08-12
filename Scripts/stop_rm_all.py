import os
import shutil

# Stop Containers
print("== Killed Containers")
os.system("docker-compose kill")
print()

print("== Remove Containers")
os.system("docker-compose rm -f")
print()

print("== Down Containers")
os.system("docker-compose down")
print()

# Remove Images
print("== Remove Images")
os.system("docker rmi group_center:latest")
print()

# Remove Directory ./Data
# dir_path = "./Data"
# if os.path.exists(dir_path):
#     print("== Remove Directory ./Data")
#     shutil.rmtree(dir_path)
#     print()
