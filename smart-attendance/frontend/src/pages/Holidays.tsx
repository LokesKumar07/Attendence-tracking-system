import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { Plus, Trash2, Edit, Loader2, X, Calendar } from 'lucide-react';
import toast from 'react-hot-toast';

interface Holiday {
  id: number;
  holidayDate: string;
  description: string;
}

const Holidays: React.FC = () => {
  const [holidays, setHolidays] = useState<Holiday[]>([]);
  const [loading, setLoading] = useState(true);

  // Modal
  const [showModal, setShowModal] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);
  const [holidayDate, setHolidayDate] = useState('');
  const [description, setDescription] = useState('');

  const fetchHolidays = async () => {
    setLoading(true);
    try {
      const res = await api.get('/holidays');
      setHolidays(res.data);
    } catch (err) {
      toast.error('Failed to load holidays calendar');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHolidays();
  }, []);

  const handleOpenAdd = () => {
    setEditId(null);
    setHolidayDate('');
    setDescription('');
    setShowModal(true);
  };

  const handleOpenEdit = (h: Holiday) => {
    setEditId(h.id);
    setHolidayDate(h.holidayDate);
    setDescription(h.description);
    setShowModal(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!holidayDate || !description) {
      toast.error('Date and description are required');
      return;
    }

    const payload = { holidayDate, description };

    try {
      if (editId) {
        await api.put(`/holidays/${editId}`, payload);
        toast.success('Holiday updated');
      } else {
        await api.post('/holidays', payload);
        toast.success('Holiday scheduled');
      }
      setShowModal(false);
      fetchHolidays();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Failed to save holiday details');
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Remove this scheduled holiday from calendar?')) return;
    try {
      await api.delete(`/holidays/${id}`);
      toast.success('Holiday removed');
      fetchHolidays();
    } catch (err) {
      toast.error('Failed to remove holiday');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center bg-white border border-slate-150 rounded-2xl p-6 shadow-sm">
        <div>
          <h3 className="font-bold text-slate-800 text-base">Holidays Calendar</h3>
          <p className="text-xs text-slate-400 font-medium mt-1">Configure institutional holidays. Attendance scheduler skips holiday dates automatically.</p>
        </div>
        <button
          onClick={handleOpenAdd}
          className="flex items-center gap-2 px-4 py-2 bg-violet-600 hover:bg-violet-700 text-white rounded-lg text-sm font-semibold shadow-sm"
        >
          <Plus className="w-4 h-4" /> Add Holiday
        </button>
      </div>

      <div className="bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-sm">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-slate-50 border-b border-slate-200">
              <th className="p-4 text-xs font-bold uppercase text-slate-400">Date</th>
              <th className="p-4 text-xs font-bold uppercase text-slate-400">Description</th>
              <th className="p-4 text-xs font-bold uppercase text-slate-400 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-150">
            {loading ? (
              <tr>
                <td colSpan={3} className="p-8 text-center">
                  <Loader2 className="w-6 h-6 animate-spin text-violet-600 mx-auto" />
                </td>
              </tr>
            ) : holidays.length === 0 ? (
              <tr>
                <td colSpan={3} className="p-8 text-center text-xs text-slate-400">No scheduled holidays.</td>
              </tr>
            ) : (
              holidays.map((h) => (
                <tr key={h.id}>
                  <td className="p-4 text-xs font-bold text-slate-800 flex items-center gap-2">
                    <Calendar className="w-4 h-4 text-violet-600" />
                    {h.holidayDate}
                  </td>
                  <td className="p-4 text-xs font-semibold text-slate-800">{h.description}</td>
                  <td className="p-4 text-right">
                    <div className="flex justify-end gap-3">
                      <button onClick={() => handleOpenEdit(h)} className="text-slate-400 hover:text-violet-600">
                        <Edit className="w-4 h-4" />
                      </button>
                      <button onClick={() => handleDelete(h.id)} className="text-slate-400 hover:text-rose-600">
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
          <div className="bg-white rounded-2xl max-w-sm w-full shadow-2xl overflow-hidden border border-slate-100">
            <div className="p-6 border-b border-slate-100 flex justify-between items-center">
              <h3 className="font-bold text-slate-800 text-base">{editId ? 'Edit Holiday' : 'Add Holiday'}</h3>
              <button onClick={() => setShowModal(false)} className="text-slate-400 hover:text-slate-600">
                <X className="w-5 h-5" />
              </button>
            </div>
            <form onSubmit={handleSave} className="p-6 space-y-4">
              <div>
                <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">Holiday Date</label>
                <input
                  type="date"
                  value={holidayDate}
                  onChange={(e) => setHolidayDate(e.target.value)}
                  className="w-full px-3 py-1.5 border border-slate-200 rounded-lg text-xs"
                />
              </div>

              <div>
                <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">Description</label>
                <input
                  type="text"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="w-full px-3 py-1.5 border border-slate-200 rounded-lg text-xs"
                  placeholder="e.g. Independence Day holiday"
                />
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
                  Schedule
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Holidays;
