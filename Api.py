"""
BlinkERP — API Server (Flask)
==========================================================
Bridges Android app + Web frontend <-> shared SQLite database.

Endpoints:
  POST /api/register          — teacher/student registration
  POST /api/login             — teacher/student login
  POST /api/start_session     — teacher starts a class
  POST /api/end_session       — teacher ends a class
  POST /api/mark              — student BLE detected -> mark attendance
  GET  /api/health            — connectivity check from phone
  GET  /api/students          — list students (with filters)
  GET  /api/sessions          — list sessions
  POST /api/sessions          — create session (web frontend)
  GET  /api/sessions/:id      — get session details
  POST /api/sessions/:id/mark — manual mark student
  POST /api/sessions/:id/end  — end session by id
  GET  /api/stats             — quick dashboard stats

  Admin endpoints:
  GET    /api/admin/departments        — list departments
  POST   /api/admin/departments        — add department
  DELETE /api/admin/departments/:id    — delete department
  GET    /api/admin/sections           — list sections
  POST   /api/admin/sections           — add section
  DELETE /api/admin/sections/:id       — delete section
  GET    /api/admin/students           — list students (filtered)
  POST   /api/admin/students           — add student
  DELETE /api/admin/students/:id       — delete student

  BLE endpoints:
  POST /api/ble/start          — start BLE scan (stub)
  POST /api/ble/stop           — stop BLE scan (stub)
  GET  /api/ble/devices        — get detected BLE devices

Run:  python Api.py
Phone + laptop must be on same WiFi/hotspot.
"""

from flask import Flask, request, jsonify, send_file, render_template
from flask_cors import CORS
import database as db
import os

app = Flask(__name__, static_folder='static', template_folder='templates')
CORS(app)
db.init_db()


# ================================================================
# AUTH — Register & Login
# ================================================================

@app.route("/api/register", methods=["POST"])
def register():
    data = request.get_json(force=True)
    name = data.get("name", "").strip()
    password = data.get("password", "").strip()
    role = data.get("role", "").strip()

    if not name or not password or not role:
        return jsonify({"error": "Name, password, and role are required"}), 400
    if role not in ("teacher", "student"):
        return jsonify({"error": "Role must be 'teacher' or 'student'"}), 400

    branch = data.get("branch", "").strip() or None
    course = data.get("course", "").strip() or None
    year = data.get("year", "").strip() or None
    section = data.get("section", "").strip() or None
    subject = data.get("subject", "").strip() or None

    user = db.register_user(name, password, role, course, year, branch, section, subject)
    if user is None:
        return jsonify({"error": "Already registered with same name/course/year/branch/section"}), 409

    return jsonify({"status": "registered", "user": user})


@app.route("/api/login", methods=["POST"])
def login():
    data = request.get_json(force=True)
    name = data.get("name", "").strip()
    password = data.get("password", "").strip()
    role = data.get("role", "").strip()

    if not name or not password or not role:
        return jsonify({"error": "Name, password, and role are required"}), 400

    user = db.login_user(name, password, role)
    if user is None:
        return jsonify({"error": "Invalid credentials"}), 401

    return jsonify({"status": "logged_in", "user": user})


# ================================================================
# SESSIONS
# ================================================================

@app.route("/api/start_session", methods=["POST"])
def start_session():
    data = request.get_json(force=True)
    required = ["teacher_name", "branch", "section", "subject"]
    if not all(k in data and data[k] for k in required):
        return jsonify({"error": f"Missing fields, need: {required}"}), 400

    session_id = db.start_session(data["teacher_name"], data["branch"], data["section"], data["subject"])
    return jsonify({"session_id": session_id, "status": "started"})


@app.route("/api/end_session", methods=["POST"])
def end_session():
    data = request.get_json(force=True)
    session_id = data.get("session_id")
    if not session_id:
        return jsonify({"error": "Missing session_id"}), 400
    db.end_session(session_id)
    return jsonify({"status": "ended"})


