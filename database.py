"""
BlinkERP — Database Layer
----------------------------------------------------------
Schema is designed college-level from day one (branch, section,
subject all stored per record) even though the MVP UI only
exposes a teacher-facing view. This means an Admin/HOD dashboard,
at-risk-student alerts, or WhatsApp auto-notify can all be added
LATER as new query functions below — no schema changes needed.

Tables:
  users       -> teacher/student login (name, password, role)
  departments -> department list (id, name)
  sections    -> sections per department (id, department_id, name)
  students    -> master list of students (name + their department/section)
  sessions    -> one row per "Start Class" click (a class period)
  attendance  -> one row per student per session (Present/Absent, Auto/Manual)
  ble_devices -> BLE devices seen during a session
"""

import sqlite3
import hashlib
from datetime import datetime
from contextlib import contextmanager

DB_PATH = "BlinkERP.db"


def _hash_password(password: str) -> str:
    """Simple SHA-256 hash for passwords. For production, use bcrypt."""
    return hashlib.sha256(password.encode()).hexdigest()


@contextmanager
def get_conn():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA foreign_keys = ON")
    try:
        yield conn
        conn.commit()
    finally:
        conn.close()


def init_db():
    """Create tables if they don't exist yet. Safe to call every app start."""
    with get_conn() as conn:
        # --- Users table (teacher + student login) ---
        conn.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                password TEXT NOT NULL,
                role TEXT NOT NULL,          -- 'teacher' | 'student'
                course TEXT,
                year TEXT,
                branch TEXT,
                section TEXT,
                subject TEXT,
                created_at TEXT NOT NULL,
                UNIQUE(name, role, course, year, branch, section)
            )
        """)
        # Migration: Add columns if missing
        try:
            conn.execute("ALTER TABLE users ADD COLUMN course TEXT")
        except sqlite3.OperationalError: pass
        try:
            conn.execute("ALTER TABLE users ADD COLUMN year TEXT")
        except sqlite3.OperationalError: pass


        # --- Departments ---
        conn.execute("""
            CREATE TABLE IF NOT EXISTS departments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE
            )
        """)
        # --- Sections ---
        conn.execute("""
            CREATE TABLE IF NOT EXISTS sections (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                department_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                UNIQUE(department_id, name),
                FOREIGN KEY(department_id) REFERENCES departments(id)
            )
        """)
        # --- Students ---
        conn.execute("""
            CREATE TABLE IF NOT EXISTS students (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                branch TEXT NOT NULL,
                section TEXT NOT NULL,
                roll_no TEXT,
                ble_address TEXT,
                UNIQUE(name, branch, section)
            )
        """)
        # Migration: Add ble_address column if missing (older databases)
        try:
            conn.execute("ALTER TABLE students ADD COLUMN ble_address TEXT")
        except sqlite3.OperationalError:
            pass
        # --- Sessions ---
        conn.execute("""
            CREATE TABLE IF NOT EXISTS sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                teacher_name TEXT NOT NULL,
                branch TEXT NOT NULL,
                section TEXT NOT NULL,
                subject TEXT NOT NULL,
                date TEXT NOT NULL,
                start_time TEXT NOT NULL,
                end_time TEXT,
                status TEXT DEFAULT 'active'
            )
        """)
        # --- Attendance ---
        conn.execute("""
            CREATE TABLE IF NOT EXISTS attendance (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_id INTEGER NOT NULL,
                student_name TEXT NOT NULL,
                status TEXT NOT NULL,
                mode TEXT NOT NULL,
                marked_at TEXT NOT NULL,
                FOREIGN KEY(session_id) REFERENCES sessions(id)
            )
        """)
        # --- BLE Devices (tracked during session) ---
        conn.execute("""
            CREATE TABLE IF NOT EXISTS ble_devices (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_id INTEGER NOT NULL,
                device_name TEXT,
                device_address TEXT NOT NULL,
                student_name TEXT,
                detected_at TEXT NOT NULL,
                FOREIGN KEY(session_id) REFERENCES sessions(id)
            )
        """)
        # --- Class Notes (uploaded by teachers) ---
        conn.execute("""
            CREATE TABLE IF NOT EXISTS class_notes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                teacher_name TEXT NOT NULL,
                branch TEXT NOT NULL,
                section TEXT NOT NULL,
                subject TEXT NOT NULL,
                title TEXT NOT NULL,
                content TEXT,
                file_path TEXT,
                file_name TEXT,
                created_at TEXT NOT NULL
            )
        """)
        # --- PYQ (Previous Year Questions) ---
        conn.execute("""
            CREATE TABLE IF NOT EXISTS pyqs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                teacher_name TEXT NOT NULL,
                branch TEXT NOT NULL,
                subject TEXT NOT NULL,
                title TEXT NOT NULL,
                semester TEXT,
                year TEXT,
                exam_type TEXT,
                content TEXT,
                file_path TEXT,
                file_name TEXT,
                drive_link TEXT,
                created_at TEXT NOT NULL
            )
        """)
        # Migration: Add semester column if missing
        try:
            conn.execute("ALTER TABLE pyqs ADD COLUMN semester TEXT")
        except sqlite3.OperationalError:
            pass
        # Migration: Add drive_link column if missing
        try:
            conn.execute("ALTER TABLE pyqs ADD COLUMN drive_link TEXT")
        except sqlite3.OperationalError:
            pass

        # --- Default Departments & Sections (seed data) ---
        default_depts = ['CSE', 'CSE AIML', 'ECE', 'ME', 'CE', 'IT']
        for dept_name in default_depts:
            try:
                conn.execute("INSERT INTO departments (name) VALUES (?)", (dept_name,))
            except sqlite3.IntegrityError:
                pass  # already exists

        # Add Section A and B for each department
        for dept_name in default_depts:
            row = conn.execute("SELECT id FROM departments WHERE name=?", (dept_name,)).fetchone()
            if row:
                dept_id = row[0]
                for sec_name in ['A', 'B']:
                    try:
                        conn.execute("INSERT INTO sections (department_id, name) VALUES (?, ?)", (dept_id, sec_name))
                    except sqlite3.IntegrityError:
                        pass  # already exists

        # Add some default students if none exist
        student_count = conn.execute("SELECT COUNT(*) as c FROM students").fetchone()["c"]
        if student_count == 0:
            default_students = [
                ('Aarav Sharma', 'CSE', 'A', 'CSE-A-001'),
                ('Priya Singh', 'CSE', 'A', 'CSE-A-002'),
                ('Rohan Gupta', 'CSE', 'A', 'CSE-A-003'),
                ('Neha Verma', 'CSE', 'B', 'CSE-B-001'),
                ('Karan Mehta', 'CSE', 'B', 'CSE-B-002'),
                ('Ishita Rao', 'CSE', 'B', 'CSE-B-003'),
                ('Aditya Kumar', 'CSE AIML', 'A', 'CAIML-A-001'),
                ('Sneha Patel', 'CSE AIML', 'A', 'CAIML-A-002'),
                ('Vikram Joshi', 'CSE AIML', 'B', 'CAIML-B-001'),
                ('Ananya Reddy', 'CSE AIML', 'B', 'CAIML-B-002'),
                ('Rahul Verma', 'ECE', 'A', 'ECE-A-001'),
                ('Pooja Sharma', 'ECE', 'B', 'ECE-B-001'),
            ]
            for name, branch, section, roll in default_students:
                try:
                    conn.execute(
                        "INSERT OR IGNORE INTO students (name, branch, section, roll_no) VALUES (?, ?, ?, ?)",
                        (name, branch, section, roll)
                    )
                except sqlite3.IntegrityError:
                    pass

        # Seed default PYQs if empty
        pyq_count = conn.execute("SELECT COUNT(*) as c FROM pyqs").fetchone()["c"]
        if pyq_count == 0:
            default_pyqs = [
                ("System", "CSE", "Data Structures & Algorithms", "DSA End-Sem Question Paper 2024", "3", "2024", "End-Sem", "https://drive.google.com/drive/folders/1RjrS65gXzj3e8WgEuupoKvORYIkaN0gy"),
                ("System", "CSE", "Database Management Systems", "DBMS Mid-Sem Examination 2024", "4", "2024", "Mid-Sem", "https://drive.google.com/drive/folders/1YnOHoSe6hTj-5ay8jJQBV82Ccd_FzPq7"),
                ("System", "CSE", "Operating Systems", "OS End-Term Paper 2023", "4", "2023", "End-Sem", "https://drive.google.com/drive/folders/1YnOHoSe6hTj-5ay8jJQBV82Ccd_FzPq7"),
                ("System", "CSE", "Computer Networks", "CN End-Sem Paper 2024", "5", "2024", "End-Sem", "https://drive.google.com/drive/folders/1LamJnDD42nB0bVJjBYOuippVBMHJoKbP"),
                ("System", "All", "Engineering Mathematics I", "Maths-I End-Sem Paper 2023", "1", "2023", "End-Sem", "https://drive.google.com/drive/folders/15bkgrVGA5s5Mh1q9l2KLMW9HIboPJ9Vb"),
                ("System", "All", "Engineering Physics", "Physics Mid-Sem Paper 2023", "2", "2023", "Mid-Sem", "https://drive.google.com/drive/folders/1Rq-lxFVc5_ZEAMXCw6W2enBd-pUQPlWW"),
                ("System", "CSE", "Compiler Design", "Compiler Design Paper 2023", "6", "2023", "End-Sem", "https://drive.google.com/drive/folders/196BrUsA15q_rlmlLMkz_32GkjhrOuYn9"),
                ("System", "CSE AIML", "Machine Learning & AI", "ML End-Sem Paper 2024", "7", "2024", "End-Sem", "https://drive.google.com/drive/folders/1Q1BR4mNFJuHMskY5pRvNmUrSe8BsvnGS"),
            ]
            for t_name, br, sub, title, sem, yr, ex, link in default_pyqs:
                conn.execute("""
                    INSERT INTO pyqs (teacher_name, branch, subject, title, semester, year, exam_type, drive_link, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))
                """, (t_name, br, sub, title, sem, yr, ex, link))

        # Seed sample note if empty
        notes_count = conn.execute("SELECT COUNT(*) as c FROM class_notes").fetchone()["c"]
        if notes_count == 0:
            conn.execute("""
                INSERT INTO class_notes (teacher_name, branch, section, subject, title, content, created_at)
                VALUES ('Prof. Sharma', 'CSE', 'A', 'Data Structures', 'Module 1: Binary Search Trees & AVL Trees', 'Complete handwritten notes covering BST insertion, deletion, and AVL tree rotations with solved gate questions.', datetime('now'))
            """)


# ================================================================
# USERS — Teacher / Student registration & login
# ================================================================

def register_user(name, password, role, course=None, year=None, branch=None, section=None, subject=None):
    """Register a new teacher or student. Returns user dict or updates existing."""
    with get_conn() as conn:
        existing = conn.execute(
            "SELECT id, name, role, course, year, branch, section, subject FROM users WHERE LOWER(TRIM(name))=LOWER(TRIM(?)) AND LOWER(TRIM(role))=LOWER(TRIM(?))",
            (name, role),
        ).fetchone()
        hashed = _hash_password(password)
        now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        if existing:
            # Update password and any details if user already exists
            conn.execute(
                """UPDATE users SET password=?, 
                                   course=COALESCE(?, course), 
                                   year=COALESCE(?, year), 
                                   branch=COALESCE(?, branch), 
                                   section=COALESCE(?, section), 
                                   subject=COALESCE(?, subject) 
                   WHERE id=?""",
                (hashed, course, year, branch, section, subject, existing["id"]),
            )
            final_branch = branch or existing["branch"]
            final_sec = section or existing["section"]
            if role.lower() == "student" and final_branch and final_sec:
                conn.execute(
                    "INSERT OR IGNORE INTO students (name, branch, section) VALUES (?, ?, ?)",
                    (name.strip(), final_branch.strip(), final_sec.strip()),
                )
            return {
                "id": existing["id"],
                "name": name,
                "role": role,
                "course": course or existing["course"],
                "year": year or existing["year"],
                "branch": final_branch,
                "section": final_sec,
                "subject": subject or existing["subject"],
            }

        cur = conn.execute(
            "INSERT INTO users (name, password, role, course, year, branch, section, subject, created_at) VALUES (?,?,?,?,?,?,?,?,?)",
            (name, hashed, role, course, year, branch, section, subject, now),
        )
        if role.lower() == "student" and branch and section:
            conn.execute(
                "INSERT OR IGNORE INTO students (name, branch, section) VALUES (?, ?, ?)",
                (name.strip(), branch.strip(), section.strip()),
            )
        return {
            "id": cur.lastrowid,
            "name": name,
            "role": role,
            "course": course,
            "year": year,
            "branch": branch,
            "section": section,
            "subject": subject,
        }


def login_user(name, password, role):
    """Login by name + password + role (case-insensitive name & role). Returns user dict or None."""
    with get_conn() as conn:
        hashed = _hash_password(password)
        row = conn.execute(
            "SELECT id, name, role, course, year, branch, section, subject FROM users WHERE LOWER(TRIM(name))=LOWER(TRIM(?)) AND password=? AND LOWER(TRIM(role))=LOWER(TRIM(?))",
            (name, hashed, role),
        ).fetchone()
        return dict(row) if row else None


def get_user(user_id):
    """Get user by ID."""
    with get_conn() as conn:
        row = conn.execute(
            "SELECT id, name, role, course, year, branch, section, subject FROM users WHERE id=?",
            (user_id,),
        ).fetchone()
        return dict(row) if row else None


def get_all_users(role=None, course=None, year=None, branch=None, section=None):
    """Get all registered users with filters."""
    query = "SELECT id, name, role, course, year, branch, section, subject, created_at FROM users WHERE 1=1"
    params = []
    if role:
        query += " AND role=?"
        params.append(role)
    if course:
        query += " AND course=?"
        params.append(course)
    if year:
        query += " AND year=?"
        params.append(year)
    if branch:
        query += " AND branch=?"
        params.append(branch)
    if section:
        query += " AND section=?"
        params.append(section)

    query += " ORDER BY created_at DESC"

    with get_conn() as conn:
        rows = conn.execute(query, params).fetchall()
        return [dict(r) for r in rows]



# ================================================================
# DEPARTMENTS (Admin)
# ================================================================

def get_departments():
    """List all departments with section count and student count."""
    with get_conn() as conn:
        rows = conn.execute("""
            SELECT d.id, d.name,
                   GROUP_CONCAT(DISTINCT sec.name) AS sections,
                   COUNT(DISTINCT st.id) AS student_count
            FROM departments d
            LEFT JOIN sections sec ON sec.department_id = d.id
            LEFT JOIN students st ON st.branch = d.name
            GROUP BY d.id, d.name
            ORDER BY d.name
        """).fetchall()
        return [dict(r) for r in rows]


def add_department(name):
    """Add a department. Returns id or None if exists."""
    with get_conn() as conn:
        try:
            cur = conn.execute("INSERT INTO departments (name) VALUES (?)", (name.strip(),))
            return {"id": cur.lastrowid, "name": name.strip()}
        except sqlite3.IntegrityError:
            return None


def delete_department(dept_id):
    """Delete department and cascade to sections/students."""
    with get_conn() as conn:
        # Get department name to delete matching students
        dept = conn.execute("SELECT name FROM departments WHERE id=?", (dept_id,)).fetchone()
        if dept:
            dept_name = dept["name"]
            # Delete students in this branch
            conn.execute("DELETE FROM students WHERE branch=?", (dept_name,))
            # Delete sections
            conn.execute("DELETE FROM sections WHERE department_id=?", (dept_id,))
            # Delete department
            conn.execute("DELETE FROM departments WHERE id=?", (dept_id,))
            return True
        return False


# ================================================================
# SECTIONS (Admin)
# ================================================================

def get_sections_by_dept(department_id=None):
    """List sections with department name."""
    with get_conn() as conn:
        query = """
            SELECT sec.id, sec.department_id, sec.name, d.name AS dept_name
            FROM sections sec
            JOIN departments d ON d.id = sec.department_id
        """
        params = []
        if department_id:
            query += " WHERE sec.department_id = ?"
            params.append(department_id)
        query += " ORDER BY d.name, sec.name"
        rows = conn.execute(query, params).fetchall()
        return [dict(r) for r in rows]


def add_section(department_id, name):
    """Add a section to a department."""
    with get_conn() as conn:
        try:
            cur = conn.execute(
                "INSERT INTO sections (department_id, name) VALUES (?, ?)",
                (department_id, name.strip()),
            )
            return {"id": cur.lastrowid, "department_id": department_id, "name": name.strip()}
        except sqlite3.IntegrityError:
            return None


def delete_section(section_id):
    """Delete a section."""
    with get_conn() as conn:
        section = conn.execute("SELECT * FROM sections WHERE id=?", (section_id,)).fetchone()
        if section:
            # Get department name to delete matching students
            dept = conn.execute(
                "SELECT name FROM departments WHERE id=?", (section["department_id"],)
            ).fetchone()
            if dept:
                conn.execute(
                    "DELETE FROM students WHERE branch=? AND section=?",
                    (dept["name"], section["name"]),
                )
            conn.execute("DELETE FROM sections WHERE id=?", (section_id,))
            return True
        return False


# ================================================================
# STUDENTS
# ================================================================

def add_student(name, branch, section, roll_no=None, ble_address=None):
    """Add a student (legacy + new style)."""
    with get_conn() as conn:
        conn.execute(
            "INSERT OR IGNORE INTO students (name, branch, section, roll_no, ble_address) VALUES (?,?,?,?,?)",
            (name, branch, section, roll_no, ble_address),
        )


def add_student_admin(name, roll, department_id, section_id, ble_address=None):
    """Add student via admin panel with department_id/section_id."""
    with get_conn() as conn:
        dept = conn.execute("SELECT name FROM departments WHERE id=?", (department_id,)).fetchone()
        sec = conn.execute("SELECT name FROM sections WHERE id=?", (section_id,)).fetchone()
        if not dept or not sec:
            return False
        try:
            conn.execute(
                "INSERT INTO students (name, branch, section, roll_no, ble_address) VALUES (?,?,?,?,?)",
                (name, dept["name"], sec["name"], roll, ble_address),
            )
            return True
        except sqlite3.IntegrityError:
            return False


def get_students(branch=None, section=None):
    query = "SELECT * FROM students WHERE 1=1"
    params = []
    if branch:
        query += " AND branch=?"
        params.append(branch)
    if section:
        query += " AND section=?"
        params.append(section)
    with get_conn() as conn:
        return [dict(r) for r in conn.execute(query, params).fetchall()]


def get_students_admin(department_id=None, section_id=None):
    """Get students with department/section names for admin panel."""
    # Check if ble_address column exists
    with get_conn() as conn:
        cols = [row[1] for row in conn.execute("PRAGMA table_info(students)").fetchall()]
        has_ble = 'ble_address' in cols

    select_cols = "st.id, st.name, st.roll_no AS roll, st.branch AS department, st.section"
    if has_ble:
        select_cols += ", st.ble_address"
    else:
        select_cols += ", NULL AS ble_address"

    query = f"""
        SELECT {select_cols}
        FROM students st
        WHERE 1=1
    """
    params = []
    if department_id:
        query += " AND st.branch = (SELECT name FROM departments WHERE id=?)"
        params.append(department_id)
    if section_id:
        query += " AND st.section = (SELECT name FROM sections WHERE id=?)"
        params.append(section_id)
    with get_conn() as conn:
        return [dict(r) for r in conn.execute(query, params).fetchall()]


def delete_student(student_id):
    """Delete a student by ID."""
    with get_conn() as conn:
        conn.execute("DELETE FROM students WHERE id=?", (student_id,))
        return True


def get_branches():
    with get_conn() as conn:
        rows = conn.execute("SELECT DISTINCT branch FROM students ORDER BY branch").fetchall()
        return [r["branch"] for r in rows]


def get_sections(branch):
    with get_conn() as conn:
        rows = conn.execute(
            "SELECT DISTINCT section FROM students WHERE branch=? ORDER BY section", (branch,)
        ).fetchall()
        return [r["section"] for r in rows]


# ================================================================
# SESSIONS
# ================================================================

def start_session(teacher_name, branch, section, subject):
    now = datetime.now()
    with get_conn() as conn:
        cur = conn.execute(
            """INSERT INTO sessions (teacher_name, branch, section, subject, date, start_time, status)
               VALUES (?,?,?,?,?,?, 'active')""",
            (teacher_name, branch, section, subject, now.strftime("%Y-%m-%d"), now.strftime("%H:%M:%S")),
        )
        return cur.lastrowid


def end_session(session_id):
    with get_conn() as conn:
        conn.execute(
            "UPDATE sessions SET status='ended', end_time=? WHERE id=?",
            (datetime.now().strftime("%H:%M:%S"), session_id),
        )


def get_active_session(teacher_name):
    with get_conn() as conn:
        row = conn.execute(
            "SELECT * FROM sessions WHERE teacher_name=? AND status='active' ORDER BY id DESC LIMIT 1",
            (teacher_name,),
        ).fetchone()
        return dict(row) if row else None


def get_active_session_for_section(branch, section):
    """Used by the Android student app — it knows its own branch/section."""
    with get_conn() as conn:
        row = conn.execute(
            "SELECT * FROM sessions WHERE branch=? AND section=? AND status='active' ORDER BY id DESC LIMIT 1",
            (branch, section),
        ).fetchone()
        return dict(row) if row else None


def get_any_active_session_for_branch(branch):
    """Fallback: find any active session for this branch regardless of section.
    Used when student's section string doesn't exactly match teacher's session."""
    with get_conn() as conn:
        row = conn.execute(
            "SELECT * FROM sessions WHERE branch=? AND status='active' ORDER BY id DESC LIMIT 1",
            (branch,),
        ).fetchone()
        return dict(row) if row else None


