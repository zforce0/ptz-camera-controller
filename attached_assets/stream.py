import cv2

# Update the RTSP URL based on your camera's settings:
rtsp_url = "rtsp://admin:abcd1234@192.168.1.108:554/Streaming/Channels/101"  # Common format
# Alternative URLs to try:
# rtsp_url = "rtsp://admin:abcd1234@192.168.1.108:554/cam/realmonitor?channel=1&subtype=0"
# rtsp_url = "rtsp://admin:abcd1234@192.168.1.108:554/h264"

cap = cv2.VideoCapture(rtsp_url)

while cap.isOpened():
    ret, frame = cap.read()
    if not ret:
        print("Failed to receive frame. Check URL/credentials/network.")
        break
    cv2.imshow('PTZ Camera Stream', frame)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()
