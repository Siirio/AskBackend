import os
import sys
from pathlib import Path

import paramiko


def load_credentials():
    host = os.environ.get("ASK_VPS_STAGE_HOST")
    user = os.environ.get("ASK_VPS_STAGE_USER")
    password = os.environ.get("ASK_VPS_STAGE_PASSWORD")
    if host and user and password:
        return host, user, password
    env_path = Path(__file__).resolve().parents[2] / ".env"
    values = {}
    for line in env_path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            key, _, value = line.partition("=")
            values[key.strip()] = value.strip()
    return (values["ASK_VPS_STAGE_HOST"],
            values["ASK_VPS_STAGE_USER"],
            values["ASK_VPS_STAGE_PASSWORD"])


def connect():
    host, user, password = load_credentials()
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(host, username=user, password=password, timeout=60,
                   banner_timeout=60, auth_timeout=60)
    return client
