#!/usr/bin/env python3
import sys
import os
import paramiko

HOST = os.environ["ASK_VPS_HOST"]
USER = os.environ["ASK_VPS_USER"]
PASSWORD = os.environ["ASK_VPS_PASSWORD"]

local_path = sys.argv[1]
remote_path = sys.argv[2]

transport = paramiko.Transport((HOST, 22))
transport.connect(username=USER, password=PASSWORD)
sftp = paramiko.SFTPClient.from_transport(transport)
sftp.put(local_path, remote_path)
sftp.close()
transport.close()
print(f"uploaded {local_path} -> {remote_path}")
