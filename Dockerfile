# https://hub.docker.com/_/openjdk/tags
FROM openjdk:26-oraclelinux9

# MAINTAINER Haomin Kong
LABEL maintainer="Haomin Kong"

ENV BASE_PATH="/usr/local/group-center"
ENV RUN_IN_DOCKER=True

ENV PACKAGE_MANAGER="microdnf"

WORKDIR "$BASE_PATH"

# Upgrade all packages to the latest version
RUN    $PACKAGE_MANAGER update -y \
    && $PACKAGE_MANAGER clean all

# Install Software
RUN    $PACKAGE_MANAGER install -y git python3 python3-pip bash tzdata net-tools findutils \
    && $PACKAGE_MANAGER clean all

# Set the timezone
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/${TZ} /etc/localtime \
    && echo ${TZ} > /etc/timezone

ENV LOGS_PATH="$BASE_PATH/logs"

ENV FILE_ENV_PATH="$BASE_PATH/Config/Program/FileEnv.toml"

# Copy Directory
COPY Scripts/ $BASE_PATH/Scripts/

# Copy Files
COPY group-center-docker.jar $BASE_PATH/group-center-docker.jar
COPY src/main/resources/application-docker.yml $BASE_PATH/application.yml
COPY docker.start.sh $BASE_PATH/docker.start.sh

RUN sed -i 's/\r$//' $BASE_PATH/*.sh \
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
