import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { Settings as SettingsIcon, ShieldAlert } from 'lucide-react';
import toast from 'react-hot-toast';

const Settings: React.FC = () => {
  const [windowBefore, setWindowBefore] = useState('10');
  const [windowAfter, setWindowAfter] = useState('20');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const fetchSettings = async () => {
    try {
      const res = await api.get('/settings');
      const before = res.data.find((s: any) => s.settingKey === 'attendance_window_before_mins')?.settingValue || '10';
      const after = res.data.find((s: any) => s.settingKey === 'attendance_window_after_mins')?.settingValue || '20';
      setWindowBefore(before);
      setWindowAfter(after);
    } catch (err) {
      //
    }
  };

  useEffect(() => {
    fetchSettings();
  }, []);

  const handleUpdateSettings = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.put('/settings', {
        attendance_window_before_mins: windowBefore,
        attendance_window_after_mins: windowAfter
      });
      toast.success('System settings successfully updated');
    } catch (err) {
      toast.error('Failed to update system configurations');
    }
  };

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!currentPassword || !newPassword) {
      toast.error('All password fields are required');
      return;
    }
    setLoading(true);
    try {
      await api.put('/teacher/change-password', { currentPassword, newPassword });
      toast.success('Teacher password modified successfully');
      setCurrentPassword('');
      setNewPassword('');
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Incorrect current password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
      {/* Attendance window settings */}
      <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm space-y-4">
        <h4 className="font-bold text-slate-800 text-base flex items-center gap-2">
          <SettingsIcon className="w-5 h-5 text-violet-600" /> Attendance Windows
        </h4>
        <p className="text-xs text-slate-400 font-medium">Configure buffer ranges where attendance marking is enabled relative to class timing.</p>
        
        <form onSubmit={handleUpdateSettings} className="space-y-4 pt-2">
          <div>
            <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">Opens (minutes before period start)</label>
            <input
              type="number"
              value={windowBefore}
              onChange={(e) => setWindowBefore(e.target.value)}
              className="w-full px-3 py-1.5 border border-slate-200 rounded-lg text-xs"
            />
          </div>

          <div>
            <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">Closes (minutes after period end)</label>
            <input
              type="number"
              value={windowAfter}
              onChange={(e) => setWindowAfter(e.target.value)}
              className="w-full px-3 py-1.5 border border-slate-200 rounded-lg text-xs"
            />
          </div>

          <button type="submit" className="px-5 py-2.5 bg-violet-600 hover:bg-violet-700 text-white rounded-lg text-xs font-bold shadow-sm transition-all">
            Save Configuration
          </button>
        </form>
      </div>

      {/* Change Password settings */}
      <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm space-y-4">
        <h4 className="font-bold text-slate-800 text-base flex items-center gap-2">
          <ShieldAlert className="w-5 h-5 text-violet-600" /> Update Password
        </h4>
        <p className="text-xs text-slate-400 font-medium">Change account credentials securely. Session updates automatically.</p>
        
        <form onSubmit={handleChangePassword} className="space-y-4 pt-2">
          <div>
            <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">Current Password</label>
            <input
              type="password"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              className="w-full px-3 py-1.5 border border-slate-200 rounded-lg text-xs"
              placeholder="••••••••"
            />
          </div>

          <div>
            <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">New Password</label>
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              className="w-full px-3 py-1.5 border border-slate-200 rounded-lg text-xs"
              placeholder="••••••••"
            />
          </div>

          <button type="submit" disabled={loading} className="px-5 py-2.5 bg-violet-600 hover:bg-violet-700 text-white rounded-lg text-xs font-bold shadow-sm transition-all">
            {loading ? 'Updating...' : 'Change Password'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default Settings;
