<<<<<<< HEAD
# Smart Attend

A teacher-focused classroom attendance prototype built with **Python + Flask**. Attendance records are stored in the browser with `localStorage`, so no login, database, or backend setup is required.

## Run locally

1. Ensure Python 3.10+ is installed.
2. In this project folder, create and activate a virtual environment (optional but recommended):

   ```powershell
   py -m venv .venv
   .\.venv\Scripts\Activate.ps1# SmartRoll — Bluetooth Attendance (MVP scaffold)

## Structure
```
smartroll/
├── app.py            # Streamlit dashboard (Start Class, live attendance, My Records/export)
├── database.py       # SQLite layer — schema already supports branch/section/subject (college-level)
├── ble_beacon.html    # Standalone Web Bluetooth proof-of-concept — TEST THIS FIRST
├── requirements.txt
└── README.md
```

## How to run
```bash
pip install -r requirements.txt
streamlit run app.py
```
Opens at `http://localhost:8501`.

## ⚠️ Do this first: test ble_beacon.html
Open `ble_beacon.html` directly in Chrome on **two devices** (or two Chrome
profiles) before wiring BLE into the Streamlit app. This checks whether
device-to-device discovery actually works reliably on your hardware.

**Known limitation to expect:** a plain webpage generally can't act as a
BLE *advertiser/peripheral* in most browsers — Web Bluetooth is built for
scanning/connecting to BLE peripherals (headphones, sensors, etc.), not
phone-to-phone discovery. If the POC doesn't reliably detect a second
phone, don't lose the 10 days debugging it — switch to one of:
1. A small native Android app (Kotlin, BLE advertising APIs) for the
   student side — more reliable, more setup time.
2. A cheap dedicated beacon (ESP32, ~₹300) as the "teacher" broadcaster,
   with phones only doing the scanning side (which Web Bluetooth handles
   well).

## Why the database is already "college-level"
`database.py`'s tables (`students`, `sessions`, `attendance`) all store
`branch` and `section` on every row, even though today's UI only shows
one teacher's own classes. This means:
- An Admin/HOD dashboard = a new page that calls the existing query
  functions without a `teacher_name` filter. No schema change.
- At-risk-student alerts = a new function that groups
  `get_attendance_history()` results by student and flags low attendance.
  No schema change.
- WhatsApp auto-notify = hook the at-risk function's output into an n8n
  workflow. No schema change.

Extension points are marked `# FUTURE:` in `database.py` and `app.py`.

## Current MVP scope
- One teacher, multiple branch/section/subject combinations
- Start Class → live attendance list
- Manual "Mark Present" fallback (BLE auto-detect wires into the same
  `db.mark_attendance(..., mode="Auto")` call)
- My Records page with branch/section filter + Excel export for the
  college ERP
   ```

3. Install the dependency and launch the app:

   ```powershell
   pip install -r requirements.txt
   py app.py
   ```

4. Open `http://127.0.0.1:5000` in your browser.

## Prototype notes

- **BLE Detection Demo Mode** simulates nearby student detection one student at a time. Browsers cannot automatically advertise and scan student phones through Bluetooth in the way a production Android app can.
- A teacher can always mark students present manually.
- Completed sessions persist in browser `localStorage` and can be exported as ERP-ready CSV files.
- Production Android app will add real Bluetooth Low Energy detection.
=======
# Blink-ERP
Blutooth based attendence system
>>>>>>> 6c1999bef04cc0652d782de20abbfbcb474e4c8c
