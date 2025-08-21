import RPi.GPIO as GPIO
import time
import socket

def feed_action():
    # GPIO Setup (same as your working test)
    GPIO.setmode(GPIO.BCM)
    GPIO.setup(13, GPIO.OUT)

    # Create PWM instance (same as your working test)
    p = GPIO.PWM(13, 50)  # 50Hz frequency
    p.start(0)  # Start with 0% duty cycle
    
    print("Starting feed sequence")
    try:
        # Use the same duty cycles that worked in your test
        duty_cycles = [2.5, 5.0, 7.5, 10.0, 12.5]
        
        # Move through the positions (similar to your test)
        for duty in duty_cycles:
            p.ChangeDutyCycle(duty)
            print(f"Duty cycle: {duty}")
            time.sleep(1)  # Reduced sleep time for faster feeding
        
        # Return to neutral position
        p.ChangeDutyCycle(7.5)
        time.sleep(1)
        print("Feed sequence completed")
        
        
    except Exception as e:
        print(f"Error during feed action: {e}")
        p.stop()
        GPIO.cleanup()
    except KeyboardInterrupt:
        p.stop()
        GPIO.cleanup()
        
# Network setup
HOST = "0.0.0.0"  # Listen on all interfaces
PORT = 12345       # Use the same port as in your Android app

try:
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s.bind((HOST, PORT))
        s.listen()
        print(f"Server listening on port {PORT}...")
        
        while True:
            conn, addr = s.accept()
            with conn:
                print(f"Connected by {addr}")
                data = conn.recv(1024).decode().strip()
                if data == "Feed":
                    print("Received feed command")
                    feed_action()
                    conn.sendall(b"ACK")  # Send acknowledgment
                else:
                    print(f"Unknown command: {data}")
                    conn.sendall(b"UNKNOWN")
                    
except KeyboardInterrupt:
    print("Server shutting down")
except Exception as e:
    print(f"An error occurred: {e}")


    
