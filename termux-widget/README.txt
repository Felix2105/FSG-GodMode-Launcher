FSG GOD-MODE  ·  TERMUX:WIDGET SHORTCUTS
========================================

These scripts are ready for Termux:Widget.

INSTALL
-------
1. Install Termux and Termux:Widget (same signature family).
2. Copy the .sh files into ~/.shortcuts/ :

   mkdir -p ~/.shortcuts
   cp /path/to/termux-widget/*.sh ~/.shortcuts/
   chmod +x ~/.shortcuts/*.sh

3. Add the Termux:Widget to your home screen (or keep using the God-Mode launcher tiles).

SCRIPTS
-------
01-START-APEX.sh          – start your existing APEX stack
02-APEX-STATUS.sh         – quick health / process check
03-OPEN-DASHBOARD.sh      – open local dashboard in browser / WebView
04-OPEN-BUSINESS-HQ.sh    – open Business HQ page
05-RESTART-APEX.sh        – stop + start APEX cleanly

They reuse your existing ~/start-apex-final.sh (or adjust the path inside the scripts).
They never delete or overwrite your original APEX installation.
