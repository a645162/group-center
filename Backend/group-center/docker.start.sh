#!/usr/bin/env bash

# Function to check if an environment variable is declared
is_env_declared() {
    local var_name=$1

    # 使用 declare -p 命令来检查环境变量是否已声明
    if declare -p "$var_name" &> /dev/null; then
        return 0  # 如果变量已声明，返回 true
    else
        return 1  # 如果变量未声明，返回 false
    fi
}

check_env(){
  if is_env_declared "SPRING_DATASOURCE_URL"; then
      echo "Environment Variable SPRING_DATASOURCE_URL is declared."
  else
      if is_env_declared "SPRING_DATASOURCE_HOST"; then
          echo "Environment Variable SPRING_DATASOURCE_HOST is declared."
          export SPRING_DATASOURCE_URL="jdbc:mysql://${SPRING_DATASOURCE_HOST}/group_center?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"
      else
          exit 1
      fi
  fi

  if is_env_declared "SPRING_DATASOURCE_USERNAME"; then
      echo "Environment Variable SPRING_DATASOURCE_USERNAME is declared."
  else
      export SPRING_DATASOURCE_USERNAME="root"
  fi

  if is_env_declared "SPRING_DATASOURCE_PASSWORD"; then
      echo "Environment Variable SPRING_DATASOURCE_PASSWORD is declared."
  else
      export SPRING_DATASOURCE_PASSWORD="123456"
  fi
}

check_env

cd "${BASE_PATH}" || exit
java -jar "${BASE_PATH}/group-center-docker.jar" --spring.config.location=file:application.yml
