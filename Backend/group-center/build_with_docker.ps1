# Check if the image exists
if (-not (docker images -q group-center-build)) {
    # If it doesn't exist, build the image
    Write-Host "Building image group-center-build"
    docker build `
        -t group-center-build `
        -f Docker/Dockerfile-Build `
        .
} else {
    Write-Host "Image group-center-build already exists"
}

docker run `
    --rm `
    -v .:/usr/local/group-center `
    group-center-build

# Delete the container and image
Write-Host "Deleting image"
docker rmi group-center-build

Write-Host "Script execution completed"