def get_sessions_for_teacher(teacher_name):
    with get_conn() as conn:
        rows = conn.execute(
            "SELECT * FROM sessions WHERE teacher_name=? ORDER BY date DESC, start_time DESC",
            (teacher_name,),
        ).fetchall()
        return [dict(r) for r in rows]


def get_all_sessions():
    """Get all sessions with present/total counts — used by web frontend."""
    with get_conn() as conn:
        rows = conn.execute("""
            SELECT s.*,
                   COUNT(DISTINCT st.id) AS total_count,
                   COUNT(DISTINCT CASE WHEN a.status='Present' THEN a.student_name END) AS present_count
            FROM sessions s
            LEFT JOIN students st ON st.branch = s.branch AND st.section = s.section
            LEFT JOIN attendance a ON a.session_id = s.id
            GROUP BY s.id
            ORDER BY s.date DESC, s.start_time DESC
        """).fetchall()
        return [dict(r) for r in rows]


def delete_session(session_id):
    """Delete a session and its attendance records."""
    with get_conn() as conn:
        # First check if session exists
        existing = conn.execute("SELECT id FROM sessions WHERE id=?", (session_id,)).fetchone()
        if not existing:
            return False
        conn.execute("DELETE FROM attendance WHERE session_id=?", (session_id,))
        conn.execute("DELETE FROM ble_devices WHERE session_id=?", (session_id,))
        conn.execute("DELETE FROM sessions WHERE id=?", (session_id,))
        return True


