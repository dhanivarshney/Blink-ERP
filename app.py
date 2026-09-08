"""
SmartRoll — Teacher Dashboard (Streamlit)
----------------------------------------------------------
Professional dashboard with charts, animations, banners,
class notes, and live attendance visualization.

Run with:  streamlit run app.py
"""

import streamlit as st
import pandas as pd
import io
from datetime import date, datetime
import database as db

st.set_page_config(page_title="SmartRoll", page_icon="📶", layout="wide")
db.init_db()

# ---------- Auto-refresh ----------
if "active_session_id" in st.session_state and st.session_state["active_session_id"]:
    st.markdown('<meta http-equiv="refresh" content="5">', unsafe_allow_html=True)

# ---------- Professional CSS ----------
st.markdown("""
<style>
    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap');
    
    .stApp {
        background: linear-gradient(135deg, #0f0c29 0%, #1a1a3e 40%, #24243e 100%);
        font-family: 'Inter', sans-serif;
    }
    .stApp * { font-family: 'Inter', sans-serif !important; }
    
    /* Animated Header Banner */
    .hero-banner {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
        background-size: 200% 200%;
        animation: gradientShift 4s ease infinite;
        padding: 2.5rem 2.5rem;
        border-radius: 20px;
        margin-bottom: 1.5rem;
        box-shadow: 0 8px 32px rgba(102, 126, 234, 0.35);
        position: relative;
        overflow: hidden;
    }
    .hero-banner::before {
        content: '';
        position: absolute;
        top: -50%; left: -50%;
        width: 200%; height: 200%;
        background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 60%);
        animation: shimmer 3s ease-in-out infinite;
    }
    @keyframes gradientShift { 0%,100% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } }
    @keyframes shimmer { 0%,100% { transform: translateX(-30%) translateY(-30%); } 50% { transform: translateX(30%) translateY(30%); } }
    
    .hero-banner h1 { color: white !important; font-size: 2.2rem !important; margin: 0 !important; font-weight: 800 !important; position: relative; }
    .hero-banner p { color: rgba(255,255,255,0.85) !important; font-size: 1rem !important; margin: 0.4rem 0 0 0 !important; position: relative; }
    
    /* Nav Bar */
    .nav-bar {
        display: flex;
        gap: 0.5rem;
        background: rgba(255,255,255,0.04);
        border: 1px solid rgba(255,255,255,0.08);
        border-radius: 14px;
        padding: 0.5rem;
        margin-bottom: 1.5rem;
    }
    .nav-item {
        padding: 0.7rem 1.2rem;
        border-radius: 10px;
        color: rgba(255,255,255,0.6);
        font-size: 0.9rem;
        font-weight: 600;
        transition: all 0.3s ease;
        cursor: pointer;
    }
    .nav-item.active {
        background: linear-gradient(135deg, #667eea, #764ba2);
        color: white;
        box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
    }
    .nav-item:hover:not(.active) { background: rgba(255,255,255,0.08); color: white; }
    
    /* Stat Cards with animation */
    .stat-card {
        background: rgba(255,255,255,0.04);
        backdrop-filter: blur(10px);
        border: 1px solid rgba(255,255,255,0.08);
        border-radius: 18px;
        padding: 1.5rem;
        text-align: center;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        animation: fadeInUp 0.5s ease backwards;
    }
    .stat-card:nth-child(1) { animation-delay: 0.1s; }
    .stat-card:nth-child(2) { animation-delay: 0.2s; }
    .stat-card:nth-child(3) { animation-delay: 0.3s; }
    .stat-card:nth-child(4) { animation-delay: 0.4s; }
    .stat-card:hover {
        transform: translateY(-6px) scale(1.02);
        box-shadow: 0 16px 48px rgba(102, 126, 234, 0.25);
        border-color: rgba(102, 126, 234, 0.3);
    }
    .stat-card .stat-value {
        font-size: 2.4rem;
        font-weight: 800;
        background: linear-gradient(135deg, #667eea, #764ba2);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
    }
    .stat-card .drive-value {
        font-size: 1.8rem;
        font-weight: 700;
        color: #818CF8;
        background: linear-gradient(135deg, #667eea, #818CF8);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
    }
    .drive-card {
        background: rgba(102,126,234,0.08);
        border: 1px solid rgba(102,126,234,0.2);
        border-radius: 18px;
        padding: 1.5rem;
        text-align: center;
        transition: all 0.3s ease;
        animation: fadeInUp 0.5s ease backwards;
    }
    .drive-card:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 32px rgba(102,126,234,0.2);
        background: rgba(102,126,234,0.12);
    }
    .drive-card .drive-icon {
        font-size: 2.5rem;
        margin-bottom: 0.5rem;
    }
    .drive-card .drive-title {
        color: white;
        font-size: 1.2rem;
        font-weight: 700;
        margin-bottom: 0.3rem;
    }
    .drive-card .drive-subtitle {
        color: rgba(255,255,255,0.5);
        font-size: 0.85rem;
    }
    .drive-card .drive-link {
        display: inline-block;
        margin-top: 0.8rem;
        background: linear-gradient(135deg, #667eea, #764ba2);
        color: white;
        padding: 0.5rem 1.5rem;
        border-radius: 8px;
        font-weight: 600;
        font-size: 0.9rem;
        text-decoration: none;
        transition: all 0.2s ease;
    }
    .drive-card .drive-link:hover {
        transform: scale(1.05);
        box-shadow: 0 4px 16px rgba(102,126,234,0.3);
    }
    .pyq-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
        gap: 1rem;
        margin-top: 1rem;
    }
    .pyq-card {
        background: rgba(255,255,255,0.04);
        border: 1px solid rgba(255,255,255,0.08);
        border-radius: 14px;
        padding: 1.2rem;
        transition: all 0.2s ease;
        cursor: pointer;
    }
    .pyq-card:hover {
        transform: translateY(-3px);
        box-shadow: 0 8px 24px rgba(102,126,234,0.15);
        border-color: rgba(102,126,234,0.3);
    }
    .pyq-card .pyq-icon {
        font-size: 1.8rem;
        margin-bottom: 0.5rem;
    }
    .pyq-card .pyq-title {
        color: white;
        font-size: 1rem;
        font-weight: 700;
        margin-bottom: 0.3rem;
    }
    .pyq-card .pyq-meta {
        color: rgba(255,255,255,0.4);
        font-size: 0.75rem;
        margin-bottom: 0.5rem;
    }
    .pyq-card .pyq-tag {
        display: inline-block;
        padding: 0.15rem 0.5rem;
        border-radius: 4px;
        font-size: 0.7rem;
        font-weight: 600;
        margin-right: 0.25rem;
        margin-bottom: 0.25rem;
    }
    .pyq-card .view-btn {
        display: block;
        margin-top: 0.5rem;
        background: linear-gradient(135deg, #667eea, #764ba2);
        color: white;
        padding: 0.4rem 1rem;
        border-radius: 6px;
        font-size: 0.8rem;
        font-weight: 600;
        text-align: center;
        text-decoration: none;
    }
    .stat-card .stat-label {
        color: rgba(255,255,255,0.5);
        font-size: 0.8rem;
        margin-top: 0.3rem;
        text-transform: uppercase;
        letter-spacing: 1.5px;
    }
    @keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
    
    /* Live Badge */
    .live-badge {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        background: linear-gradient(135deg, #00c853, #00e676);
        color: #0a2e0a;
        padding: 0.4rem 1rem;
        border-radius: 20px;
        font-weight: 700;
        font-size: 0.8rem;
        letter-spacing: 1px;
        animation: pulse 2s infinite;
    }
    @keyframes pulse { 0%,100% { box-shadow: 0 0 0 0 rgba(0,200,83,0.4); } 50% { box-shadow: 0 0 0 12px rgba(0,200,83,0); } }
    
    /* Session Card */
    .session-card {
        background: rgba(255,255,255,0.05);
        backdrop-filter: blur(12px);
        border: 1px solid rgba(255,255,255,0.1);
        border-radius: 18px;
        padding: 2rem;
        margin-bottom: 1rem;
        animation: fadeInUp 0.6s ease backwards;
    }
    
    /* Student Row */
    .student-row {
        display: flex; align-items: center; justify-content: space-between;
        background: rgba(255,255,255,0.03);
        border: 1px solid rgba(255,255,255,0.06);
        border-radius: 14px;
        padding: 0.9rem 1.2rem;
        margin-bottom: 0.5rem;
        transition: all 0.25s ease;
        animation: slideInLeft 0.3s ease backwards;
    }
    .student-row:hover { background: rgba(255,255,255,0.07); border-color: rgba(102, 126, 234, 0.25); transform: translateX(4px); }
    .student-row.present { border-left: 4px solid #00c853; }
    .student-row.absent { border-left: 4px solid rgba(255,255,255,0.1); }
    .student-name { color: white; font-weight: 600; font-size: 0.95rem; }
    .student-status { font-size: 0.75rem; padding: 0.25rem 0.7rem; border-radius: 8px; font-weight: 600; }
    .status-auto { background: rgba(0,200,83,0.15); color: #00e676; }
    .status-manual { background: rgba(33,150,243,0.15); color: #64b5f6; }
    .status-absent { background: rgba(255,255,255,0.06); color: rgba(255,255,255,0.35); }
    @keyframes slideInLeft { from { opacity: 0; transform: translateX(-20px); } to { opacity: 1; transform: translateX(0); } }
    
    /* Section Headers */
    .section-header {
        color: rgba(255,255,255,0.45);
        font-size: 0.7rem;
        text-transform: uppercase;
        letter-spacing: 2.5px;
        font-weight: 700;
        margin: 2rem 0 1rem 0;
        padding-bottom: 0.5rem;
        border-bottom: 1px solid rgba(255,255,255,0.05);
    }
    
    /* Progress Bar */
    .progress-container { background: rgba(255,255,255,0.06); border-radius: 12px; height: 10px; overflow: hidden; margin: 0.5rem 0; }
    .progress-fill { height: 100%; border-radius: 12px; background: linear-gradient(90deg, #667eea, #764ba2, #f093fb); background-size: 200%; animation: progressShine 2s linear infinite; }
    @keyframes progressShine { 0% { background-position: 0%; } 100% { background-position: 200%; } }
    
    /* Risk Cards */
    .risk-card { background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08); border-radius: 14px; padding: 1.2rem 1.5rem; margin-bottom: 0.8rem; transition: all 0.2s ease; }
    .risk-card:hover { transform: translateX(4px); }
    .risk-high { border-left: 4px solid #ff5252; }
    .risk-medium { border-left: 4px solid #ffab40; }
    
    /* User Cards */
    .user-card {
        background: rgba(255,255,255,0.03);
        border: 1px solid rgba(255,255,255,0.06);
        border-radius: 14px;
        padding: 1rem 1.2rem;
        margin-bottom: 0.6rem;
        display: flex; align-items: center; gap: 1rem;
        transition: all 0.25s ease;
        animation: fadeInUp 0.4s ease backwards;
    }
    .user-card:hover { background: rgba(255,255,255,0.07); border-color: rgba(102, 126, 234, 0.2); transform: translateX(4px); }
    .user-icon { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 1.3rem; flex-shrink: 0; }
    .user-icon.teacher { background: rgba(102,126,234,0.15); }
    .user-icon.student { background: rgba(0,200,83,0.15); }
    .user-name { color: white; font-weight: 600; font-size: 0.95rem; }
    .user-meta { color: rgba(255,255,255,0.4); font-size: 0.78rem; margin-top: 0.15rem; }
    .user-badge { font-size: 0.65rem; padding: 0.2rem 0.6rem; border-radius: 6px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; flex-shrink: 0; }
    .badge-teacher { background: rgba(102,126,234,0.15); color: #818CF8; }
    .badge-student { background: rgba(0,200,83,0.15); color: #00e676; }
    
    /* Note Cards */
    .note-card {
        background: rgba(255,255,255,0.04);
        border: 1px solid rgba(255,255,255,0.08);
        border-radius: 14px;
        padding: 1.2rem 1.5rem;
        margin-bottom: 0.8rem;
        border-left: 4px solid #667eea;
        transition: all 0.25s ease;
        animation: fadeInUp 0.4s ease backwards;
    }
    .note-card:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(102,126,234,0.15); }
    .note-title { color: white; font-weight: 700; font-size: 1.05rem; }
    .note-meta { color: rgba(255,255,255,0.4); font-size: 0.78rem; margin-top: 0.3rem; }
    .note-content { color: rgba(255,255,255,0.6); font-size: 0.88rem; margin-top: 0.5rem; line-height: 1.5; }
    
    /* BLE Animation */
    .ble-scanning {
        display: flex; align-items: center; gap: 0.5rem;
        background: rgba(102,126,234,0.08);
        border: 1px solid rgba(102,126,234,0.15);
        border-radius: 12px;
        padding: 0.8rem 1.2rem;
        margin: 0.5rem 0;
        animation: fadeInUp 0.3s ease;
    }
    .ble-dot { width: 8px; height: 8px; background: #667eea; border-radius: 50%; animation: blink 1.5s infinite; }
    @keyframes blink { 0%,100% { opacity: 1; } 50% { opacity: 0.3; } }
    
    .new-detection {
        background: linear-gradient(135deg, rgba(0,200,83,0.12), rgba(0,230,118,0.04));
        border: 1px solid rgba(0,200,83,0.25);
        border-radius: 12px;
        padding: 1rem 1.2rem;
        margin: 0.4rem 0;
        animation: slideInLeft 0.3s ease;
    }
    
    /* Sidebar */
    section[data-testid="stSidebar"] { background: rgba(15,12,41,0.97) !important; border-right: 1px solid rgba(255,255,255,0.05) !important; }
    section[data-testid="stSidebar"] .stRadio > div { gap: 0.3rem; }
    section[data-testid="stSidebar"] .stRadio > div > label {
        background: rgba(255,255,255,0.03);
        border-radius: 10px;
        padding: 0.6rem 1rem;
        border: 1px solid rgba(255,255,255,0.04);
        transition: all 0.2s ease;
    }
    section[data-testid="stSidebar"] .stRadio > div > label:hover {
        background: rgba(102,126,234,0.12);
        border-color: rgba(102,126,234,0.25);
    }
    
    .stButton > button { border-radius: 12px !important; font-weight: 600 !important; transition: all 0.2s ease !important; }
    .stButton > button:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,0,0,0.2) !important; }
    
    .block-container { padding-top: 1.5rem !important; }
    
    ::-webkit-scrollbar { width: 6px; }
    ::-webkit-scrollbar-track { background: transparent; }
    ::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.12); border-radius: 3px; }
    
    /* Chart container */
    .chart-container {
        background: rgba(255,255,255,0.03);
        border: 1px solid rgba(255,255,255,0.06);
        border-radius: 16px;
        padding: 1.5rem;
        animation: fadeInUp 0.5s ease backwards;
    }
</style>
""", unsafe_allow_html=True)

