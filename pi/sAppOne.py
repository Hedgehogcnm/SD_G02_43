#!/usr/bin/env python3
import RPi.GPIO as GPIO
import time
import socket
import sys

def setup_gpio():
    try:
        GPIO.setmode(GPIO.BCM)
        GPIO.setup(13, GPIO.OUT)
        p = GPIO.PWM(13, 50)
        p.start(7.5)
        return p
    except Exception as e:
        print(f"GPIO setup failed: {e}")
        return None

def feed_action(pwm, level):
    print(f"Starting feed sequence - Level {level}")
    try:
        open_position = 10.0
        closed_position = 7.5
        
        level_durations = {
            1: 3.0,  # Shortest diet
            2: 5.0,  # Medium
            3: 7.5,  # Idk why we even got 4 level tbh fuck me
            4: 10.0   # Longest fattie
        }
        
        duration = level_durations.get(level, 1.5)
        
        print(f"Latch CLOSED")
        pwm.ChangeDutyCycle(open_position)
        print(f"Latch OPEN for {duration} seconds (Level {level})")
        time.sleep(duration)
        
        print(f"Closing latch...")
        pwm.ChangeDutyCycle(closed_position)
        time.sleep(0.5)
        print("Latch CLOSED - feeding complete")
        
    except Exception as e:
        print(f"Error during feed action: {e}")

def run_server(port=12345):
    print("Setting up GPIO...")
    pwm = setup_gpio()
    if not pwm:
        return
    
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            s.bind(("0.0.0.0", port))
            s.listen()
            print(f"Pet Feeder Server READY")
            print(f"Listening on port {port}...")
            print("Waiting for Our App...")
            print("Expected commands: 'Feed:1', 'Feed:2', 'Feed:3', 'Feed:4'")
            print("Press Ctrl+C to stop server")
            print("-" * 50)
            
            while True:
                conn, addr = s.accept()
                with conn:
                    print(f"Connected by Android app: {addr}")
                    data = conn.recv(1024).decode().strip()
                    print(f"Received command: '{data}'")
                    
                    if data.startswith("Feed:"):
                        try:
                            level = int(data.split(":")[1])
                            if 1 <= level <= 4:
                                print(f"Valid command - Level {level}")
                                feed_action(pwm, level)
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
        pwm.stop()
        GPIO.cleanup()
        print("GPIO cleanup complete")

if __name__ == "__main__":
    print("PET FEEDER SCRIPT I HATE THIS AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
    print("=" * 40)
    run_server()