def get_session_by_id(session_id):
    """Get session details including student attendance list."""
    with get_conn() as conn:
        row = conn.execute("SELECT * FROM sessions WHERE id=?", (session_id,)).fetchone()
        if not row:
            return None
        session = dict(row)
        # Get all students in this section
        students = get_students(session["branch"], session["section"])
        # Get attendance for this session
        marked = {}
        for a in conn.execute(
            "SELECT student_name, status, mode, marked_at FROM attendance WHERE session_id=?",
            (session_id,),
        ).fetchall():
            marked[a["student_name"]] = dict(a)
        # Merge
        student_list = []
        for s in students:
            name = s["name"]
            if name in marked:
                student_list.append({
                    "student_id": s["id"],
                    "name": name,
                    "roll": s.get("roll_no", ""),
                    "status": marked[name]["status"],
                    "method": marked[name]["mode"],
                    "marked_at": marked[name]["marked_at"],
                })
            else:
                student_list.append({
                    "student_id": s["id"],
                    "name": name,
                    "roll": s.get("roll_no", ""),
                    "status": "Absent",
                    "method": None,
                    "marked_at": None,
                })
        session["students"] = student_list
        return session


def get_session_by_id_live(session_id):
    """Lightweight live view — only returns students who are actually Present.
    Does NOT join the full student roster, so no fake 'Absent' entries appear."""
    with get_conn() as conn:
        row = conn.execute("SELECT * FROM sessions WHERE id=?", (session_id,)).fetchone()
        if not row:
            return None
        session = dict(row)
        # Only fetch attendance records (no roster join)
        rows = conn.execute(
            "SELECT student_name, status, mode, marked_at FROM attendance WHERE session_id=? AND status='Present'",
            (session_id,),
        ).fetchall()
        present_list = [
            {
                "name": r["student_name"],
                "status": "Present",
                "method": r["mode"],
                "marked_at": r["marked_at"],
            }
            for r in rows
        ]
        session["students"] = present_list
        session["present_count"] = len(present_list)
        session["total_count"] = len(present_list)
        return session


