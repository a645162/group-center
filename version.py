def get_version():
    relative_path = "./src/main/resources/settings/version.properties"
    version = "0.0.0"

    with open(relative_path, "r") as f:
        lines = f.readlines()
        for this_line in lines:
            line = this_line
            line = line.strip()
            if line.startswith("version="):
                version = line.replace("version=", "").strip()

    version = version.replace("-SNAPSHOT", "")

    return version.strip()


if __name__ == "__main__":
    print(get_version(), end="")