# ---------- Sidebar ----------
with st.sidebar:
    st.markdown("""
    <div style="text-align:center; padding:1rem 0;">
        <div style="font-size:2.8rem; animation: fadeInUp 0.5s ease;">📶</div>
        <div style="color:white; font-size:1.4rem; font-weight:800; margin-top:0.3rem;">SmartRoll</div>
        <div style="color:rgba(255,255,255,0.35); font-size:0.72rem; letter-spacing:1px;">BLE ATTENDANCE SYSTEM</div>
    </div>
    <div style="height:1px; background:linear-gradient(90deg, transparent, rgba(255,255,255,0.1), transparent); margin:0.5rem 0;"></div>
    """, unsafe_allow_html=True)
    
    teacher_name = st.text_input("👩‍🏫 Teacher Name", value=st.session_state.get("teacher_name", ""))
    if teacher_name:
        st.session_state["teacher_name"] = teacher_name
    
    st.markdown('<div style="height:1px; background:linear-gradient(90deg, transparent, rgba(255,255,255,0.08), transparent); margin:0.5rem 0;"></div>', unsafe_allow_html=True)
    page = st.radio("Navigation", ["📊 Dashboard", "📈 Analytics", "📋 Records", "⚠️ At-Risk", "📚 Notes", "📝 PYQ Papers", "👥 Users"], label_visibility="collapsed")

