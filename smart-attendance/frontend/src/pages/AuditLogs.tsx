import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { ShieldCheck, Loader2 } from 'lucide-react';
import toast from 'react-hot-toast';

interface AuditLog {
  id: number;
  action: string;
  details: string;
  ipAddress: string;
  createdAt: string;
}

const AuditLogs: React.FC = () => {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchLogs = async () => {
    try {
      const res = await api.get('/audit');
      setLogs(res.data);
    } catch (err) {
      toast.error('Failed to load audit logs history');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, []);

  return (
    <div className="space-y-6">
      <div className="bg-white border border-slate-150 rounded-2xl p-6 shadow-sm">
        <h3 className="font-bold text-slate-800 text-base">Security Audit Trails</h3>
        <p className="text-xs text-slate-400 font-medium mt-1">Append-only security log containing logins, logouts, session creations and corrections details.</p>
      </div>

      <div className="bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-sm">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-slate-50 border-b border-slate-200">
              <th className="p-4 text-xs font-bold uppercase text-slate-400">Timestamp</th>
              <th className="p-4 text-xs font-bold uppercase text-slate-400">Action Type</th>
              <th className="p-4 text-xs font-bold uppercase text-slate-400">Log Details</th>
              <th className="p-4 text-xs font-bold uppercase text-slate-400">IP Address</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-150">
            {loading ? (
              <tr>
                <td colSpan={4} className="p-8 text-center">
                  <Loader2 className="w-6 h-6 animate-spin text-violet-600 mx-auto" />
                </td>
              </tr>
            ) : logs.length === 0 ? (
              <tr>
                <td colSpan={4} className="p-8 text-center text-xs text-slate-400">No logs found.</td>
              </tr>
            ) : (
              logs.map((log) => (
                <tr key={log.id} className="hover:bg-slate-50/20">
                  <td className="p-4 text-xs text-slate-500 font-medium">{log.createdAt}</td>
                  <td className="p-4">
                    <span className="bg-violet-100 text-violet-800 text-[10px] font-bold px-2 py-0.5 rounded uppercase">
                      {log.action}
                    </span>
                  </td>
                  <td className="p-4 text-xs font-semibold text-slate-800">{log.details}</td>
                  <td className="p-4 text-xs text-slate-400 font-mono">{log.ipAddress}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AuditLogs;
