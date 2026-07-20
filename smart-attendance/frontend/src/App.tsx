import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import Layout from './components/Layout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import AttendanceSessionsList from './pages/AttendanceSessionsList';
import AttendanceMarking from './pages/AttendanceMarking';
import Students from './pages/Students';
import Subjects from './pages/Subjects';
import Timetable from './pages/Timetable';
import Holidays from './pages/Holidays';
import Reports from './pages/Reports';
import AuditLogs from './pages/AuditLogs';
import Settings from './pages/Settings';

// Route guards
const PrivateRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const isAuth = !!sessionStorage.getItem('accessToken');
  return isAuth ? <>{children}</> : <Navigate to="/login" replace />;
};

const App: React.FC = () => {
  return (
    <Router>
      <Toaster position="top-right" toastOptions={{ duration: 4000 }} />
      <Routes>
        {/* Public auth paths */}
        <Route path="/login" element={<Login />} />

        {/* Private wrapped teacher paths */}
        <Route
          path="/*"
          element={
            <PrivateRoute>
              <Layout>
                <Routes>
                  <Route path="/" element={<Dashboard />} />
                  <Route path="/attendance" element={<AttendanceSessionsList />} />
                  <Route path="/attendance/:sessionId" element={<AttendanceMarking />} />
                  <Route path="/students" element={<Students />} />
                  <Route path="/subjects" element={<Subjects />} />
                  <Route path="/timetable" element={<Timetable />} />
                  <Route path="/holidays" element={<Holidays />} />
                  <Route path="/reports" element={<Reports />} />
                  <Route path="/audit" element={<AuditLogs />} />
                  <Route path="/settings" element={<Settings />} />
                  <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
              </Layout>
            </PrivateRoute>
          }
        />
      </Routes>
    </Router>
  );
};

export default App;