@app.route("/api/sessions", methods=["GET"])
def list_sessions():
    teacher = request.args.get("teacher")
    if teacher:
        sessions = db.get_sessions_for_teacher(teacher)
    else:
        sessions = db.get_all_sessions()
    return jsonify(sessions)


@app.route("/api/sessions", methods=["POST"])
def create_session():
    """Create a new session — used by web frontend."""
    data = request.get_json(force=True)
    teacher_name = data.get("teacher_name", "admin")
    department_id = data.get("department_id")
    section_id = data.get("section_id")
    subject = data.get("subject", "")

    # Resolve department_id/section_id to branch/section names
    if department_id and section_id:
        with db.get_conn() as conn:
            dept = conn.execute("SELECT name FROM departments WHERE id=?", (department_id,)).fetchone()
            sec = conn.execute("SELECT name FROM sections WHERE id=?", (section_id,)).fetchone()
        if not dept or not sec:
            return jsonify({"error": "Invalid department or section"}), 400
        branch = dept["name"]
        section = sec["name"]
    else:
        branch = data.get("branch", "")
        section = data.get("section", "")

    if not branch or not section or not subject:
        return jsonify({"error": "Missing branch/section/subject"}), 400

    session_id = db.start_session(teacher_name, branch, section, subject)
    session = db.get_session_by_id(session_id)
    if session:
        session["active"] = True
        session["department"] = session.get("branch", "")
        session["section_name"] = session.get("section", "")
        session["started_at"] = f"{session.get('date', '')}T{session.get('start_time', '')}"
    return jsonify(session or {"session_id": session_id, "status": "started"})


@app.route("/api/sessions/<int:session_id>", methods=["GET"])
def get_session(session_id):
    session = db.get_session_by_id(session_id)
    if not session:
        return jsonify({"error": "Session not found"}), 404
    # Add fields expected by web frontend
    session["active"] = session.get("status") == "active"
    session["department"] = session.get("branch", "")
    session["section_name"] = session.get("section", "")
    session["started_at"] = f"{session.get('date', '')}T{session.get('start_time', '')}"
    return jsonify(session)


@app.route("/api/sessions/<int:session_id>/end", methods=["POST"])
def end_session_by_id(session_id):
    db.end_session(session_id)
    return jsonify({"status": "ended"})


@app.route("/api/sessions/<int:session_id>", methods=["DELETE"])
def delete_session_by_id(session_id):
    """Delete a session and its attendance records."""
    ok = db.delete_session(session_id)
    if not ok:
        return jsonify({"error": "Session not found"}), 404
    return jsonify({"status": "deleted"})


@app.route("/api/sessions/<int:session_id>/delete", methods=["POST"])
def delete_session_post(session_id):
    """Delete a session and its attendance records (POST to avoid CORS issues)."""
    ok = db.delete_session(session_id)
    if not ok:
        return jsonify({"error": "Session not found"}), 404
    return jsonify({"status": "deleted"})


@app.route("/api/sessions/<int:session_id>/mark", methods=["POST"])
def mark_by_id(session_id):
    data = request.get_json(force=True)
    student_id = data.get("student_id")
    if not student_id:
        return jsonify({"error": "Missing student_id"}), 400

    students = db.get_students()
    student = next((s for s in students if s["id"] == student_id), None)
    if not student:
        return jsonify({"error": "Student not found"}), 404

    marked = db.mark_attendance(session_id, student["name"], "Present", "Manual")
    return jsonify({"status": "marked" if marked else "already_marked"})


# ================================================================
# ATTENDANCE — Mark (from Android student app)
# ================================================================

@app.route("/api/mark", methods=["POST"])
def mark():
    """Called by the STUDENT app when BLE detects teacher's beacon."""
    data = request.get_json(force=True)
    required = ["student_name", "branch", "section"]
    if not all(k in data and data[k] for k in required):
        return jsonify({"error": f"Missing fields, need: {required}"}), 400

    session = db.get_active_session_for_section(data["branch"], data["section"])
    if not session:
        return jsonify({"error": "No active class session for this branch/section right now"}), 404

    # Ensure the student exists in the master roster (INSERT OR IGNORE), so
    # they appear on the laptop's live dashboard roster even if they only
    # registered via the app and were never added through the admin panel.
    db.add_student(data["student_name"], data["branch"], data["section"])

    mode = data.get("mode", "Auto")
    marked = db.mark_attendance(session["id"], data["student_name"], "Present", mode)
    if marked:
        return jsonify({"status": "marked", "session_id": session["id"]})
    else:
        return jsonify({"status": "already_marked", "session_id": session["id"]})


