import { Routes, Route, Navigate } from 'react-router-dom';
import PrivateRoute  from './components/PrivateRoute';
import LoginPage     from './pages/LoginPage';
import RegisterPage  from './pages/RegisterPage';
import DecoyPage     from './pages/DecoyPage';
import FilesPage     from './pages/FilesPage';
import UploadPage    from './pages/UploadPage';
import AuditLogPage  from './pages/AuditLogPage';
import LocationsPage from './pages/LocationsPage';
import NotFoundPage  from './pages/NotFoundPage';

export default function App() {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/login"    element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/decoy"    element={<DecoyPage />} />

      {/* Protected routes */}
      <Route path="/files"     element={<PrivateRoute><FilesPage /></PrivateRoute>} />
      <Route path="/upload"    element={<PrivateRoute><UploadPage /></PrivateRoute>} />
      <Route path="/logs"      element={<PrivateRoute><AuditLogPage /></PrivateRoute>} />
      <Route path="/locations" element={<PrivateRoute><LocationsPage /></PrivateRoute>} />

      {/* Root redirect */}
      <Route path="/" element={<Navigate to="/files" replace />} />

      {/* 404 */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
