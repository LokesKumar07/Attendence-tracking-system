import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { 
  Users, 
  CheckCircle, 
  Clock, 
  BookOpen, 
  ArrowRight,
  RefreshCw
} from 'lucide-react';
import toast from 'react-hot-toast';

interface PeriodInfo {
  periodId: number;
  periodNumber: number;
  startTime: string;
  endTime: string;
  subjectName: string;
  subjectCode: string;
  classSection: string;
  status: string;
  isActive: boolean;
}

interface SubjectStats {
  subjectId: number;
  subjectName: string;
  subjectCode: string;
  totalStudents: number;
  completedSessions: number;
  pendingSessions: number;
}

interface DashboardData {
  currentDate: string;
  currentDay: string;
  currentTime: string;
  currentPeriod: PeriodInfo | null;
  previousPeriod: PeriodInfo | null;
  nextPeriod: PeriodInfo | null;
  totalStudents: number;
  completedSessions: number;
  pendingSessions: number;
  subjectStats: SubjectStats[];
  todaySummary: {
    presentCount: number;
    absentCount: number;
    lateCount: number;
    onDutyCount: number;
  };
  recentActivities: Array<{
    timeAgo: string;
    description: string;
    type: string;
  }>;
}

const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [selectedDate, setSelectedDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [selectedDayOrder, setSelectedDayOrder] = useState<number>(1);
  const [todaySchedule, setTodaySchedule] = useState<PeriodInfo[]>([]);

  const fetchDashboard = async () => {
    try {
      const res = await api.get(`/attendance/dashboard-status?date=${selectedDate}&dayOrder=${selectedDayOrder}`);
      setData(res.data);
      
      // Fetch full active timetable for schedule mapping
      const ttRes = await api.get('/timetable');
      
      const mappedSchedule = ttRes.data
        .filter((entry: any) => entry.dayOrder === selectedDayOrder)
        .map((entry: any) => ({
          periodId: entry.id,
          periodNumber: entry.periodNumber,
          startTime: entry.startTime,
          endTime: entry.endTime,
          subjectName: entry.subjectName || 'FREE',
          subjectCode: entry.subjectCode || '-',
          classSection: entry.subjectName ? 'Odd Sem' : '-',
          status: entry.entryType === 'FREE' ? 'FREE' : 'PENDING',
          isActive: false
        }));
        
      setTodaySchedule(mappedSchedule);
    } catch (err) {
      toast.error('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
  }, [selectedDate, selectedDayOrder]);

  if (loading || !data) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="flex flex-col items-center gap-4">
          <RefreshCw className="w-8 h-8 text-violet-600 animate-spin" />
          <p className="text-sm font-medium text-slate-500">Loading teacher dashboard...</p>
        </div>
      </div>
    );
  }

  const handleOpenAttendance = async (p: PeriodInfo) => {
    try {
      const dateStr = selectedDate;
      const ttRes = await api.get('/timetable');
      const matchedEntry = ttRes.data.find((entry: any) => entry.id === p.periodId);
      
      if (!matchedEntry || !matchedEntry.subjectId) {
        toast.error('Cannot open session for free period');
        return;
      }

      // Check if session is already created or create new
      const res = await api.post(`/attendance/sessions/open?date=${dateStr}&subjectId=${matchedEntry.subjectId}&periodId=${matchedEntry.periodNumber}`);
      toast.success('Attendance session successfully opened');
      navigate(`/attendance/${res.data.id}`);
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Failed to open session');
    }
  };

  return (
    <div className="space-y-8">
      {/* Date & Profile banner */}
      <div className="bg-white border border-slate-100 rounded-2xl p-6 flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
        <div>
          <h3 className="font-bold text-slate-800 text-xl">Good day, Ms. V. Lathika</h3>
          <p className="text-xs text-slate-400 font-medium mt-1">Select date and Day Order to mark or review attendance sessions.</p>
        </div>
        <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-4 w-full md:w-auto">
          <div className="flex items-center gap-2">
            <span className="text-xs font-bold text-slate-400 uppercase">Date:</span>
            <input
              type="date"
              value={selectedDate}
              onChange={(e) => setSelectedDate(e.target.value)}
              className="px-3 py-1.5 border border-slate-200 rounded-lg text-xs font-semibold focus:outline-none focus:border-violet-500"
            />
          </div>
          <div className="flex items-center gap-2">
            <span className="text-xs font-bold text-slate-400 uppercase">Day Order:</span>
            <select
              value={selectedDayOrder}
              onChange={(e) => setSelectedDayOrder(Number(e.target.value))}
              className="px-3 py-1.5 border border-slate-200 rounded-lg text-xs font-semibold focus:outline-none focus:border-violet-500 bg-white"
            >
              <option value={1}>Day Order I</option>
              <option value={2}>Day Order II</option>
              <option value={3}>Day Order III</option>
              <option value={4}>Day Order IV</option>
              <option value={5}>Day Order V</option>
              <option value={6}>Day Order VI</option>
            </select>
          </div>
          <div className="flex items-center gap-3 bg-violet-50 px-4 py-2 rounded-xl border border-violet-100/50">
            <Clock className="w-3.5 h-3.5 text-violet-600" />
            <span className="text-xs font-bold text-violet-800 uppercase">
              {data.currentTime} • {data.currentDay}
            </span>
          </div>
        </div>
      </div>

      {/* Subject-Wise Overview Metrics */}
      <div className="space-y-4">
        <h4 className="font-bold text-slate-800 text-base">Subject-Wise Overview</h4>
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
          {data.subjectStats && data.subjectStats.map((sub) => (
            <div key={sub.subjectId} className="bg-white border border-slate-150 rounded-2xl p-5 shadow-sm space-y-4 hover:border-violet-300 transition-all">
              <div className="flex justify-between items-start">
                <div className="space-y-1">
                  <span className="bg-violet-100 text-violet-800 text-[10px] font-extrabold px-2 py-0.5 rounded uppercase">{sub.subjectCode}</span>
                  <h4 className="font-bold text-slate-800 text-sm leading-tight line-clamp-2">{sub.subjectName}</h4>
                </div>
                <div className="w-8 h-8 bg-violet-50 text-violet-600 rounded-lg flex items-center justify-center shrink-0">
                  <BookOpen className="w-4 h-4" />
                </div>
              </div>
              <div className="grid grid-cols-3 gap-2 border-t border-slate-100 pt-3 text-center">
                <div>
                  <span className="text-[10px] font-bold text-slate-400 uppercase block">Students</span>
                  <span className="text-sm font-extrabold text-slate-700 block mt-0.5">{sub.totalStudents}</span>
                </div>
                <div>
                  <span className="text-[10px] font-bold text-slate-400 uppercase block">Completed</span>
                  <span className="text-sm font-extrabold text-emerald-600 block mt-0.5">{sub.completedSessions}</span>
                </div>
                <div>
                  <span className="text-[10px] font-bold text-slate-400 uppercase block">Pending</span>
                  <span className="text-sm font-extrabold text-amber-500 block mt-0.5">{sub.pendingSessions}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Current Context Tracker */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm">
            <h4 className="font-bold text-slate-800 text-base mb-6">Active/Current Slot Information</h4>
            {data.currentPeriod ? (
              <div className="bg-violet-50/50 border border-violet-100 rounded-xl p-5 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <div className="space-y-1.5">
                  <div className="flex items-center gap-2">
                    <span className="bg-violet-600 text-white font-bold text-xs px-2 py-0.5 rounded uppercase">Period {data.currentPeriod.periodNumber}</span>
                    <span className="text-xs font-semibold text-slate-400">{data.currentPeriod.startTime} - {data.currentPeriod.endTime}</span>
                  </div>
                  <h5 className="font-bold text-slate-800 text-lg leading-tight">{data.currentPeriod.subjectName}</h5>
                  <p className="text-xs text-slate-500 font-semibold">{data.currentPeriod.subjectCode} • {data.currentPeriod.classSection}</p>
                </div>
                {data.currentPeriod.status !== 'FREE' && (
                  <button
                    onClick={() => handleOpenAttendance(data.currentPeriod!)}
                    className="flex items-center gap-2 px-5 py-2.5 bg-violet-600 hover:bg-violet-700 text-white text-sm font-semibold rounded-lg shadow-sm transition-all"
                  >
                    Open Attendance <ArrowRight className="w-4 h-4" />
                  </button>
                )}
              </div>
            ) : (
              <div className="bg-slate-50 border border-slate-200 border-dashed rounded-xl p-8 text-center">
                <BookOpen className="w-8 h-8 text-slate-400 mx-auto mb-2" />
                <p className="text-sm font-semibold text-slate-700">No Class active at the moment</p>
                <p className="text-xs text-slate-400 mt-1">Check today's schedule grid to open upcoming classes</p>
              </div>
            )}
          </div>

          {/* Today's schedule grid */}
          <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm">
            <h4 className="font-bold text-slate-800 text-base mb-4">Today's Class Grid</h4>
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-slate-100">
                    <th className="py-3 text-xs font-bold uppercase tracking-wider text-slate-400">Slot</th>
                    <th className="py-3 text-xs font-bold uppercase tracking-wider text-slate-400">Time</th>
                    <th className="py-3 text-xs font-bold uppercase tracking-wider text-slate-400">Subject</th>
                    <th className="py-3 text-xs font-bold uppercase tracking-wider text-slate-400">Status</th>
                    <th className="py-3 text-xs font-bold uppercase tracking-wider text-slate-400 text-right">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {todaySchedule.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="py-6 text-center text-xs text-slate-400 font-medium">
                        No working days or classes configured for today.
                      </td>
                    </tr>
                  ) : (
                    todaySchedule.map((p) => (
                      <tr key={p.periodNumber}>
                        <td className="py-3 text-xs font-bold text-slate-800">Period {p.periodNumber}</td>
                        <td className="py-3 text-xs text-slate-500">{p.startTime} - {p.endTime}</td>
                        <td className="py-3 text-xs font-semibold text-slate-800">{p.subjectName}</td>
                        <td className="py-3">
                          <span className={`text-[10px] font-bold px-2 py-0.5 rounded uppercase ${
                            p.subjectName === 'FREE' 
                              ? 'bg-slate-100 text-slate-600'
                              : 'bg-amber-100 text-amber-800'
                          }`}>
                            {p.subjectName === 'FREE' ? 'FREE' : 'PENDING'}
                          </span>
                        </td>
                        <td className="py-3 text-right">
                          {p.subjectName !== 'FREE' && (
                            <button
                              onClick={() => handleOpenAttendance(p)}
                              className="text-xs text-violet-600 font-bold hover:underline"
                            >
                              Check-In
                            </button>
                          )}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Live Updates & Activity Logs sidebar */}
        <div className="space-y-6">
          <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm">
            <h4 className="font-bold text-slate-800 text-base mb-4 font-sans">Recent Activity Trail</h4>
            <div className="space-y-4">
              {data.recentActivities.length === 0 ? (
                <p className="text-xs text-slate-400 text-center py-6">No audits logs recorded yet today.</p>
              ) : (
                data.recentActivities.map((act, i) => (
                  <div key={i} className="flex items-start gap-3 border-l-2 border-violet-500 pl-3">
                    <div>
                      <p className="text-xs text-slate-700 font-medium">{act.description}</p>
                      <span className="text-[10px] text-slate-400 mt-0.5 block">{act.timeAgo}</span>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
