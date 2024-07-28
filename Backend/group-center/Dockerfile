FROM alibabadragonwell/dragonwell:21-ubuntu
MAINTAINER Haomin Kong

RUN cp -a /etc/apt/sources.list /etc/apt/sources.list.bak \
    && sed -i "s@http://.*archive.ubuntu.com@http://mirrors.huaweicloud.com@g" /etc/apt/sources.list \
    && sed -i "s@http://.*security.ubuntu.com@http://mirrors.huaweicloud.com@g" /etc/apt/sources.list \
    && apt update \
    && apt upgrade -y \
    && apt install -y net-tools \
    && apt clean

LABEL AUTHOR="Haomin Kong" VERSION=1.0.0

ENV BASE_PATH="/usr/local/group-center"
ENV RUN_IN_DOCKER=True

WORKDIR "$BASE_PATH"

ENV LOGS_PATH="$BASE_PATH/logs"

ENV FILE_ENV_PATH="$BASE_PATH/Config/Program/FileEnv.toml"

# Copy Directory
COPY Scripts/ $BASE_PATH/Scripts/

# Copy Files
COPY group-center-docker.jar $BASE_PATH/group-center-docker.jar
COPY docker.start.sh $BASE_PATH/docker.start.sh

RUN    sed -i 's/\r$//' $BASE_PATH/*.sh \
    && sed -i 's/\r$//' $BASE_PATH/Scripts/*.sh \
    && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo Haomin Kong >> /docker_author \
    && echo "Asia/Shanghai" > /etc/timezone \
    && chmod +x "$BASE_PATH/docker.start.sh"

ENTRYPOINT ["/usr/local/group-center/docker.start.sh"]

# Only for debug use!
# ENTRYPOINT ["top"]
