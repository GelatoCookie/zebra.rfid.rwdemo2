# DataWedge RFID/Barcode Demo App (ECRT)

## Version 0.0.1

This application demonstrates the integration of Zebra DataWedge API for managing RFID and Barcode scanning operations on Zebra Android devices.

### Key Features
- **Real-time Hardware Status**: Uses DataWedge Notification API to monitor and display the state of the Barcode Scanner and RFID Sled.
- **Modern UI Indicators**: Color-coded status bar for immediate hardware feedback.
    - **Green**: Ready (Waiting/Connected/Scanning).
    - **Blue**: Activated (RWDemo Profile is active and configured).
    - **Red**: Error/Disconnected (Disconnected/No Plug-in/Unknown).
- **Consolidated Configuration**: Automatically creates and configures the "RWDemo" DataWedge profile.
- **Robust Hardware Detection**: Validates RFID plug-in availability via `SET_CONFIG` results.

### Integration Details
- **Notification API**: Registered for `SCANNER_STATUS` and `RFID_STATUS`.
- **Intent API**: Uses `com.symbol.datawedge.api.ACTION` for all hardware triggers and configuration.
- **Package**: `com.zebra.rfid.rwdemo2`

---
© 2026 Zebra Technologies Corporation and/or its affiliates.
