import subprocess
import datetime
import os

docker_name = "GroupCenterMySQL"
mysql_user = "root"  # 修改为你的MySQL用户名
mysql_password = "your_password"  # 修改为你的MySQL密码
mysql_db = "your_database"  # 修改为你的数据库名
backup_save_path = r"./backup"  # 修改为你的备份保存路径

os.makedirs(backup_save_path, exist_ok=True)

def backup_mysql():
    if not os.path.exists(backup_save_path):
        os.makedirs(backup_save_path)
    backup_file = os.path.join(
        backup_save_path,
        f"{mysql_db}_backup_{datetime.datetime.now().strftime('%Y%m%d_%H%M%S')}.sql"
    )
    dump_cmd = [
        "docker", "exec", docker_name,
        "mysqldump",
        f"-u{mysql_user}",
        f"-p{mysql_password}",
        mysql_db
    ]
    try:
        with open(backup_file, "w", encoding="utf-8") as f:
            print(f"正在备份数据库到: {backup_file}")
            subprocess.run(dump_cmd, stdout=f, check=True)
        print("备份完成。")
    except Exception as e:
        print(f"备份失败: {e}")

if __name__ == "__main__":
    backup_mysql()