# ================================================================
# STUDENTS & STATS
# ================================================================

@app.route("/api/students", methods=["GET"])
def list_students():
    branch = request.args.get("branch")
    section = request.args.get("section")
    students = db.get_students(branch, section)
    return jsonify(students)


@app.route("/api/stats", methods=["GET"])
def stats():
    return jsonify(db.get_stats())


@app.route("/api/session/<int:session_id>/live", methods=["GET"])
def session_live(session_id):
    """Live session data — used by dashboard for real-time polling."""
    session = db.get_session_by_id(session_id)
    if not session:
        return jsonify({"error": "Session not found"}), 404
    # Return only what the dashboard needs for live updates
    return jsonify({
        "session_id": session["id"],
        "status": session["status"],
        "subject": session["subject"],
        "branch": session["branch"],
        "section": session["section"],
        "students": session["students"],
        "present_count": sum(1 for s in session["students"] if s["status"] == "Present"),
        "total_count": len(session["students"]),
    })


@app.route("/api/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"})


# ================================================================
# CLASS NOTES
# ================================================================

# ================================================================
# PYQ (Previous Year Questions)
# ================================================================

@app.route("/api/pyqs", methods=["GET"])
def list_pyqs():
    """List PYQs with filters."""
    teacher = request.args.get("teacher")
    branch = request.args.get("branch")
    subject = request.args.get("subject")
    semester = request.args.get("semester")
    year = request.args.get("year")
    pyqs = db.get_pyqs(teacher, branch, subject, semester, year)
    return jsonify(pyqs)


@app.route("/api/pyqs", methods=["POST"])
def add_pyq():
    """Add a PYQ (text or file)."""
    data = request.get_json(force=True)
    teacher_name = data.get("teacher_name", "").strip()
    branch = data.get("branch", "").strip()
    subject = data.get("subject", "").strip()
    title = data.get("title", "").strip()
    semester = data.get("semester", "").strip() or None
    year = data.get("year", "").strip() or None
    exam_type = data.get("exam_type", "").strip() or None
    content = data.get("content", "").strip() or None
    drive_link = data.get("drive_link", "").strip() or None

    if not teacher_name or not branch or not subject or not title:
        return jsonify({"error": "teacher_name, branch, subject, title are required"}), 400

    pyq_id = db.add_pyq(teacher_name, branch, subject, title, semester, year, exam_type, content, drive_link=drive_link)
    return jsonify({"status": "added", "pyq_id": pyq_id})


@app.route("/api/pyqs/upload", methods=["POST"])
def upload_pyq_file():
    """Upload a PYQ file."""
    teacher_name = request.form.get("teacher_name", "")
    branch = request.form.get("branch", "")
    subject = request.form.get("subject", "")
    title = request.form.get("title", "")
    semester = request.form.get("semester", "") or None
    year = request.form.get("year", "") or None
    exam_type = request.form.get("exam_type", "") or None
    drive_link = request.form.get("drive_link", "") or None

    if not teacher_name or not branch or not subject or not title:
        return jsonify({"error": "Missing required fields"}), 400

    file = request.files.get("file")
    if not file:
        return jsonify({"error": "No file uploaded"}), 400

    os.makedirs("uploads", exist_ok=True)
    file_path = os.path.join("uploads", file.filename)
    file.save(file_path)

    pyq_id = db.add_pyq(
        teacher_name, branch, subject, title, semester, year, exam_type,
        content=None, file_path=file_path, file_name=file.filename, drive_link=drive_link
    )
    return jsonify({"status": "uploaded", "pyq_id": pyq_id, "file_name": file.filename})


