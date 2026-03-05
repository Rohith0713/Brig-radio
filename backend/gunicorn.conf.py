"""
Gunicorn configuration for production deployment on AWS EC2.
Usage: gunicorn -c gunicorn.conf.py wsgi:app
"""
import multiprocessing
import os

# ── Server Socket ────────────────────────────────────────
bind = os.getenv("GUNICORN_BIND", "0.0.0.0:8000")

# ── Worker Processes ─────────────────────────────────────
# Recommended: (2 × CPU cores) + 1
workers = int(os.getenv("GUNICORN_WORKERS", multiprocessing.cpu_count() * 2 + 1))
worker_class = "sync"
worker_connections = 1000

# ── Timeouts ─────────────────────────────────────────────
timeout = 120          # Kill worker after 120s of silence
graceful_timeout = 30  # Wait 30s for worker to finish requests
keepalive = 5          # Keep connections alive for 5s

# ── Limits ───────────────────────────────────────────────
max_requests = 1000          # Restart worker after 1000 requests (prevents memory leaks)
max_requests_jitter = 50     # Randomize restart to avoid all workers restarting at once

# ── Logging ──────────────────────────────────────────────
accesslog = "-"              # Log to stdout (captured by systemd/journald)
errorlog = "-"               # Log to stderr
loglevel = "info"

# ── Process Naming ───────────────────────────────────────
proc_name = "brigradio"

# ── Security ─────────────────────────────────────────────
# Preload app so code is loaded before forking workers
# This saves memory but requires restart on code changes
preload_app = True

# ── Temporary Directory ──────────────────────────────────
# Use /dev/shm for worker heartbeat temp files (faster on EC2)
tmp_dir = "/dev/shm"
