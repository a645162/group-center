import os

image_name = "group-center-builder"
print("=" * 20)
print(f"Build Jar({image_name}) using Docker")
print("=" * 20)

# Build Image
print("=" * 20)
print("== Build Image")
os.system(f"docker build -t {image_name} -f Docker/Dockerfile-Build .")

# Run Containers
print("=" * 20)
print("== Run Containers")
os.system(f"docker run --rm  -v .:/usr/local/group-center {image_name}")

# Remove Image
print("=" * 20)
print("== Remove Image")
os.system(f"docker rmi {image_name}")

# Done
print("=" * 20)
print("== Done")
print("=" * 20)
