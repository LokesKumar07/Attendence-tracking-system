import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { Plus, Edit, Loader2, X, AlertTriangle } from 'lucide-react';
import toast from 'react-hot-toast';

interface TimetableEntry {
  id: number;
  timetableId: number;
  dayOrder: number;
  periodNumber: number;
  startTime: string;
  endTime: string;
  subjectId: number | null;
  subjectCode: string | null;
  subjectName: string | null;
  entryType: string;
}

const DAY_ORDER_LABELS: Record<number, string> = {
  1: 'Day Order I',
  2: 'Day Order II',
  3: 'Day Order III',
  4: 'Day Order IV',
  5: 'Day Order V',
  6: 'Day Order VI',
};

const Timetable: React.FC = () => {
  const [entries, setEntries] = useState<TimetableEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [subjects, setSubjects] = useState<any[]>([]);

  // Modal edit
  const [showModal, setShowModal] = useState(false);
  const [selectedEntry, setSelectedEntry] = useState<TimetableEntry | null>(null);
  const [subjectId, setSubjectId] = useState<string>('');
  const [entryType, setEntryType] = useState('CLASS');
  const [startTime, setStartTime] = useState('09:35');
  const [endTime, setEndTime] = useState('10:30');

  const dayOrders = [1, 2, 3, 4, 5, 6];
  const periods = [1, 2, 3, 4, 5, 6];

  const fetchTimetable = async () => {
    setLoading(true);
    try {
      const res = await api.get('/timetable');
      setEntries(res.data);
      const subRes = await api.get('/subjects?isActive=true');
      setSubjects(subRes.data);
    } catch (err) {
      toast.error('Failed to load timetable configurations');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTimetable();
  }, []);

  const handleOpenEdit = (entry: TimetableEntry) => {
    setSelectedEntry(entry);
    setSubjectId(entry.subjectId ? String(entry.subjectId) : '');
    setEntryType(entry.entryType);
    setStartTime(entry.startTime.substring(0, 5));
    setEndTime(entry.endTime.substring(0, 5));
    setShowModal(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedEntry) return;

    const payload = {
      timetableId: selectedEntry.timetableId,
      dayOrder: selectedEntry.dayOrder,
      periodNumber: selectedEntry.periodNumber,
      startTime: startTime.length === 5 ? startTime + ':00' : startTime,
      endTime: endTime.length === 5 ? endTime + ':00' : endTime,
      subjectId: subjectId ? Number(subjectId) : null,
      entryType: subjectId ? 'CLASS' : entryType
    };

    try {
      await api.put(`/timetable/entries/${selectedEntry.id}`, payload);
      toast.success('Timetable entry updated successfully');
      setShowModal(false);
      fetchTimetable();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Conflict detected in slot timetable');
    }
  };

  const getCell = (dayOrder: number, pNum: number) => {
    return entries.find(e => e.dayOrder === dayOrder && e.periodNumber === pNum);
  };

  return (
    <div className="space-y-6">
      <div className="bg-white border border-slate-150 rounded-2xl p-6 shadow-sm flex items-start gap-4">
        <AlertTriangle className="w-10 h-10 text-amber-500 flex-shrink-0" />
        <div>
          <h3 className="font-bold text-slate-800 text-base">Day Order Timetable Grid Configuration</h3>
          <p className="text-xs text-slate-400 font-medium mt-1 leading-relaxed">
            Ms. V. Lathika's current active periods are shown below. Click on any slot to change the assigned subject course, time limits or type (Class, Lab, Break, Lunch, Free). Clash validation is automatically processed in the backend database constraints.
          </p>
        </div>
      </div>

      <div className="bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-center border-collapse table-fixed min-w-[800px]">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-200">
                <th className="p-4 text-xs font-bold uppercase text-slate-400 w-32">Day Order / Slot</th>
                {periods.map(p => (
                  <th key={p} className="p-4 text-xs font-bold uppercase text-slate-400">
                    Period {p}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-150">
              {loading ? (
                <tr>
                  <td colSpan={7} className="p-8">
                    <Loader2 className="w-6 h-6 animate-spin text-violet-600 mx-auto" />
                  </td>
                </tr>
              ) : (
                dayOrders.map(dayOrder => (
                  <tr key={dayOrder}>
                    <td className="p-4 bg-slate-50/50 text-xs font-bold text-slate-700 text-left capitalize">
                      {DAY_ORDER_LABELS[dayOrder]}
                    </td>
                    {periods.map(pNum => {
                      const cell = getCell(dayOrder, pNum);
                      const isFree = !cell || cell.entryType === 'FREE' || !cell.subjectName;
                      
                      return (
                        <td 
                          key={pNum} 
                          onClick={() => cell && handleOpenEdit(cell)}
                          className={`p-3 text-xs border border-slate-100 cursor-pointer transition-all hover:bg-violet-50/40 ${
                            isFree ? 'bg-slate-50/20' : 'bg-violet-50/60'
                          }`}
                        >
                          {cell ? (
                            <div className="space-y-1">
                              <p className={`font-bold ${isFree ? 'text-slate-400' : 'text-violet-900'}`}>
                                {cell.subjectName || 'FREE'}
                              </p>
                              <p className="text-[10px] text-slate-400">
                                {cell.startTime.substring(0, 5)} - {cell.endTime.substring(0, 5)}
                              </p>
                              {cell.subjectCode && (
                                <span className="bg-violet-100 text-violet-700 text-[9px] px-1 py-0.5 rounded font-semibold uppercase">
                                  {cell.subjectCode}
                                </span>
                              )}
                            </div>
                          ) : (
                            <span className="text-slate-300">-</span>
                          )}
                        </td>
                      );
                    })}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {showModal && selectedEntry && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
          <div className="bg-white rounded-2xl max-w-sm w-full shadow-2xl overflow-hidden border border-slate-100">
            <div className="p-6 border-b border-slate-100 flex justify-between items-center">
              <h3 className="font-bold text-slate-800 text-base">Modify Period Slot</h3>
              <button onClick={() => setShowModal(false)} className="text-slate-400 hover:text-slate-600">
                <X className="w-5 h-5" />
              </button>
            </div>
            <form onSubmit={handleSave} className="p-6 space-y-4">
              <div className="bg-slate-50 p-3 rounded-lg text-xs space-y-1">
                <p className="text-slate-500 font-medium">Day: <span className="text-slate-800 font-bold uppercase">{DAY_ORDER_LABELS[selectedEntry.dayOrder]}</span></p>
                <p className="text-slate-500 font-medium">Period: <span className="text-slate-800 font-bold">Period {selectedEntry.periodNumber}</span></p>
              </div>

              <div>
                <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">Assign Subject</label>
                <select
                  value={subjectId}
                  onChange={(e) => {
                    setSubjectId(e.target.value);
                    if (e.target.value) setEntryType('CLASS');
                  }}
                  className="w-full px-3 py-1.5 border border-slate-200 rounded-lg text-xs"
                >
                  <option value="">FREE PERIOD / BREAK / LUNCH</option>
                  {subjects.map((sub) => (
                    <option key={sub.id} value={sub.id}>[{sub.code}] {sub.name}</option>
                  ))}
                </select>
              </div>

              {!subjectId && (
                <div>
                  <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">Entry Type</label>
                  <select
                    value={entryType}
                    onChange={(e) => setEntryType(e.target.value)}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg text-xs"
                  >
                    <option value="FREE">FREE</option>
                    <option value="BREAK">BREAK</option>
                    <option value="LUNCH">LUNCH</option>
                  </select>
                </div>
              )}

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">Start Time</label>
                  <input
                    type="time"
                    value={startTime}
                    onChange={(e) => setStartTime(e.target.value)}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg text-xs"
                  />
                </div>
                <div>
                  <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">End Time</label>
                  <input
                    type="time"
                    value={endTime}
                    onChange={(e) => setEndTime(e.target.value)}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg text-xs"
                  />
                </div>
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 border border-slate-200 text-slate-600 rounded-lg text-xs"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-violet-600 hover:bg-violet-700 text-white rounded-lg text-xs font-semibold"
                >
                  Apply Slot
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Timetable;
