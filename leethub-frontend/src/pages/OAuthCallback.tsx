import React, { useEffect, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../contexts/useAuth';
import toast from 'react-hot-toast';

export const OAuthCallback: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { login } = useAuth();
  const loginAttempted = useRef(false);

  useEffect(() => {
    // Guard against React StrictMode double-invocation and re-renders.
    // GitHub OAuth codes are single-use; calling login() more than once
    // causes "Bad credentials" errors on the duplicate requests.
    if (loginAttempted.current) return;
    loginAttempted.current = true;

    const code = searchParams.get('code');
    const state = searchParams.get('state');
    const error = searchParams.get('error');

    if (code && state) {
      login(code, state)
        .then(() => navigate('/dashboard'))
        .catch(() => {
          toast.error('Authentication failed');
          navigate('/login');
        });
    } else if (error) {
      toast.error('Authentication failed');
      navigate('/login');
    } else {
      navigate('/login');
    }
  }, [searchParams, navigate, login]);

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-950">
      <div className="flex flex-col items-center space-y-4">
        <div className="h-12 w-12 animate-spin rounded-full border-4 border-gray-800 border-t-emerald-500"></div>
        <p className="text-gray-400">Authenticating with GitHub...</p>
      </div>
    </div>
  );
};
