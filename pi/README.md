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

## Firebase Connection
1. Requires to setup service_account.json
   - Navigate to project setting in firebase
   - Go to Service accounts
   - Generate a private key
   - Change the private key json file name to service_account.json
   - Put the file in home directory
2. Connecting firebase
   - *import firebase_admin*
   - *from firebase_admin import credentials*
   - *cred = credentials.Certificate("service_account.json")*
   - *firebase_admin.initialize_app(cred)*
3. Firestore
   - Same procedure as connecting firebase
   - *from firebase_admin import **firestore**, credentials*
   - ***db = firestore.client()***

## Live Audio
1. - sudo apt update
   - sudo apt install portaudio19-dev python3-pyaudio
   - sudo apt install python3-flask ffmpeg alsa-utils
   - sudo apt install python3-flask
   - sudo apt install python3-pyaudio
   - pip install pyaudio
