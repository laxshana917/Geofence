# Geofence IoT Anti-Theft System

## 📖 Description
This project implements a **Geofence-based IoT Anti-Theft System** using Android and Raspberry Pi.  
The system monitors a defined geographical area and detects unauthorized movement or breaches using geofencing and motion sensors.

When a device or object exits the defined geofence, the system triggers a real-time alert via Firebase Realtime Database. The Raspberry Pi, connected to motion sensors and a camera, captures any suspicious activity and uploads images to Firebase Storage. Notifications are sent to the user via Firebase Cloud Messaging (FCM), enabling instant alerts and remote monitoring.

This project demonstrates integration of **mobile apps, IoT devices, cloud databases, and real-time notification systems**, providing hands-on experience with security, automation, and smart monitoring solutions.

## 🚀 Features
- Geofence monitoring to track authorized boundaries
- Motion detection using PIR sensors
- Real-time alerts via Firebase Realtime Database
- Image capture and storage on Firebase Storage
- Notifications sent through Firebase Cloud Messaging (FCM)
- Full integration of Android app with IoT devices

## 🛠️ Technologies Used
- Android (Java)  for the mobile app
- Raspberry Pi with PIR motion sensor
- Firebase Realtime Database & Firebase Storage
- Firebase Cloud Messaging (FCM) for notifications
- Python scripts for Raspberry Pi automation
