#!/usr/bin/env python3
import sys
import io
import os
import paramiko

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8", errors="replace")
sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding="utf-8", errors="replace")

HOST = os.environ["ASK_VPS_HOST"]
USER = os.environ["ASK_VPS_USER"]
PASSWORD = os.environ["ASK_VPS_PASSWORD"]


def run(client, cmd, timeout=120):
    print(f"$ {cmd}")
    stdin, stdout, stderr = client.exec_command(cmd, timeout=timeout, get_pty=True)
    out = stdout.read().decode(errors="replace")
    err = stderr.read().decode(errors="replace")
    code = stdout.channel.recv_exit_status()
    if out:
        print(out)
    if err:
        print(err, file=sys.stderr)
    print(f"[exit {code}]")
    return code, out, err


def main():
    script = sys.argv[1] if len(sys.argv) > 1 else None
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(HOST, username=USER, password=PASSWORD, timeout=30)

    if script:
        with open(script, "r", encoding="utf-8") as f:
            cmd = f.read()
        run(client, cmd, timeout=600)
    else:
        for line in sys.stdin:
            line = line.rstrip("\n")
            if not line.strip():
                continue
            run(client, line)

    client.close()


if __name__ == "__main__":
    main()
