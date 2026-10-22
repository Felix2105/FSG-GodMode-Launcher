#!/data/data/com.termux/files/usr/bin/bash
# FSG God-Mode · Start APEX
termux-toast "Starting APEX stack…"
if [ -f "$HOME/start-apex-final.sh" ]; then
  bash "$HOME/start-apex-final.sh"
else
  termux-toast "start-apex-final.sh not found – edit this script"
fi
