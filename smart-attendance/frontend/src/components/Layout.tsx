import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { 
  LayoutDashboard, 
  Users, 
  BookOpen, 
  CalendarDays, 
  Calendar, 
  CheckSquare, 
  BarChart3, 
  Settings, 
  History, 
  LogOut 
} from 'lucide-react';

interface LayoutProps {
  children: React.ReactNode;
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const fullName = sessionStorage.getItem('fullName') || 'Ms. V. Lathika';
  const email = sessionStorage.getItem('email') || 'lathika@ppg.edu.in';

  const menuItems = [
    { name: 'Dashboard', path: '/', icon: LayoutDashboard },
    { name: 'Attendance', path: '/attendance', icon: CheckSquare },
    { name: 'Students', path: '/students', icon: Users },
    { name: 'Subjects', path: '/subjects', icon: BookOpen },
    { name: 'Timetable', path: '/timetable', icon: CalendarDays },
    { name: 'Holidays', path: '/holidays', icon: Calendar },
    { name: 'Reports', path: '/reports', icon: BarChart3 },
    { name: 'Audit Logs', path: '/audit', icon: History },
    { name: 'Settings', path: '/settings', icon: Settings },
  ];

  const handleLogout = () => {
    sessionStorage.clear();
    localStorage.clear();
    navigate('/login');
  };

  return (
    <div className="flex h-screen bg-slate-50">
      {/* Sidebar */}
      <aside className="w-64 bg-white border-r border-slate-200 flex flex-col justify-between">
        <div>
          {/* Logo / Header */}
          <div className="p-6 border-b border-slate-100 flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-violet-600 flex items-center justify-center text-white font-bold text-lg">
              S
            </div>
            <div>
              <h1 className="font-bold text-slate-800 text-base leading-none">SmartAttend</h1>
              <span className="text-xs text-slate-400 font-medium">PPG College</span>
            </div>
          </div>

          {/* Navigation Links */}
          <nav className="p-4 space-y-1">
            {menuItems.map((item) => {
              const Icon = item.icon;
              const isActive = location.pathname === item.path;
              return (
                <Link
                  key={item.name}
                  to={item.path}
                  className={`flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm font-medium transition-all ${
                    isActive 
                      ? 'bg-violet-50 text-violet-700' 
                      : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
                  }`}
                >
                  <Icon className={`w-4 h-4 ${isActive ? 'text-violet-600' : 'text-slate-400'}`} />
                  {item.name}
                </Link>
              );
            })}
          </nav>
        </div>

        {/* User Profile & Logout */}
        <div className="p-4 border-t border-slate-100 space-y-3">
          <div className="flex items-center gap-3 px-2">
            <div className="w-9 h-9 rounded-full bg-violet-100 flex items-center justify-center text-violet-700 font-semibold text-sm">
              {fullName.split(' ').pop()?.charAt(0) || 'L'}
            </div>
            <div className="overflow-hidden">
              <p className="text-xs font-semibold text-slate-800 truncate">{fullName}</p>
              <p className="text-[10px] text-slate-400 truncate">{email}</p>
            </div>
          </div>
          
          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-3 px-4 py-2 rounded-lg text-sm font-medium text-rose-600 hover:bg-rose-50 transition-all"
          >
            <LogOut className="w-4 h-4 text-rose-500" />
            Logout
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="flex-1 overflow-y-auto">
        <header className="h-16 border-b border-slate-200 bg-white flex items-center justify-between px-8">
          <h2 className="text-lg font-bold text-slate-800">
            {menuItems.find(item => item.path === location.pathname)?.name || 'SmartAttend'}
          </h2>
          <div className="text-xs text-slate-400 font-medium">
            Role: <span className="bg-violet-100 text-violet-800 px-2 py-0.5 rounded font-semibold">TEACHER</span>
          </div>
        </header>
        <div className="p-8">
          {children}
        </div>
      </main>
    </div>
  );
};

export default Layout;
