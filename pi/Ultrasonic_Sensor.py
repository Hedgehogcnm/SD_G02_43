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
file = open(ip_file_path, "r")
ip_address = file.read()

# Initialize Firestore
db = firestore.client()

# Set pins
TRIG = 23  # Associate pin 23 to TRIG
ECHO = 24  # Associate pin 24 to ECHO

# Open the GPIO chip and set the GPIO direction
h = GPIO.gpiochip_open(0)
GPIO.gpio_claim_output(h, TRIG)
GPIO.gpio_claim_input(h, ECHO)

def updateFoodLevel(foodLevel):
    doc_ref = db.collection("Feeder").where("ip_address", "==", ip_address).stream()
    for doc in doc_ref:
        db.collection("Feeder").document(doc.id).update({
            "food_level": foodLevel,
            "food_level_timestamp": datetime.utcnow()
        })
        print("Update food level ", foodLevel ,"\nTo cam_ip: ", ip_address)
        print("Timestamp: ", datetime.utcnow())
        
def get_distance():
    # Set TRIG LOW
    GPIO.gpio_write(h, TRIG, 0)
    time.sleep(2)

    # Send 10us pulse to TRIG
    GPIO.gpio_write(h, TRIG, 1)
    time.sleep(0.00001)
    GPIO.gpio_write(h, TRIG, 0)

    # Start recording the time when the wave is sent
    while GPIO.gpio_read(h, ECHO) == 0:
        pulse_start = time.time()

    # Record time of arrival
    while GPIO.gpio_read(h, ECHO) == 1:
        pulse_end = time.time()

    # Calculate the difference in times
    pulse_duration = pulse_end - pulse_start

    # Multiply with the sonic speed (34300 cm/s)
    # and divide by 2, because there and back
    distance = pulse_duration * 17150
    distance = round(distance, 2)

    return distance

# Main program
if __name__ == '__main__':
    counter = 5
    readings = []
    try:
        for i in range(counter):
            dist = get_distance()
            readings.append(dist)
            print("Measured Distance = {:.2f} cm".format(dist))
            time.sleep(1)
            if(dist < 12):
                print("Sufficient Food")
            else:
                print("Insufficient Food")
            time.sleep(1)
        
        # Find median in case of outliers
        median_dist = statistics.median(readings)
        updateFoodLevel(round(median_dist,2))
    # Reset by pressing CTRL + C
    except KeyboardInterrupt:
        print("Measurement stopped by User")
        GPIO.gpiochip_close(h)