# ================================================================
# ATTENDANCE
# ================================================================

def mark_attendance(session_id, student_name, status, mode):
    """mode = 'Auto' (BLE detected) or 'Manual'."""
    with get_conn() as conn:
        existing = conn.execute(
            "SELECT id FROM attendance WHERE session_id=? AND student_name=?",
            (session_id, student_name),
        ).fetchone()
        if existing:
            return False
        conn.execute(
            """INSERT INTO attendance (session_id, student_name, status, mode, marked_at)
               VALUES (?,?,?,?,?)""",
            (session_id, student_name, status, mode, datetime.now().strftime("%H:%M:%S")),
        )
        return True


def get_attendance_for_session(session_id):
    with get_conn() as conn:
        rows = conn.execute(
            "SELECT * FROM attendance WHERE session_id=? ORDER BY marked_at", (session_id,)
        ).fetchall()
        return [dict(r) for r in rows]


def get_attendance_history(teacher_name, branch=None, section=None, subject=None):
    """Used by the 'My Records' screen — filterable, exportable."""
    query = """
        SELECT s.date, s.subject, s.branch, s.section, a.student_name, a.status, a.mode, a.marked_at
        FROM attendance a JOIN sessions s ON a.session_id = s.id
        WHERE s.teacher_name = ?
    """
    params = [teacher_name]
    if branch:
        query += " AND s.branch=?"
        params.append(branch)
    if section:
        query += " AND s.section=?"
        params.append(section)
    if subject:
        query += " AND s.subject=?"
        params.append(subject)
    query += " ORDER BY s.date DESC, a.marked_at DESC"
    with get_conn() as conn:
        return [dict(r) for r in conn.execute(query, params).fetchall()]


