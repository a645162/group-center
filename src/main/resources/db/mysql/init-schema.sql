CREATE TABLE group_center.gpu_task_info
(
    id                           INT AUTO_INCREMENT PRIMARY KEY,
    server_name                  VARCHAR(255) NOT NULL,
    server_name_eng              VARCHAR(255) NOT NULL,
    task_id                      VARCHAR(255) NOT NULL,
    message_type                 VARCHAR(255) NOT NULL,
    task_type                    VARCHAR(255) NOT NULL,
    task_status                  VARCHAR(255) NOT NULL,
    task_user                    VARCHAR(255) NOT NULL,
    task_pid                     INT          NOT NULL,
    task_main_memory             INT          NOT NULL,
    gpu_usage_percent            FLOAT        NOT NULL,
    gpu_memory_usage_string      VARCHAR(255) NOT NULL,
    gpu_memory_free_string       VARCHAR(255) NOT NULL,
    gpu_memory_total_string      VARCHAR(255) NOT NULL,
    gpu_memory_percent           FLOAT        NOT NULL,
    task_gpu_id                  INT          NOT NULL,
    task_gpu_name                VARCHAR(255) NOT NULL,
    task_gpu_memory_gb           FLOAT        NOT NULL,
    task_gpu_memory_human        VARCHAR(255) NOT NULL,
    task_gpu_memory_max_gb       FLOAT        NOT NULL,
    is_multi_gpu                 BOOLEAN      NOT NULL,
    multi_device_local_rank      INT          NOT NULL,
    multi_device_world_size      INT          NOT NULL,
    top_python_pid               INT          NOT NULL,
    cuda_root                    VARCHAR(255) NOT NULL,
    cuda_version                 VARCHAR(255) NOT NULL,
    is_debug_mode                BOOLEAN      NOT NULL,
    task_start_time              BIGINT       NOT NULL,
    task_finish_time             BIGINT       NOT NULL,
    task_start_time_obj          TIMESTAMP    NOT NULL,
    task_finish_time_obj         TIMESTAMP    NOT NULL,
    task_running_time_string     VARCHAR(255) NOT NULL,
    task_running_time_in_seconds INT          NOT NULL,
    project_directory            VARCHAR(255) NOT NULL,
    project_name                 VARCHAR(255) NOT NULL,
    screen_session_name          VARCHAR(255) NOT NULL,
    py_file_name                 VARCHAR(255) NOT NULL,
    python_version               VARCHAR(255) NOT NULL,
    command_line                 TEXT         NOT NULL,
    conda_env_name               VARCHAR(255) NOT NULL
);
