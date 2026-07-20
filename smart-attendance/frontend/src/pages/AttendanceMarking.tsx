import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../services/api';
import { 
  Users, 
  Check, 
  AlertCircle, 
  ChevronLeft, 
  Save, 
  CheckSquare, 
  Search,
  Loader2,
  Lock,
  Edit2
} from 'lucide-react';
import toast from 'react-hot-toast';

interface Record {
  recordId: number | null;
  studentId: number;
  registerNumber: string;
  rollNumber: string;
  name: string;
  status: 'PRESENT' | 'ABSENT' | 'LATE' | 'ON_DUTY' | 'UNMARKED';
  remarks: string;
}

interface SessionData {
  id: number;
  attendanceDate: string;
  subjectCode: string;
  subjectName: string;
  periodNumber: number;
  periodTimeRange: string;
  status: string;
  records: Record[];
}

const AttendanceMarking: React.FC = () => {
  const { sessionId } = useParams<{ sessionId: string }>();
  const navigate = useNavigate();
  const [data, setData] = useState<SessionData | null>(null);
  const [records, setRecords] = useState<Record[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  
  // Submit Confirmation Modal
  const [showConfirm, setShowConfirm] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [idempotencyKey] = useState(() => Math.random().toString(36).substring(2) + Date.now().toString(36));

  // Correction Modal
  const [showCorrection, setShowCorrection] = useState(false);
  const [correctionRecord, setCorrectionRecord] = useState<Record | null>(null);
  const [correctionReason, setCorrectionReason] = useState('');
  const [correctionStatus, setCorrectionStatus] = useState<'PRESENT' | 'ABSENT' | 'LATE' | 'ON_DUTY'>('PRESENT');

  const fetchSession = async () => {
    try {
      const res = await api.get(`/attendance/sessions/${sessionId}`);
      setData(res.data);
      setRecords(res.data.records);
    } catch (err) {
      toast.error('Failed to load session details');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (sessionId) fetchSession();
  }, [sessionId]);

  if (loading || !data) {
    return (
      <div className="flex items-center justify-center min-h-[50vh]">
        <Loader2 className="w-8 h-8 animate-spin text-violet-600" />
      </div>
    );
  }

  const totals = {
    total: records.length,
    present: records.filter(r => r.status === 'PRESENT').length,
    absent: records.filter(r => r.status === 'ABSENT').length,
    late: records.filter(r => r.status === 'LATE').length,
    onDuty: records.filter(r => r.status === 'ON_DUTY').length,
    unmarked: records.filter(r => r.status === 'UNMARKED').length,
  };

  const handleMarkStatus = (studentId: number, status: 'PRESENT' | 'ABSENT' | 'LATE' | 'ON_DUTY') => {
    // If session is already finalized (LOCKED), you must correct it through corrections endpoint
    if (data.status === 'LOCKED') {
      const rec = records.find(r => r.studentId === studentId);
      if (rec) {
        setCorrectionRecord(rec);
        setCorrectionStatus(status);
        setCorrectionReason('');
        setShowCorrection(true);
      }
      return;
    }

    setRecords(prev => prev.map(r => r.studentId === studentId ? { ...r, status } : r));
  };

  const handleMarkAllPresent = () => {
    if (data.status === 'LOCKED') return;
    setRecords(prev => prev.map(r => ({ ...r, status: 'PRESENT' })));
    toast.success('Marked all students present');
  };

  const handleReset = () => {
    if (data.status === 'LOCKED') return;
    setRecords(prev => prev.map(r => ({ ...r, status: 'UNMARKED' })));
    toast.success('Attendance unmarked');
  };

  const handleSave = async (isDraft: boolean) => {
    if (!isDraft && totals.unmarked > 0) {
      toast.error('All student records must be marked before submitting');
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        idempotencyKey,
        isDraft,
        records: records.map(r => ({
          studentId: r.studentId,
          status: r.status === 'UNMARKED' ? 'ABSENT' : r.status, // fall back to absent for unmarked drafts
          remarks: r.remarks || ''
        }))
      };

      await api.post(`/attendance/sessions/${sessionId}/submit`, payload);
      toast.success(isDraft ? 'Attendance saved as draft' : 'Attendance submitted successfully');
      setShowConfirm(false);
      navigate('/');
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Connection lost. Please retry.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCorrectionSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!correctionRecord || !correctionRecord.recordId) {
      toast.error('Unable to correct unmarked student record');
      return;
    }
    if (!correctionReason.trim()) {
      toast.error('Correction reason is mandatory');
      return;
    }

    try {
      await api.post('/attendance/corrections', {
        recordId: correctionRecord.recordId,
        newStatus: correctionStatus,
        reason: correctionReason
      });
      toast.success('Attendance records corrected successfully');
      setShowCorrection(false);
      fetchSession();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Failed to submit correction request');
    }
  };

  const filteredRecords = records.filter(r => 
    r.name.toLowerCase().includes(search.toLowerCase()) || 
    r.registerNumber.toLowerCase().includes(search.toLowerCase())
  );

  const isFinalized = data.status === 'LOCKED';

  return (
    <div className="space-y-6">
      {/* Session Metadata row */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 bg-white border border-slate-150 rounded-2xl p-6 shadow-sm">
        <div className="space-y-1">
          <button onClick={() => navigate('/')} className="flex items-center gap-1.5 text-xs font-semibold text-slate-500 hover:text-slate-800 mb-2">
            <ChevronLeft className="w-4 h-4" /> Back to Dashboard
          </button>
          <div className="flex items-center gap-2">
            <span className="bg-violet-100 text-violet-800 text-[10px] font-bold px-2 py-0.5 rounded uppercase">Period {data.periodNumber}</span>
            <span className="text-xs text-slate-400 font-semibold">{data.periodTimeRange}</span>
          </div>
          <h3 className="font-bold text-slate-800 text-lg">{data.subjectName}</h3>
          <p className="text-xs text-slate-500 font-medium">{data.subjectCode} • {data.attendanceDate}</p>
        </div>

        {isFinalized && (
          <div className="flex items-center gap-2 bg-emerald-50 text-emerald-800 border border-emerald-150 px-4 py-2 rounded-xl text-xs font-bold uppercase">
            <Lock className="w-4 h-4" /> Submitted & Finalized
          </div>
        )}
      </div>

      {/* Stats Counter Widget */}
      <div className="grid grid-cols-2 md:grid-cols-6 gap-4">
        {[
          { label: 'Total', count: totals.total, color: 'text-slate-800 bg-slate-100' },
          { label: 'Present', count: totals.present, color: 'text-emerald-800 bg-emerald-50' },
          { label: 'Absent', count: totals.absent, color: 'text-rose-800 bg-rose-50' },
          { label: 'Late', count: totals.late, color: 'text-amber-800 bg-amber-50' },
          { label: 'On-Duty', count: totals.onDuty, color: 'text-indigo-800 bg-indigo-50' },
          { label: 'Unmarked', count: totals.unmarked, color: 'text-slate-400 bg-slate-50' },
        ].map((stat, i) => (
          <div key={i} className={`p-4 rounded-xl text-center shadow-xs border border-slate-100 ${stat.color}`}>
            <span className="text-[10px] font-bold uppercase tracking-wider block opacity-70">{stat.label}</span>
            <span className="text-2xl font-black block mt-1">{stat.count}</span>
          </div>
        ))}
      </div>

      {/* Control bar */}
      <div className="flex flex-col md:flex-row justify-between items-center gap-4 bg-white border border-slate-150 rounded-2xl p-6 shadow-sm">
        <div className="relative w-full md:w-80">
          <Search className="absolute left-3 top-3 w-4 h-4 text-slate-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-slate-200 focus:outline-none focus:border-violet-500 rounded-lg text-sm"
            placeholder="Search by student name or roll..."
          />
        </div>

        {!isFinalized && (
          <div className="flex items-center gap-3 w-full md:w-auto">
            <button onClick={handleMarkAllPresent} className="px-4 py-2 border border-slate-200 hover:bg-slate-50 text-slate-600 rounded-lg text-xs font-semibold w-full md:w-auto">
              Mark All Present
            </button>
            <button onClick={handleReset} className="px-4 py-2 border border-slate-200 hover:bg-slate-50 text-slate-600 rounded-lg text-xs font-semibold w-full md:w-auto">
              Reset
            </button>
            <button onClick={() => handleSave(true)} className="flex items-center justify-center gap-2 px-4 py-2 border border-violet-200 text-violet-700 bg-violet-50 hover:bg-violet-100 rounded-lg text-xs font-semibold w-full md:w-auto">
              <Save className="w-4 h-4" /> Save Draft
            </button>
            <button onClick={() => setShowConfirm(true)} className="flex items-center justify-center gap-2 px-5 py-2 bg-violet-600 hover:bg-violet-700 text-white rounded-lg text-xs font-semibold w-full md:w-auto shadow-sm">
              <CheckSquare className="w-4 h-4" /> Submit
            </button>
          </div>
        )}
      </div>

      {/* Student List Matrix */}
      <div className="bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-sm">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-slate-50 border-b border-slate-200">
              <th className="p-4 text-xs font-bold uppercase text-slate-400">Register No</th>
              <th className="p-4 text-xs font-bold uppercase text-slate-400">Roll No</th>
              <th className="p-4 text-xs font-bold uppercase text-slate-400">Student Name</th>
              <th className="p-4 text-xs font-bold uppercase text-slate-400 text-center w-80">Attendance Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-150">
            {filteredRecords.map((r) => {
              return (
                <tr key={r.studentId} className="hover:bg-slate-50/40">
                  <td className="p-4 text-xs font-bold text-slate-800">{r.registerNumber}</td>
                  <td className="p-4 text-xs text-slate-500">{r.rollNumber}</td>
                  <td className="p-4 text-xs font-semibold text-slate-800">{r.name}</td>
                  <td className="p-4">
                    <div className="flex justify-center gap-1.5">
                      {[
                        { status: 'PRESENT', label: 'Present', color: 'border-emerald-200 hover:bg-emerald-50 text-emerald-800', active: 'bg-emerald-600 text-white' },
                        { status: 'ABSENT', label: 'Absent', color: 'border-rose-200 hover:bg-rose-50 text-rose-800', active: 'bg-rose-600 text-white' },
                        { status: 'LATE', label: 'Late', color: 'border-amber-200 hover:bg-amber-50 text-amber-800', active: 'bg-amber-500 text-white' },
                        { status: 'ON_DUTY', label: 'On-Duty', color: 'border-indigo-200 hover:bg-indigo-50 text-indigo-800', active: 'bg-indigo-600 text-white' },
                      ].map((btn) => {
                        const isSelected = r.status === btn.status;
                        return (
                          <button
                            key={btn.status}
                            onClick={() => handleMarkStatus(r.studentId, btn.status as any)}
                            className={`px-3 py-1.5 border rounded-lg text-xs font-semibold cursor-pointer transition-all ${
                              isSelected ? btn.active : 'bg-white ' + btn.color
                            }`}
                          >
                            {isSelected && isFinalized ? (
                              <span className="flex items-center gap-1"><Edit2 className="w-3 h-3" /> {btn.label}</span>
                            ) : btn.label}
                          </button>
                        );
                      })}
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Confirmation Modal */}
      {showConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
          <div className="bg-white rounded-2xl max-w-sm w-full shadow-2xl p-6 border border-slate-100 text-center space-y-4 animate-in fade-in zoom-in-95 duration-150">
            <AlertCircle className="w-12 h-12 text-violet-600 mx-auto" />
            <h3 className="font-bold text-slate-800 text-lg">Confirm Attendance Submission</h3>
            <p className="text-xs text-slate-500">Are you sure you want to finalize and lock the attendance record for this class? You can modify entries post-submission only with a valid correction reason.</p>
            
            <div className="bg-slate-50 p-4 rounded-xl text-left text-xs font-semibold text-slate-600 grid grid-cols-2 gap-2">
              <p>Present: <span className="font-extrabold text-emerald-600">{totals.present}</span></p>
              <p>Absent: <span className="font-extrabold text-rose-600">{totals.absent}</span></p>
              <p>Late: <span className="font-extrabold text-amber-500">{totals.late}</span></p>
              <p>On-Duty: <span className="font-extrabold text-indigo-600">{totals.onDuty}</span></p>
            </div>

            <div className="flex gap-3 pt-2">
              <button
                disabled={submitting}
                onClick={() => setShowConfirm(false)}
                className="w-full py-2.5 border border-slate-200 text-slate-600 rounded-lg text-xs font-semibold"
              >
                Go Back
              </button>
              <button
                disabled={submitting}
                onClick={() => handleSave(false)}
                className="w-full py-2.5 bg-violet-600 hover:bg-violet-700 text-white rounded-lg text-xs font-semibold shadow-sm flex items-center justify-center gap-1.5"
              >
                {submitting ? 'Submitting...' : 'Submit Records'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Correction Reason Modal */}
      {showCorrection && correctionRecord && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
          <div className="bg-white rounded-2xl max-w-md w-full shadow-2xl overflow-hidden border border-slate-100">
            <div className="p-6 border-b border-slate-100 flex justify-between items-center">
              <h3 className="font-bold text-slate-800 text-base font-sans">Attendance Correction</h3>
              <button onClick={() => setShowCorrection(false)} className="text-slate-400 hover:text-slate-600">
                <Lock className="w-5 h-5" />
              </button>
            </div>
            <form onSubmit={handleCorrectionSubmit} className="p-6 space-y-4">
              <div className="bg-slate-50 p-4 rounded-xl text-xs space-y-1 font-semibold text-slate-600">
                <p>Student: <span className="text-slate-850 font-black">{correctionRecord.name}</span></p>
                <p>Reg Number: <span className="text-slate-850 font-bold">{correctionRecord.registerNumber}</span></p>
                <p>Old Status: <span className="text-rose-600 font-extrabold uppercase">{correctionRecord.status}</span></p>
                <p>New Status: <span className="text-emerald-600 font-extrabold uppercase">{correctionStatus}</span></p>
              </div>

              <div>
                <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">Reason for Correction</label>
                <textarea
                  required
                  value={correctionReason}
                  onChange={(e) => setCorrectionReason(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg text-xs focus:outline-none focus:border-violet-500 h-24"
                  placeholder="e.g. Student was present but was marked absent accidentally."
                />
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setShowCorrection(false)}
                  className="px-4 py-2 border border-slate-200 text-slate-600 rounded-lg text-xs"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-violet-600 hover:bg-violet-700 text-white rounded-lg text-xs font-semibold shadow-sm"
                >
                  Apply Correction
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default AttendanceMarking;