if not teacher_name:
    st.markdown("""
    <div class="hero-banner">
        <h1>📶 SmartRoll</h1>
        <p>BLE-based Attendance System — Enter your name in the sidebar to begin.</p>
    </div>
    <div style="text-align:center; padding:4rem 2rem; color:rgba(255,255,255,0.35);">
        <div style="font-size:5rem; animation: fadeInUp 0.6s ease;">📡</div>
        <div style="font-size:1.3rem; font-weight:700; margin-top:1rem; animation: fadeInUp 0.7s ease;">Welcome to SmartRoll</div>
        <div style="margin-top:0.5rem; animation: fadeInUp 0.8s ease;">Enter your name in the sidebar to start managing attendance.</div>
    </div>
    """, unsafe_allow_html=True)
    st.stop()

stats = db.get_stats()

# ============================================================
# PAGE 1: Dashboard
# ============================================================
if page == "📊 Dashboard":
    active = db.get_active_session(teacher_name)
    if active:
        st.session_state["active_session_id"] = active["id"]
    else:
        st.session_state.pop("active_session_id", None)
    
    st.markdown(f"""
    <div class="hero-banner">
        <h1>📊 Attendance Dashboard</h1>
        <p>Welcome back, <strong>{teacher_name}</strong> — Manage live attendance sessions.</p>
    </div>
    """, unsafe_allow_html=True)
    
    c1, c2, c3, c4 = st.columns(4)
    with c1: st.markdown(f"""<div class="stat-card"><div class="stat-value">{stats['students']}</div><div class="stat-label">Students</div></div>""", unsafe_allow_html=True)
    with c2: st.markdown(f"""<div class="stat-card"><div class="stat-value">{stats['sessions']}</div><div class="stat-label">Sessions</div></div>""", unsafe_allow_html=True)
    with c3: st.markdown(f"""<div class="stat-card"><div class="stat-value">{stats['departments']}</div><div class="stat-label">Departments</div></div>""", unsafe_allow_html=True)
    with c4:
        live = "🟢 LIVE" if active else "⚪ IDLE"
        st.markdown(f"""<div class="stat-card"><div class="stat-value" style="font-size:1.4rem;">{live}</div><div class="stat-label">Status</div></div>""", unsafe_allow_html=True)

    if not active:
        st.markdown("""
        <div class="session-card">
            <div style="text-align:center; padding:2rem 1rem;">
                <div style="font-size:3.5rem; animation: fadeInUp 0.5s ease;">📱</div>
                <div style="color:white; font-size:1.2rem; font-weight:700; margin-top:0.8rem;">No Active Session</div>
                <div style="color:rgba(255,255,255,0.4); margin-top:0.5rem; font-size:0.95rem;">
                    Start a session from the <strong>Teacher App</strong> on your phone.<br>
                    It will appear here automatically.
                </div>
            </div>
        </div>
        """, unsafe_allow_html=True)
    else:
        sid = active["id"]
        roster = db.get_students(active["branch"], active["section"])
        marked = {a["student_name"]: a for a in db.get_attendance_for_session(sid)}
        pc = len(marked)
        tc = len(roster)
        pct = round((pc / tc * 100), 1) if tc > 0 else 0
        
        st.markdown(f"""
        <div class="session-card">
            <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:1rem;">
                <div>
                    <span class="live-badge">● LIVE</span>
                    <div style="color:white; font-size:1.5rem; font-weight:800; margin-top:0.5rem;">{active['subject']}</div>
                    <div style="color:rgba(255,255,255,0.5); font-size:0.9rem; margin-top:0.2rem;">
                        {active['branch']} — Section {active['section']} · Started {active['start_time']}
                    </div>
                </div>
                <div style="text-align:right;">
                    <div style="color:rgba(255,255,255,0.5); font-size:0.7rem; text-transform:uppercase; letter-spacing:1.5px;">Attendance</div>
                    <div style="color:white; font-size:2.2rem; font-weight:800;">{pct}%</div>
                    <div style="color:rgba(255,255,255,0.4); font-size:0.85rem;">{pc}/{tc} present</div>
                </div>
            </div>
            <div class="progress-container" style="margin-top:1rem;">
                <div class="progress-fill" style="width:{pct}%;"></div>
            </div>
        </div>
        """, unsafe_allow_html=True)
        
        if st.button("⏹️ End Class", key="end_class"):
            db.end_session(sid)
            st.session_state.pop("active_session_id", None)
            st.rerun()
        
        st.markdown('<div class="section-header">📡 Live BLE Detection</div>', unsafe_allow_html=True)
        st.markdown("""<div class="ble-scanning"><div class="ble-dot"></div>
            <span style="color:rgba(255,255,255,0.65); font-size:0.85rem;">Scanning — Auto-refreshes every 5s</span></div>""", unsafe_allow_html=True)
        
        auto = {n: i for n, i in marked.items() if i["mode"] == "Auto"}
        for n, i in auto.items():
            st.markdown(f"""<div class="new-detection">
                <span style="color:#00e676; font-weight:600;">✅ {n}</span>
                <span style="color:rgba(255,255,255,0.35); font-size:0.85rem; margin-left:0.5rem;">BLE · {i['marked_at']}</span></div>""", unsafe_allow_html=True)
        
        st.markdown(f'<div class="section-header">Full Roster — {tc} Students</div>', unsafe_allow_html=True)
        for i, s in enumerate(roster):
            nm = s["name"]
            if nm in marked:
                m = marked[nm]
                sh = '<span class="student-status status-auto">✅ BLE Auto</span>' if m["mode"] == "Auto" else '<span class="student-status status-manual">✅ Manual</span>'
                rc = "present"
            else:
                sh = '<span class="student-status status-absent">⚪ Absent</span>'
                rc = "absent"
            c1, c2, c3 = st.columns([4, 2, 1])
            with c1:
                st.markdown(f"""<div class="student-row {rc}"><span class="student-name">{i+1}. {nm}</span>{sh}</div>""", unsafe_allow_html=True)
            with c3:
                if nm not in marked:
                    if st.button("Mark", key=f"m_{nm}"):
                        db.mark_attendance(sid, nm, "Present", "Manual")
                        st.rerun()
    
    # ============================================================
    # PYQ Quick Access Section (below attendance)
    # ============================================================
    st.markdown('<div class="section-header">📚 Quick Access — PYQ Papers</div>', unsafe_allow_html=True)
    
    drive_base = "https://drive.google.com/drive/folders/1eTxp4mDjGcuDexieUMYrfqAuKBG3S46n"
    pyqs_dash = db.get_pyqs(teacher_name=teacher_name)
    if pyqs_dash:
        st.markdown('<div class="pyq-grid">', unsafe_allow_html=True)
        for pyq in pyqs_dash[:6]:  # Show first 6
            drive_url = pyq.get('drive_link') or f"{drive_base}?searchTitle={pyq['title'].replace(' ', '+')}"
            ext = pyq.get('file_name', '').split('.')[-1].lower() if pyq.get('file_name') else ''
            file_icon = {"pdf":"📕","doc":"📘","docx":"📘","ppt":"📊","pptx":"📊","txt":"📄"}.get(ext, "📝")
            tags = []
            if pyq.get('semester'): tags.append(f'<span class="pyq-tag" style="background:rgba(102,126,234,0.15);color:#818CF8;">{pyq["semester"]}</span>')
            if pyq.get('year'): tags.append(f'<span class="pyq-tag" style="background:rgba(255,255,255,0.06);color:rgba(255,255,255,0.5);">{pyq["year"]}</span>')
            if pyq.get('exam_type'): tags.append(f'<span class="pyq-tag" style="background:rgba(0,200,83,0.12);color:#00e676;">{pyq["exam_type"]}</span>')
            st.markdown(f'''<div class="pyq-card" onclick="window.open('{drive_url}', '_blank')">
                <div class="pyq-icon">{file_icon}</div>
                <div class="pyq-title">{pyq['title']}</div>
                <div class="pyq-meta">{pyq['subject']} · {pyq['branch']}</div>
                <div>{''.join(tags)}</div>
                <a href="{drive_url}" class="view-btn" target="_blank">🔗 Open in Drive</a>
            </div>''', unsafe_allow_html=True)
        st.markdown('</div>', unsafe_allow_html=True)
    else:
        st.markdown('<div style="text-align:center; padding:1rem; color:rgba(255,255,255,0.3); font-size:0.85rem;">No PYQs uploaded yet</div>', unsafe_allow_html=True)

