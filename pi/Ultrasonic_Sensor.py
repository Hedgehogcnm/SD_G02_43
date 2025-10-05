#!/home/kfc/venv/bin/python

import lgpio as GPIO
import time
from datetime import datetime
import os
import statistics

# Import firebase
import firebase_admin
from firebase_admin import firestore, credentials

# Initialize Firebase
cred = credentials.Certificate("service_account.json")
firebase_admin.initialize_app(cred)

# Initialize pi ip
ip_file_path = os.path.expanduser("~/ip_address.txt")
with open(ip_file_path, "r") as file:
    ip_address = file.read().strip()

# Initialize Firestore
db = firestore.client()

# Set pins
TRIG = 23  # TRIG pin
ECHO = 24  # ECHO pin

# Open GPIO chip
h = GPIO.gpiochip_open(0)
GPIO.gpio_claim_output(h, TRIG)
GPIO.gpio_claim_input(h, ECHO)

def updateFoodLevel(foodLevel):
    docs = db.collection("Feeder").where("ip_address", "==", ip_address).stream()
    for doc in docs:
        db.collection("Feeder").document(doc.id).update({
            "food_level": foodLevel,
            "food_level_timestamp": datetime.utcnow()
        })
        print(f"Updated food level {foodLevel} for cam_ip: {ip_address}")
        print("Timestamp:", datetime.utcnow())

def get_distance():
    # Ensure TRIG LOW
    GPIO.gpio_write(h, TRIG, 0)
    time.sleep(0.05)

    # Send 10 µs pulse
    GPIO.gpio_write(h, TRIG, 1)
    time.sleep(0.00001)
    GPIO.gpio_write(h, TRIG, 0)

    # Wait for ECHO HIGH
    while GPIO.gpio_read(h, ECHO) == 0:
        pulse_start = time.time()

    # Wait for ECHO LOW
    while GPIO.gpio_read(h, ECHO) == 1:
        pulse_end = time.time()

    pulse_duration = pulse_end - pulse_start
    distance = pulse_duration * 17150  # cm
    return round(distance, 2)

if __name__ == '__main__':
    readings = []
    try:
        for _ in range(5):  # take 5 readings
            dist = get_distance()
            readings.append(dist)
            print(f"Measured Distance = {dist:.2f} cm")
            if dist < 12:
                print("Sufficient Food")
            else:
                print("Insufficient Food")
            time.sleep(1)

        # Find median to smooth out noise
        median_dist = statistics.median(readings)
        updateFoodLevel(round(median_dist, 2))

    except KeyboardInterrupt:
        print("Measurement stopped by User")
    finally:
        GPIO.gpiochip_close(h)
