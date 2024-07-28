FROM alibabadragonwell/dragonwell:21-ubuntu

MAINTAINER Haomin Kong

LABEL AUTHOR="Haomin Kong" VERSION=1.0.0
ENV DOCKER_MODE="Haomin Kong"

ENV BASE_PATH="/usr/local/group-center"

WORKDIR "$BASE_PATH"

ENV LOGS_PATH="$BASE_PATH/logs"

ENV FILE_ENV_PATH="$BASE_PATH/Config/Program/FileEnv.toml"

# Copy Files
COPY group-center-docker.jar $BASE_PATH/group-center-docker.jar
COPY docker.start.sh $BASE_PATH/docker.start.sh

RUN    sed -i 's/\r$//' $BASE_PATH/*.sh \
    && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo Haomin Kong >> /docker_author \
    && echo "Asia/Shanghai" > /etc/timezone \
    && chmod +x "$BASE_PATH/docker.start.sh"

ENTRYPOINT ["/usr/local/group-center/docker.start.sh"]

# Only for debug use!
# ENTRYPOINT ["top"]
