# Design: RFID & Barcode Operations

## Overview
This document details the architecture and flow of RFID and Barcode operations in the DataWedgeApp RfidECRT_RWDemo2 project.

## Key Components
- **RWDemoActivity**: Main UI and logic controller for RFID/Barcode operations.
- **RWDemoIntentParams**: Centralized intent and parameter definitions for DataWedge API.

## Operation Flows

### 1. App Startup & Profile Configuration
- On launch, the app checks for the DataWedge profile "RWDemo".
- If not present, it creates and configures the profile with required plugins (RFID, Barcode, Intent, Keystroke).
- Registers for DataWedge notifications (RFID/Scanner status).

### 2. RFID Operation Flow
- User taps the RFID trigger button.
- App sends SOFT_RFID_TRIGGER intent to DataWedge.
- DataWedge activates RFID hardware and returns status via notification.
- App updates UI status bar (color and text) based on notification.
- Tag data is received via intent and displayed in the output view.

### 3. Barcode Operation Flow
- User taps the Barcode trigger button.
- App sends SOFT_SCAN_TRIGGER intent to DataWedge.
- DataWedge activates Barcode hardware and returns status via notification.
- App updates UI status bar (color and text) based on notification.
- Barcode data is received via intent and displayed in the output view.

## Flowchart

```mermaid
flowchart TD
    Start([App Start])
    CheckProfile{Profile Exists?}
    CreateProfile[Create & Configure Profile]
    RegisterNotif[Register for Notifications]
    Ready[Ready for User Input]
    RFIDBtn[User taps RFID Button]
    BarcodeBtn[User taps Barcode Button]
    SendRFID[Send SOFT_RFID_TRIGGER Intent]
    SendBarcode[Send SOFT_SCAN_TRIGGER Intent]
    DW_RFID[DataWedge: Activate RFID]
    DW_Barcode[DataWedge: Activate Barcode]
    NotifRFID[Receive RFID Status Notification]
    NotifBarcode[Receive Barcode Status Notification]
    UpdateUI[Update UI Status]
    DataRFID[Receive RFID Data Intent]
    DataBarcode[Receive Barcode Data Intent]
    ShowRFID[Display Tag Data]
    ShowBarcode[Display Barcode Data]

    Start --> CheckProfile
    CheckProfile -- No --> CreateProfile --> RegisterNotif --> Ready
    CheckProfile -- Yes --> RegisterNotif --> Ready
    Ready --> RFIDBtn --> SendRFID --> DW_RFID --> NotifRFID --> UpdateUI --> DataRFID --> ShowRFID
    Ready --> BarcodeBtn --> SendBarcode --> DW_Barcode --> NotifBarcode --> UpdateUI --> DataBarcode --> ShowBarcode
```

## Suggestions
- Consider modularizing DataWedge intent handling for easier testing and maintenance.
- Add more user feedback (toasts, dialogs) for error states.
- Document DataWedge profile requirements in README for easier onboarding.
- Add unit tests for intent construction and parsing logic.
