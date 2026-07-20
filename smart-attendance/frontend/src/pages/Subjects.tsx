import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { Plus, Trash2, Edit, Loader2, X } from 'lucide-react';
import toast from 'react-hot-toast';

interface Subject {
  id: number;
  code: string;
  name: string;
  type: string; // THEORY, LABORATORY
  semesterId: number;
  semesterName: string;
  isActive: boolean;
}

const Subjects: React.FC = () => {
  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [loading, setLoading] = useState(true);
  
  // Modal state
  const [showModal, setShowModal] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);
  const [code, setCode] = useState('');
  const [name, setName] = useState('');
  const [type, setType] = useState('THEORY');
  const [semesterId, setSemesterId] = useState('1');
  const [isActive, setIsActive] = useState(true);

  const fetchSubjects = async () => {
    setLoading(true);
    try {
      const res = await api.get('/subjects');
      setSubjects(res.data);
    } catch (err) {
      toast.error('Failed to load subjects');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSubjects();
  }, []);

  const handleOpenAdd = () => {
    setEditId(null);
    setCode('');
    setName('');
    setType('THEORY');
    setSemesterId('1');
    setIsActive(true);
    setShowModal(true);
  };

  const handleOpenEdit = (s: Subject) => {
    setEditId(s.id);
    setCode(s.code);
    setName(s.name);
    setType(s.type);
    setSemesterId(String(s.semesterId));
    setIsActive(s.isActive);
    setShowModal(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!code || !name) {
      toast.error('Subject Code and Name are mandatory');
      return;
    }

    const payload = {
      code,
      name,
      type,
      semesterId: Number(semesterId),
      isActive
    };

    try {
      if (editId) {
        await api.put(`/subjects/${editId}`, payload);
        toast.success('Subject records updated');
      } else {
        await api.post('/subjects', payload);
        toast.success('Subject successfully created');
      }
      setShowModal(false);
      fetchSubjects();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Failed to save subject');
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Are you sure you want to deactivate this subject?')) return;
    try {
      await api.delete(`/subjects/${id}`);
      toast.success('Subject state set to inactive');
      fetchSubjects();
    } catch (err) {
      toast.error('Failed to deactivate subject');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center bg-white border border-slate-150 rounded-2xl p-6 shadow-sm">
        <div>
          <h3 className="font-bold text-slate-800 text-base">Course Subjects List</h3>
          <p className="text-xs text-slate-400 font-medium mt-1">Configure subjects, syllabus code and theory/lab categories.</p>
        </div>
        <button
          onClick={handleOpenAdd}
          className="flex items-center gap-2 px-4 py-2 bg-violet-600 hover:bg-violet-700 text-white rounded-lg text-sm font-semibold shadow-sm"
        >
          <Plus className="w-4 h-4" /> Add Subject
        </button>
      </div>

      <div className="bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-sm">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-slate-50 border-b border-slate-200">
              <th className="p-4 text-xs font-bold uppercase text-slate-400">Subject Code</th>
              <th className="p-4 text-xs font-bold uppercase text-slate-400">Subject Name</th>
              <th className="p-4 text-xs font-bold uppercase text-slate-400">Type</th>
              <th className="p-4 text-xs font-bold uppercase text-slate-400">Semester</th>
              <th className="p-4 text-xs font-bold uppercase text-slate-400">State</th>
              <th className="p-4 text-xs font-bold uppercase text-slate-400 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-150">
            {loading ? (
              <tr>
                <td colSpan={6} className="p-8 text-center">
                  <Loader2 className="w-6 h-6 animate-spin text-violet-600 mx-auto" />
                </td>
              </tr>
            ) : subjects.length === 0 ? (
              <tr>
                <td colSpan={6} className="p-8 text-center text-xs text-slate-400">No subjects configured. Add one to start.</td>
              </tr>
            ) : (
              subjects.map((s) => (
                <tr key={s.id}>
                  <td className="p-4 text-xs font-bold text-slate-800">{s.code}</td>
                  <td className="p-4 text-xs font-semibold text-slate-800">{s.name}</td>
                  <td className="p-4 text-xs text-slate-500">{s.type}</td>
                  <td className="p-4 text-xs text-slate-500">{s.semesterName}</td>
                  <td className="p-4">
                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded uppercase ${
                      s.isActive ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-800'
                    }`}>
                      {s.isActive ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="p-4 text-right">
                    <div className="flex justify-end gap-3">
                      <button onClick={() => handleOpenEdit(s)} className="text-slate-400 hover:text-violet-600">
                        <Edit className="w-4 h-4" />
                      </button>
                      <button onClick={() => handleDelete(s.id)} className="text-slate-400 hover:text-rose-600">
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
          <div className="bg-white rounded-2xl max-w-md w-full shadow-2xl overflow-hidden border border-slate-100">
            <div className="p-6 border-b border-slate-100 flex justify-between items-center">
              <h3 className="font-bold text-slate-800 text-base">{editId ? 'Edit Subject' : 'New Subject'}</h3>
              <button onClick={() => setShowModal(false)} className="text-slate-400 hover:text-slate-600">
                <X className="w-5 h-5" />
              </button>
            </div>
            <form onSubmit={handleSave} className="p-6 space-y-4">
              <div>
                <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">Subject Code</label>
                <input
                  type="text"
                  value={code}
                  onChange={(e) => setCode(e.target.value)}
                  className="w-full px-3 py-1.5 border border-slate-200 rounded-lg text-xs"
                  placeholder="AGRI101"
                />
              </div>

              <div>
                <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">Subject Name</label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="w-full px-3 py-1.5 border border-slate-200 rounded-lg text-xs"
                  placeholder="Agricultural Economy of India"
                />
              </div>

              <div>
                <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">Type</label>
                <select
                  value={type}
                  onChange={(e) => setType(e.target.value)}
                  className="w-full px-3 py-1.5 border border-slate-200 rounded-lg text-xs"
                >
                  <option value="THEORY">THEORY</option>
                  <option value="LABORATORY">LABORATORY</option>
                </select>
              </div>

              <div className="flex items-center gap-2 py-2">
                <input
                  type="checkbox"
                  id="subAct"
                  checked={isActive}
                  onChange={(e) => setIsActive(e.target.checked)}
                  className="rounded border-slate-300 text-violet-600 focus:ring-violet-500"
                />
                <label htmlFor="subAct" className="text-xs font-semibold text-slate-600">Active Course</label>
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
                  Save Subject
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Subjects;
