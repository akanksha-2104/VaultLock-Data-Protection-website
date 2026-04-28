import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosInstance';
import { AppLayout, AlertBanner, SectionCard, PrimaryButton, SecondaryButton, PageHeader } from '../components/UI';

const ALLOWED_EXTENSIONS = ['pdf', 'doc', 'docx', 'png', 'jpg', 'jpeg', 'gif', 'txt', 'xlsx', 'csv'];

export default function UploadPage() {
  const [file, setFile]             = useState(null);
  const [tag, setTag]               = useState('PRIVATE');
  const [sharedEmails, setShared]   = useState('');
  const [dragging, setDragging]     = useState(false);
  const [loading, setLoading]       = useState(false);
  const [error, setError]           = useState('');
  const [success, setSuccess]       = useState('');
  const inputRef = useRef();
  const navigate = useNavigate();

  const validateFile = (f) => {
    const ext = f.name.split('.').pop().toLowerCase();
    if (!ALLOWED_EXTENSIONS.includes(ext)) {
      setError(`File type ".${ext}" is not allowed. Allowed: ${ALLOWED_EXTENSIONS.join(', ')}`);
      return false;
    }
    if (f.size > 50 * 1024 * 1024) {
      setError('File size must be under 50 MB.');
      return false;
    }
    return true;
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setDragging(false);
    const dropped = e.dataTransfer.files[0];
    if (dropped && validateFile(dropped)) { setFile(dropped); setError(''); }
  };

  const handleFileChange = (e) => {
    const selected = e.target.files[0];
    if (selected && validateFile(selected)) { setFile(selected); setError(''); }
  };

  const handleUpload = async () => {
    if (!file) { setError('Please select a file first.'); return; }
    if (tag === 'RESTRICTED' && !sharedEmails.trim()) {
      setError('Restricted files must include at least one shared user email.'); return;
    }
    setLoading(true);
    setError('');
    setSuccess('');

    const formData = new FormData();
    formData.append('file', file);
    formData.append('tag', tag);
    if (sharedEmails.trim()) formData.append('sharedWithEmails', sharedEmails.trim());

    try {
      const res = await api.post('/api/files/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setSuccess(`"${res.data.originalFilename}" uploaded successfully!`);
      setFile(null);
      setShared('');
      setTag('PRIVATE');
      if (inputRef.current) inputRef.current.value = '';
      setTimeout(() => navigate('/files'), 1500);
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data || 'Upload failed.';
      setError(typeof msg === 'string' ? msg : 'Upload failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <AppLayout>
      <PageHeader title="Upload File" subtitle="Add files to your secure vault" />

      {error   && <AlertBanner type="danger">{error}</AlertBanner>}
      {success && <AlertBanner type="success">{success}</AlertBanner>}

      <SectionCard header={<span style={{ fontSize: '13px', fontWeight: '500' }}>New upload</span>}>
        <div style={{ padding: '1.25rem' }}>

          {/* Drop zone */}
          <div
            style={{ ...s.dropZone, ...(dragging ? s.dropZoneActive : {}), ...(file ? s.dropZoneHasFile : {}) }}
            onDragOver={e => { e.preventDefault(); setDragging(true); }}
            onDragLeave={() => setDragging(false)}
            onDrop={handleDrop}
            onClick={() => inputRef.current?.click()}
          >
            <input ref={inputRef} type="file" style={{ display: 'none' }} onChange={handleFileChange} />
            <div style={s.dropIcon}>
              <svg viewBox="0 0 24 24" fill="none" stroke="#185FA5" strokeWidth="2" width="20" height="20">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                <polyline points="17 8 12 3 7 8" />
                <line x1="12" y1="3" x2="12" y2="15" />
              </svg>
            </div>
            {file ? (
              <>
                <div style={s.dropTitle}>{file.name}</div>
                <div style={s.dropSub}>{(file.size / 1024).toFixed(1)} KB · Click to change</div>
              </>
            ) : (
              <>
                <div style={s.dropTitle}>Click to browse or drag & drop</div>
                <div style={s.dropSub}>PDF, DOCX, PNG, JPG, TXT up to 50 MB</div>
              </>
            )}
          </div>

          {/* Tag + shared emails */}
          <div style={{ display: 'flex', gap: '12px', marginTop: '1rem', flexWrap: 'wrap' }}>
            <div style={{ flex: '1', minWidth: '160px' }}>
              <label style={s.label}>Access tag</label>
              <select value={tag} onChange={e => setTag(e.target.value)} style={s.select}>
                <option value="PRIVATE">PRIVATE — Only you</option>
                <option value="PUBLIC">PUBLIC — Everyone</option>
                <option value="RESTRICTED">RESTRICTED — Specific users</option>
              </select>
            </div>
            {tag === 'RESTRICTED' && (
              <div style={{ flex: '2', minWidth: '200px' }}>
                <label style={s.label}>Share with (emails, comma-separated)</label>
                <input
                  type="text" value={sharedEmails} placeholder="alice@co.com, bob@co.com"
                  onChange={e => setShared(e.target.value)} style={s.input}
                />
              </div>
            )}
          </div>

          <div style={{ display: 'flex', gap: '10px', marginTop: '1.25rem' }}>
            <PrimaryButton onClick={handleUpload}>{loading ? 'Uploading...' : 'Upload file →'}</PrimaryButton>
            <SecondaryButton onClick={() => navigate('/files')}>Cancel</SecondaryButton>
          </div>
        </div>
      </SectionCard>

      {/* Access levels info */}
      <SectionCard header={<span style={{ fontSize: '13px', fontWeight: '500' }}>Access levels explained</span>}>
        <div style={{ padding: '1.25rem', display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '12px' }}>
          {[
            { tag: 'PRIVATE', desc: 'Only you can view, download, or modify this file.' },
            { tag: 'PUBLIC', desc: 'Anyone with the link can view or download without authentication.' },
            { tag: 'RESTRICTED', desc: 'Shared with specific registered users only. Requires login.' },
          ].map(({ tag: t, desc }) => (
            <div key={t} style={{ padding: '1rem', borderRadius: '8px', background: '#f7f7f5' }}>
              <div style={{ fontSize: '12px', fontWeight: '500', color: 'var(--text-primary)', marginBottom: '4px' }}>{t}</div>
              <div style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: '1.5' }}>{desc}</div>
            </div>
          ))}
        </div>
      </SectionCard>
    </AppLayout>
  );
}

const s = {
  dropZone: { border: '1.5px dashed rgba(0,0,0,0.12)', borderRadius: '10px', padding: '2rem', textAlign: 'center', cursor: 'pointer', transition: 'all 0.15s' },
  dropZoneActive: { borderColor: '#378ADD', background: '#E6F1FB' },
  dropZoneHasFile: { borderColor: '#3B6D11', background: '#EAF3DE' },
  dropIcon: { width: '40px', height: '40px', background: '#E6F1FB', borderRadius: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 0.75rem' },
  dropTitle: { fontSize: '14px', fontWeight: '500', color: 'var(--text-primary)', marginBottom: '4px' },
  dropSub: { fontSize: '12px', color: 'var(--text-secondary)' },
  label: { display: 'block', fontSize: '11px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px', textTransform: 'uppercase', letterSpacing: '0.5px' },
  select: { width: '100%', padding: '9px 10px', border: '0.5px solid rgba(0,0,0,0.12)', borderRadius: '6px', fontSize: '13px', background: '#ffffff', color: 'var(--text-primary)' },
  input: { width: '100%', padding: '9px 10px', border: '0.5px solid rgba(0,0,0,0.12)', borderRadius: '6px', fontSize: '13px', background: '#ffffff', color: 'var(--text-primary)' },
};
