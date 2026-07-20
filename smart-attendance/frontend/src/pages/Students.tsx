import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { 
  Plus, 
  Search, 
  Filter, 
  Upload, 
  Trash2, 
  Edit, 
  Loader2, 
  X,
  FileSpreadsheet
} from 'lucide-react';
import toast from 'react-hot-toast';

interface Student {
  id: number;
  registerNumber: string;
  rollNumber: string;
  name: string;
  departmentId: number;
  departmentName: string;
  academicYearId: number;
  academicYearName: string;
  semesterId: number;
  semesterName: string;
  classSectionId: number;
  classSectionName: string;
  yearOfStudy: number;
  email: string;
  phoneNumber: string;
  isActive: boolean;
  subjectIds?: number[];
}

const Students: React.FC = () => {
  const [students, setStudents] = useState<Student[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [subjectFilter, setSubjectFilter] = useState<string>('all');
  const [sectionFilter, setSectionFilter] = useState<string>('all');
  
  // Options
  const [depts, setDepts] = useState<any[]>([]);
  const [sections, setSections] = useState<any[]>([]);
  const [semesters, setSemesters] = useState<any[]>([]);
  const [academicYears, setAcademicYears] = useState<any[]>([]);

  // Modal
  const [showModal, setShowModal] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);
  const [name, setName] = useState('');
  const [registerNumber, setRegisterNumber] = useState('');
  const [rollNumber, setRollNumber] = useState('');
  const [departmentId, setDepartmentId] = useState('');
  const [academicYearId, setAcademicYearId] = useState('');
  const [semesterId, setSemesterId] = useState('');
  const [classSectionId, setClassSectionId] = useState('');
  const [yearOfStudy, setYearOfStudy] = useState(1);
  const [email, setEmail] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [isActive, setIsActive] = useState(true);
  const [allSubjects, setAllSubjects] = useState<any[]>([]);
  const [selectedSubjectIds, setSelectedSubjectIds] = useState<number[]>([]);

  // File Upload
  const [uploading, setUploading] = useState(false);

  const fetchMetadata = async () => {
    try {
      // Just fetch standard fallback lists or generate default entries if DB is fresh
      // To keep it simple, we will fetch the list of departments, sections etc
      // In a real app we would have dedicated CRUD. Let's fetch them or fallback.
      const stdList = await api.get('/students');
      
      // Extract unique list of depts to render filters
      const uniqueDepts = Array.from(new Set(stdList.data.map((s: any) => JSON.stringify({ id: s.departmentId, name: s.departmentName }))));
      setDepts(uniqueDepts.map((d: any) => JSON.parse(d)));

      // Fetch all class sections seeded from DB
      let fetchedSections: any[] = [];
      try {
        const sectionsRes = await api.get('/class-sections');
        fetchedSections = sectionsRes.data;
      } catch (err) {
        const uniqueSections = Array.from(new Set(stdList.data.map((s: any) => JSON.stringify({ id: s.classSectionId, name: s.classSectionName }))));
        fetchedSections = uniqueSections.map((s: any) => JSON.parse(s));
      }

      if (fetchedSections.length === 0) {
        fetchedSections = [
          { id: 1, name: 'I B.Com' },
          { id: 2, name: 'II B.Com' },
          { id: 3, name: 'III B.Com' },
          { id: 4, name: 'II B.Com (PA)' }
        ];
      }
      setSections(fetchedSections);
      
      // Defaults if empty
      if (uniqueDepts.length === 0) {
        setDepts([{ id: 1, name: 'PG and Research Department of Commerce' }]);
      }
      
      setSemesters([{ id: 1, name: 'ODD' }]);
      setAcademicYears([{ id: 1, name: '2026-2027' }]);

      const subsRes = await api.get('/subjects');
      setAllSubjects(subsRes.data);

    } catch (err) {
      // Fallback
    }
  };

  const fetchStudents = async () => {
    setLoading(true);
    try {
      const act = statusFilter === 'active' ? true : statusFilter === 'inactive' ? false : undefined;
      const subject = subjectFilter !== 'all' ? subjectFilter : undefined;
      const sec = sectionFilter !== 'all' ? sectionFilter : undefined;
      
      let query = `/students?search=${search}`;
      if (act !== undefined) query += `&isActive=${act}`;
      if (subject) query += `&subjectId=${subject}`;
      if (sec) query += `&classSectionId=${sec}`;
      
      const res = await api.get(query);
      setStudents(res.data);
    } catch (err) {
      toast.error('Failed to load students list');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMetadata();
  }, []);

  useEffect(() => {
    fetchStudents();
  }, [search, statusFilter, subjectFilter, sectionFilter]);

  const handleOpenAdd = () => {
    setEditId(null);
    setName('');
    setRegisterNumber('');
    setRollNumber('');
    setDepartmentId(depts[0]?.id || '1');
    setAcademicYearId(academicYears[0]?.id || '1');
    setSemesterId(semesters[0]?.id || '1');
    setClassSectionId(sections[0]?.id || '1');
    setYearOfStudy(1);
    setEmail('');
    setPhoneNumber('');
    setIsActive(true);
    setSelectedSubjectIds([]);
    setShowModal(true);
  };

  const handleOpenEdit = (s: Student) => {
    setEditId(s.id);
    setName(s.name);
    setRegisterNumber(s.registerNumber);
    setRollNumber(s.rollNumber);
    setDepartmentId(String(s.departmentId));
    setAcademicYearId(String(s.academicYearId));
    setSemesterId(String(s.semesterId));
    setClassSectionId(String(s.classSectionId));
    setYearOfStudy(s.yearOfStudy);
    setEmail(s.email || '');
    setPhoneNumber(s.phoneNumber || '');
    setIsActive(s.isActive);
    setSelectedSubjectIds(s.subjectIds || []);
    setShowModal(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name || !registerNumber) {
      toast.error('Name and Register number are mandatory');
      return;
    }

    const payload = {
      name,
      registerNumber,
      rollNumber: registerNumber,
      departmentId: Number(departmentId),
      academicYearId: Number(academicYearId),
      semesterId: Number(semesterId),
      classSectionId: Number(classSectionId),
      yearOfStudy: Number(yearOfStudy),
      email: email || null,
      phoneNumber: phoneNumber || null,
      isActive,
      subjectIds: selectedSubjectIds
    };

    try {
      if (editId) {
        await api.put(`/students/${editId}`, payload);
        toast.success('Student records updated successfully');
      } else {
        await api.post('/students', payload);
        toast.success('Student added successfully');
      }
      setShowModal(false);
      fetchStudents();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Failed to save student details');
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Are you sure you want to deactivate or remove this student?')) return;
    try {
      await api.delete(`/students/${id}`);
      toast.success('Student removed/deactivated');
      fetchStudents();
    } catch (err) {
      toast.error('Failed to update student state');
    }
  };

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploading(true);
    const formData = new FormData();
    formData.append('file', file);

    try {
      await api.post('/students/import', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      toast.success('Student database updated from spreadsheet successfully');
      fetchStudents();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Failed to parse student spreadsheet');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Search & Actions banner */}
      <div className="flex flex-col md:flex-row justify-between items-center gap-4 bg-white border border-slate-150 rounded-2xl p-6 shadow-sm">
        <div className="relative w-full md:w-80">
          <Search className="absolute left-3 top-3 w-4 h-4 text-slate-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-slate-200 focus:outline-none focus:border-violet-500 rounded-lg text-sm"
            placeholder="Search by name or reg number..."
          />
        </div>

        <div className="flex items-center gap-3 w-full md:w-auto">
          {/* File Upload Trigger */}
          <label className="flex items-center justify-center gap-2 px-4 py-2 border border-slate-200 text-slate-600 rounded-lg text-sm font-semibold hover:bg-slate-50 cursor-pointer w-full md:w-auto">
            {uploading ? (
              <Loader2 className="w-4 h-4 animate-spin text-slate-500" />
            ) : (
              <Upload className="w-4 h-4 text-slate-400" />
            )}
            Import Sheet
            <input type="file" onChange={handleFileUpload} accept=".csv,.xlsx" className="hidden" />
          </label>

          <button
            onClick={handleOpenAdd}
            className="flex items-center justify-center gap-2 px-4 py-2 bg-violet-600 hover:bg-violet-700 text-white rounded-lg text-sm font-semibold w-full md:w-auto shadow-sm"
          >
            <Plus className="w-4 h-4" /> Add Student
          </button>
        </div>
      </div>

      {/* Filter panel */}
      <div className="flex flex-wrap gap-4 bg-white border border-slate-150 rounded-2xl p-6 shadow-sm">
        <div className="flex items-center gap-2 text-xs font-bold text-slate-400 uppercase tracking-wider">
          <Filter className="w-4 h-4" /> Filters
        </div>
        
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="px-3 py-1.5 border border-slate-200 rounded-md text-xs font-semibold focus:outline-none focus:border-violet-500"
        >
          <option value="all">All States</option>
          <option value="active">Active Only</option>
          <option value="inactive">Inactive Only</option>
        </select>

        <select
          value={subjectFilter}
          onChange={(e) => setSubjectFilter(e.target.value)}
          className="px-3 py-1.5 border border-slate-200 rounded-md text-xs font-semibold focus:outline-none focus:border-violet-500 max-w-xs"
        >
          <option value="all">All Subjects</option>
          {allSubjects.map((sub) => (
            <option key={sub.id} value={sub.id}>{sub.name}</option>
          ))}
        </select>

        <select
          value={sectionFilter}
          onChange={(e) => setSectionFilter(e.target.value)}
          className="px-3 py-1.5 border border-slate-200 rounded-md text-xs font-semibold focus:outline-none focus:border-violet-500"
        >
          <option value="all">All Sections</option>
          {sections.map((s) => (
            <option key={s.id} value={s.id}>{s.name}</option>
          ))}
        </select>
      </div>

      {/* Table grid */}
      <div className="bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-200">
                <th className="p-4 text-xs font-bold uppercase tracking-wider text-slate-400">Register No</th>
                <th className="p-4 text-xs font-bold uppercase tracking-wider text-slate-400">Student Name</th>
                <th className="p-4 text-xs font-bold uppercase tracking-wider text-slate-400">Section</th>
                <th className="p-4 text-xs font-bold uppercase tracking-wider text-slate-400">Year</th>
                <th className="p-4 text-xs font-bold uppercase tracking-wider text-slate-400">State</th>
                <th className="p-4 text-xs font-bold uppercase tracking-wider text-slate-400 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-150">
              {loading ? (
                <tr>
                  <td colSpan={6} className="p-8 text-center">
                    <Loader2 className="w-6 h-6 animate-spin text-violet-600 mx-auto mb-2" />
                    <p className="text-xs text-slate-400 font-medium">Fetching students database...</p>
                  </td>
                </tr>
              ) : students.length === 0 ? (
                <tr>
                  <td colSpan={6} className="p-8 text-center">
                    <p className="text-sm font-semibold text-slate-700">No student records found</p>
                    <p className="text-xs text-slate-400 mt-1">Import a CSV sheet or add one manually above.</p>
                  </td>
                </tr>
              ) : (
                students.map((s) => (
                  <tr key={s.id}>
                    <td className="p-4 text-xs font-bold text-slate-800">{s.registerNumber}</td>
                    <td className="p-4 text-xs font-semibold text-slate-800">{s.name}</td>
                    <td className="p-4 text-xs text-slate-500">{s.classSectionName}</td>
                    <td className="p-4 text-xs text-slate-500">{s.yearOfStudy} Year</td>
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
      </div>

      {/* Save Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
          <div className="bg-white rounded-2xl max-w-lg w-full shadow-2xl overflow-hidden border border-slate-100 animate-in fade-in zoom-in-95 duration-150">
            <div className="p-6 border-b border-slate-100 flex justify-between items-center">
              <h3 className="font-bold text-slate-800 text-base">
                {editId ? 'Modify Student Record' : 'Enroll New Student'}
              </h3>
              <button onClick={() => setShowModal(false)} className="text-slate-400 hover:text-slate-600">
                <X className="w-5 h-5" />
              </button>
            </div>
            <form onSubmit={handleSave} className="p-6 space-y-4">
              <div>
                <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">Register Number</label>
                <input
                  type="text"
                  value={registerNumber}
                  onChange={(e) => setRegisterNumber(e.target.value)}
                  className="w-full px-3 py-1.5 border border-slate-200 rounded-lg focus:outline-none focus:border-violet-500 text-xs"
                  placeholder="23IT001"
                />
              </div>

              <div>
                <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">Student Full Name</label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="w-full px-3 py-1.5 border border-slate-200 rounded-lg focus:outline-none focus:border-violet-500 text-xs"
                  placeholder="Arun Kumar"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">Section</label>
                  <select
                    value={classSectionId}
                    onChange={(e) => setClassSectionId(e.target.value)}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg focus:outline-none focus:border-violet-500 text-xs"
                  >
                    {sections.map((s) => (
                      <option key={s.id} value={s.id}>{s.name}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">Year of Study</label>
                  <input
                    type="number"
                    value={yearOfStudy}
                    onChange={(e) => setYearOfStudy(Number(e.target.value))}
                    min={1}
                    max={4}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg focus:outline-none focus:border-violet-500 text-xs"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">Email</label>
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg focus:outline-none focus:border-violet-500 text-xs"
                    placeholder="arun@ppg.edu.in"
                  />
                </div>
                <div>
                  <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">Phone Number</label>
                  <input
                    type="text"
                    value={phoneNumber}
                    onChange={(e) => setPhoneNumber(e.target.value)}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg focus:outline-none focus:border-violet-500 text-xs"
                    placeholder="9876543210"
                  />
                </div>
              </div>

              <div className="flex items-center gap-2 py-2">
                <input
                  type="checkbox"
                  id="activeCheck"
                  checked={isActive}
                  onChange={(e) => setIsActive(e.target.checked)}
                  className="rounded border-slate-300 text-violet-600 focus:ring-violet-500"
                />
                <label htmlFor="activeCheck" className="text-xs font-semibold text-slate-600">Active status (marked present eligible)</label>
              </div>

              <div>
                <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">Enrolled Subjects</label>
                <div className="border border-slate-200 rounded-lg p-3 max-h-36 overflow-y-auto space-y-2 bg-slate-50/20">
                  {allSubjects.length === 0 ? (
                    <span className="text-[10px] text-slate-400">No subjects loaded. Add subjects first.</span>
                  ) : (
                    allSubjects.map((sub) => (
                      <label key={sub.id} className="flex items-center gap-2 text-xs font-semibold text-slate-700 cursor-pointer hover:text-slate-900">
                        <input
                          type="checkbox"
                          checked={selectedSubjectIds.includes(sub.id)}
                          onChange={(e) => {
                            if (e.target.checked) {
                              setSelectedSubjectIds([...selectedSubjectIds, sub.id]);
                            } else {
                              setSelectedSubjectIds(selectedSubjectIds.filter(id => id !== sub.id));
                            }
                          }}
                          className="rounded border-slate-300 text-violet-600 focus:ring-violet-500"
                        />
                        <span>{sub.name} <span className="text-[10px] text-slate-400 font-mono">({sub.code})</span></span>
                      </label>
                    ))
                  )}
                </div>
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 border border-slate-200 text-slate-600 rounded-lg text-xs font-semibold hover:bg-slate-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-violet-600 hover:bg-violet-700 text-white rounded-lg text-xs font-semibold shadow-sm"
                >
                  Save Record
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Students;
