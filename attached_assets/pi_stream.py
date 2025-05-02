#!/usr/bin/env python3
import cv2
import time
import argparse

def stream_camera(stream_type="main"):
    """
    Stream video from IP camera using the working RTSP URLs
    
    Args:
        stream_type: "main" for high quality or "sub" for lower quality stream
    """
    # Define the working RTSP URLs
    rtsp_urls = {
        "main": "rtsp://admin:abcd1234@192.168.1.108:554/h264/ch1/main/av_stream",
        "sub": "rtsp://admin:abcd1234@192.168.1.108:554/h264/ch1/sub/av_stream"
    }
    
    # Select URL based on stream type
    if stream_type not in rtsp_urls:
        print(f"Invalid stream type: {stream_type}. Using 'main' stream.")
        stream_type = "main"
    
    rtsp_url = rtsp_urls[stream_type]
    print(f"Connecting to camera using {stream_type} stream: {rtsp_url}")
    
    # Open the RTSP stream
    cap = cv2.VideoCapture(rtsp_url)
    
    # Check if connection was successful
    if not cap.isOpened():
        print("Error: Could not connect to camera stream.")
        return
    
    # Get video properties
    frame_width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    frame_height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
    fps = cap.get(cv2.CAP_PROP_FPS)
    
    print(f"Stream properties: {frame_width}x{frame_height} at {fps} FPS")
    
    # Start streaming
    try:
        start_time = time.time()
        frames_count = 0
        
        while True:
            ret, frame = cap.read()
            
            if not ret:
                print("Failed to receive frame. Reconnecting...")
                cap.release()
                time.sleep(1)
                cap = cv2.VideoCapture(rtsp_url)
                continue
            
            # Calculate and display FPS
            frames_count += 1
            elapsed_time = time.time() - start_time
            if elapsed_time >= 5:  # Update FPS every 5 seconds
                fps_actual = frames_count / elapsed_time
                print(f"Current FPS: {fps_actual:.2f}")
                frames_count = 0
                start_time = time.time()
            
            # Display the frame
            cv2.imshow(f'Camera Stream ({stream_type})', frame)
            
            # Break loop if 'q' is pressed
            if cv2.waitKey(1) & 0xFF == ord('q'):
                break
                
    except KeyboardInterrupt:
        print("Stream interrupted by user")
    finally:
        # Clean up
        cap.release()
        cv2.destroyAllWindows()
        print("Stream closed")

if __name__ == "__main__":
    # Set up command line arguments
    parser = argparse.ArgumentParser(description='Stream video from IP camera')
    parser.add_argument('--stream', type=str, default='main', 
                        choices=['main', 'sub'],
                        help='Stream type: "main" (high quality) or "sub" (low quality)')
    
    args = parser.parse_args()
    
    # Start streaming
    stream_camera(args.stream)