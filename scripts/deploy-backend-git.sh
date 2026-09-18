#!/usr/bin/env bash
set -euo pipefail

REPO_URL="${REPO_URL:-https://github.com/Durdenxito/ChangeScout.git}"
BRANCH="${BRANCH:-main}"
REPO_DIR="${REPO_DIR:-/opt/changescout/source}"
APP_DIR="${APP_DIR:-/opt/changescout/backend}"
SERVICE_NAME="${SERVICE_NAME:-changescout-backend}"

if [ "$REPO_DIR" = "$APP_DIR" ]; then
  echo "REPO_DIR y APP_DIR no pueden ser iguales"
  exit 1
fi

case "$APP_DIR" in
  /opt/changescout/*) ;;
  *) echo "APP_DIR debe estar dentro de /opt/changescout"; exit 1 ;;
esac

if [ ! -d "$REPO_DIR/.git" ]; then
  sudo mkdir -p "$(dirname "$REPO_DIR")"
  sudo git clone --branch "$BRANCH" "$REPO_URL" "$REPO_DIR"
fi

cd "$REPO_DIR"
sudo git fetch origin "$BRANCH"
sudo git checkout "$BRANCH"
sudo git pull --ff-only origin "$BRANCH"

sudo ./gradlew --no-daemon :backend:test :backend:installDist

sudo systemctl stop "$SERVICE_NAME"
sudo rm -rf "$APP_DIR"/*
sudo mkdir -p "$APP_DIR"
sudo cp -a "$REPO_DIR/backend/build/install/backend/." "$APP_DIR/"
sudo systemctl start "$SERVICE_NAME"

for attempt in 1 2 3 4 5; do
  if curl -fsS http://127.0.0.1:8080/health; then
    exit 0
  fi
  sleep 2
done

sudo systemctl --no-pager status "$SERVICE_NAME"
sudo journalctl -u "$SERVICE_NAME" -n 80 --no-pager
exit 1