# ============================================================
# PAGE 2: Analytics (Charts & Graphs)
# ============================================================
elif page == "📈 Analytics":
    st.markdown("""
    <div class="hero-banner">
        <h1>📈 Attendance Analytics</h1>
        <p>Visualize attendance patterns and trends with interactive charts.</p>
    </div>
    """, unsafe_allow_html=True)
    
    # Attendance Over Time
    st.markdown('<div class="section-header">Attendance Over Time</div>', unsafe_allow_html=True)
    history = db.get_attendance_history(teacher_name)
    if history:
        df = pd.DataFrame(history)
        df["date"] = pd.to_datetime(df["date"], errors="coerce")
        daily = df.groupby("date").agg(
            total=("student_name", "count"),
            present=("status", lambda x: (x == "Present").sum()),
            auto=("mode", lambda x: (x == "Auto").sum()),
            manual=("mode", lambda x: (x == "Manual").sum()),
        ).reset_index()
        daily["absent"] = daily["total"] - daily["present"]
        daily["pct"] = (daily["present"] / daily["total"] * 100).round(1)
        
        col1, col2 = st.columns(2)
        with col1:
            st.markdown('<div class="chart-container">', unsafe_allow_html=True)
            st.subheader("📅 Daily Attendance %")
            st.line_chart(daily.set_index("date")["pct"], use_container_width=True, color="#667eea")
            st.markdown('</div>', unsafe_allow_html=True)
        with col2:
            st.markdown('<div class="chart-container">', unsafe_allow_html=True)
            st.subheader("📊 Present vs Absent")
            st.bar_chart(daily.set_index("date")[["present", "absent"]], use_container_width=True)
            st.markdown('</div>', unsafe_allow_html=True)
        
        col3, col4 = st.columns(2)
        with col3:
            st.markdown('<div class="chart-container">', unsafe_allow_html=True)
            st.subheader("🔵 BLE Auto vs Manual")
            st.bar_chart(daily.set_index("date")[["auto", "manual"]], use_container_width=True)
            st.markdown('</div>', unsafe_allow_html=True)
        with col4:
            st.markdown('<div class="chart-container">', unsafe_allow_html=True)
            st.subheader("📊 Mode Breakdown")
            st.metric("Total Records", len(df))
            st.metric("Present", (df["status"] == "Present").sum())
            st.metric("BLE Auto", (df["mode"] == "Auto").sum())
            st.metric("Manual", (df["mode"] == "Manual").sum())
            st.markdown('</div>', unsafe_allow_html=True)
    else:
        st.markdown("""<div style="text-align:center; padding:4rem; color:rgba(255,255,255,0.3);">
            <div style="font-size:3.5rem;">📊</div>
            <div style="margin-top:0.5rem; font-size:1.1rem;">No data yet. Start taking attendance to see analytics.</div></div>""", unsafe_allow_html=True)

