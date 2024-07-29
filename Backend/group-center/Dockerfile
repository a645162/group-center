# https://hub.docker.com/r/alibabadragonwell/dragonwell
#FROM alibabadragonwell/dragonwell:21-ubuntu
#FROM alibabadragonwell/dragonwell:21-anolis

FROM dragonwell-registry.cn-hangzhou.cr.aliyuncs.com/dragonwell/dragonwell:21-anolis

# https://github-wiki-see.page/m/dragonwell-project/dragonwell21/wiki/Use-Dragonwell-21-docker-images
# dragonwell-registry.cn-hangzhou.cr.aliyuncs.com/dragonwell/dragonwell:21
# dragonwell-registry.cn-hangzhou.cr.aliyuncs.com/dragonwell/dragonwell:21-anolis
# dragonwell-registry.cn-hangzhou.cr.aliyuncs.com/dragonwell/dragonwell:21-standard-ga-anolis
# dragonwell-registry.cn-hangzhou.cr.aliyuncs.com/dragonwell/dragonwell:21.0.1.0.1.12-standard-ga-anolis

MAINTAINER Haomin Kong

# Install Software
RUN    yum update -y \
    && yum install -y tzdata net-tools \
    && yum clean all

# Set the timezone
ENV TZ=Asia/Shanghai
RUN    ln -snf /usr/share/zoneinfo/${TZ} /etc/localtime \
    && echo ${TZ} > /etc/timezone

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
COPY src/main/resources/application-docker.yml $BASE_PATH/application.yml
COPY docker.start.sh $BASE_PATH/docker.start.sh

RUN    sed -i 's/\r$//' $BASE_PATH/*.sh \
    && sed -i 's/\r$//' $BASE_PATH/Scripts/*.sh \
    && chmod +x "$BASE_PATH/docker.start.sh"

HEALTHCHECK \
    --interval=10s \
    --timeout=3s \
    --start-period=30s \
    --retries=1 \
    CMD /bin/netstat -anpt|grep 15090

ENTRYPOINT ["/usr/local/group-center/docker.start.sh"]

# Only for debug use!
# ENTRYPOINT ["top"]