@app.route("/api/pyqs/<int:pyq_id>", methods=["DELETE"])
def delete_pyq(pyq_id):
    """Delete a PYQ."""
    db.delete_pyq(pyq_id)
    return jsonify({"status": "deleted"})


@app.route("/api/pyqs/download/<int:pyq_id>", methods=["GET"])
def download_pyq(pyq_id):
    """Download PYQ file."""
    with db.get_conn() as conn:
        pyq = conn.execute("SELECT file_path, file_name FROM pyqs WHERE id=?", (pyq_id,)).fetchone()
    if not pyq or not pyq["file_path"]:
        return jsonify({"error": "File not found"}), 404
    if not os.path.exists(pyq["file_path"]):
        return jsonify({"error": "File missing from disk"}), 404
    return send_file(pyq["file_path"], as_attachment=True, download_name=pyq["file_name"])


@app.route("/api/notes", methods=["GET"])
def list_notes():
    """List class notes with filters."""
    teacher = request.args.get("teacher")
    branch = request.args.get("branch")
    section = request.args.get("section")
    subject = request.args.get("subject")
    notes = db.get_class_notes(teacher, branch, section, subject)
    return jsonify(notes)


@app.route("/api/notes", methods=["POST"])
def add_note():
    """Add a class note (text or file)."""
    data = request.get_json(force=True)
    teacher_name = data.get("teacher_name", "").strip()
    branch = data.get("branch", "").strip()
    section = data.get("section", "").strip()
    subject = data.get("subject", "").strip()
    title = data.get("title", "").strip()
    content = data.get("content", "").strip()

    if not teacher_name or not branch or not section or not subject or not title:
        return jsonify({"error": "teacher_name, branch, section, subject, title are required"}), 400

    note_id = db.add_class_note(teacher_name, branch, section, subject, title, content)
    return jsonify({"status": "added", "note_id": note_id})


@app.route("/api/notes/<int:note_id>", methods=["DELETE"])
def delete_note(note_id):
    """Delete a class note."""
    db.delete_class_note(note_id)
    return jsonify({"status": "deleted"})


@app.route("/api/notes/upload", methods=["POST"])
def upload_note_file():
    """Upload a file as class note."""
    teacher_name = request.form.get("teacher_name", "")
    branch = request.form.get("branch", "")
    section = request.form.get("section", "")
    subject = request.form.get("subject", "")
    title = request.form.get("title", "")

    if not teacher_name or not branch or not section or not subject or not title:
        return jsonify({"error": "Missing required fields"}), 400

    file = request.files.get("file")
    if not file:
        return jsonify({"error": "No file uploaded"}), 400

    os.makedirs("uploads", exist_ok=True)
    file_path = os.path.join("uploads", file.filename)
    file.save(file_path)

    note_id = db.add_class_note(
        teacher_name, branch, section, subject, title,
        content=None, file_path=file_path, file_name=file.filename
    )
    return jsonify({"status": "uploaded", "note_id": note_id, "file_name": file.filename})


@app.route("/api/notes/download/<int:note_id>", methods=["GET"])
def download_note(note_id):
    """Download the file attached to a note."""
    with db.get_conn() as conn:
        note = conn.execute("SELECT file_path, file_name FROM class_notes WHERE id=?", (note_id,)).fetchone()
    if not note or not note["file_path"]:
        return jsonify({"error": "File not found"}), 404
    if not os.path.exists(note["file_path"]):
        return jsonify({"error": "File missing from disk"}), 404
    return send_file(note["file_path"], as_attachment=True, download_name=note["file_name"])


# ================================================================
# ADMIN — Departments
# ================================================================

@app.route("/api/admin/departments", methods=["GET"])
def admin_list_departments():
    return jsonify(db.get_departments())


@app.route("/api/admin/departments", methods=["POST"])
def admin_add_department():
    data = request.get_json(force=True)
    name = data.get("name", "").strip()
    if not name:
        return jsonify({"error": "Department name required"}), 400
    dept = db.add_department(name)
    if dept is None:
        return jsonify({"error": "Department already exists"}), 409
    return jsonify(dept)


