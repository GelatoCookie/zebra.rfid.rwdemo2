## DataWedge Profile Requirements

This app requires a DataWedge profile named `RWDemo` with the following configuration:

- **RFID Input Plugin**: Enabled, with `rfid_input_enabled=true`.
- **Barcode Input Plugin**: Enabled, with `scanner_input_enabled=true`.
- **Intent Output Plugin**: Enabled, with output directed to this app's package and action `com.zebra.rfid.rwdemo.RWDEMO2`.
- **Keystroke Output Plugin**: Optional, disabled by default.
- **Associated Activities**: All activities in this app should be associated with the profile.
- The app will auto-create and configure this profile if it does not exist.

If you encounter issues with profile creation or hardware status, ensure DataWedge is installed and the device supports the required plugins.
# DataWedge RFID/Barcode Demo App (ECRT)

## Directory Structure

### Local Project Structure

```
DataWedgeApp/RfidECRT_RWDemo2/
├── app/
│   ├── src/main/java/com/zebra/rfid/rwdemo2/  # Main source code (RWDemoActivity, RWDemoIntentParams, etc.)
│   ├── res/                                   # Resources (layouts, strings, etc.)
│   └── ...
├── build_deploy_launch.sh                     # Build & deploy script
├── README.md
├── DESIGN.md                                  # Design and flow documentation
└── ...
```

### Remote Repository

GitHub: https://github.com/GelatoCookie/zebra.rfid.rwdemo2

---
See [DESIGN.md](DESIGN.md) for detailed RFID and Barcode operation flows and architecture.

## Version 0.0.2

This release includes improvements for modularity, user feedback, and scanning experience:

### What's New
- **Modularized DataWedge Handling**: Introduced `DataWedgeHelper` class for cleaner, testable intent handling and configuration.
- **Enhanced User Feedback**: Added Toasts and error dialogs for DataWedge errors and hardware status changes.
- **Progress Dialog for Scanning**: Progress dialog now appears during barcode or RFID scanning and dismisses automatically when scanning completes or becomes idle.
- **Documentation Updates**: Expanded README and DESIGN.md for onboarding and troubleshooting.

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
## Changelog

### 0.0.2 (March 29, 2026)
- Modularized DataWedge intent handling (DataWedgeHelper)
- Improved user feedback (Toasts, dialogs)
- Progress dialog for scanning operations
- Documentation and onboarding improvements

### 0.0.1 (April 2, 2026)
- Initial public release
- Real-time hardware status, modern UI, robust configuration

---
© 2026 Zebra Technologies Corporation and/or its affiliates.
