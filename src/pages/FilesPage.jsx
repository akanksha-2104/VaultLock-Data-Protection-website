import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosInstance';
import { AppLayout, AlertBanner, StatCard, SectionCard, TagPill, IconButton, PrimaryButton, PageHeader } from '../components/UI';

function getFileType(filename = '') {
  const ext = filename.split('.').pop().toLowerCase();
  if (ext === 'pdf') return { label: 'PDF', bg: '#FCEBEB', color: '#A32D2D' };
  if (['doc', 'docx'].includes(ext)) return { label: 'DOC', bg: '#E6F1FB', color: '#0C447C' };
  if (['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(ext)) return { label: 'IMG', bg: '#EAF3DE', color: '#3B6D11' };
  return { label: 'FILE', bg: '#F1EFE8', color: '#444441' };
}

function formatSize(bytes) {
  if (!bytes) return '—';
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / 1048576).toFixed(1) + ' MB';
}

function formatDate(dateStr) {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

export default function FilesPage() {
  const [files, setFiles]        = useState([]);
  const [loading, setLoading]    = useState(true);
  const [error, setError]        = useState('');
  const [notification, setNotif] = useState(null);
  const navigate = useNavigate();

  const notify = (msg, type = 'success') => {
    setNotif({ msg, type });
    setTimeout(() => setNotif(null), 3500);
  };

  useEffect(() => {
    setLoading(true);
    api.get('/api/files')
      .then(res => {
        setFiles(Array.isArray(res.data) ? res.data : []);
        setError('');
      })
      .catch(err => {
        // 401 is already handled by AuthContext (session check).
        // For other errors show a message but don't redirect.
        if (err.response?.status !== 401) {
          setError('Failed to load files. Make sure the backend is running.');
        }
      })
      .finally(() => setLoading(false));
  }, []);

  const handleDownload = async (file) => {
    try {
      const res = await api.get(`/api/files/${file.id}/download`, { responseType: 'blob' });
      const url = URL.createObjectURL(new Blob([res.data]));
      const a   = document.createElement('a');
      a.href     = url;
      a.download = file.originalFilename;
      a.click();
      URL.revokeObjectURL(url);
      notify(`Downloading ${file.originalFilename}`);
    } catch {
      notify('Download failed.', 'danger');
    }
  };

  const handleDelete = async (fileId, filename) => {
    if (!window.confirm(`Delete "${filename}"?`)) return;
    try {
      await api.delete(`/api/files/${fileId}`);
      setFiles(prev => prev.filter(f => f.id !== fileId));
      notify(`${filename} deleted.`);
    } catch {
      notify('Delete failed.', 'danger');
    }
  };

  const counts = {
    total:      files.length,
    private:    files.filter(f => f.tag === 'PRIVATE').length,
    public:     files.filter(f => f.tag === 'PUBLIC').length,
    restricted: files.filter(f => f.tag === 'RESTRICTED').length,
  };

  return (
    <AppLayout>
      <PageHeader title="My Files" subtitle="Manage your uploaded files and access permissions" />

      {notification && <AlertBanner type={notification.type}>{notification.msg}</AlertBanner>}
      {error        && <AlertBanner type="danger">{error}</AlertBanner>}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '10px', marginBottom: '1.5rem' }}>
        <StatCard label="Total Files" value={counts.total}      badge="All files"    badgeType="info"    />
        <StatCard label="Private"     value={counts.private}    badge="Owner only"   badgeType="neutral" />
        <StatCard label="Public"      value={counts.public}     badge="Open access"  badgeType="success" />
        <StatCard label="Restricted"  value={counts.restricted} badge="Shared"       badgeType="warning" />
      </div>

      <SectionCard
        header={
          <>
            <span style={{ fontSize: '13px', fontWeight: '500' }}>Files ({files.length})</span>
            <PrimaryButton onClick={() => navigate('/upload')}>+ Upload file</PrimaryButton>
          </>
        }
      >
        {loading ? (
          <div style={{ padding: '2rem', textAlign: 'center', fontSize: '13px', color: 'var(--text-secondary)' }}>
            Loading files...
          </div>
        ) : files.length === 0 && !error ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-secondary)', fontSize: '13px' }}>
            No files uploaded yet.{' '}
            <span style={{ color: '#185FA5', cursor: 'pointer' }} onClick={() => navigate('/upload')}>
              Upload your first file →
            </span>
          </div>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ background: '#f7f7f5' }}>
                {['Name', 'Size', 'Tag', 'Uploaded', 'Actions'].map(h => (
                  <th key={h} style={{ padding: '8px 1.25rem', fontSize: '11px', fontWeight: '500', color: 'var(--text-secondary)', textAlign: 'left', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {files.map(file => {
                const ft = getFileType(file.originalFilename);
                return (
                  <tr key={file.id} style={{ borderTop: '0.5px solid rgba(0,0,0,0.06)' }}>
                    <td style={{ padding: '10px 1.25rem', fontSize: '13px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <div style={{ width: '28px', height: '28px', borderRadius: '6px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '10px', fontWeight: '600', background: ft.bg, color: ft.color, flexShrink: 0 }}>
                          {ft.label}
                        </div>
                        <span>{file.originalFilename}</span>
                      </div>
                    </td>
                    <td style={{ padding: '10px 1.25rem', fontSize: '13px', color: 'var(--text-secondary)' }}>{formatSize(file.size)}</td>
                    <td style={{ padding: '10px 1.25rem' }}><TagPill tag={file.tag} /></td>
                    <td style={{ padding: '10px 1.25rem', fontSize: '13px', color: 'var(--text-secondary)' }}>{formatDate(file.uploadedAt)}</td>
                    <td style={{ padding: '10px 1.25rem' }}>
                      <div style={{ display: 'flex', gap: '6px' }}>
                        <IconButton title="Download" onClick={() => handleDownload(file)}>
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="13" height="13">
                            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                            <polyline points="7 10 12 15 17 10" />
                            <line x1="12" y1="15" x2="12" y2="3" />
                          </svg>
                        </IconButton>
                        <IconButton title="Delete" danger onClick={() => handleDelete(file.id, file.originalFilename)}>
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="13" height="13">
                            <polyline points="3 6 5 6 21 6" />
                            <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
                            <path d="M10 11v6" /><path d="M14 11v6" />
                          </svg>
                        </IconButton>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </SectionCard>
    </AppLayout>
  );
}
