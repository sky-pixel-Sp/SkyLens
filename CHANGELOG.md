# Changelog

Notable changes to the SkyLens Android project are documented here.

## 0.1.2 — 2026-11-19

### Added
- In-app language switch for English and Bahasa Indonesia.
- Persistent language preference stored locally on the device.
- First-launch language selection follows the Android device language for Indonesian devices; otherwise English is used.
- Language selector is available on both the camera screen and permission screen.
- Indonesian Android launcher/app label resources.
- Complete bilingual README with feature boundaries, build instructions, testing checklist, and privacy notes.

### Changed
- Bumped Android version code to 3 and version name to 0.1.2.
- Localized camera controls, permission messaging, gallery, capability information, and runtime status messages.

## 0.1.1

### Fixed
- Rear-camera capability scan now selects the rear camera with the largest reported JPEG output instead of the first rear-facing camera.
- 120 fps support is reported only when a constrained high-speed FPS range actually includes 120 fps.

### Added
- Regression tests for 120 fps range detection.

## 0.1.0

- Initial native Android camera MVP with CameraX preview, photo/video capture, paired-file Motion Capture, PRO exposure control, Camera2 capability inspection, and an in-app gallery.