# ============================================================
# PAGE 3: Records
# ============================================================
elif page == "📋 Records":
    st.markdown(f"""
    <div class="hero-banner">
        <h1>📋 Attendance Records</h1>
        <p>View and export attendance history for <strong>{teacher_name}</strong>.</p>
    </div>
    """, unsafe_allow_html=True)
    
    branches = db.get_branches()
    c1, c2, c3 = st.columns(3)
    with c1: bf = st.selectbox("Department", ["All"] + branches)
    with c2: sf = st.selectbox("Section", ["All"] + (db.get_sections(bf) if bf != "All" else []))
    
    history = db.get_attendance_history(teacher_name, branch=None if bf == "All" else bf, section=None if sf == "All" else sf)
    if not history:
        st.markdown("""<div style="text-align:center; padding:3rem; color:rgba(255,255,255,0.3);">
            <div style="font-size:3rem;">📭</div><div style="margin-top:0.5rem;">No records found.</div></div>""", unsafe_allow_html=True)
    else:
        df = pd.DataFrame(history)
        tp = (df["status"] == "Present").sum()
        ta = (df["mode"] == "Auto").sum()
        tm = (df["mode"] == "Manual").sum()
        c1, c2, c3 = st.columns(3)
        with c1: st.markdown(f"""<div class="stat-card"><div class="stat-value">{tp}</div><div class="stat-label">Present</div></div>""", unsafe_allow_html=True)
        with c2: st.markdown(f"""<div class="stat-card"><div class="stat-value">{ta}</div><div class="stat-label">BLE Auto</div></div>""", unsafe_allow_html=True)
        with c3: st.markdown(f"""<div class="stat-card"><div class="stat-value">{tm}</div><div class="stat-label">Manual</div></div>""", unsafe_allow_html=True)
        st.dataframe(df, use_container_width=True, height=400)
        buf = io.BytesIO()
        df.to_excel(buf, index=False, engine="openpyxl")
        st.download_button("⬇️ Export Excel", data=buf.getvalue(), file_name=f"smartroll_{date.today()}.xlsx", mime="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", use_container_width=True)

