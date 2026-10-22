#!/data/data/com.termux/files/usr/bin/bash
# FSG God-Mode · Restart APEX
termux-toast "Restarting APEX…"
pkill -f "apex" 2>/dev/null || true
sleep 1
if [ -f "$HOME/start-apex-final.sh" ]; then
  bash "$HOME/start-apex-final.sh"
  termux-toast "APEX restarted"
else
  termux-toast "start-apex-final.sh not found"
fi
