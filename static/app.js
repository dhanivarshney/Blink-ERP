/* ═══════════════════════════════════════════════════════════════
   BlinkERP — Professional Dashboard JS
   Live BLE Session • Real-time Polling • Radar Animation
   ═══════════════════════════════════════════════════════════════ */

let currentUser = null;
let allDepts = [];
let allSections = [];
let activeSession = null;
let pollTimer = null;
let bleRunning = false;
let selectedFile = null;
let selectedPyqFile = null;
let currentPyqSem = '';
let chartInstances = {};
let feedHistory = [];
let devicePositions = [];

const COLORS = {
  blue: '#3b82f6', green: '#10b981', purple: '#8b5cf6',
  red: '#ef4444', amber: '#f59e0b', pink: '#ec4899',
  cyan: '#06b6d4', slate: '#64748b',
  blueA: 'rgba(59,130,246,0.15)', greenA: 'rgba(16,185,129,0.15)',
  purpleA: 'rgba(139,92,246,0.15)', redA: 'rgba(239,68,68,0.15)',
  amberA: 'rgba(245,158,11,0.15)',
};

Chart.defaults.font.family = "'Inter', sans-serif";
Chart.defaults.color = '#94a3b8';
Chart.defaults.plugins.legend.labels.usePointStyle = true;
Chart.defaults.plugins.legend.labels.pointStyle = 'circle';
Chart.defaults.plugins.legend.labels.padding = 14;
Chart.defaults.plugins.legend.labels.color = '#94a3b8';

// ─── BG PARTICLES ──────────────────────────────────────────────
function initParticles() {
  const container = document.getElementById('bgParticles');
  if (!container) return;
  for (let i = 0; i < 40; i++) {
    const p = document.createElement('div');
    p.className = 'bg-particle';
    p.style.left = Math.random() * 100 + '%';
    p.style.animationDuration = (8 + Math.random() * 15) + 's';
    p.style.animationDelay = Math.random() * 10 + 's';
    p.style.width = p.style.height = (2 + Math.random() * 3) + 'px';
    p.style.opacity = 0.1 + Math.random() * 0.2;
    container.appendChild(p);
  }
}
document.addEventListener('DOMContentLoaded', initParticles);

// ─── API HELPER ────────────────────────────────────────────────
async function api(method, path, body) {
  const opts = { method, headers: { 'Content-Type': 'application/json' } };
  if (body) opts.body = JSON.stringify(body);
  const res = await fetch(path, opts);
  const text = await res.text();
  let json;
  try { json = JSON.parse(text); } catch(e) {
    throw new Error('Server error — make sure Api.py is running (python Api.py)');
  }
  if (!res.ok) throw new Error(json.error || 'Request failed');
  return json;
}

// ─── PAGE NAVIGATION ───────────────────────────────────────────
function showPage(page) {
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  document.getElementById('page-' + page)?.classList.add('active');
  if (page === 'app') initApp();
}

