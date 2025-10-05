## Features
1. RSTP ip camera
2. Ultrasonic sensor for food level
3. Playable audio
4. Dispense food

## Installation Guide For Raspberry Pi
1. Clone the repo "https://github.com/Hedgehogcnm/SD_G02_43"
2. Run the bash script in '/bin' folder
   - 2.1 If the bash script isn't an executable yet, run "chmod +x download_mediamtx.sh" in that directory.

## Live Cam
1. Live Cam requires either port forward your router or VPN
2. VPN
   - Install tailscale on both Raspberry Pi and devices
   - Login the same tailscale account for both raspberry pi and devices
   - Enter the ip assigned by tailscale at the device you wish to view live cam
   - Using port 8889 and cam1
   - Example: http://'raspberry-pi ip':8889/cam1