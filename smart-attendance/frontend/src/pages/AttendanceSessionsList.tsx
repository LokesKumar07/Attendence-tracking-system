import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { CalendarDays, Loader2, ArrowRight } from 'lucide-react';
import toast from 'react-hot-toast';

interface Session {
  id: number;
  attendanceDate: string;
  subjectCode: string;
  subjectName: string;
  periodNumber: number;
  periodTimeRange: string;
  status: string;
  createdByTeacher: string;
}

const AttendanceSessionsList: React.FC = () => {
  const navigate = useNavigate();
  const [date, setDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [sessions, setSessions] = useState<Session[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchSessions = async () => {
    setLoading(true);
    try {
      const res = await api.get(`/attendance/sessions?date=${date}`);
      setSessions(res.data);
    } catch (err) {
      toast.error('Failed to load attendance sessions');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSessions();
  }, [date]);

  return (
    <div className="space-y-6">
      <div className="bg-white border border-slate-150 rounded-2xl p-6 shadow-sm flex flex-col sm:flex-row justify-between items-center gap-4">
        <div>
          <h3 className="font-bold text-slate-800 text-base">Daily Attendance Sessions</h3>
          <p className="text-xs text-slate-400 font-medium mt-1">Select date to view and mark attendance records.</p>
        </div>
        <div className="flex items-center gap-2">
          <CalendarDays className="w-4 h-4 text-violet-600" />
          <input
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            className="px-3 py-1.5 border border-slate-200 rounded-lg text-xs"
          />
        </div>
      </div>

      <div className="bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-sm">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-slate-50 border-b border-slate-200">
              <th className="p-4 text-xs font-bold uppercase text-slate-400">Date</th>
              <th className="p-4 text-xs font-bold uppercase text-slate-400">Period</th>
              <th className="p-4 text-xs font-bold uppercase text-slate-400">Subject</th>
              <th className="p-4 text-xs font-bold uppercase text-slate-400">Status</th>
              <th className="p-4 text-xs font-bold uppercase text-slate-400 text-right">Action</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-150">
            {loading ? (
              <tr>
                <td colSpan={5} className="p-8 text-center">
                  <Loader2 className="w-6 h-6 animate-spin text-violet-600 mx-auto" />
                </td>
              </tr>
            ) : sessions.length === 0 ? (
              <tr>
                <td colSpan={5} className="p-8 text-center text-xs text-slate-400">
                  No sessions generated for this date. Check dashboard timeline for auto slot opening.
                </td>
              </tr>
            ) : (
              sessions.map((s) => (
                <tr key={s.id}>
                  <td className="p-4 text-xs font-semibold text-slate-800">{s.attendanceDate}</td>
                  <td className="p-4 text-xs text-slate-500">
                    <span className="bg-violet-50 text-violet-700 px-2 py-0.5 rounded font-bold uppercase mr-2 text-[10px]">
                      Period {s.periodNumber}
                    </span>
                    {s.periodTimeRange}
                  </td>
                  <td className="p-4 text-xs font-semibold text-slate-800">
                    <p>{s.subjectName}</p>
                    <span className="text-[10px] text-slate-400 font-normal">{s.subjectCode}</span>
                  </td>
                  <td className="p-4">
                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded uppercase ${
                      s.status === 'SUBMITTED' ? 'bg-emerald-100 text-emerald-800' : 'bg-amber-100 text-amber-800'
                    }`}>
                      {s.status}
                    </span>
                  </td>
                  <td className="p-4 text-right">
                    <button
                      onClick={() => navigate(`/attendance/${s.id}`)}
                      className="text-xs text-violet-600 font-bold hover:underline inline-flex items-center gap-1"
                    >
                      {s.status === 'SUBMITTED' ? 'View/Correct' : 'Mark Sheet'} <ArrowRight className="w-3.5 h-3.5" />
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AttendanceSessionsList;