def get_at_risk_students(teacher_name, threshold_percent=75):
    """
    Students whose attendance is below threshold_percent.
    Uses LEFT JOIN so students who were never present are still counted.
    """
    query = """
        SELECT st.name AS student_name, se.branch, se.section,
               COUNT(DISTINCT se.id) AS total_sessions,
               COUNT(DISTINCT CASE WHEN a.status = 'Present' THEN se.id END) AS present_count
        FROM sessions se
        JOIN students st ON st.branch = se.branch AND st.section = se.section
        LEFT JOIN attendance a ON a.session_id = se.id AND a.student_name = st.name
        WHERE se.teacher_name = ?
        GROUP BY st.name, se.branch, se.section
    """
    with get_conn() as conn:
        rows = [dict(r) for r in conn.execute(query, (teacher_name,)).fetchall()]

    at_risk = []
    for row in rows:
        total = row["total_sessions"]
        present = row["present_count"]
        percent = round((present / total) * 100, 1) if total > 0 else 0.0
        if percent < threshold_percent:
            # Get a subject example for this student (first session's subject)
            with get_conn() as conn:
                subject_row = conn.execute(
                    "SELECT subject FROM sessions WHERE branch=? AND section=? AND teacher_name=? LIMIT 1",
                    (row["branch"], row["section"], teacher_name)
                ).fetchone()
                example_subject = subject_row["subject"] if subject_row else "—"
            at_risk.append({
                "student_name": row["student_name"],
                "subject": example_subject,
                "branch": row["branch"],
                "section": row["section"],
                "total_sessions": total,
                "present_count": present,
                "attendance_percent": percent,
            })

    at_risk.sort(key=lambda x: x["attendance_percent"])
    return at_risk


# ================================================================
# CLASS NOTES
# ================================================================