# ============================================================
# PAGE 4: At-Risk
# ============================================================
elif page == "⚠️ At-Risk":
    st.markdown("""
    <div class="hero-banner">
        <h1>⚠️ At-Risk Students</h1>
        <p>Identify students below the required attendance threshold.</p>
    </div>
    """, unsafe_allow_html=True)
    
    threshold = st.slider("Threshold (%)", 50, 90, 75, 5)
    at_risk = db.get_at_risk_students(teacher_name, threshold)
    
    if not at_risk:
        st.markdown("""<div style="text-align:center; padding:3rem;">
            <div style="font-size:3.5rem;">🎉</div>
            <div style="color:#00e676; font-size:1.2rem; font-weight:700; margin-top:0.5rem;">All Clear!</div>
            <div style="color:rgba(255,255,255,0.4);">No students below threshold.</div></div>""", unsafe_allow_html=True)
    else:
        worst = min(at_risk, key=lambda x: x["attendance_percent"])
        avg = round(sum(s["attendance_percent"] for s in at_risk) / len(at_risk), 1)
        c1, c2, c3 = st.columns(3)
        with c1: st.markdown(f"""<div class="stat-card"><div class="stat-value" style="color:#ff5252;">{len(at_risk)}</div><div class="stat-label">At Risk</div></div>""", unsafe_allow_html=True)
        with c2: st.markdown(f"""<div class="stat-card"><div class="stat-value" style="color:#ffab40;">{worst['attendance_percent']}%</div><div class="stat-label">Lowest</div></div>""", unsafe_allow_html=True)
        with c3: st.markdown(f"""<div class="stat-card"><div class="stat-value">{avg}%</div><div class="stat-label">Avg At-Risk</div></div>""", unsafe_allow_html=True)
        
        for s in at_risk:
            rc = "risk-high" if s["attendance_percent"] < 50 else "risk-medium"
            color = "#ff5252" if s["attendance_percent"] < 50 else "#ffab40"
            st.markdown(f"""<div class="risk-card {rc}">
                <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:0.5rem;">
                    <div><div style="color:white; font-weight:600; font-size:1.05rem;">{s['student_name']}</div>
                    <div style="color:rgba(255,255,255,0.4); font-size:0.8rem;">{s['subject']} · {s['branch']}-{s['section']}</div></div>
                    <div style="text-align:right;"><div style="color:{color}; font-weight:700; font-size:1.2rem;">{s['attendance_percent']}%</div>
                    <div style="color:rgba(255,255,255,0.3); font-size:0.75rem;">{s['present_count']}/{s['total_sessions']}</div></div></div>
                <div class="progress-container" style="margin-top:0.6rem;">
                    <div class="progress-fill" style="width:{s['attendance_percent']}%; background:linear-gradient(90deg, {color}, {color}88);"></div></div></div>""", unsafe_allow_html=True)

