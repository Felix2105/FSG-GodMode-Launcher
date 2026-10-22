#!/data/data/com.termux/files/usr/bin/bash
# FSG God-Mode · APEX Status
echo "=== APEX / FSG STATUS ==="
date
echo
echo "--- Listening ports ---"
ss -tlnp 2>/dev/null | grep -E '8080|8765' || netstat -tlnp 2>/dev/null | grep -E '8080|8765'
echo
echo "--- Health ---"
curl -s --max-time 3 http://127.0.0.1:8765/api/health || echo "Backend not responding"
echo
termux-toast "Status printed – check Termux"