function switchView(view) {
  // Permission check: block unauthorized views
  const roleViews = {
    teacher: ['dashboard', 'live', 'students', 'sessions', 'analytics', 'notes', 'pyq'],
    student: ['dashboard', 'my-attendance', 'live', 'students', 'sessions', 'notes', 'pyq'],
    admin:   ['dashboard', 'students', 'sessions', 'analytics', 'admin']
  };
  const allowed = roleViews[currentUser?.role] || roleViews.teacher;
  if (!allowed.includes(view)) return;

  document.querySelectorAll('#page-app .view').forEach(v => v.classList.remove('active'));
  document.getElementById('view-' + view)?.classList.add('active');
  document.querySelectorAll('.nav-item').forEach(n => {
    n.classList.toggle('active', n.dataset.view === view);
  });
  // Load view data
  const loaders = {
    dashboard: loadDashboard,
    live: loadLiveView,
    'my-attendance': loadMyAttendance,
    students: loadStudentsList,
    sessions: loadSessionsList,
    analytics: loadAnalytics,
    notes: loadNotes,
    pyq: loadPyqs,
    admin: loadAdminData,
  };
  if (loaders[view]) loaders[view]();
  // Stop dashboard polling when navigating away
  if (view !== 'dashboard') stopDashPolling();
  // Admin auto-refresh
  if (view === 'admin') startAdminAutoRefresh();
  else stopAdminAutoRefresh();
  document.getElementById('sidebar')?.classList.remove('open');
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function toggleSidebar() {
  document.getElementById('sidebar')?.classList.toggle('open');
}

// ─── AUTH ──────────────────────────────────────────────────────
function switchAuthTab(tab) {
  document.querySelectorAll('.auth-tab').forEach(t => t.classList.remove('active'));
  event.target.classList.add('active');
  document.getElementById('loginForm').classList.toggle('hidden', tab !== 'login');
  document.getElementById('registerForm').classList.toggle('hidden', tab !== 'register');
}

function selectRole(btn, form) {
  const prefix = form === 'login' ? 'login' : 'reg';
  btn.parentElement.querySelectorAll('.role-btn').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
  document.getElementById(prefix + 'Role').value = btn.dataset.role;
}

async function handleLogin(e) {
  e.preventDefault();
  const name = document.getElementById('loginName').value.trim();
  const pass = document.getElementById('loginPass').value;
  const role = document.getElementById('loginRole').value;
  const errEl = document.getElementById('loginError');
  errEl.classList.add('hidden');

  if (role === 'admin') {
    currentUser = { name: 'Admin', role: 'admin' };
    setupUserUI();
    showPage('app');
    return;
  }

  try {
    const res = await api('POST', '/api/login', { name, password: pass, role });
    currentUser = res.user;
    setupUserUI();
    showPage('app');
  } catch (e) {
    errEl.textContent = e.message;
    errEl.classList.remove('hidden');
  }
}

async function handleRegister(e) {
  e.preventDefault();
  const name = document.getElementById('regName').value.trim();
  const pass = document.getElementById('regPass').value;
  const role = document.getElementById('regRole').value;
  const course = document.getElementById('regCourse').value;
  const year = document.getElementById('regYear').value;
  const branch = document.getElementById('regBranch').value;
  const section = document.getElementById('regSection').value;
  const errEl = document.getElementById('regError');
  errEl.classList.add('hidden');

  try {
    const res = await api('POST', '/api/register', {
      name, password: pass, role, course, year, branch, section
    });
    currentUser = res.user;
    setupUserUI();
    showPage('app');
  } catch (e) {
    errEl.textContent = e.message;
    errEl.classList.remove('hidden');
  }
}

function handleLogout() {
  currentUser = null;
  activeSession = null;
  stopPolling();
  stopDashPolling();
  stopAdminAutoRefresh();
  showPage('landing');
}

function setupUserUI() {
  if (!currentUser) return;
  const initials = currentUser.name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
  document.getElementById('userAvatar').textContent = initials;
  document.getElementById('userName').textContent = currentUser.name;
  const roleEmoji = currentUser.role === 'teacher' ? '👨‍🏫' : currentUser.role === 'student' ? '🎓' : '⚙️';
  document.getElementById('userRole').textContent = roleEmoji + ' ' + currentUser.role;
  applyRolePermissions(currentUser.role);
}

// ─── ROLE-BASED PERMISSIONS ──────────────────────────────────
function applyRolePermissions(role) {
  // Define which views each role can access
  const roleViews = {
    teacher: ['dashboard', 'live', 'students', 'sessions', 'analytics', 'notes', 'pyq'],
    student: ['dashboard', 'my-attendance', 'live', 'students', 'sessions', 'notes', 'pyq'],
    admin:   ['dashboard', 'students', 'sessions', 'analytics', 'admin']
  };
  const allowed = roleViews[role] || roleViews.teacher;

  // Show/hide sidebar nav items based on role
  document.querySelectorAll('.sidebar-nav .nav-item[data-roles], .sidebar-nav .nav-section-label[data-roles]').forEach(el => {
    const roles = (el.dataset.roles || '').split(',');
    el.style.display = roles.includes(role) ? '' : 'none';
  });

  // Hide empty section labels (where all sibling nav-items are hidden)
  document.querySelectorAll('.sidebar-nav .nav-section-label').forEach(label => {
    let nextEl = label.nextElementSibling;
    let hasVisible = false;
    while (nextEl && !nextEl.classList.contains('nav-section-label')) {
      if (nextEl.style.display !== 'none' && nextEl.classList.contains('nav-item')) {
        hasVisible = true;
        break;
      }
      nextEl = nextEl.nextElementSibling;
    }
    label.style.display = hasVisible ? '' : 'none';
  });

  // Hide/show dashboard Start Session button (only teachers can start sessions)
  const dashActions = document.getElementById('dashActions');
  if (dashActions) dashActions.style.display = (role === 'teacher') ? '' : 'none';

  // Hide note upload card for students
  const noteUploadCard = document.getElementById('noteUploadCard');
  if (noteUploadCard) noteUploadCard.style.display = (role === 'student') ? 'none' : '';

  // Hide PYQ upload card for students
  const pyqUploadCard = document.getElementById('pyqUploadCard');
  if (pyqUploadCard) pyqUploadCard.style.display = (role === 'student') ? 'none' : '';

  // Auto-switch to first allowed view
  const activeView = document.querySelector('#page-app .view.active');
  const activeId = activeView ? activeView.id.replace('view-', '') : 'dashboard';
  if (!allowed.includes(activeId)) {
    switchView(allowed[0]);
  }
}

// ─── APP INIT ──────────────────────────────────────────────────
async function initApp() {
  try {
    allDepts = await api('GET', '/api/admin/departments');
    allSections = await api('GET', '/api/admin/sections');
  } catch (e) {}
  loadDashboard();
}

// ─── DASHBOARD ─────────────────────────────────────────────────
async function loadDashboard() {
  try {
    const role = currentUser?.role || 'teacher';

    // ── STUDENT DASHBOARD ──
    if (role === 'student') {
      const h = new Date().getHours();
      const greet = h < 12 ? 'Good Morning ☀️' : h < 17 ? 'Good Afternoon 🌤️' : 'Good Evening 🌙';
      document.getElementById('dashGreeting').textContent = greet + ', ' + (currentUser?.name || 'Student');

      // Hide teacher-only elements
      const liveBanner = document.getElementById('liveBanner');
      if (liveBanner) liveBanner.style.display = 'none';

      // Get student's sessions and attendance
      const sessions = await api('GET', '/api/sessions');
      const mySessions = sessions.filter(s =>
        s.branch === currentUser?.branch && s.section === currentUser?.section
      );

      let present = 0, total = mySessions.length;
      for (const sess of mySessions) {
        try {
          const detail = await api('GET', `/api/sessions/${sess.id}`);
          const me = (detail.students || []).find(s => s.name === currentUser?.name);
          if (me && me.status === 'Present') present++;
        } catch (e) {}
      }
      const pct = total > 0 ? Math.round(present / total * 100) : 0;

      document.getElementById('dashStats').innerHTML = `
        <div class="stat-box" style="animation-delay:0.05s"><div class="stat-icon">📚</div><div class="stat-val">${currentUser?.branch || '—'}-${currentUser?.section || '—'}</div><div class="stat-lbl">My Class</div></div>
        <div class="stat-box" style="animation-delay:0.1s"><div class="stat-icon">📋</div><div class="stat-val">${total}</div><div class="stat-lbl">Total Sessions</div></div>
        <div class="stat-box" style="animation-delay:0.15s"><div class="stat-icon">✅</div><div class="stat-val">${present}</div><div class="stat-lbl">Present</div></div>
        <div class="stat-box" style="animation-delay:0.2s"><div class="stat-icon">📊</div><div class="stat-val">${pct}%</div><div class="stat-lbl">My Attendance</div></div>
      `;

      // Recent sessions for this student
      const recentEl = document.getElementById('recentSessions');
      if (recentEl) {
        const recent = mySessions.slice(0, 5);
        if (!recent.length) {
          recentEl.innerHTML = '<div class="empty-state"><div class="empty-icon">📋</div><strong>No sessions yet for your class</strong></div>';
        } else {
          recentEl.innerHTML = recent.map(s => {
            const d = new Date(s.date);
            const isActive = s.status === 'active';
            return `<div class="list-item">
              <div class="item-icon ${isActive ? 'green' : 'blue'}">${isActive ? '🟢' : '📋'}</div>
              <div class="item-info">
                <strong>${s.subject || ''}</strong>
                <small>${d.toLocaleDateString('en-IN')} • ${s.start_time || ''}</small>
              </div>
              <span class="status-pill ${isActive ? 'active' : 'ended'}">${isActive ? 'Live' : 'Done'}</span>
            </div>`;
          }).join('');
        }
      }

      // Quick links for student
      destroyChart('dashPie');
      destroyChart('dashLine');
      const pieCtx = document.getElementById('dashPieChart');
      if (pieCtx) {
        chartInstances['dashPie'] = new Chart(pieCtx, {
          type: 'doughnut',
          data: {
            labels: ['Present', 'Absent'],
            datasets: [{ data: [present, total - present], backgroundColor: [COLORS.green, COLORS.red], borderWidth: 0, hoverOffset: 8 }]
          },
          options: { responsive: true, maintainAspectRatio: true, aspectRatio: 1.4, cutout: '70%', plugins: { legend: { position: 'bottom', labels: { padding: 16 } } } }
        });
      }

      // Top students section - show classmates for student
      const topEl = document.getElementById('topStudentsDash');
      if (topEl) {
        topEl.innerHTML = `<div class="list-item" onclick="switchView('my-attendance')" style="cursor:pointer;">
          <div class="item-icon green">📊</div>
          <div class="item-info"><strong>View Full Attendance</strong><small>Click to see detailed history</small></div>
          <span class="status-pill active">→</span>
        </div>
        <div class="list-item" onclick="switchView('notes')" style="cursor:pointer;">
          <div class="item-icon blue">📚</div>
          <div class="item-info"><strong>Class Notes</strong><small>Study materials from teachers</small></div>
          <span class="status-pill active">→</span>
        </div>
        <div class="list-item" onclick="switchView('pyq')" style="cursor:pointer;">
          <div class="item-icon purple">📝</div>
          <div class="item-info"><strong>PYQ Papers</strong><small>Previous year question papers</small></div>
          <span class="status-pill active">→</span>
        </div>`;
      }
      return;
    }

    // ── TEACHER & ADMIN DASHBOARD (full) ──
    startDashPolling(); // Auto-poll every 5s when on dashboard
    const [stats, sessions, overview, daily, topPerformers] = await Promise.all([
      api('GET', '/api/stats'),
      api('GET', '/api/sessions'),
      api('GET', '/api/analytics/overview'),
      api('GET', '/api/analytics/daily'),
      api('GET', '/api/analytics/top-performers'),
    ]);

    // Check for active session
    const activeSessions = sessions.filter(s => s.status === 'active' || s.active);
    const liveBanner = document.getElementById('liveBanner');
    const dashLiveFeed = document.getElementById('dashLiveFeed');
    if (liveBanner) {
      if (activeSessions.length > 0) {
        liveBanner.style.display = '';
        document.getElementById('liveBannerText').textContent =
          `${activeSessions[0].subject || 'Session'} — ${activeSessions[0].department || activeSessions[0].branch || ''}-${activeSessions[0].section_name || activeSessions[0].section || ''}`;
        activeSession = activeSessions[0];
        // Show live feed on dashboard
        if (dashLiveFeed) {
          dashLiveFeed.style.display = '';
          loadDashLiveFeed(activeSessions[0].id);
        }
      } else {
        liveBanner.style.display = 'none';
        if (dashLiveFeed) dashLiveFeed.style.display = 'none';
      }
    }

    // Greeting
    const h = new Date().getHours();
    const greet = h < 12 ? 'Good Morning ☀️' : h < 17 ? 'Good Afternoon 🌤️' : 'Good Evening 🌙';
    document.getElementById('dashGreeting').textContent = greet;

    // Stats with animation
    const avgAtt = overview.avg_attendance || 0;
    document.getElementById('dashStats').innerHTML = `
      <div class="stat-box" style="animation-delay:0.05s"><div class="stat-icon">🏛️</div><div class="stat-val">${stats.departments}</div><div class="stat-lbl">Departments</div></div>
      <div class="stat-box" style="animation-delay:0.1s"><div class="stat-icon">🎓</div><div class="stat-val">${stats.students}</div><div class="stat-lbl">Students</div></div>
      <div class="stat-box" style="animation-delay:0.15s"><div class="stat-icon">📋</div><div class="stat-val">${stats.sessions}</div><div class="stat-lbl">Sessions</div></div>
      <div class="stat-box" style="animation-delay:0.2s"><div class="stat-icon">✅</div><div class="stat-val">${avgAtt}%</div><div class="stat-lbl">Avg Attendance</div></div>
    `;

    // Student 75% Bunk Hero on Dashboard
    const studentHero = document.getElementById('dashStudentBunkHero');
    if (studentHero && currentUser?.role === 'student') {
      try {
        const studentName = currentUser.name;
        const branch = currentUser.branch || '';
        const section = currentUser.section || '';
        const ana = await api('GET', `/api/student/analytics?name=${encodeURIComponent(studentName)}&branch=${encodeURIComponent(branch)}&section=${encodeURIComponent(section)}`);
        studentHero.style.display = 'block';
        const isSafe = ana.eligibility_status === 'Safe';
        document.getElementById('dashBunkIcon').textContent = isSafe ? '🎉' : '⚠️';
        document.getElementById('dashBunkTitle').textContent = `Overall Attendance: ${ana.attendance_pct}% (${ana.present_count}/${ana.total_sessions} Classes)`;
        const pill = document.getElementById('dashBunkPill');
        pill.textContent = isSafe ? 'SAFE' : 'SHORTAGE';
        pill.style.background = isSafe ? 'rgba(16,185,129,0.2)' : 'rgba(239,68,68,0.2)';
        pill.style.color = isSafe ? '#10b981' : '#ef4444';
        studentHero.style.borderLeftColor = isSafe ? '#10b981' : '#ef4444';
        document.getElementById('dashBunkText').textContent = ana.bunk_message;
      } catch (err) {
        studentHero.style.display = 'none';
      }
    } else if (studentHero) {
      studentHero.style.display = 'none';
    }


    // Pie chart
    destroyChart('dashPie');
    const pieCtx = document.getElementById('dashPieChart');
    if (pieCtx) {
      chartInstances['dashPie'] = new Chart(pieCtx, {
        type: 'doughnut',
        data: {
          labels: ['Present', 'Absent'],
          datasets: [{ data: [overview.total_present || 0, overview.total_absent || 0], backgroundColor: [COLORS.green, COLORS.red], borderWidth: 0, hoverOffset: 8 }]
        },
        options: {
          responsive: true, maintainAspectRatio: true, aspectRatio: 1.4, cutout: '70%',
          plugins: { legend: { position: 'bottom', labels: { padding: 16 } } }
        }
      });
    }

    // Line chart
    destroyChart('dashLine');
    const lineCtx = document.getElementById('dashLineChart');
    if (lineCtx && daily.length) {
      const labels = daily.map(d => new Date(d.date).toLocaleDateString('en-IN', { day: '2-digit', month: 'short' })).reverse();
      chartInstances['dashLine'] = new Chart(lineCtx, {
        type: 'line',
        data: {
          labels,
          datasets: [
            { label: 'Present', data: daily.map(d => d.present).reverse(), borderColor: COLORS.green, backgroundColor: COLORS.greenA, fill: true, tension: 0.4, pointRadius: 4, pointHoverRadius: 6, borderWidth: 2.5 },
            { label: 'Absent', data: daily.map(d => d.absent).reverse(), borderColor: COLORS.red, backgroundColor: COLORS.redA, fill: true, tension: 0.4, pointRadius: 4, pointHoverRadius: 6, borderWidth: 2.5 }
          ]
        },
        options: {
          responsive: true, maintainAspectRatio: true, aspectRatio: 1.6,
          plugins: { legend: { position: 'top' } },
          scales: { y: { beginAtZero: true, grid: { color: 'rgba(255,255,255,0.04)' } }, x: { grid: { display: false } } }
        }
      });
    }

    // Recent sessions
    const recentEl = document.getElementById('recentSessions');
    const recent = sessions.slice(0, 5);
    if (!recent.length) {
      recentEl.innerHTML = '<div class="empty-state"><div class="empty-icon">📋</div><strong>No sessions yet</strong></div>';
    } else {
      recentEl.innerHTML = recent.map(s => {
        const d = new Date(s.started_at || s.date);
        const isActive = s.status === 'active' || s.active;
        return `<div class="list-item">
          <div class="item-icon ${isActive ? 'green' : 'blue'}">${isActive ? '🟢' : '📋'}</div>
          <div class="item-info">
            <strong>${s.department || s.branch || ''}-${s.section_name || s.section || ''} · ${s.subject || ''}</strong>
            <small>${d.toLocaleDateString('en-IN')} • ${s.present_count || 0}/${s.total_count || 0} present</small>
          </div>
          <span class="status-pill ${isActive ? 'active' : 'ended'}">${isActive ? 'Active' : 'Ended'}</span>
        </div>`;
      }).join('');
    }

    // Top students
    const topEl = document.getElementById('topStudentsDash');
    if (!topPerformers.length) {
      topEl.innerHTML = '<div class="empty-state"><div class="empty-icon">🏆</div><strong>No data yet</strong></div>';
    } else {
      topEl.innerHTML = topPerformers.slice(0, 5).map((s, i) => {
        const rc = i === 0 ? 'amber' : i === 1 ? 'blue' : i === 2 ? 'purple' : 'blue';
        const medal = i === 0 ? '🥇' : i === 1 ? '🥈' : i === 2 ? '🥉' : (i + 1);
        return `<div class="list-item">
          <div class="item-icon ${rc}">${medal}</div>
          <div class="item-info"><strong>${s.student_name}</strong><small>${s.present_count}/${s.total_sessions} sessions</small></div>
          <div style="font:700 18px 'Inter';color:var(--green);">${s.attendance_percent}%</div>
        </div>`;
      }).join('');
    }
  } catch (e) { console.error('Dashboard error:', e); }
}

// ─── MY ATTENDANCE (Student View) ─────────────────────────────
let myAttData = [];

async function loadMyAttendance() {
  if (!currentUser || currentUser.role !== 'student') return;
  try {
    const studentName = currentUser.name;
    const branch = currentUser.branch || '';
    const section = currentUser.section || '';

    document.getElementById('myAttSubtitle').textContent =
      `${branch ? branch + '-' + section + ' • ' : ''}${studentName}`;

    // Fast unified call to student analytics endpoint
    const data = await api('GET', `/api/student/analytics?name=${encodeURIComponent(studentName)}&branch=${encodeURIComponent(branch)}&section=${encodeURIComponent(section)}`);

    const total = data.total_sessions || 0;
    const present = data.present_count || 0;
    const absent = data.absent_count || 0;
    const pct = data.attendance_pct || 0;
    const isSafe = data.eligibility_status === 'Safe';

    // Store for CSV export
    myAttData = (data.history || []).map(h => ({
      subject: h.subject || '',
      date: h.date || '',
      start_time: h.start_time || '',
      branch: h.branch || branch,
      section: h.section || section,
      status: h.status || 'Absent',
      method: h.mode || null,
      marked_at: h.marked_at || null
    }));

    // Stats Row
    document.getElementById('myAttStats').innerHTML = `
      <div class="stat-box"><div class="stat-icon">📋</div><div class="stat-val">${total}</div><div class="stat-lbl">Total Sessions</div></div>
      <div class="stat-box"><div class="stat-icon">✅</div><div class="stat-val" style="color:var(--green);">${present}</div><div class="stat-lbl">Present</div></div>
      <div class="stat-box"><div class="stat-icon">❌</div><div class="stat-val" style="color:var(--red);">${absent}</div><div class="stat-lbl">Absent</div></div>
      <div class="stat-box"><div class="stat-icon">📊</div><div class="stat-val" style="color:${isSafe ? 'var(--green)' : 'var(--red)'};">${pct}%</div><div class="stat-lbl">Overall Attendance</div></div>
    `;

    // ─── 75% Bunk & Eligibility Card ───
    const bunkCard = document.getElementById('bunkCalculatorCard');
    if (bunkCard) {
      bunkCard.style.display = 'block';
      const badge = document.getElementById('bunkStatusBadge');
      const badgeText = document.getElementById('bunkStatusText');
      const metricVal = document.getElementById('bunkMetricVal');
      const metricLbl = document.getElementById('bunkMetricLbl');
      const msgIcon = document.getElementById('bunkMsgIcon');
      const msg = document.getElementById('bunkMessage');
      const currPct = document.getElementById('bunkCurrentPct');
      const barFill = document.getElementById('bunkBarFill');

      if (isSafe) {
        badge.className = 'bunk-status-badge safe';
        badgeText.textContent = 'ELIGIBLE / SAFE';
        metricVal.textContent = data.bunkable_classes || 0;
        metricVal.style.color = '#10b981';
        metricLbl.textContent = 'Upcoming classes you can bunk';
        msgIcon.textContent = '🎉';
        barFill.style.background = 'linear-gradient(90deg, #10b981, #059669)';
      } else {
        badge.className = 'bunk-status-badge warning';
        badgeText.textContent = 'ATTENDANCE SHORTAGE';
        metricVal.textContent = data.needed_classes || 1;
        metricVal.style.color = '#ef4444';
        metricLbl.textContent = 'Classes you must attend consecutively';
        msgIcon.textContent = '⚠️';
        barFill.style.background = 'linear-gradient(90deg, #ef4444, #dc2626)';
      }

      msg.textContent = data.bunk_message || '';
      currPct.textContent = `${pct}%`;
      barFill.style.width = `${Math.min(100, Math.max(0, pct))}%`;
    }

    // ─── Subject-wise Breakdown Grid ───
    const subjects = data.subjects || [];
    const subBadge = document.getElementById('myAttSubCountBadge');
    if (subBadge) subBadge.textContent = `${subjects.length} Subjects`;

    const subGrid = document.getElementById('myAttSubjectGrid');
    if (subGrid) {
      if (!subjects.length) {
        subGrid.innerHTML = '<div class="empty-state"><div class="empty-icon">📖</div><strong>No subject records yet</strong></div>';
      } else {
        subGrid.innerHTML = `<div class="sub-breakdown-grid">` + subjects.map(s => {
          const sSafe = s.percentage >= 75.0;
          return `
            <div class="sub-card ${sSafe ? 'safe' : 'risk'}">
              <div class="sub-card-top">
                <strong>${s.subject}</strong>
                <span class="status-pill ${sSafe ? 'present' : 'absent'}">${sSafe ? 'Safe' : 'At Risk'}</span>
              </div>
              <div class="sub-card-metrics">
                <div class="sub-pct" style="color:${sSafe ? 'var(--green)' : 'var(--red)'};">${s.percentage}%</div>
                <div class="sub-fraction">${s.present} / ${s.total} attended</div>
              </div>
              <div class="sub-track">
                <div class="sub-bar" style="width:${Math.min(100, s.percentage)}%; background:${sSafe ? 'var(--green)' : 'var(--red)'};"></div>
              </div>
            </div>`;
        }).join('') + `</div>`;
      }
    }

    // ─── History List ───
    const listEl = document.getElementById('myAttList');
    if (!myAttData.length) {
      listEl.innerHTML = '<div class="empty-state"><div class="empty-icon">📋</div><strong>No class sessions recorded yet</strong></div>';
    } else {
      listEl.innerHTML = myAttData.map((r, i) => {
        const isPresent = r.status === 'Present';
        const isAuto = r.method === 'Auto';
        return `<div class="list-item" style="animation-delay:${i * 0.03}s">
          <div class="item-icon ${isPresent ? 'green' : 'blue'}" style="font-size:13px;">${isPresent ? '✓' : '✗'}</div>
          <div class="item-info">
            <strong>${r.subject}</strong>
            <small>${r.date} • ${r.start_time} • ${r.branch}-${r.section}</small>
          </div>
          <span class="status-pill ${isPresent ? (isAuto ? 'auto' : 'present') : 'absent'}">
            ${isPresent ? (isAuto ? '⚡ BLE Auto' : '✅ Present') : '❌ Absent'}
          </span>
        </div>`;
      }).join('');
    }

    // ─── Charts ───
    destroyChart('myAttPie');
    const pieCtx = document.getElementById('myAttChart');
    if (pieCtx) {
      chartInstances['myAttPie'] = new Chart(pieCtx, {
        type: 'doughnut',
        data: {
          labels: ['Present', 'Absent'],
          datasets: [{ data: [present, absent], backgroundColor: [COLORS.green, COLORS.red], borderWidth: 0 }]
        },
        options: { responsive: true, maintainAspectRatio: true, aspectRatio: 1.4, cutout: '68%', plugins: { legend: { position: 'bottom' } } }
      });
    }

    destroyChart('myAttSub');
    const subCtx = document.getElementById('myAttSubChart');
    if (subCtx && subjects.length) {
      chartInstances['myAttSub'] = new Chart(subCtx, {
        type: 'bar',
        data: {
          labels: subjects.map(s => s.subject),
          datasets: [
            { label: 'Present', data: subjects.map(s => s.present), backgroundColor: COLORS.green, borderRadius: 6 },
            { label: 'Absent', data: subjects.map(s => s.absent), backgroundColor: COLORS.redA, borderRadius: 6 }
          ]
        },
        options: { responsive: true, maintainAspectRatio: true, aspectRatio: 1.6, plugins: { legend: { position: 'top' } }, scales: { y: { beginAtZero: true, grid: { color: 'rgba(255,255,255,0.04)' } }, x: { grid: { display: false } } } }
      });
    }
  } catch (e) {
    console.error('My Attendance error:', e);
  }
}


function exportMyAttendanceCSV() {
  if (!myAttData.length) { alert('No data to export'); return; }
  let csv = 'Subject,Date,Time,Branch,Section,Status,Method\n';
  myAttData.forEach(r => {
    csv += `"${r.subject}","${r.date}","${r.start_time}","${r.branch}","${r.section}","${r.status}","${r.method || ''}"\n`;
  });
  const blob = new Blob([csv], { type: 'text/csv' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url; a.download = `my_attendance_${currentUser?.name || 'student'}.csv`;
  a.click(); URL.revokeObjectURL(url);
}

// ─── LIVE SESSION VIEW (Show-stopper!) ─────────────────────────
async function loadLiveView() {
  // Find active session
  try {
    const sessions = await api('GET', '/api/sessions');
    const activeSessions = sessions.filter(s => s.status === 'active' || s.active);
    const statusEl = document.getElementById('liveViewStatus');

    if (!activeSessions.length) {
      statusEl.textContent = 'No active session — Start one to begin BLE scanning';
      document.getElementById('liveFeed').innerHTML = `
        <div class="empty-state">
          <div class="empty-icon">📡</div>
          <strong>No active session</strong>
          <p>Start a session from the Teacher App or click "Start Session" to begin</p>
        </div>`;
      document.getElementById('liveRoster').innerHTML = '';
      document.getElementById('liveStats').innerHTML = '';
      return;
    }

    activeSession = activeSessions[0];
    statusEl.textContent = `${activeSession.subject} — ${activeSession.department || activeSession.branch}-${activeSession.section_name || activeSession.section} • LIVE`;

    // Get session details
    const sessionData = await api('GET', `/api/sessions/${activeSession.id}`);
    renderLiveStats(sessionData);
    renderLiveFeed(sessionData);
    renderLiveRoster(sessionData);
    startLivePolling();
  } catch (e) {
    console.error('Live view error:', e);
  }
}

function renderLiveStats(session) {
  const students = session.students || [];
  const present = students.filter(s => s.status === 'Present');
  const auto = present.filter(s => s.method === 'Auto');
  const manual = present.filter(s => s.method === 'Manual');
  const absent = students.filter(s => s.status === 'Absent');
  const pct = students.length > 0 ? Math.round(present.length / students.length * 100) : 0;

  document.getElementById('liveStats').innerHTML = `
    <div class="live-stat-box"><div class="ls-icon">📡</div><div class="ls-val" style="color:var(--green)">${present.length}</div><div class="ls-lbl">Present</div></div>
    <div class="live-stat-box"><div class="ls-icon">⚡</div><div class="ls-val" style="color:var(--blue)">${auto.length}</div><div class="ls-lbl">BLE Auto</div></div>
    <div class="live-stat-box"><div class="ls-icon">✋</div><div class="ls-val" style="color:var(--purple)">${manual.length}</div><div class="ls-lbl">Manual</div></div>
    <div class="live-stat-box"><div class="ls-icon">📊</div><div class="ls-val">${pct}%</div><div class="ls-lbl">Attendance</div></div>
  `;

  document.getElementById('livePresentCount').textContent = `${present.length} Present`;
}

function renderLiveFeed(session) {
  const students = session.students || [];
  const present = students.filter(s => s.status === 'Present').sort((a, b) => (b.marked_at || '').localeCompare(a.marked_at || ''));

  if (!present.length) {
    document.getElementById('liveFeed').innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">📡</div>
        <strong>Waiting for BLE detections...</strong>
        <p>Students will appear here in real-time as they are detected</p>
      </div>`;
    return;
  }

  document.getElementById('liveFeed').innerHTML = present.map((s, i) => {
    const isAuto = s.method === 'Auto';
    return `<div class="live-feed-item" style="animation-delay:${i * 0.05}s">
      <div class="feed-dot"></div>
      <div class="feed-info">
        <strong>${s.name}</strong>
        <small>${isAuto ? 'BLE detected' : 'Manual mark'} • ${s.marked_at || ''}</small>
      </div>
      <span class="feed-badge ${isAuto ? '' : 'manual'}">${isAuto ? '⚡ BLE' : '✋ Manual'}</span>
    </div>`;
  }).join('');
}

function renderLiveRoster(session) {
  const students = session.students || [];
  if (!students.length) {
    document.getElementById('liveRoster').innerHTML = '<div class="empty-state"><div class="empty-icon">📋</div><strong>No students in roster</strong></div>';
    return;
  }

  document.getElementById('liveRoster').innerHTML = students.map((s, i) => {
    const isPresent = s.status === 'Present';
    const isAuto = s.method === 'Auto';
    return `<div class="list-item" style="animation-delay:${i * 0.03}s">
      <div class="item-icon ${isPresent ? 'green' : 'blue'}" style="font-size:13px;">${isPresent ? '✓' : (i + 1)}</div>
      <div class="item-info">
        <strong>${s.name}</strong>
        <small>${s.roll || ''} • ${s.marked_at || 'Not marked'}</small>
      </div>
      <span class="status-pill ${isPresent ? (isAuto ? 'auto' : 'present') : 'absent'}">
        ${isPresent ? (isAuto ? '⚡ BLE' : '✅ Present') : '⚪ Absent'}
      </span>
    </div>`;
  }).join('');
}

// ─── LIVE POLLING ──────────────────────────────────────────────
function startLivePolling() {
  stopPolling();
  pollTimer = setInterval(async () => {
    if (!activeSession) return;
    try {
      const sessionData = await api('GET', `/api/sessions/${activeSession.id}`);

      // Session ended from the phone → stop polling and say so
      if (sessionData.status === 'ended') {
        stopPolling();
        document.getElementById('liveViewStatus').textContent = 'Session ended — start a new one to begin again';
        const bleStatusBadgeText = document.getElementById('bleStatusTextBadge');
        if (bleStatusBadgeText) bleStatusBadgeText.textContent = 'ENDED';
        document.getElementById('bleStatusBadge')?.classList.remove('scanning');
        return;
      }

      const prevPresent = document.getElementById('livePresentCount')?.textContent || '0 Present';
      renderLiveStats(sessionData);
      renderLiveFeed(sessionData);
      renderLiveRoster(sessionData);

      // Check for new detections (animate radar)
      const currentPresent = (sessionData.students || []).filter(s => s.status === 'Present').length;
      const prevCount = parseInt(prevPresent) || 0;
      if (currentPresent > prevCount) {
        addRadarDot();
        pulseRadarCenter();
      }
    } catch (e) { console.error('Poll error:', e); }
  }, 3000); // Poll every 3 seconds
}

function stopPolling() {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null; }
}

// ─── DASHBOARD AUTO-POLL ──────────────────────────────────────
let dashPollTimer = null;

function startDashPolling() {
  stopDashPolling();
  dashPollTimer = setInterval(async () => {
    if (!currentUser || currentUser.role === 'student') return;
    try {
      const sessions = await api('GET', '/api/sessions');
      const activeSessions = sessions.filter(s => s.status === 'active' || s.active);
      const liveBanner = document.getElementById('liveBanner');
      if (!liveBanner) return;

      const dashLiveFeed = document.getElementById('dashLiveFeed');
      if (activeSessions.length > 0) {
        liveBanner.style.display = '';
        document.getElementById('liveBannerText').textContent =
          `${activeSessions[0].subject || 'Session'} — ${activeSessions[0].department || activeSessions[0].branch || ''}-${activeSessions[0].section_name || activeSessions[0].section || ''}`;
        activeSession = activeSessions[0];
        // Show and refresh live feed
        if (dashLiveFeed) {
          dashLiveFeed.style.display = '';
          loadDashLiveFeed(activeSessions[0].id);
        }
      } else {
        liveBanner.style.display = 'none';
        if (dashLiveFeed) dashLiveFeed.style.display = 'none';
      }
    } catch (e) { /* silent */ }
  }, 5000); // Poll every 5 seconds
}

function stopDashPolling() {
  if (dashPollTimer) { clearInterval(dashPollTimer); dashPollTimer = null; }
}

// ─── DASHBOARD LIVE FEED ──────────────────────────────────────
async function loadDashLiveFeed(sessionId) {
  try {
    const session = await api('GET', `/api/sessions/${sessionId}`);
    const students = session.students || [];
    const present = students.filter(s => s.status === 'Present');
    const auto = present.filter(s => s.method === 'Auto');
    const total = students.length;
    const pct = total > 0 ? Math.round(present.length / total * 100) : 0;

    // Update count badge
    const countEl = document.getElementById('dashLiveCount');
    if (countEl) countEl.textContent = `${present.length} Present`;

    // Update live feed list
    const feedEl = document.getElementById('dashLiveFeedList');
    if (!feedEl) return;

    if (!present.length) {
      feedEl.innerHTML = `
        <div class="empty-state">
          <div class="empty-icon">📡</div>
          <strong>Waiting for BLE detections...</strong>
          <p>Students will appear here in real-time as they are detected</p>
        </div>`;
      return;
    }

    // Sort by most recently marked
    const sorted = present.sort((a, b) => (b.marked_at || '').localeCompare(a.marked_at || ''));

    feedEl.innerHTML = sorted.map((s, i) => {
      const isAuto = s.method === 'Auto';
      return `<div class="live-feed-item" style="animation-delay:${i * 0.04}s">
        <div class="feed-dot"></div>
        <div class="feed-info">
          <strong>${s.name}</strong>
          <small>${isAuto ? 'BLE detected' : 'Manual mark'} • ${s.marked_at || ''}</small>
        </div>
        <span class="feed-badge ${isAuto ? '' : 'manual'}">${isAuto ? '⚡ BLE' : '✋ Manual'}</span>
      </div>`;
    }).join('');

    // Also update dashboard stats live
    const statsEl = document.getElementById('dashStats');
    if (statsEl) {
      const stats = await api('GET', '/api/stats');
      const overview = await api('GET', '/api/analytics/overview');
      const avgAtt = overview.avg_attendance || 0;
      statsEl.innerHTML = `
        <div class="stat-box" style="animation-delay:0.05s"><div class="stat-icon">📡</div><div class="stat-val" style="color:var(--green)">${present.length}</div><div class="stat-lbl">Present Now</div></div>
        <div class="stat-box" style="animation-delay:0.1s"><div class="stat-icon">⚡</div><div class="stat-val" style="color:var(--blue)">${auto.length}</div><div class="stat-lbl">BLE Auto</div></div>
        <div class="stat-box" style="animation-delay:0.15s"><div class="stat-icon">📊</div><div class="stat-val">${pct}%</div><div class="stat-lbl">Attendance</div></div>
        <div class="stat-box" style="animation-delay:0.2s"><div class="stat-icon">🎓</div><div class="stat-val">${total}</div><div class="stat-lbl">Total Students</div></div>
      `;
    }
  } catch (e) { console.error('Dashboard live feed error:', e); }
}

// ─── BLE RADAR ─────────────────────────────────────────────────
function addRadarDot() {
  const container = document.getElementById('bleRadar');
  const dotsContainer = document.getElementById('radarDots');
  if (!container || !dotsContainer) return;

  const angle = Math.random() * Math.PI * 2;
  const radius = 40 + Math.random() * 80;
  const x = 140 + Math.cos(angle) * radius;
  const y = 140 + Math.sin(angle) * radius;

  const dot = document.createElement('div');
  dot.className = 'radar-dot';
  dot.style.left = (x - 5) + 'px';
  dot.style.top = (y - 5) + 'px';
  dotsContainer.appendChild(dot);

  // Update device count
  const countEl = document.getElementById('deviceCountNum');
  if (countEl) {
    const current = parseInt(countEl.textContent) || 0;
    countEl.textContent = current + 1;
  }

  // Auto-remove after 8 seconds
  setTimeout(() => {
    if (dot.parentNode) {
      dot.style.opacity = '0';
      dot.style.transition = 'opacity 0.5s';
      setTimeout(() => dot.remove(), 500);
    }
  }, 8000);
}

function pulseRadarCenter() {
  const center = document.querySelector('.radar-center');
  if (center) {
    center.style.boxShadow = '0 0 40px rgba(16,185,129,0.5)';
    setTimeout(() => { center.style.boxShadow = '0 0 20px rgba(59,130,246,0.2)'; }, 600);
  }
}

async function toggleBLE() {
  const radarContainer = document.getElementById('bleRadar');
  const badge = document.getElementById('bleStatusBadge');
  const statusText = document.getElementById('bleStatusText');
  const statusBadge = document.getElementById('bleStatusTextBadge');
  const toggleBtn = document.getElementById('bleToggleBtn');

  if (bleRunning) {
    try { await api('POST', '/api/ble/stop'); } catch (e) {}
    bleRunning = false;
    radarContainer?.classList.remove('scanning');
    badge?.classList.remove('scanning');
    toggleBtn.textContent = '▶ Start Scan';
    statusText.textContent = 'Scanner stopped';
    statusBadge.textContent = 'IDLE';
  } else {
    try {
      await api('POST', '/api/ble/start', { session_id: activeSession?.id });
      bleRunning = true;
      radarContainer?.classList.add('scanning');
      badge?.classList.add('scanning');
      toggleBtn.textContent = '⏹ Stop Scan';
      statusText.textContent = 'Scanning for BLE devices...';
      statusBadge.textContent = 'SCANNING';
    } catch (e) {
      statusText.textContent = 'Error: ' + e.message;
    }
  }
}

function exportCSV() {
  if (!activeSession) { alert('No active session'); return; }
  api('GET', `/api/sessions/${activeSession.id}`).then(session => {
    const students = session.students || [];
    let csv = 'Name,Roll,Status,Method,Marked At\n';
    students.forEach(s => {
      csv += `"${s.name}","${s.roll || ''}","${s.status}","${s.method || ''}","${s.marked_at || ''}"\n`;
    });
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = `attendance_${session.subject || 'session'}.csv`;
    a.click(); URL.revokeObjectURL(url);
  });
}

// ─── STUDENTS LIST ─────────────────────────────────────────────
async function loadStudentsList() {
  try {
    // Fetch from both sources: students table (admin-added/attendance) AND users table (app-registered)
    const [students, registeredUsers] = await Promise.all([
      api('GET', '/api/admin/students').catch(() => []),
      api('GET', '/api/admin/users?role=student').catch(() => [])
    ]);

    // Merge: students table is primary, add any app-registered students not already present
    const allStudents = [...students];
    const existingNames = new Set(students.map(s => s.name.toLowerCase()));
    for (const u of registeredUsers) {
      if (!existingNames.has(u.name.toLowerCase())) {
        allStudents.push({
          name: u.name,
          department: u.branch || u.course || '—',
          section: u.section || '—',
          roll: 'Registered via App',
          ble_address: null,
          source: 'app'
        });
      }
    }

    const el = document.getElementById('studentsList');
    const countBadge = document.getElementById('studentCountBadge');
    if (countBadge) countBadge.textContent = allStudents.length;

    if (!allStudents.length) {
      el.innerHTML = '<div class="empty-state"><div class="empty-icon">🎓</div><strong>No students in database</strong><p>Add students from Admin panel or register via Android app</p></div>';
      return;
    }
    el.innerHTML = allStudents.map(s => {
      const initials = s.name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
      const isApp = s.source === 'app';
      return `<div class="list-item">
        <div class="item-icon ${isApp ? 'green' : 'blue'}">${initials}</div>
        <div class="item-info">
          <strong>${s.name}</strong>
          <small>${s.department || '—'} • ${s.section || '—'} • ${s.roll || 'No Roll'}</small>
        </div>
        ${isApp ? '<span class="status-pill auto">📱 App</span>' : ''}
        ${s.ble_address ? `<span class="status-pill auto">⚡ ${s.ble_address}</span>` : ''}
      </div>`;
    }).join('');
  } catch (e) { console.error(e); }
}

// ─── SESSIONS LIST ─────────────────────────────────────────────
async function loadSessionsList() {
  try {
    const sessions = await api('GET', '/api/sessions');
    const el = document.getElementById('sessionsList');
    if (!sessions.length) {
      el.innerHTML = '<div class="empty-state"><div class="empty-icon">📋</div><strong>No sessions yet</strong></div>';
      return;
    }
    el.innerHTML = sessions.map(s => {
      const d = new Date(s.started_at || s.date);
      const isActive = s.status === 'active' || s.active;
      return `<div class="list-item">
        <div class="item-icon ${isActive ? 'green' : 'blue'}">${isActive ? '🟢' : '📋'}</div>
        <div class="item-info">
          <strong>${s.department || s.branch || ''}-${s.section_name || s.section || ''} · ${s.subject || ''}</strong>
          <small>${d.toLocaleDateString('en-IN')} • ${s.present_count || 0}/${s.total_count || 0} present • ${s.start_time || ''}</small>
        </div>
        <div style="display:flex;align-items:center;gap:6px;">
          <span class="status-pill ${isActive ? 'active' : 'ended'}">${isActive ? 'Active' : 'Ended'}</span>
          <button class="btn-x" onclick="deleteSession(${s.id}, ${isActive})" title="Delete session" style="font-size:12px; padding:4px 8px;">🗑</button>
        </div>
      </div>`;
    }).join('');
  } catch (e) { console.error(e); }
}

async function deleteSession(id, isActive) {
  var msg = isActive
    ? 'This session is LIVE. End and delete it along with all attendance records?'
    : 'Delete this session and all its attendance records?';
  if (!confirm(msg)) return;
  try {
    // If active, end it first
    if (isActive) {
      await api('POST', '/api/sessions/' + id + '/end');
    }
    // Then delete via POST (avoids CORS issues with DELETE method)
    var res = await api('POST', '/api/sessions/' + id + '/delete');
    loadSessionsList();
    loadDashboard();
  } catch (e) {
    alert('Failed to delete: ' + e.message);
  }
}

// ─── ANALYTICS ─────────────────────────────────────────────────
async function loadAnalytics() {
  try {
    const [overview, daily, subjects, depts, bleData, weekly, monthly, topPerformers] = await Promise.all([
      api('GET', '/api/analytics/overview'),
      api('GET', '/api/analytics/daily'),
      api('GET', '/api/analytics/subjects'),
      api('GET', '/api/analytics/departments'),
      api('GET', '/api/analytics/ble-vs-manual'),
      api('GET', '/api/analytics/weekly-pattern'),
      api('GET', '/api/analytics/monthly'),
      api('GET', '/api/analytics/top-performers'),
    ]);

    document.getElementById('analyticsStats').innerHTML = `
      <div class="stat-box"><div class="stat-icon">📊</div><div class="stat-val">${overview.total_sessions}</div><div class="stat-lbl">Sessions</div></div>
      <div class="stat-box"><div class="stat-icon">✅</div><div class="stat-val">${overview.avg_attendance}%</div><div class="stat-lbl">Avg Attendance</div></div>
      <div class="stat-box"><div class="stat-icon">⚡</div><div class="stat-val">${overview.total_auto}</div><div class="stat-lbl">BLE Auto</div></div>
      <div class="stat-box"><div class="stat-icon">✋</div><div class="stat-val">${overview.total_manual}</div><div class="stat-lbl">Manual</div></div>
    `;

    // Pie
    destroyChart('anaPie');
    const pieCtx = document.getElementById('anaPieChart');
    if (pieCtx) chartInstances['anaPie'] = new Chart(pieCtx, {
      type: 'doughnut', data: { labels: ['Present', 'Absent'], datasets: [{ data: [overview.total_present || 0, overview.total_absent || 0], backgroundColor: [COLORS.green, COLORS.red], borderWidth: 0 }] },
      options: { responsive: true, maintainAspectRatio: true, aspectRatio: 1.3, cutout: '68%', plugins: { legend: { position: 'bottom' } } }
    });

    // BLE
    destroyChart('anaBle');
    const bleCtx = document.getElementById('anaBleChart');
    if (bleCtx) chartInstances['anaBle'] = new Chart(bleCtx, {
      type: 'doughnut', data: { labels: ['BLE Auto', 'Manual'], datasets: [{ data: [bleData.auto || 0, bleData.manual || 0], backgroundColor: [COLORS.blue, COLORS.purple], borderWidth: 0 }] },
      options: { responsive: true, maintainAspectRatio: true, aspectRatio: 1.3, cutout: '68%', plugins: { legend: { position: 'bottom' } } }
    });

    // Subject bar
    destroyChart('anaSub');
    const subCtx = document.getElementById('anaSubChart');
    if (subCtx && subjects.length) chartInstances['anaSub'] = new Chart(subCtx, {
      type: 'bar', data: { labels: subjects.map(d => d.subject), datasets: [{ label: 'Present', data: subjects.map(d => d.present), backgroundColor: COLORS.green, borderRadius: 6 }, { label: 'Absent', data: subjects.map(d => d.absent), backgroundColor: COLORS.redA, borderRadius: 6 }] },
      options: { responsive: true, maintainAspectRatio: true, aspectRatio: 1.6, plugins: { legend: { position: 'top' } }, scales: { y: { beginAtZero: true, grid: { color: 'rgba(255,255,255,0.04)' } }, x: { grid: { display: false } } } }
    });

    // Dept horizontal bar
    destroyChart('anaDept');
    const deptCtx = document.getElementById('anaDeptChart');
    if (deptCtx && depts.length) chartInstances['anaDept'] = new Chart(deptCtx, {
      type: 'bar', data: { labels: depts.map(d => d.department), datasets: [{ label: 'Present', data: depts.map(d => d.present), backgroundColor: COLORS.blue, borderRadius: 6 }, { label: 'Absent', data: depts.map(d => d.absent), backgroundColor: COLORS.redA, borderRadius: 6 }] },
      options: { indexAxis: 'y', responsive: true, maintainAspectRatio: true, aspectRatio: 1.6, plugins: { legend: { position: 'top' } }, scales: { x: { beginAtZero: true, grid: { color: 'rgba(255,255,255,0.04)' } }, y: { grid: { display: false } } } }
    });

    // Weekly radar
    destroyChart('anaWeek');
    const weekCtx = document.getElementById('anaWeekChart');
    if (weekCtx && weekly.length) chartInstances['anaWeek'] = new Chart(weekCtx, {
      type: 'radar', data: { labels: weekly.map(d => d.day_name), datasets: [{ label: 'Present', data: weekly.map(d => d.present), borderColor: COLORS.green, backgroundColor: COLORS.greenA, pointRadius: 4, borderWidth: 2 }, { label: 'Absent', data: weekly.map(d => d.absent), borderColor: COLORS.red, backgroundColor: COLORS.redA, pointRadius: 4, borderWidth: 2 }] },
      options: { responsive: true, maintainAspectRatio: true, aspectRatio: 1.3, plugins: { legend: { position: 'bottom' } }, scales: { r: { beginAtZero: true, grid: { color: 'rgba(255,255,255,0.05)' } } } }
    });

    // Monthly bar
    destroyChart('anaMonth');
    const monthCtx = document.getElementById('anaMonthChart');
    if (monthCtx && monthly.length) chartInstances['anaMonth'] = new Chart(monthCtx, {
      type: 'bar', data: { labels: monthly.map(d => d.month), datasets: [{ label: 'Present', data: monthly.map(d => d.present), backgroundColor: COLORS.blue, borderRadius: 8 }, { label: 'Absent', data: monthly.map(d => d.absent), backgroundColor: COLORS.amberA, borderRadius: 8 }] },
      options: { responsive: true, maintainAspectRatio: true, aspectRatio: 2, plugins: { legend: { position: 'top' } }, scales: { y: { beginAtZero: true, grid: { color: 'rgba(255,255,255,0.04)' } }, x: { grid: { display: false } } } }
    });

    // Top performers
    const topEl = document.getElementById('topPerformersAna');
    if (!topPerformers.length) {
      topEl.innerHTML = '<div class="empty-state"><div class="empty-icon">🏆</div><strong>No data yet</strong></div>';
    } else {
      topEl.innerHTML = topPerformers.map((s, i) => {
        const rc = i === 0 ? 'amber' : i === 1 ? 'blue' : i === 2 ? 'purple' : 'blue';
        const medal = i === 0 ? '🥇' : i === 1 ? '🥈' : i === 2 ? '🥉' : (i + 1);
        return `<div class="list-item">
          <div class="item-icon ${rc}">${medal}</div>
          <div class="item-info"><strong>${s.student_name}</strong><small>${s.present_count}/${s.total_sessions} sessions</small></div>
          <div style="font:700 16px 'Inter';color:var(--green);">${s.attendance_percent}%</div>
        </div>`;
      }).join('');
    }
  } catch (e) { console.error('Analytics error:', e); }
}

function destroyChart(id) {
  if (chartInstances[id]) { chartInstances[id].destroy(); delete chartInstances[id]; }
}

// ─── NOTES ─────────────────────────────────────────────────────
async function loadNotes() {
  try {
    const notes = await api('GET', '/api/notes');
    const el = document.getElementById('notesList');
    document.getElementById('notesCount').textContent = notes.length + ' notes';
    if (!notes.length) {
      el.innerHTML = '<div class="empty-state"><div class="empty-icon">📚</div><strong>No notes yet</strong></div>';
      return;
    }
    el.innerHTML = notes.map(n => {
      const hasFile = !!n.file_name;
      const ext = hasFile ? n.file_name.split('.').pop().toLowerCase() : '';
      const iconMap = { pdf: '📕', doc: '📘', docx: '📘', ppt: '📊', pptx: '📊', txt: '📄', png: '🖼️', jpg: '🖼️', jpeg: '🖼️' };
      const iconColor = { pdf: 'red', doc: 'blue', docx: 'blue', ppt: 'purple', pptx: 'purple', txt: 'green', png: 'amber', jpg: 'amber', jpeg: 'amber' };
      return `<div class="list-item">
        <div class="item-icon ${iconColor[ext] || 'blue'}">${iconMap[ext] || '📝'}</div>
        <div class="item-info">
          <strong>${n.title}</strong>
          <small>${n.subject} • ${n.branch}-${n.section} • ${n.created_at || ''}</small>
        </div>
        <div class="item-actions">
          ${hasFile ? `<a href="/api/notes/download/${n.id}" class="btn-sm" style="color:var(--blue);">⇩</a>` : ''}
          ${currentUser?.role !== 'student' ? `<button class="btn-x" onclick="deleteNote(${n.id})" style="font-size:11px;">✕</button>` : ''}
        </div>
      </div>`;
    }).join('');
  } catch (e) { console.error(e); }
}

async function uploadNote() {
  const title = document.getElementById('noteTitle').value.trim();
  const subject = document.getElementById('noteSubject').value.trim();
  const branch = document.getElementById('noteBranch').value.trim();
  const section = document.getElementById('noteSection').value.trim();
  const content = document.getElementById('noteContent').value.trim();
  const btn = document.querySelector('#view-notes .btn-glow');

  if (!title || !subject || !branch || !section) {
    alert('Please fill Title, Subject, Department, and Section.');
    return;
  }

  if (btn) { btn.disabled = true; btn.textContent = 'Uploading...'; }

  try {
    if (selectedFile) {
      const fd = new FormData();
      fd.append('teacher_name', currentUser?.name || 'admin');
      fd.append('branch', branch);
      fd.append('section', section);
      fd.append('subject', subject);
      fd.append('title', title);
      fd.append('file', selectedFile);
      const res = await fetch('/api/notes/upload', { method: 'POST', body: fd });
      const json = await res.json();
      if (!res.ok) throw new Error(json.error || 'Upload failed');
      alert('✅ Note uploaded with file: ' + json.file_name);
    } else {
      await api('POST', '/api/notes', {
        teacher_name: currentUser?.name || 'admin', branch, section, subject, title, content
      });
      alert('✅ Note added successfully!');
    }

    ['noteTitle', 'noteSubject', 'noteBranch', 'noteSection', 'noteContent'].forEach(id => {
      document.getElementById(id).value = '';
    });
    clearFile();
    loadNotes();
  } catch (e) {
    alert('❌ Error: ' + e.message);
  } finally {
    if (btn) { btn.disabled = false; btn.textContent = 'Upload Note →'; }
  }
}

async function deleteNote(id) {
  if (!confirm('Delete this note?')) return;
  await api('DELETE', `/api/notes/${id}`);
  loadNotes();
}

// ─── PYQ ───────────────────────────────────────────────────────
async function loadPyqs(semester) {
  try {
    const sem = semester || currentPyqSem || '';
    const url = sem ? `/api/pyqs?semester=${encodeURIComponent(sem)}` : '/api/pyqs';
    const pyqs = await api('GET', url);
    const el = document.getElementById('pyqList');
    document.getElementById('pyqCount').textContent = pyqs.length + ' papers';
    if (!pyqs.length) {
      el.innerHTML = `<div class="empty-state"><div class="empty-icon">📝</div><strong>No PYQs ${sem ? 'for ' + sem : 'yet'}</strong><p>Upload your first PYQ paper above</p></div>`;
      return;
    }
    el.innerHTML = pyqs.map(p => {
      const hasFile = !!p.file_name;
      const ext = hasFile ? p.file_name.split('.').pop().toLowerCase() : '';
      const iconMap = { pdf: '📕', doc: '📘', docx: '📘', ppt: '📊', pptx: '📊', txt: '📄' };
      const iconColor = { pdf: 'red', doc: 'blue', docx: 'blue', ppt: 'purple', pptx: 'purple', txt: 'green' };
      return `<div class="list-item">
        <div class="item-icon ${iconColor[ext] || 'amber'}">${iconMap[ext] || '📝'}</div>
        <div class="item-info">
          <strong>${p.title}</strong>
          <small>${p.subject} • ${p.branch}${p.semester ? ' • ' + p.semester : ''}${p.year ? ' • ' + p.year : ''}${p.exam_type ? ' • ' + p.exam_type : ''}</small>
        </div>
        <div class="item-actions">
          ${hasFile ? `<a href="/api/pyqs/download/${p.id}" class="btn-sm" style="color:var(--blue);">⇩</a>` : ''}
          ${currentUser?.role !== 'student' ? `<button class="btn-x" onclick="deletePyq(${p.id})" style="font-size:11px;">✕</button>` : ''}
        </div>
      </div>`;
    }).join('');
  } catch (e) { console.error(e); }
}

function filterPyqSem(sem) {
  currentPyqSem = sem;
  document.querySelectorAll('#semesterFilters .sem-card').forEach(c => {
    c.classList.toggle('active', c.textContent.trim().includes(sem || 'All'));
  });
  loadPyqs(sem);
}

async function uploadPyq() {
  const title = document.getElementById('pyqTitle').value.trim();
  const subject = document.getElementById('pyqSubject').value.trim();
  const branch = document.getElementById('pyqBranch').value;
  const semester = document.getElementById('pyqSemester').value;
  const year = document.getElementById('pyqYear').value;
  const examType = document.getElementById('pyqExamType').value;
  const content = document.getElementById('pyqContent').value.trim();
  const btn = document.querySelector('#view-pyq .btn-glow');

  if (!title || !subject || !branch) { alert('Please fill Title, Subject, and Department.'); return; }
  if (btn) { btn.disabled = true; btn.textContent = 'Uploading...'; }

  try {
    if (selectedPyqFile) {
      const fd = new FormData();
      fd.append('teacher_name', currentUser?.name || 'admin');
      fd.append('branch', branch);
      fd.append('subject', subject);
      fd.append('title', title);
      fd.append('semester', semester);
      fd.append('year', year);
      fd.append('exam_type', examType);
      fd.append('file', selectedPyqFile);
      const res = await fetch('/api/pyqs/upload', { method: 'POST', body: fd });
      const json = await res.json();
      if (!res.ok) throw new Error(json.error || 'Upload failed');
      alert('✅ PYQ uploaded: ' + json.file_name);
    } else {
      await api('POST', '/api/pyqs', {
        teacher_name: currentUser?.name || 'admin', branch, subject, title, semester, year, exam_type: examType, content
      });
      alert('✅ PYQ added!');
    }

    ['pyqTitle', 'pyqSubject', 'pyqContent'].forEach(id => document.getElementById(id).value = '');
    ['pyqBranch', 'pyqSemester', 'pyqYear', 'pyqExamType'].forEach(id => document.getElementById(id).value = '');
    clearPyqFile();
    loadPyqs();
  } catch (e) {
    alert('❌ Error: ' + e.message);
  } finally {
    if (btn) { btn.disabled = false; btn.textContent = 'Upload PYQ →'; }
  }
}

async function deletePyq(id) {
  if (!confirm('Delete this PYQ?')) return;
  await api('DELETE', `/api/pyqs/${id}`);
  loadPyqs();
}

function clearPyqFile() {
  selectedPyqFile = null;
  const fileInput = document.getElementById('pyqFile');
  if (fileInput) fileInput.value = '';
  const preview = document.getElementById('pyqFilePreview');
  const content = document.getElementById('pyqFileContent');
  if (preview) preview.classList.add('hidden');
  if (content) content.style.display = '';
}

function clearFile() {
  selectedFile = null;
  const fileInput = document.getElementById('noteFile');
  if (fileInput) fileInput.value = '';
  const preview = document.getElementById('fileDropPreview');
  const content = document.getElementById('fileDropContent');
  if (preview) preview.classList.add('hidden');
  if (content) content.style.display = '';
}

// ─── ADMIN ─────────────────────────────────────────────────────
let adminAutoRefresh = null;

function switchAdminTab(tabId, btn) {
  document.querySelectorAll('.admin-panel').forEach(p => p.classList.add('hidden'));
  document.getElementById(tabId)?.classList.remove('hidden');
  document.querySelectorAll('.atab').forEach(b => b.classList.remove('active'));
  btn?.classList.add('active');
  if (tabId === 'adminDepts') loadAdminData();
  if (tabId === 'adminUsers') loadUserTable();
}

function startAdminAutoRefresh() {
  stopAdminAutoRefresh();
  adminAutoRefresh = setInterval(() => {
    // Silently refresh admin data in background
    loadAdminData().catch(() => {});
    loadUserTable().catch(() => {});
  }, 10000); // Every 10 seconds
}

function stopAdminAutoRefresh() {
  if (adminAutoRefresh) { clearInterval(adminAutoRefresh); adminAutoRefresh = null; }
}

async function loadAdminData() {
  try {
    allDepts = await api('GET', '/api/admin/departments');
    allSections = await api('GET', '/api/admin/sections');
    renderDepts(); renderSections(); fillDeptDropdowns(); loadStuTable();
    // Also load users if on users tab
    const usersPanel = document.getElementById('adminUsers');
    if (usersPanel && !usersPanel.classList.contains('hidden')) {
      loadUserTable();
    }
  } catch (e) {}
}

function refreshAdminData() {
  loadAdminData();
  loadUserTable();
  loadStudentsList();
  // Visual feedback
  const btn = event?.target;
  if (btn) {
    btn.textContent = '✓ Refreshed';
    setTimeout(() => { btn.textContent = '↻ Refresh'; }, 1500);
  }
}

function renderDepts() {
  const el = document.getElementById('deptList');
  if (!allDepts.length) { el.innerHTML = '<div class="empty-state"><div class="empty-icon">🏛️</div><strong>No departments</strong></div>'; return; }
  el.innerHTML = allDepts.map(d => `<div class="list-item">
    <div class="item-icon blue">${d.name[0]}</div>
    <div class="item-info"><strong>${d.name}</strong><small>Sections: ${d.sections || '—'} • ${d.student_count || 0} students</small></div>
    <button class="btn-x" onclick="delDept(${d.id})">✕</button>
  </div>`).join('');
}

async function addDept() {
  const name = document.getElementById('newDeptName').value.trim();
  if (!name) return alert('Enter name');
  await api('POST', '/api/admin/departments', { name });
  document.getElementById('newDeptName').value = '';
  loadAdminData();
}

async function delDept(id) { if (!confirm('Delete?')) return; await api('DELETE', `/api/admin/departments/${id}`); loadAdminData(); }

function renderSections() {
  const el = document.getElementById('secList');
  if (!allSections.length) { el.innerHTML = '<div class="empty-state"><div class="empty-icon">📑</div><strong>No sections</strong></div>'; return; }
  el.innerHTML = allSections.map(s => `<div class="list-item">
    <div class="item-icon purple">${s.dept_name[0]}</div>
    <div class="item-info"><strong>${s.dept_name} - ${s.name}</strong><small>Section ${s.name}</small></div>
    <button class="btn-x" onclick="delSec(${s.id})">✕</button>
  </div>`).join('');
}

async function addSec() {
  const deptId = document.getElementById('secDeptSelect').value;
  const name = document.getElementById('newSecName').value.trim();
  if (!deptId || !name) return alert('Select dept and enter name');
  await api('POST', '/api/admin/sections', { department_id: parseInt(deptId), name });
  document.getElementById('newSecName').value = '';
  loadAdminData();
}

async function delSec(id) { if (!confirm('Delete?')) return; await api('DELETE', `/api/admin/sections/${id}`); loadAdminData(); }

function fillDeptDropdowns() {
  const opts = allDepts.map(d => `<option value="${d.id}">${d.name}</option>`).join('');
  ['secDeptSelect', 'stuDept'].forEach(id => {
    const sel = document.getElementById(id);
    if (sel) sel.innerHTML = '<option value="">Select…</option>' + opts;
  });
}

function loadStuSections() {
  const deptId = document.getElementById('stuDept').value;
  const secSel = document.getElementById('stuSection');
  if (!secSel) return;
  const secs = allSections.filter(s => s.department_id == deptId);
  secSel.innerHTML = secs.length ? secs.map(s => `<option value="${s.id}">${s.name}</option>`).join('') : '<option value="">No sections</option>';
}

async function loadStuTable() {
  try {
    const students = await api('GET', '/api/admin/students');
    const el = document.getElementById('stuList');
    if (!students.length) { el.innerHTML = '<div class="empty-state"><div class="empty-icon">🎓</div><strong>No students</strong></div>'; return; }
    el.innerHTML = students.map(s => `<div class="list-item">
      <div class="item-icon blue">${s.name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)}</div>
      <div class="item-info"><strong>${s.name}</strong><small>${s.roll || '—'} • ${s.department}-${s.section}</small></div>
      <button class="btn-x" onclick="delStudent(${s.id})">✕</button>
    </div>`).join('');
  } catch (e) {}
}

async function addStudent() {
  const name = document.getElementById('stuName').value.trim();
  const roll = document.getElementById('stuRoll').value.trim();
  const deptId = document.getElementById('stuDept').value;
  const secId = document.getElementById('stuSection').value;
  const ble = document.getElementById('stuBle').value.trim();
  if (!name || !roll || !deptId || !secId) return alert('Fill all fields');
  await api('POST', '/api/admin/students', { name, roll, department_id: parseInt(deptId), section_id: parseInt(secId), ble_address: ble || null });
  ['stuName', 'stuRoll', 'stuBle'].forEach(id => document.getElementById(id).value = '');
  loadStuTable();
}

async function delStudent(id) { if (!confirm('Delete?')) return; await api('DELETE', `/api/admin/students/${id}`); loadStuTable(); }

async function loadUserTable() {
  try {
    const users = await api('GET', '/api/admin/users');
    const badge = document.getElementById('adminUserCountBadge');
    if (badge) badge.textContent = `${users.length} Users`;
    const el = document.getElementById('userList');
    if (!users.length) { el.innerHTML = '<div class="empty-state"><div class="empty-icon">👥</div><strong>No users</strong></div>'; return; }
    el.innerHTML = users.map(u => {
      const isT = u.role === 'teacher';
      const safeName = (u.name || '').replace(/'/g, "\\'");
      return `<div class="list-item">
        <div class="item-icon ${isT ? 'purple' : 'green'}">${isT ? '👨‍🏫' : '🎓'}</div>
        <div class="item-info">
          <strong>${u.name}</strong>
          <small>${(u.role || '').toUpperCase()} • ${u.branch || '—'} ${u.section ? '('+u.section+')' : ''} ${u.subject ? '• ' + u.subject : ''}</small>
        </div>
        <div style="display:flex; gap:8px; align-items:center;">
          <button class="btn-outline sm" onclick="openResetPasswordModal(${u.id}, '${safeName}')">🔑 Reset Pass</button>
          <button class="btn-x" onclick="delUser(${u.id})" title="Delete user">✕</button>
        </div>
      </div>`;
    }).join('');
  } catch (e) {
    console.error('Failed to load user table:', e);
  }
}

async function delUser(id) {
  if (!confirm('Delete this user account permanently?')) return;
  await api('DELETE', `/api/admin/users/${id}`);
  loadUserTable();
}

async function adminCreateUser() {
  const name = document.getElementById('adminNewName').value.trim();
  const password = document.getElementById('adminNewPass').value.trim();
  const role = document.getElementById('adminNewRole').value;
  const branch = document.getElementById('adminNewBranch').value.trim();
  const section = document.getElementById('adminNewSection').value.trim();
  const subject = document.getElementById('adminNewSubject').value.trim();

  if (!name || !password || !role) {
    alert('Name, password, and role are required!');
    return;
  }
  try {
    await api('POST', '/api/admin/users', { name, password, role, branch, section, subject });
    alert(`User ${name} created successfully!`);
    document.getElementById('adminNewName').value = '';
    document.getElementById('adminNewPass').value = '';
    document.getElementById('adminNewBranch').value = '';
    document.getElementById('adminNewSection').value = '';
    document.getElementById('adminNewSubject').value = '';
    loadUserTable();
  } catch (e) {
    alert(e.message || 'Failed to create user');
  }
}

function openResetPasswordModal(userId, userName) {
  const modal = document.getElementById('resetPasswordModal');
  if (!modal) return;
  document.getElementById('resetPasswordUserId').value = userId;
  document.getElementById('resetPasswordTargetUser').textContent = `Set a new password for: ${userName}`;
  document.getElementById('newAdminPasswordInput').value = '';
  document.getElementById('resetPasswordError').classList.add('hidden');
  modal.classList.remove('hidden');
}

function closeResetPasswordModal() {
  const modal = document.getElementById('resetPasswordModal');
  if (modal) modal.classList.add('hidden');
}

async function submitResetPassword() {
  const userId = document.getElementById('resetPasswordUserId').value;
  const newPassword = document.getElementById('newAdminPasswordInput').value.trim();
  const errEl = document.getElementById('resetPasswordError');
  errEl.classList.add('hidden');

  if (!newPassword || newPassword.length < 4) {
    errEl.textContent = 'Password must be at least 4 characters';
    errEl.classList.remove('hidden');
    return;
  }

  try {
    await api('POST', `/api/admin/users/${userId}/password`, { new_password: newPassword });
    alert('Password updated successfully!');
    closeResetPasswordModal();
  } catch (e) {
    errEl.textContent = e.message || 'Failed to update password';
    errEl.classList.remove('hidden');
  }
}


// ─── SESSIONS ──────────────────────────────────────────────────
async function startNewSession() {
  // Open the session modal
  openSessionModal();
}

async function openSessionModal() {
  const modal = document.getElementById('sessionModal');
  const branchSel = document.getElementById('sessionBranch');
  const sectionSel = document.getElementById('sessionSection');
  const errEl = document.getElementById('sessionError');
  errEl.classList.add('hidden');

  // Pre-fill branch/section if teacher already has them
  if (currentUser?.branch) branchSel.value = currentUser.branch;

  // Load departments into branch dropdown
  try {
    const depts = await api('GET', '/api/admin/departments');
    branchSel.innerHTML = '<option value="">Select Branch</option>' +
      depts.map(d => `<option value="${d.name}" ${d.name === currentUser?.branch ? 'selected' : ''}>${d.name}</option>`).join('');
  } catch (e) {
    // Fallback: use common branches
    branchSel.innerHTML = '<option value="">Select Branch</option>' +
      ['CSE', 'CSE AIML', 'ECE', 'ME', 'CE', 'IT'].map(b =>
        `<option value="${b}" ${b === currentUser?.branch ? 'selected' : ''}>${b}</option>`
      ).join('');
  }

  // Load sections
  try {
    const secs = await api('GET', '/api/admin/sections');
    allSections = secs;
    filterSections();
  } catch (e) {
    sectionSel.innerHTML = '<option value="">Select Section</option>' +
      ['A', 'B', 'C', 'D'].map(s => `<option value="${s}" ${s === currentUser?.section ? 'selected' : ''}>${s}</option>`).join('');
  }

  branchSel.onchange = filterSections;
  document.getElementById('sessionSubject').value = currentUser?.subject || '';
  modal.classList.remove('hidden');
}

function filterSections() {
  const branch = document.getElementById('sessionBranch').value;
  const sectionSel = document.getElementById('sessionSection');

  // If we have sections from admin API, filter by department
  if (allSections.length && branch) {
    const dept = allSections.find(s => s.dept_name === branch || s.name);
    const filtered = allSections.filter(s => s.dept_name === branch);
    if (filtered.length) {
      sectionSel.innerHTML = '<option value="">Select Section</option>' +
        filtered.map(s => `<option value="${s.name}" ${s.name === currentUser?.section ? 'selected' : ''}>${s.name}</option>`).join('');
      return;
    }
  }

  // Fallback
  sectionSel.innerHTML = '<option value="">Select Section</option>' +
    ['A', 'B', 'C', 'D'].map(s => `<option value="${s}" ${s === currentUser?.section ? 'selected' : ''}>${s}</option>`).join('');
}

function closeSessionModal() {
  document.getElementById('sessionModal')?.classList.add('hidden');
}

async function submitNewSession() {
  const branch = document.getElementById('sessionBranch').value;
  const section = document.getElementById('sessionSection').value;
  const subject = document.getElementById('sessionSubject').value.trim();
  const errEl = document.getElementById('sessionError');
  const btn = document.getElementById('sessionSubmitBtn');
  errEl.classList.add('hidden');

  if (!branch || !section || !subject) {
    errEl.textContent = 'Please fill all fields (Branch, Section, Subject)';
    errEl.classList.remove('hidden');
    return;
  }

  btn.disabled = true;
  btn.textContent = 'Starting...';

  try {
    const res = await api('POST', '/api/sessions', {
      teacher_name: currentUser?.name || 'admin',
      branch,
      section,
      subject
    });
    closeSessionModal();
    activeSession = res;
    alert('✅ Session started: ' + subject + ' — ' + branch + '-' + section + ' is now LIVE!');
    switchView('live');
  } catch (e) {
    errEl.textContent = e.message;
    errEl.classList.remove('hidden');
  } finally {
    btn.disabled = false;
    btn.textContent = '🚀 Start Session';
  }
}

// ─── FILE UPLOAD INIT ──────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  // Note file
  const fileInput = document.getElementById('noteFile');
  if (fileInput) {
    fileInput.addEventListener('change', e => {
      const file = e.target.files[0];
      if (file) {
        selectedFile = file;
        document.getElementById('fileDropName').textContent = file.name;
        document.getElementById('fileDropPreview').classList.remove('hidden');
        document.getElementById('fileDropContent').style.display = 'none';
      }
    });
  }
  const dropArea = document.getElementById('fileDropArea');
  if (dropArea) {
    dropArea.addEventListener('dragover', e => { e.preventDefault(); dropArea.classList.add('dragover'); });
    dropArea.addEventListener('dragleave', () => dropArea.classList.remove('dragover'));
    dropArea.addEventListener('drop', e => {
      e.preventDefault(); dropArea.classList.remove('dragover');
      const file = e.dataTransfer.files[0];
      if (file) {
        selectedFile = file;
        document.getElementById('fileDropName').textContent = file.name;
        document.getElementById('fileDropPreview').classList.remove('hidden');
        document.getElementById('fileDropContent').style.display = 'none';
      }
    });
  }

  // PYQ file
  const pyqFileInput = document.getElementById('pyqFile');
  if (pyqFileInput) {
    pyqFileInput.addEventListener('change', e => {
      const file = e.target.files[0];
      if (file) {
        selectedPyqFile = file;
        document.getElementById('pyqFileName').textContent = file.name;
        document.getElementById('pyqFilePreview').classList.remove('hidden');
        document.getElementById('pyqFileContent').style.display = 'none';
      }
    });
  }
  const pyqDrop = document.getElementById('pyqFileDrop');
  if (pyqDrop) {
    pyqDrop.addEventListener('dragover', e => { e.preventDefault(); pyqDrop.classList.add('dragover'); });
    pyqDrop.addEventListener('dragleave', () => pyqDrop.classList.remove('dragover'));
    pyqDrop.addEventListener('drop', e => {
      e.preventDefault(); pyqDrop.classList.remove('dragover');
      const file = e.dataTransfer.files[0];
      if (file) {
        selectedPyqFile = file;
        document.getElementById('pyqFileName').textContent = file.name;
        document.getElementById('pyqFilePreview').classList.remove('hidden');
        document.getElementById('pyqFileContent').style.display = 'none';
      }
    });
  }
});
