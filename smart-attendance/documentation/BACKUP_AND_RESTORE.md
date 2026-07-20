# Backup and Restore Protocols

This document details policies and operations for backing up and restoring attendance databases.

## Backup Procedure
To execute a database snapshot backup, run `mysqldump` from your server console:
```bash
mysqldump -h <host> -u <username> -p<password> smart_attendance > backup_$(date +%F).sql
```
For Docker deployments:
```bash
docker exec -t smartattend-db mysqldump -u root -proot smart_attendance > backup.sql
```

## Restore Procedure
To restore attendance snapshots:
1. Re-initialize default databases.
2. Direct raw scripts to database endpoints:
   ```bash
   mysql -h <host> -u <username> -p<password> smart_attendance < backup.sql
   ```
3. Restart server components to verify baseline data accuracy.
