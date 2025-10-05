import socket
import pyaudio

HOST = "0.0.0.0"
PORT = 5000

# Initialize PyAudio
p = pyaudio.PyAudio()
stream = p.open(format=pyaudio.paInt16,
                channels=1,
                rate=16000,
                output=True)

print(f"Server listening on {HOST}:{PORT}...")

# Create socket
server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.bind((HOST, PORT))
server.listen(1)

while True:
    print("Waiting for a client...")
    conn, addr = server.accept()
    print(f"Connected from: {addr}")

    try:
        while True:
            data = conn.recv(1024)
            if not data:
                print("Client disconnected.")
                break
            stream.write(data)
    except Exception as e:
        print("Error:", e)
    finally:
        conn.close()
        print("Ready for next connection.")