def add_class_note(teacher_name, branch, section, subject, title, content=None, file_path=None, file_name=None):
    """Add a class note uploaded by teacher."""
    with get_conn() as conn:
        now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        cur = conn.execute(
            "INSERT INTO class_notes (teacher_name, branch, section, subject, title, content, file_path, file_name, created_at) VALUES (?,?,?,?,?,?,?,?,?)",
            (teacher_name, branch, section, subject, title, content, file_path, file_name, now),
        )
        return cur.lastrowid


# ================================================================
# PYQ (Previous Year Questions)
# ================================================================

def add_pyq(teacher_name, branch, subject, title, semester=None, year=None, exam_type=None, content=None, file_path=None, file_name=None, drive_link=None):
    """Add a PYQ entry."""
    with get_conn() as conn:
        now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        cur = conn.execute(
            "INSERT INTO pyqs (teacher_name, branch, subject, title, semester, year, exam_type, content, file_path, file_name, drive_link, created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
            (teacher_name, branch, subject, title, semester, year, exam_type, content, file_path, file_name, drive_link, now),
        )
        return cur.lastrowid


def get_pyqs(teacher_name=None, branch=None, subject=None, semester=None, year=None, drive_link=None):
    """Get PYQs with filters."""
    query = "SELECT * FROM pyqs WHERE 1=1"
    params = []
    if teacher_name:
        query += " AND teacher_name=?"
        params.append(teacher_name)
    if branch:
        query += " AND branch=?"
        params.append(branch)
    if subject:
        query += " AND subject=?"
        params.append(subject)
    if semester:
        query += " AND semester=?"
        params.append(semester)
    if year:
        query += " AND year=?"
        params.append(year)
    if drive_link:
        query += " AND drive_link=?"
        params.append(drive_link)
    query += " ORDER BY created_at DESC"
    with get_conn() as conn:
        return [dict(r) for r in conn.execute(query, params).fetchall()]


def delete_pyq(pyq_id):
    """Delete a PYQ."""
    with get_conn() as conn:
        conn.execute("DELETE FROM pyqs WHERE id=?", (pyq_id,))
        return True


def get_class_notes(teacher_name=None, branch=None, section=None, subject=None):
    """Get class notes with filters."""
    query = "SELECT * FROM class_notes WHERE 1=1"
    params = []
    if teacher_name:
        query += " AND teacher_name=?"
        params.append(teacher_name)
    if branch:
        query += " AND (branch=? OR branch='All' OR branch='' OR branch IS NULL)"
        params.append(branch)
    if section:
        query += " AND (section=? OR section='All' OR section='' OR section IS NULL)"
        params.append(section)
    if subject:
        query += " AND subject=?"
        params.append(subject)
    query += " ORDER BY created_at DESC"
    with get_conn() as conn:
        return [dict(r) for r in conn.execute(query, params).fetchall()]


def delete_class_note(note_id):
    """Delete a class note."""
    with get_conn() as conn:
        conn.execute("DELETE FROM class_notes WHERE id=?", (note_id,))
        return True


# ================================================================
# BLE DEVICES
# ================================================================

def log_ble_device(session_id, device_name, device_address, student_name=None):
    """Log a BLE device detected during a session."""
    with get_conn() as conn:
        # Check if already logged for this session
        existing = conn.execute(
            "SELECT id FROM ble_devices WHERE session_id=? AND device_address=?",
            (session_id, device_address),
        ).fetchone()
        if existing:
            return False
        conn.execute(
            """INSERT INTO ble_devices (session_id, device_name, device_address, student_name, detected_at)
               VALUES (?,?,?,?,?)""",
            (session_id, device_name, device_address, student_name, datetime.now().strftime("%H:%M:%S")),
        )
        return True


def get_ble_devices(session_id):
    """Get all BLE devices detected in a session."""
    with get_conn() as conn:
        rows = conn.execute(
            "SELECT * FROM ble_devices WHERE session_id=? ORDER BY detected_at",
            (session_id,),
        ).fetchall()
        return [dict(r) for r in rows]


# ================================================================
# STATS (for dashboard)
# ================================================================

def get_stats():
    """Quick stats for the dashboard."""
    with get_conn() as conn:
        students = conn.execute("SELECT COUNT(*) as c FROM students").fetchone()["c"]
        sessions = conn.execute("SELECT COUNT(*) as c FROM sessions").fetchone()["c"]
        branches = conn.execute("SELECT COUNT(DISTINCT branch) as c FROM students").fetchone()["c"]
        depts = conn.execute("SELECT COUNT(*) as c FROM departments").fetchone()["c"]
        return {"students": students, "sessions": sessions, "departments": depts, "branches": branches}


# ================================================================
# ANALYTICS
# ================================================================

def get_analytics_overview():
    """Overview stats for analytics page."""
    with get_conn() as conn:
        total_students = conn.execute("SELECT COUNT(*) as c FROM students").fetchone()["c"]
        total_sessions = conn.execute("SELECT COUNT(*) as c FROM sessions").fetchone()["c"]
        total_present = conn.execute("SELECT COUNT(*) as c FROM attendance WHERE status='Present'").fetchone()["c"]
        total_absent = conn.execute("SELECT COUNT(*) as c FROM attendance WHERE status='Absent'").fetchone()["c"]
        total_auto = conn.execute("SELECT COUNT(*) as c FROM attendance WHERE mode='Auto'").fetchone()["c"]
        total_manual = conn.execute("SELECT COUNT(*) as c FROM attendance WHERE mode='Manual'").fetchone()["c"]
        total_departments = conn.execute("SELECT COUNT(*) as c FROM departments").fetchone()["c"]
        total_users = conn.execute("SELECT COUNT(*) as c FROM users").fetchone()["c"]
        avg_attendance = round((total_present / (total_present + total_absent) * 100), 1) if (total_present + total_absent) > 0 else 0
        
        return {
            "total_students": total_students,
            "total_sessions": total_sessions,
            "total_present": total_present,
            "total_absent": total_absent,
            "total_auto": total_auto,
            "total_manual": total_manual,
            "total_departments": total_departments,
            "total_users": total_users,
            "avg_attendance": avg_attendance,
        }


