# AudioStream-Android

Consent-based live audio streaming between two Android devices.

## Architecture

Mobile 1 (Sender)
        |
        | WebRTC audio
        v
Signaling Server
        |
        v
Mobile 2 (Receiver)

## Features

### Mobile 1
- Microphone permission
- Android Foreground Service
- Visible microphone notification
- Start / Stop microphone session
- WebRTC audio sender

### Mobile 2
- Receive live WebRTC audio
- Listen ON / OFF
- Record ON / OFF
- Save recording locally

### Signaling server
- WebSocket signaling
- Room/session support
- WebRTC offer/answer exchange
- ICE candidate exchange

## Requirements

- Android Studio
- JDK 17
- Android SDK 35
- Node.js 18+
- Two Android devices
- Internet connection

## Important

The microphone session is explicitly started by the user and remains
visible through Android's foreground-service notification.

Recording on Mobile 2 is also explicitly controlled by the user.