# ============================================================
# PAGE 5: Class Notes
# ============================================================
elif page == "📚 Notes":
    st.markdown("""
    <div class="hero-banner">
        <h1>📚 Class Notes</h1>
        <p>Upload and share study materials with students.</p>
    </div>
    """, unsafe_allow_html=True)
    
    # Add Note Form (with file upload)
    with st.expander("➕ Add New Note", expanded=False):
        with st.form("note_form", clear_on_submit=True):
            nc1, nc2 = st.columns(2)
            with nc1: title = st.text_input("Title*")
            with nc2: subject = st.text_input("Subject*")
            branch_n = st.text_input("Branch*")
            section_n = st.text_input("Section*")
            content = st.text_area("Content", height=120)
            uploaded_file = st.file_uploader("📎 Attach File (PDF, DOC, PPT)", type=["pdf","doc","docx","ppt","pptx","txt"])
            
            if st.form_submit_button("📝 Save Note", use_container_width=True):
                if title and subject and branch_n and section_n:
                    file_path = None
                    file_name = None
                    if uploaded_file is not None:
                        import os
                        os.makedirs("uploads", exist_ok=True)
                        file_path = os.path.join("uploads", uploaded_file.name)
                        with open(file_path, "wb") as f:
                            f.write(uploaded_file.getbuffer())
                        file_name = uploaded_file.name
                    db.add_class_note(teacher_name, branch_n, section_n, subject, title, content, file_path, file_name)
                    st.success("Note saved!" + (f" with file: {file_name}" if file_name else ""))
                    st.rerun()
                else:
                    st.warning("Title, Subject, Branch, Section are required.")
    
    # List Notes
    notes = db.get_class_notes(teacher_name=teacher_name)
    if notes:
        st.markdown(f'<div class="section-header">{len(notes)} Notes</div>', unsafe_allow_html=True)
        for note in notes:
            st.markdown(f"""<div class="note-card">
                <div style="display:flex; justify-content:space-between; align-items:flex-start;">
                    <div>
                        <div class="note-title">📝 {note['title']}</div>
                        <div class="note-meta">{note['subject']} · {note['branch']}-{note['section']} · {note['created_at']}</div>
                    </div>
                </div>
                {'<div class="note-content">' + note['content'][:300] + ("..." if len(note.get("content","") or "") > 300 else "") + '</div>' if note.get('content') else ''}
                {'<div style="margin-top:0.5rem;"><a href="/api/notes/download/' + str(note['id']) + '" style="color:#818CF8; font-size:0.85rem;">📎 ' + (note.get('file_name') or 'Download') + '</a></div>' if note.get('file_path') else ''}
            </div>""", unsafe_allow_html=True)
    else:
        st.markdown("""<div style="text-align:center; padding:3rem; color:rgba(255,255,255,0.3);">
            <div style="font-size:3.5rem;">📭</div>
            <div style="margin-top:0.5rem;">No notes yet. Create your first note above!</div></div>""", unsafe_allow_html=True)