def get_daily_attendance():
    """Daily attendance data for charts."""
    with get_conn() as conn:
        rows = conn.execute("""
            SELECT s.date,
                   COUNT(CASE WHEN a.status='Present' THEN 1 END) as present,
                   COUNT(CASE WHEN a.status='Absent' THEN 1 END) as absent,
                   COUNT(CASE WHEN a.mode='Auto' THEN 1 END) as auto,
                   COUNT(CASE WHEN a.mode='Manual' THEN 1 END) as manual
            FROM sessions s
            LEFT JOIN attendance a ON a.session_id = s.id
            GROUP BY s.date
            ORDER BY s.date DESC
            LIMIT 14
        """).fetchall()
        return [dict(r) for r in rows]


def get_subject_wise_attendance():
    """Subject-wise attendance breakdown."""
    with get_conn() as conn:
        rows = conn.execute("""
            SELECT s.subject,
                   COUNT(CASE WHEN a.status='Present' THEN 1 END) as present,
                   COUNT(CASE WHEN a.status='Absent' THEN 1 END) as absent
            FROM sessions s
            LEFT JOIN attendance a ON a.session_id = s.id
            GROUP BY s.subject
            ORDER BY present DESC
        """).fetchall()
        return [dict(r) for r in rows]


def get_department_wise_attendance():
    """Department-wise attendance breakdown."""
    with get_conn() as conn:
        rows = conn.execute("""
            SELECT s.branch as department,
                   COUNT(CASE WHEN a.status='Present' THEN 1 END) as present,
                   COUNT(CASE WHEN a.status='Absent' THEN 1 END) as absent
            FROM sessions s
            LEFT JOIN attendance a ON a.session_id = s.id
            GROUP BY s.branch
            ORDER BY present DESC
        """).fetchall()
        return [dict(r) for r in rows]


def get_top_performers(limit=10):
    """Students with highest attendance percentage."""
    with get_conn() as conn:
        rows = conn.execute("""
            SELECT a.student_name,
                   COUNT(DISTINCT a.session_id) as total_sessions,
                   COUNT(CASE WHEN a.status='Present' THEN 1 END) as present_count
            FROM attendance a
            GROUP BY a.student_name
            HAVING total_sessions > 0
            ORDER BY (present_count * 100.0 / total_sessions) DESC
            LIMIT ?
        """, (limit,)).fetchall()
        result = []
        for r in rows:
            d = dict(r)
            d["attendance_percent"] = round((d["present_count"] / d["total_sessions"] * 100), 1) if d["total_sessions"] > 0 else 0
            result.append(d)
        return result


def get_weekly_pattern():
    """Attendance pattern by day of week."""
    with get_conn() as conn:
        rows = conn.execute("""
            SELECT CASE CAST(strftime('%w', s.date) AS INTEGER)
                WHEN 0 THEN 'Sun' WHEN 1 THEN 'Mon' WHEN 2 THEN 'Tue'
                WHEN 3 THEN 'Wed' WHEN 4 THEN 'Thu' WHEN 5 THEN 'Fri'
                WHEN 6 THEN 'Sat'
            END as day_name,
            COUNT(CASE WHEN a.status='Present' THEN 1 END) as present,
            COUNT(CASE WHEN a.status='Absent' THEN 1 END) as absent
            FROM sessions s
            LEFT JOIN attendance a ON a.session_id = s.id
            GROUP BY strftime('%w', s.date)
            ORDER BY strftime('%w', s.date)
        """).fetchall()
        return [dict(r) for r in rows]


def get_ble_vs_manual():
    """BLE Auto vs Manual detection stats."""
    with get_conn() as conn:
        auto = conn.execute("SELECT COUNT(*) as c FROM attendance WHERE mode='Auto'").fetchone()["c"]
        manual = conn.execute("SELECT COUNT(*) as c FROM attendance WHERE mode='Manual'").fetchone()["c"]
        return {"auto": auto, "manual": manual}


def get_monthly_summary():
    """Monthly attendance summary."""
    with get_conn() as conn:
        rows = conn.execute("""
            SELECT strftime('%Y-%m', s.date) as month,
                   COUNT(CASE WHEN a.status='Present' THEN 1 END) as present,
                   COUNT(CASE WHEN a.status='Absent' THEN 1 END) as absent
            FROM sessions s
            LEFT JOIN attendance a ON a.session_id = s.id
            GROUP BY strftime('%Y-%m', s.date)
            ORDER BY month DESC
            LIMIT 6
        """).fetchall()
        result = []
        for r in rows:
            d = dict(r)
            total = d["present"] + d["absent"]
            d["attendance_pct"] = round((d["present"] / total * 100), 1) if total > 0 else 0
            result.append(d)
        return result


def update_user_password(user_id, new_password):
    """Update user password by ID."""
    hashed = _hash_password(new_password)
    with get_conn() as conn:
        conn.execute("UPDATE users SET password=? WHERE id=?", (hashed, user_id))
        return True


