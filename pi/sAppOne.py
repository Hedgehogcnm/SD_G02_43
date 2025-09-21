#!/usr/bin/env python3
import RPi.GPIO as GPIO
import time
import socket
import sys

# -----------------------------
# GPIO + Servo setup
# -----------------------------
SERVO_PIN = 13
GPIO.setmode(GPIO.BCM)
GPIO.setup(SERVO_PIN, GPIO.OUT)

LEVEL_DURATIONS = {
    1: 1.0,
    2: 2.0,
    3: 3.0,
    4: 4.0
}

OPEN_POS = 10.0     # duty cycle for "open"
CLOSED_POS = 7.5    # duty cycle for "closed"


# -----------------------------
# Feed Action
# -----------------------------
def feed_action(level):
    print(f"Starting feed sequence - Level {level}")
    pwm = GPIO.PWM(SERVO_PIN, 50)   # 50 Hz servo signal
    pwm.start(CLOSED_POS)

    try:
        duration = LEVEL_DURATIONS.get(level, 1.5)

        # Open
        pwm.ChangeDutyCycle(OPEN_POS)
        
        # Open
        pwm.ChangeDutyCycle(OPEN_POS)
        print(f"Latch OPEN for {duration} seconds")
        time.sleep(duration)

        # Close
        pwm.ChangeDutyCycle(CLOSED_POS)
        print("Closing latch...")
        time.sleep(0.5)

    except Exception as e:
        print(f"Error during feed action: {e}")

    finally:
        pwm.stop()  # release PWM completely -> no jitter
        print("Latch CLOSED - feeding complete")


# -----------------------------
# Socket Server
# -----------------------------
def run_server(port=12345):
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            s.bind(("0.0.0.0", port))
            s.listen()
            print("Pet Feeder Server READY")
            print(f"Listening on port {port}...")
            print("Waiting for Android App...")
            print("Expected commands: 'Feed:1', 'Feed:2', 'Feed:3', 'Feed:4'")
            print("Press Ctrl+C to stop server")
            print("-" * 50)

            while True:
                               conn, addr = s.accept()
                with conn:
                    print(f"Connected by: {addr}")
                    data = conn.recv(1024).decode().strip()
                    print(f"Received command: '{data}'")

                    if data.startswith("Feed:"):
                        try:
                            level = int(data.split(":")[1])
                            if 1 <= level <= 4:
                                print(f"Valid command - Level {level}")
                                feed_action(level)
                                conn.sendall(b"ACK")
                                print("Sent response: ACK")
                            else:
                                print(f"Invalid level {level} (must be 1-4)")
                                conn.sendall(b"INVALID_LEVEL")
                                print("Sent response: INVALID_LEVEL")
                        except (IndexError, ValueError):
                            print(f"Invalid format: {data}")
                            conn.sendall(b"INVALID_FORMAT")
                            print("Sent response: INVALID_FORMAT")
                    else:
                        print(f"Unknown command: {data}")
                        conn.sendall(b"UNKNOWN")
                        print("Sent response: UNKNOWN")

                    print("-" * 50)

    except KeyboardInterrupt:
        print("\nServer shutting down...")
    except Exception as e:
        print(f"Server error: {e}")
    finally:
        GPIO.cleanup()
        print("GPIO cleanup complete")
        

# -----------------------------
# Main
# -----------------------------
if __name__ == "__main__":
    print("PET FEEDER SCRIPT STARTING")
    print("=" * 40)
    run_server()