# ============================================================
# PAGE 5B: PYQ (Previous Year Questions)
# ============================================================
elif page == "📝 PYQ Papers":
    st.markdown("""
    <div class="hero-banner">
        <h1>📝 Previous Year Questions</h1>
        <p>Upload and share PYQ papers with students — organized by semester.</p>
    </div>
    """, unsafe_allow_html=True)
    
    # Upload PYQ Form
    with st.expander("➕ Upload New PYQ", expanded=False):
        with st.form("pyq_form", clear_on_submit=True):
            col1, col2 = st.columns(2)
            with col1:
                title = st.text_input("Title*")
                subject = st.text_input("Subject*")
                branch = st.text_input("Branch*")
            with col2:
                semester = st.selectbox("Semester", ["1st Sem","2nd Sem","3rd Sem","4th Sem","5th Sem","6th Sem","7th Sem","8th Sem"])
                year = st.selectbox("Year", ["2025","2024","2023","2022","2021","2020"])
                exam_type = st.selectbox("Exam Type", ["Mid-Term","End-Term","Sessional","Quiz","Assignment"])
                content = st.text_area("Description (optional)", height=80)
            uploaded_file = st.file_uploader("📎 Attach PYQ File (PDF, DOC, PPT)", type=["pdf","doc","docx","ppt","pptx","txt"])
            drive_link = st.text_input("🔗 Google Drive Link (optional - leave blank to use default folder)", placeholder="https://drive.google.com/...")
            
            if st.form_submit_button("📤 Upload PYQ", use_container_width=True):
                if title and subject and branch:
                    file_path = None
                    file_name = None
                    if uploaded_file is not None:
                        import os
                        os.makedirs("uploads", exist_ok=True)
                        file_path = os.path.join("uploads", uploaded_file.name)
                        with open(file_path, "wb") as f:
                            f.write(uploaded_file.getbuffer())
                        file_name = uploaded_file.name
                    db.add_pyq(teacher_name, branch, subject, title, semester, year, exam_type, content, file_path, file_name, drive_link or None)
                    st.success("PYQ uploaded!" + (f" with file: {file_name}" if file_name else "") + (f" with Drive link" if drive_link else ""))
                    st.rerun()
                else:
                    st.warning("Title, Subject, Branch are required.")
    
    # Filter by year (simplified)
    years = ["All"] + sorted(set(pyq['year'] for pyq in db.get_pyqs(teacher_name=teacher_name) if pyq.get('year')), reverse=True)
    year_filter = st.selectbox("Year Filter", years, index=0)
    
    pyqs = db.get_pyqs(
        teacher_name=teacher_name,
        year=year_filter if year_filter != "All" else None
    )
    
    if pyqs:
        st.markdown(f'<div class="section-header">{len(pyqs)} PYQ Papers</div>', unsafe_allow_html=True)
        
        # Google Drive folder base link (REDM 공개 설정 필요)
        drive_base = "https://drive.google.com/drive/folders/1eTxp4mDjGcuDexieUMYrfqAuKBG3S46n"
        
        for pyq in pyqs:
            # Build drive link: use pyq.drive_link if available, else construct from title
            drive_url = pyq.get('drive_link') or f"{drive_base}?searchTitle={pyq['title'].replace(' ', '+')}"
            
            ext = pyq.get('file_name', '').split('.')[-1].lower() if pyq.get('file_name') else ''
            file_icon = {"pdf":"📕","doc":"📘","docx":"📘","ppt":"📊","pptx":"📊","txt":"📄"}.get(ext, "📝")
            
            st.markdown(f"""<div class="note-card" style="cursor:pointer;" onclick="window.open('{drive_url}', '_blank')">
                <div style="display:flex; justify-content:space-between; align-items:flex-start; gap:1rem;">
                    <div>
                        <div style="display:flex; align-items:center; gap:0.5rem;">
                            <span style="font-size:1.4rem;">{file_icon}</span>
                            <span class="note-title" style="font-size:1.15rem;">{pyq['title']}</span>
                        </div>
                        <div class="note-meta">
                            {pyq['subject']} · {pyq['branch']} · 
                            <span style="background:rgba(102,126,234,0.15); padding:0.15rem 0.5rem; border-radius:4px; color:#818CF8; font-size:0.75rem;">{pyq.get('semester','')}</span>
                            <span style="background:rgba(255,255,255,0.06); padding:0.15rem 0.5rem; border-radius:4px; color:rgba(255,255,255,0.5); font-size:0.75rem; margin-left:0.3rem;">{pyq.get('year','')}</span>
                            <span style="background:rgba(0,200,83,0.12); padding:0.15rem 0.5rem; border-radius:4px; color:#00e676; font-size:0.75rem; margin-left:0.3rem;">{pyq.get('exam_type','')}</span>
                        </div>
                        {'<div class="note-content">' + (pyq['content'][:200] + "..." if pyq.get('content') and len(pyq['content']) > 200 else pyq.get('content','')) + '</div>' if pyq.get('content') else ''}
                    </div>
                    <div style="flex-shrink:0; text-align:right;">
                        <div style="background:linear-gradient(135deg, #667eea, #764ba2); color:white; font-weight:600; padding:0.5rem 1rem; border-radius:8px; font-size:0.8rem; white-space:nowrap;">
                            🔗 View in Drive
                        </div>
                        {'<div style="margin-top:0.4rem;"><a href="/api/pyqs/download/' + str(pyq['id']) + '" style="color:#818CF8; font-size:0.8rem;">📎 Download File</a></div>' if pyq.get('file_path') else ''}
                    </div>
                </div>
            </div>""", unsafe_allow_html=True)
    else:
        st.markdown("""<div style="text-align:center; padding:3rem; color:rgba(255,255,255,0.3);">
            <div style="font-size:3.5rem;">📝</div>
            <div style="margin-top:0.5rem;">No PYQs uploaded yet. Upload your first PYQ paper above!</div></div>""", unsafe_allow_html=True)

# ============================================================
# PAGE 6: Users
# ============================================================
elif page == "👥 Users":
    st.markdown("""
    <div class="hero-banner">
        <h1>👥 Registered Users</h1>
        <p>All teachers and students registered via the Android app.</p>
    </div>
    """, unsafe_allow_html=True)
    
    c1, c2 = st.columns(2)
    with c1: rf = st.selectbox("Role", ["All", "teacher", "student"])
    with c2: cf = st.selectbox("Course", ["All", "B.Tech", "B.Pharma", "BCA", "MBA"])
    
    users = db.get_all_users(role=None if rf == "All" else rf, course=None if cf == "All" else cf)
    
    if not users:
        st.markdown("""<div style="text-align:center; padding:3rem; color:rgba(255,255,255,0.3);">
            <div style="font-size:3.5rem;">🔍</div><div style="margin-top:0.5rem;">No users registered yet.</div></div>""", unsafe_allow_html=True)
    else:
        tc = sum(1 for u in users if u["role"] == "teacher")
        sc = sum(1 for u in users if u["role"] == "student")
        c1, c2, c3 = st.columns(3)
        with c1: st.markdown(f"""<div class="stat-card"><div class="stat-value">{len(users)}</div><div class="stat-label">Total</div></div>""", unsafe_allow_html=True)
        with c2: st.markdown(f"""<div class="stat-card"><div class="stat-value">{tc}</div><div class="stat-label">Teachers</div></div>""", unsafe_allow_html=True)
        with c3: st.markdown(f"""<div class="stat-card"><div class="stat-value">{sc}</div><div class="stat-label">Students</div></div>""", unsafe_allow_html=True)
        
        for u in users:
            it = "👨‍🏫" if u["role"] == "teacher" else "🎓"
            ic = "teacher" if u["role"] == "teacher" else "student"
            bc = "badge-teacher" if u["role"] == "teacher" else "badge-student"
            rt = "TEACHER" if u["role"] == "teacher" else "STUDENT"
            meta = " · ".join(filter(None, [u.get("course"), u.get("branch"), f"Sec {u['section']}" if u.get("section") else None, u.get("year"), u.get("subject")])) or "—"
            st.markdown(f"""<div class="user-card">
                <div class="user-icon {ic}">{it}</div>
                <div style="flex:1;"><div class="user-name">{u['name']}</div><div class="user-meta">{meta}</div></div>
                <span class="user-badge {bc}">{rt}</span></div>""", unsafe_allow_html=True)
