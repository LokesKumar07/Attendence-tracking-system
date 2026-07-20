import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import toast from 'react-hot-toast';

const Login: React.FC = () => {
  const navigate = useNavigate();
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [forgotMode, setForgotMode] = useState(false);
  const [resetMode, setResetMode] = useState(false);
  const [forgotEmail, setForgotEmail] = useState('');
  const [resetToken, setResetToken] = useState('');
  const [newPassword, setNewPassword] = useState('');

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!usernameOrEmail || !password) {
      toast.error('All fields are mandatory');
      return;
    }
    setLoading(true);
    try {
      const res = await api.post('/auth/login', { usernameOrEmail, password });
      sessionStorage.setItem('accessToken', res.data.accessToken);
      localStorage.setItem('refreshToken', res.data.refreshToken);
      sessionStorage.setItem('fullName', res.data.fullName);
      sessionStorage.setItem('email', res.data.email);
      toast.success('Welcome back, ' + res.data.fullName);
      navigate('/');
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Invalid username or password.');
    } finally {
      setLoading(false);
    }
  };

  const handleForgotPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await api.post('/auth/forgot-password', { email: forgotEmail });
      toast.success('Instructions logged in console. Copy token from there!');
      setForgotMode(false);
      setResetMode(true);
    } catch (err: any) {
      toast.error('Failed to trigger reset flow');
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await api.post('/auth/reset-password', { token: resetToken, newPassword });
      toast.success('Password updated successfully');
      setResetMode(false);
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Invalid or expired token');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 px-4">
      <div className="max-w-md w-full bg-white rounded-2xl shadow-xl border border-slate-100 overflow-hidden">
        {/* Banner */}
        <div className="p-8 bg-gradient-to-tr from-violet-600 to-indigo-800 text-white text-center">
          <h2 className="text-2xl font-bold">SmartAttend</h2>
          <p className="text-xs opacity-80 mt-1">PPG College Teacher Attendance Portal</p>
        </div>

        {/* Form Body */}
        <div className="p-8">
          {!forgotMode && !resetMode && (
            <form onSubmit={handleLogin} className="space-y-6">
              <div>
                <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">Username or Email</label>
                <input
                  type="text"
                  value={usernameOrEmail}
                  onChange={(e) => setUsernameOrEmail(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-lg border border-slate-200 focus:outline-none focus:border-violet-500 text-sm"
                  placeholder="lathika"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">Password</label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-lg border border-slate-200 focus:outline-none focus:border-violet-500 text-sm"
                  placeholder="••••••••"
                />
              </div>

              <div className="flex justify-end">
                <button
                  type="button"
                  onClick={() => setForgotMode(true)}
                  className="text-xs text-violet-600 font-semibold hover:underline"
                >
                  Forgot Password?
                </button>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 bg-violet-600 hover:bg-violet-700 text-white font-semibold rounded-lg text-sm transition-all"
              >
                {loading ? 'Logging in...' : 'Sign In'}
              </button>
            </form>
          )}

          {forgotMode && (
            <form onSubmit={handleForgotPassword} className="space-y-6">
              <h3 className="font-bold text-slate-800 text-lg">Forgot Password</h3>
              <p className="text-xs text-slate-500">Provide your email address to simulate the password reset process.</p>
              
              <div>
                <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">Email Address</label>
                <input
                  type="email"
                  value={forgotEmail}
                  onChange={(e) => setForgotEmail(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-lg border border-slate-200 focus:outline-none focus:border-violet-500 text-sm"
                  placeholder="lathika@ppg.edu.in"
                />
              </div>

              <div className="flex justify-between items-center gap-4">
                <button
                  type="button"
                  onClick={() => setForgotMode(false)}
                  className="text-xs font-medium text-slate-600"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="px-6 py-2.5 bg-violet-600 hover:bg-violet-700 text-white font-semibold rounded-lg text-sm transition-all"
                >
                  {loading ? 'Processing...' : 'Send Verification'}
                </button>
              </div>
            </form>
          )}

          {resetMode && (
            <form onSubmit={handleResetPassword} className="space-y-6">
              <h3 className="font-bold text-slate-800 text-lg">Reset Password</h3>
              <p className="text-xs text-slate-500">Enter the token generated in console logs, along with your new password.</p>
              
              <div>
                <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">Reset Token</label>
                <input
                  type="text"
                  value={resetToken}
                  onChange={(e) => setResetToken(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-lg border border-slate-200 focus:outline-none focus:border-violet-500 text-sm"
                  placeholder="UUID reset token"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">New Password</label>
                <input
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-lg border border-slate-200 focus:outline-none focus:border-violet-500 text-sm"
                  placeholder="••••••••"
                />
              </div>

              <div className="flex justify-between items-center gap-4">
                <button
                  type="button"
                  onClick={() => setResetMode(false)}
                  className="text-xs font-medium text-slate-600"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="px-6 py-2.5 bg-violet-600 hover:bg-violet-700 text-white font-semibold rounded-lg text-sm transition-all"
                >
                  {loading ? 'Resetting...' : 'Reset Password'}
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

export default Login;
