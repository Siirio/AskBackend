import sys

from vps_common import connect


def main():
    cmd = sys.argv[1]
    timeout = int(sys.argv[2]) if len(sys.argv) > 2 else 120
    client = connect()
    stdin, stdout, stderr = client.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode(errors="replace")
    err = stderr.read().decode(errors="replace")
    code = stdout.channel.recv_exit_status()
    if out:
        print(out)
    if err:
        print("STDERR:", err, file=sys.stderr)
    client.close()
    sys.exit(code)


if __name__ == "__main__":
    main()
