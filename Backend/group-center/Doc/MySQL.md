# MySQL

## Backup

```bash
sudo docker exec -it GroupCenterMySQL /bin/bash
mysqldump -uroot -p123456 --all-databases >  /usr/local/backup/emp_`date +%F`.sql
exit
```

## Copy From Container to Host

```bash
sudo docker cp GroupCenterMySQL:/usr/local/backup/emp_`date +%F`.sql .
```

## Restore

```bash
sudo docker exec -it GroupCenterMySQL /bin/bash
mysql -uroot -p123456 < /usr/local/backup/emp_`date +%F`.sql
exit
```

## Ref

https://blog.csdn.net/weixin_52270081/article/details/123552094