@app.route("/api/admin/departments/<int:dept_id>", methods=["DELETE"])
def admin_delete_department(dept_id):
    db.delete_department(dept_id)
    return jsonify({"status": "deleted"})


# ================================================================
# ADMIN — Sections
# ================================================================

@app.route("/api/admin/sections", methods=["GET"])
def admin_list_sections():
    dept_id = request.args.get("department_id")
    return jsonify(db.get_sections_by_dept(int(dept_id) if dept_id else None))


@app.route("/api/admin/sections", methods=["POST"])
def admin_add_section():
    data = request.get_json(force=True)
    department_id = data.get("department_id")
    name = data.get("name", "").strip()
    if not department_id or not name:
        return jsonify({"error": "department_id and name required"}), 400
    sec = db.add_section(department_id, name)
    if sec is None:
        return jsonify({"error": "Section already exists in this department"}), 409
    return jsonify(sec)


@app.route("/api/admin/sections/<int:sec_id>", methods=["DELETE"])
def admin_delete_section(sec_id):
    db.delete_section(sec_id)
    return jsonify({"status": "deleted"})


# ================================================================
# ADMIN — Students
# ================================================================

@app.route("/api/admin/students", methods=["GET"])
def admin_list_students():
    dept_id = request.args.get("department_id")
    sec_id = request.args.get("section_id")
    return jsonify(db.get_students_admin(
        int(dept_id) if dept_id else None,
        int(sec_id) if sec_id else None,
    ))


@app.route("/api/admin/students", methods=["POST"])
def admin_add_student():
    data = request.get_json(force=True)
    name = data.get("name", "").strip()
    roll = data.get("roll", "").strip()
    department_id = data.get("department_id")
    section_id = data.get("section_id")
    ble_address = data.get("ble_address")

    if not name or not roll or not department_id or not section_id:
        return jsonify({"error": "name, roll, department_id, section_id required"}), 400

    ok = db.add_student_admin(name, roll, department_id, section_id, ble_address)
    if not ok:
        return jsonify({"error": "Invalid department or section, or student already exists"}), 400
    return jsonify({"status": "added"})


@app.route("/api/admin/students/<int:stu_id>", methods=["DELETE"])
def admin_delete_student(stu_id):
    db.delete_student(stu_id)
    return jsonify({"status": "deleted"})


# ================================================================
# ADMIN — Registered Users
# ================================================================

@app.route("/api/admin/users", methods=["GET"])
def admin_list_users():
    role = request.args.get("role")
    course = request.args.get("course")
    year = request.args.get("year")
    branch = request.args.get("branch")
    section = request.args.get("section")

    users = db.get_all_users(role, course, year, branch, section)
    return jsonify(users)

@app.route("/api/admin/users", methods=["POST"])
def admin_create_user():
    data = request.get_json(force=True) if request.is_json else {}
    name = data.get("name", "").strip()
    password = data.get("password", "").strip()
    role = data.get("role", "").strip()
    if not name or not password or not role:
        return jsonify({"error": "Name, password, and role are required"}), 400
    course = data.get("course", "").strip() or None
    year = data.get("year", "").strip() or None
    branch = data.get("branch", "").strip() or None
    section = data.get("section", "").strip() or None
    subject = data.get("subject", "").strip() or None
    user = db.register_user(name, password, role, course, year, branch, section, subject)
    if user is None:
        return jsonify({"error": "User already exists"}), 409
    return jsonify({"status": "created", "user": user})


@app.route("/api/admin/users/<int:user_id>/password", methods=["POST"])
def admin_update_user_password(user_id):
    data = request.get_json(force=True) if request.is_json else {}
    new_password = (data.get("new_password") or data.get("password", "")).strip()
    if not new_password:
        return jsonify({"error": "New password is required"}), 400
    success = db.update_user_password(user_id, new_password)
    if not success:
        return jsonify({"error": "Failed to update password"}), 500
    return jsonify({"status": "updated", "message": "Password updated successfully"})


