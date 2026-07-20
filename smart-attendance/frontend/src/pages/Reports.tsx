import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { 
  FileSpreadsheet, 
  FileText, 
  Download, 
  Search, 
  CalendarDays,
  Loader2,
  Table
} from 'lucide-react';
import toast from 'react-hot-toast';

interface ReportRow {
  registerNumber: string;
  rollNumber: string;
  studentName: string;
  totalConductedClasses: number;
  presentCount: number;
  absentCount: number;
  lateCount: number;
  onDutyCount: number;
  eligibleAttendanceCount: number;
  attendancePercentage: number;
}

interface DateWiseResponse {
  dates: string[];
  rows: Array<{
    registerNumber: string;
    rollNumber: string;
    studentName: string;
    statuses: string[];
  }>;
}

const Reports: React.FC = () => {
  const [subjects, setSubjects] = useState<any[]>([]);
  const [startDate, setStartDate] = useState(() => {
    const d = new Date();
    d.setDate(1); // default to first of month
    return d.toISOString().split('T')[0];
  });
  const [endDate, setEndDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [selectedSubject, setSelectedSubject] = useState<string>('');
  
  // Data
  const [reportRows, setReportRows] = useState<ReportRow[]>([]);
  const [dateWiseData, setDateWiseData] = useState<DateWiseResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [reportType, setReportType] = useState<'summary' | 'date-wise'>('summary');
  const [search, setSearch] = useState('');

  const fetchSubjects = async () => {
    try {
      const res = await api.get('/subjects?isActive=true');
      setSubjects(res.data);
    } catch (err) {
      //
    }
  };

  useEffect(() => {
    fetchSubjects();
  }, []);

  const handleGenerateReport = async () => {
    if (!startDate || !endDate) {
      toast.error('Start Date and End Date are mandatory');
      return;
    }
    if (new Date(endDate) < new Date(startDate)) {
      toast.error('End Date cannot be earlier than Start Date');
      return;
    }

    setLoading(true);
    try {
      let queryParams = `startDate=${startDate}&endDate=${endDate}`;
      if (selectedSubject) queryParams += `&subjectId=${selectedSubject}`;

      if (reportType === 'summary') {
        const res = await api.get(`/reports/attendance?${queryParams}`);
        setReportRows(res.data.rows);
      } else {
        const res = await api.get(`/reports/attendance/date-wise?${queryParams}`);
        setDateWiseData(res.data);
      }
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Failed to generate report');
    } finally {
      setLoading(false);
    }
  };

  const triggerExport = async (format: 'excel' | 'pdf' | 'csv') => {
    if (!startDate || !endDate) {
      toast.error('Start and End dates are mandatory to export');
      return;
    }

    try {
      let queryParams = `startDate=${startDate}&endDate=${endDate}`;
      if (selectedSubject) queryParams += `&subjectId=${selectedSubject}`;

      const res = await api.get(`/reports/attendance/export/${format}?${queryParams}`, {
        responseType: 'blob'
      });

      // Stream to browser download
      const blob = new Blob([res.data]);
      const link = document.createElement('a');
      link.href = window.URL.createObjectURL(blob);
      link.download = `Attendance_Report_${startDate}_to_${endDate}.${format === 'excel' ? 'xlsx' : format}`;
      link.click();
      toast.success(`Successfully exported report in ${format.toUpperCase()} format`);
    } catch (err) {
      toast.error('Export request failed. Please check date selections.');
    }
  };

  const filteredSummary = reportRows.filter(r => 
    r.studentName.toLowerCase().includes(search.toLowerCase()) || 
    r.registerNumber.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Parameter Control Panel */}
      <div className="bg-white border border-slate-150 rounded-2xl p-6 shadow-sm space-y-4">
        <h4 className="font-bold text-slate-800 text-base flex items-center gap-2">
          <CalendarDays className="w-5 h-5 text-violet-600" /> Filter Criteria
        </h4>
        
        <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
          <div>
            <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">Start Date</label>
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="w-full px-3 py-1.5 border border-slate-200 rounded-lg text-xs"
            />
          </div>
          <div>
            <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">End Date</label>
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="w-full px-3 py-1.5 border border-slate-200 rounded-lg text-xs"
            />
          </div>
          <div>
            <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1.5">Select Subject</label>
            <select
              value={selectedSubject}
              onChange={(e) => setSelectedSubject(e.target.value)}
              className="w-full px-3 py-1.5 border border-slate-200 rounded-lg text-xs"
            >
              <option value="">All Subjects</option>
              {subjects.map((sub) => (
                <option key={sub.id} value={sub.id}>[{sub.code}] {sub.name}</option>
              ))}
            </select>
          </div>
          <div className="flex items-end">
            <button
              onClick={handleGenerateReport}
              className="w-full py-2 bg-violet-600 hover:bg-violet-700 text-white rounded-lg text-xs font-bold shadow-sm transition-all"
            >
              Generate Report
            </button>
          </div>
        </div>
      </div>

      {/* Mode Selectors & Exports */}
      <div className="flex flex-col md:flex-row justify-between items-center gap-4 bg-white border border-slate-150 rounded-2xl p-6 shadow-sm">
        <div className="flex gap-2 w-full md:w-auto">
          <button
            onClick={() => setReportType('summary')}
            className={`px-4 py-2 text-xs font-semibold rounded-lg ${
              reportType === 'summary' 
                ? 'bg-violet-600 text-white shadow-sm'
                : 'border border-slate-200 text-slate-600 hover:bg-slate-50'
            }`}
          >
            Summary Sheet
          </button>
          <button
            onClick={() => setReportType('date-wise')}
            className={`px-4 py-2 text-xs font-semibold rounded-lg ${
              reportType === 'date-wise' 
                ? 'bg-violet-600 text-white shadow-sm'
                : 'border border-slate-200 text-slate-600 hover:bg-slate-50'
            }`}
          >
            Date-wise Grid
          </button>
        </div>

        <div className="flex gap-2 w-full md:w-auto">
          <button onClick={() => triggerExport('excel')} className="flex-1 md:flex-initial flex items-center justify-center gap-2 px-3 py-2 border border-emerald-250 text-emerald-800 hover:bg-emerald-50 rounded-lg text-xs font-semibold">
            <FileSpreadsheet className="w-4 h-4" /> Excel
          </button>
          <button onClick={() => triggerExport('pdf')} className="flex-1 md:flex-initial flex items-center justify-center gap-2 px-3 py-2 border border-rose-250 text-rose-800 hover:bg-rose-50 rounded-lg text-xs font-semibold">
            <FileText className="w-4 h-4" /> PDF
          </button>
          <button onClick={() => triggerExport('csv')} className="flex-1 md:flex-initial flex items-center justify-center gap-2 px-3 py-2 border border-slate-250 text-slate-700 hover:bg-slate-50 rounded-lg text-xs font-semibold">
            <Download className="w-4 h-4" /> CSV
          </button>
        </div>
      </div>

      {/* Main Table view */}
      <div className="bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-sm">
        {loading ? (
          <div className="p-12 text-center">
            <Loader2 className="w-8 h-8 animate-spin text-violet-600 mx-auto" />
            <p className="text-xs text-slate-400 mt-2 font-semibold">Generating report matrix...</p>
          </div>
        ) : reportType === 'summary' ? (
          <>
            <div className="p-4 border-b border-slate-100 flex items-center gap-2">
              <Search className="w-4 h-4 text-slate-400" />
              <input
                type="text"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="w-full text-xs text-slate-600 outline-none"
                placeholder="Filter summary list by student name..."
              />
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-slate-50 border-b border-slate-200">
                    <th className="p-4 text-xs font-bold uppercase text-slate-400">Register No</th>
                    <th className="p-4 text-xs font-bold uppercase text-slate-400">Roll No</th>
                    <th className="p-4 text-xs font-bold uppercase text-slate-400">Student Name</th>
                    <th className="p-4 text-xs font-bold uppercase text-slate-400 text-center">Conducted</th>
                    <th className="p-4 text-xs font-bold uppercase text-slate-400 text-center">Present</th>
                    <th className="p-4 text-xs font-bold uppercase text-slate-400 text-center">Absent</th>
                    <th className="p-4 text-xs font-bold uppercase text-slate-400 text-center">Late</th>
                    <th className="p-4 text-xs font-bold uppercase text-slate-400 text-center">On-Duty</th>
                    <th className="p-4 text-xs font-bold uppercase text-slate-400 text-right">Percentage</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-150">
                  {filteredSummary.length === 0 ? (
                    <tr>
                      <td colSpan={9} className="p-8 text-center text-xs text-slate-400">
                        No report records found. Click Generate Report to load.
                      </td>
                    </tr>
                  ) : (
                    filteredSummary.map((row, i) => (
                      <tr key={i}>
                        <td className="p-4 text-xs font-bold text-slate-800">{row.registerNumber}</td>
                        <td className="p-4 text-xs text-slate-500">{row.rollNumber}</td>
                        <td className="p-4 text-xs font-semibold text-slate-800">{row.studentName}</td>
                        <td className="p-4 text-xs text-slate-500 text-center">{row.totalConductedClasses}</td>
                        <td className="p-4 text-xs text-slate-500 text-center">{row.presentCount}</td>
                        <td className="p-4 text-xs text-slate-500 text-center">{row.absentCount}</td>
                        <td className="p-4 text-xs text-slate-500 text-center">{row.lateCount}</td>
                        <td className="p-4 text-xs text-slate-500 text-center">{row.onDutyCount}</td>
                        <td className="p-4 text-xs font-black text-right">
                          <span className={`${
                            row.attendancePercentage < 75 ? 'text-rose-600' : 'text-emerald-600'
                          }`}>
                            {row.attendancePercentage}%
                          </span>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse table-fixed min-w-[700px]">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200">
                  <th className="p-4 text-xs font-bold uppercase text-slate-400 w-24">Reg No</th>
                  <th className="p-4 text-xs font-bold uppercase text-slate-400 w-40">Student Name</th>
                  {dateWiseData?.dates.map((date) => (
                    <th key={date} className="p-3 text-xs font-bold text-slate-400 text-center">
                      {date.substring(5)} {/* MM-DD format for columns */}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-150">
                {!dateWiseData || dateWiseData.rows.length === 0 ? (
                  <tr>
                    <td colSpan={2} className="p-8 text-center text-xs text-slate-400">
                      No date-wise records found. Click Generate Report to load.
                    </td>
                  </tr>
                ) : (
                  dateWiseData.rows.map((row, i) => (
                    <tr key={i}>
                      <td className="p-4 text-xs font-bold text-slate-800">{row.registerNumber}</td>
                      <td className="p-4 text-xs font-semibold text-slate-800">{row.studentName}</td>
                      {row.statuses.map((st, j) => (
                        <td key={j} className="p-3 text-xs font-bold text-center">
                          <span className={`${
                            st === 'P' ? 'text-emerald-600 bg-emerald-50 px-1.5 py-0.5 rounded' : 
                            st === 'A' ? 'text-rose-600 bg-rose-50 px-1.5 py-0.5 rounded' : 
                            st === 'L' ? 'text-amber-600 bg-amber-50 px-1.5 py-0.5 rounded' : 
                            st === 'OD' ? 'text-indigo-600 bg-indigo-50 px-1.5 py-0.5 rounded' : 
                            'text-slate-300'
                          }`}>
                            {st}
                          </span>
                        </td>
                      ))}
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default Reports;
