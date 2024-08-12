import os

image_name = "group-center-builder"

# Build Image
print("== Build Image")
os.system(f"docker build -t {image_name} -f Docker/Dockerfile-Build .")

# Run Containers
print("== Run Containers")
os.system(f"docker run --rm  -v .:/usr/local/group-center {image_name}")

# Remove Image
print("== Remove Image")
os.system(f"docker rmi {image_name}")

# Done
print("== Done")