def get_student_full_analytics(student_name, branch=None, section=None):
    """
    Returns comprehensive personal analytics for a student:
    - Overall attendance %, total sessions, present, absent
    - Bunk Calculator metrics (bunkable count or classes needed for 75%)
    - Subject-wise attendance breakdown
    - Chronological attendance timeline
    """
    with get_conn() as conn:
        query = "SELECT id, teacher_name, branch, section, subject, date, start_time FROM sessions WHERE 1=1"
        params = []
        if branch and section:
            query += " AND (branch = ? AND section = ?)"
            params.extend([branch, section])
        query += " ORDER BY date DESC, start_time DESC"
        
        sessions = [dict(r) for r in conn.execute(query, params).fetchall()]
        
        att_rows = conn.execute(
            "SELECT session_id, status, mode, marked_at FROM attendance WHERE student_name = ?",
            (student_name,)
        ).fetchall()
        
        marked_map = {r["session_id"]: dict(r) for r in att_rows}
        
        attended_session_ids = set(marked_map.keys())
        existing_session_ids = set(s["id"] for s in sessions)
        missing_ids = attended_session_ids - existing_session_ids
        if missing_ids:
            placeholders = ",".join("?" for _ in missing_ids)
            extra = [dict(r) for r in conn.execute(
                f"SELECT id, teacher_name, branch, section, subject, date, start_time FROM sessions WHERE id IN ({placeholders})",
                list(missing_ids)
            ).fetchall()]
            sessions.extend(extra)
            sessions.sort(key=lambda s: (s.get("date", ""), s.get("start_time", "")), reverse=True)
            
        total_sessions = len(sessions)
        present_count = sum(1 for s in sessions if s["id"] in marked_map and marked_map[s["id"]]["status"] == "Present")
        absent_count = total_sessions - present_count
        attendance_pct = round((present_count / total_sessions * 100), 1) if total_sessions > 0 else 0.0
        
        # Bunk / Eligibility Calculator (75% rule)
        if attendance_pct >= 75.0:
            bunkable = int((present_count - 0.75 * total_sessions) / 0.75) if total_sessions > 0 else 0
            eligibility_status = "Safe"
            bunk_message = f"You can safely miss {bunkable} upcoming class(es) and remain above 75%!" if bunkable > 0 else "You are at 75% right now. Attend your next class to stay safe."
            needed = 0
        else:
            needed = int((0.75 * total_sessions - present_count) / 0.25) + 1 if total_sessions > 0 else 1
            eligibility_status = "Shortage"
            bunk_message = f"You must attend the next {needed} consecutive class(es) to reach 75% exam eligibility!"
            bunkable = 0
            
        # Subject-wise Breakdown
        subject_stats = {}
        for s in sessions:
            sub = s.get("subject", "General") or "General"
            if sub == "null" or not sub.strip():
                sub = "General"
            if sub not in subject_stats:
                subject_stats[sub] = {"total": 0, "present": 0}
            subject_stats[sub]["total"] += 1
            if s["id"] in marked_map and marked_map[s["id"]]["status"] == "Present":
                subject_stats[sub]["present"] += 1
                
        subjects_list = []
        for sub, data in subject_stats.items():
            s_pct = round((data["present"] / data["total"] * 100), 1) if data["total"] > 0 else 0.0
            subjects_list.append({
                "subject": sub,
                "total": data["total"],
                "present": data["present"],
                "absent": data["total"] - data["present"],
                "percentage": s_pct,
                "status": "Safe" if s_pct >= 75.0 else "At Risk"
            })
        subjects_list.sort(key=lambda x: x["percentage"], reverse=True)
        
        # Recent History
        history = []
        for s in sessions:
            m = marked_map.get(s["id"])
            raw_sub = s.get("subject", "")
            hist_sub = "General" if not raw_sub or raw_sub == "null" else raw_sub
            history.append({
                "session_id": s["id"],
                "subject": hist_sub,
                "teacher_name": s.get("teacher_name", ""),
                "branch": s.get("branch", ""),
                "section": s.get("section", ""),
                "date": s.get("date", ""),
                "start_time": s.get("start_time", ""),
                "status": "Present" if m and m["status"] == "Present" else "Absent",
                "mode": m.get("mode") if m else None,
                "marked_at": m.get("marked_at") if m else None
            })
            
        return {
            "student_name": student_name,
            "branch": branch,
            "section": section,
            "total_sessions": total_sessions,
            "present_count": present_count,
            "absent_count": absent_count,
            "attendance_pct": attendance_pct,
            "eligibility_status": eligibility_status,
            "bunkable_classes": bunkable,
            "needed_classes": needed,
            "bunk_message": bunk_message,
            "subjects": subjects_list,
            "subject_breakdown": subjects_list,
            "history": history
        }


# ================================================================
# PYQ EXTENSION METHODS
# ================================================================


def add_pyq(teacher_name, branch, subject, title, semester=None, year=None, exam_type="PYQ", content="", file_path=None, file_name=None, drive_link=None):
    """Add a new PYQ paper entry."""
    created_at = datetime.now().strftime("%Y-%m-%d %H:%M")
    with get_conn() as conn:
        cur = conn.execute("""
            INSERT INTO pyqs (teacher_name, branch, subject, title, semester, year, exam_type, content, file_path, file_name, drive_link, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, (teacher_name or "System", branch, subject, title, semester, year, exam_type, content, file_path, file_name, drive_link, created_at))
        return cur.lastrowid


def get_pyqs(teacher_name=None, branch=None, subject=None, semester=None, year=None):
    """Get list of PYQs with optional filters."""
    with get_conn() as conn:
        query = "SELECT * FROM pyqs WHERE 1=1"
        params = []
        if teacher_name:
            query += " AND teacher_name = ?"
            params.append(teacher_name)
        if branch:
            query += " AND (branch = ? OR branch = 'All' OR branch = '')"
            params.append(branch)
        if subject:
            query += " AND subject = ?"
            params.append(subject)
        if semester:
            query += " AND semester = ?"
            params.append(str(semester))
        if year:
            query += " AND year = ?"
            params.append(str(year))
        query += " ORDER BY id DESC"
        rows = conn.execute(query, params).fetchall()
        return [dict(r) for r in rows]



def delete_pyq(pyq_id):
    """Delete a PYQ by ID."""
    with get_conn() as conn:
        conn.execute("DELETE FROM pyqs WHERE id=?", (pyq_id,))
        return True