@app.route("/api/admin/users/<int:user_id>", methods=["DELETE"])
def admin_delete_user(user_id):
    with db.get_conn() as conn:
        conn.execute("DELETE FROM users WHERE id=?", (user_id,))
    return jsonify({"status": "deleted"})



# ================================================================
# BLE — Scan Control (stubs for web frontend)
# ================================================================

# In-memory state for BLE scan (real BLE would use a library like Bleak)
_ble_state = {
    "scanning": False,
    "session_id": None,
    "devices": [],
}


@app.route("/api/ble/start", methods=["POST"])
def ble_start():
    """Start BLE scan. In a real deployment, this would start a background
    BLE scanner (e.g. using Bleak on Linux). For now, it's a stub that
    the frontend polls."""
    data = request.get_json(force=True) if request.is_json else {}
    session_id = data.get("session_id")
    _ble_state["scanning"] = True
    _ble_state["session_id"] = session_id
    _ble_state["devices"] = []
    return jsonify({"status": "scanning", "mode": "real"})


@app.route("/api/ble/stop", methods=["POST"])
def ble_stop():
    _ble_state["scanning"] = False
    _ble_state["devices"] = []
    return jsonify({"status": "stopped"})


@app.route("/api/ble/devices", methods=["GET"])
def ble_devices():
    """Return detected BLE devices. In production, this would return
    real scan results from a background BLE scanner."""
    devices = []
    for d in _ble_state.get("devices", []):
        devices.append({
            "name": d.get("name", "Unknown"),
            "address": d.get("address", ""),
            "rssi": d.get("rssi", 0),
            "isStudent": d.get("is_student", False),
            "studentRoll": d.get("student_roll", ""),
        })
    return jsonify(devices)


# ================================================================
# ANALYTICS
# ================================================================

@app.route("/api/analytics/overview", methods=["GET"])
def analytics_overview():
    return jsonify(db.get_analytics_overview())


@app.route("/api/analytics/daily", methods=["GET"])
def analytics_daily():
    return jsonify(db.get_daily_attendance())


@app.route("/api/analytics/subjects", methods=["GET"])
def analytics_subjects():
    return jsonify(db.get_subject_wise_attendance())


@app.route("/api/analytics/departments", methods=["GET"])
def analytics_departments():
    return jsonify(db.get_department_wise_attendance())


@app.route("/api/analytics/top-performers", methods=["GET"])
def analytics_top_performers():
    limit = request.args.get("limit", 10, type=int)
    return jsonify(db.get_top_performers(limit))


@app.route("/api/analytics/weekly-pattern", methods=["GET"])
def analytics_weekly():
    return jsonify(db.get_weekly_pattern())


@app.route("/api/analytics/ble-vs-manual", methods=["GET"])
def analytics_ble():
    return jsonify(db.get_ble_vs_manual())


@app.route("/api/analytics/monthly", methods=["GET"])
def analytics_monthly():
    return jsonify(db.get_monthly_summary())


# ================================================================
# STUDENT PERSONAL ANALYTICS & BUNK CALCULATOR
# ================================================================

@app.route("/api/student/analytics", methods=["GET", "POST"])
def student_personal_analytics():
    if request.method == "POST":
        data = request.get_json(force=True) if request.is_json else {}
        name = data.get("name") or data.get("student_name", "")
        branch = data.get("branch")
        section = data.get("section")
    else:
        name = request.args.get("name") or request.args.get("student_name", "")
        branch = request.args.get("branch")
        section = request.args.get("section")

    if not name:
        return jsonify({"error": "student name is required"}), 400

    analytics = db.get_student_full_analytics(name, branch, section)
    return jsonify(analytics)


# ================================================================
# Run

# ================================================================

# ══════════════════════════════════════════════════════════════
# DASHBOARD — Serve the web frontend
# ══════════════════════════════════════════════════════════════

@app.route("/", methods=["GET"])
def dashboard():
    """Serve the main dashboard page."""
    return render_template("index.html")


# ══════════════════════════════════════════════════════════════
# Run
# ══════════════════════════════════════════════════════════════

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=False, threaded=True)
