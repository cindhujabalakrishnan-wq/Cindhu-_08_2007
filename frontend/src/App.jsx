import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import Dashboard from './pages/Dashboard.jsx';
import PolicyList from './pages/PolicyList.jsx';
import PolicyForm from './pages/PolicyForm.jsx';
import PolicyDetail from './pages/PolicyDetail.jsx';
import Renewals from './pages/Renewals.jsx';
import Payments from './pages/Payments.jsx';
import Documents from './pages/Documents.jsx';
import Notifications from './pages/Notifications.jsx';
import Profile from './pages/Profile.jsx';
import AdminDashboard from './pages/admin/AdminDashboard.jsx';
import AdminUsers from './pages/admin/AdminUsers.jsx';
import AdminPolicies from './pages/admin/AdminPolicies.jsx';
import AdminRenewals from './pages/admin/AdminRenewals.jsx';
import AdminCompanies from './pages/admin/AdminCompanies.jsx';
import AdminPolicyTypes from './pages/admin/AdminPolicyTypes.jsx';
import AdminAuditLogs from './pages/admin/AdminAuditLogs.jsx';
import NotFound from './pages/NotFound.jsx';

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/policies" element={<PolicyList />} />
          <Route path="/policies/new" element={<PolicyForm />} />
          <Route path="/policies/:id" element={<PolicyDetail />} />
          <Route path="/policies/:id/edit" element={<PolicyForm />} />
          <Route path="/renewals" element={<Renewals />} />
          <Route path="/payments" element={<Payments />} />
          <Route path="/documents" element={<Documents />} />
          <Route path="/notifications" element={<Notifications />} />
          <Route path="/profile" element={<Profile />} />

          <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
            <Route path="/admin" element={<AdminDashboard />} />
            <Route path="/admin/users" element={<AdminUsers />} />
            <Route path="/admin/policies" element={<AdminPolicies />} />
            <Route path="/admin/renewals" element={<AdminRenewals />} />
            <Route path="/admin/companies" element={<AdminCompanies />} />
            <Route path="/admin/policy-types" element={<AdminPolicyTypes />} />
            <Route path="/admin/audit-logs" element={<AdminAuditLogs />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}
