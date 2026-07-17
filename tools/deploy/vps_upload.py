import hashlib
import io
import os
import sys
import time

from vps_common import connect

CHUNK = 4 * 1024 * 1024
MAX_RETRIES = 30


def open_sftp(client):
    sftp = client.open_sftp()
    sftp.get_channel().settimeout(120)
    return sftp


def main():
    local = sys.argv[1]
    remote = sys.argv[2]
    remote_dir = f"{os.path.dirname(remote)}/.deploy_chunks"

    size = os.path.getsize(local)
    n_chunks = (size + CHUNK - 1) // CHUNK

    md5 = hashlib.md5()
    with open(local, "rb") as f:
        for block in iter(lambda: f.read(1024 * 1024), b""):
            md5.update(block)
    local_md5 = md5.hexdigest()
    print(f"Local: {size / 1024 / 1024:.1f} MB, {n_chunks} chunks, md5={local_md5}", flush=True)

    client = connect()
    sftp = open_sftp(client)
    client.exec_command(f"mkdir -p {remote_dir}")
    time.sleep(1)

    existing = {}
    try:
        for attr in sftp.listdir_attr(remote_dir):
            existing[attr.filename] = attr.st_size
    except IOError:
        pass
    if existing:
        print(f"Resuming: {len(existing)} chunks already on VPS", flush=True)

    start = time.time()
    uploaded = 0
    with open(local, "rb") as f:
        for i in range(n_chunks):
            f.seek(i * CHUNK)
            data = f.read(CHUNK)
            name = f"chunk_{i:04d}"
            if existing.get(name) == len(data):
                continue
            for attempt in range(MAX_RETRIES):
                try:
                    sftp.putfo(io.BytesIO(data), f"{remote_dir}/{name}", confirm=True)
                    uploaded += len(data)
                    elapsed = max(time.time() - start, 0.001)
                    print(f"  chunk {i + 1}/{n_chunks} ok "
                          f"({uploaded // 1024 // 1024} MB, {uploaded / elapsed / 1024:.0f} KB/s)",
                          flush=True)
                    break
                except Exception as e:
                    print(f"  chunk {i} attempt {attempt + 1} failed: "
                          f"{type(e).__name__} — reconnecting", flush=True)
                    try:
                        client.close()
                    except Exception:
                        pass
                    time.sleep(min(5 * (attempt + 1), 30))
                    client = connect()
                    sftp = open_sftp(client)
            else:
                print("FATAL: chunk failed after all retries", flush=True)
                sys.exit(1)

    print("Assembling on VPS...", flush=True)
    cmd = (f"cat {remote_dir}/chunk_* > {remote} && rm -rf {remote_dir} && "
           f"stat -c %s {remote} && md5sum {remote}")
    stdin, stdout, stderr = client.exec_command(cmd, timeout=300)
    out = stdout.read().decode().strip()
    client.close()

    lines = out.splitlines()
    remote_size = int(lines[0]) if lines else -1
    remote_md5 = lines[1].split()[0] if len(lines) > 1 else ""
    ok = remote_size == size and remote_md5 == local_md5
    print("VERIFIED" if ok else f"MISMATCH (size {remote_size}/{size}, md5 {remote_md5})", flush=True)
    sys.exit(0 if ok else 1)


if __name__ == "__main__":
    main()
