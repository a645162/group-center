import os
import shutil

# Stop Containers
os.system("docker-compose kill")
os.system("docker-compose rm -f")
os.system("docker-compose down")

# Remove Images
os.system("docker rmi group_center:latest")

# Remove Directory ./Data
dir_path = "./Data"
if os.path.exists(dir_path):
    shutil.rmtree(dir_path)
