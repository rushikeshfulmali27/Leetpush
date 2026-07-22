import axios from 'axios';
import toast from 'react-hot-toast';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // for HttpOnly cookies if used
});

apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

apiClient.interceptors.response.use(
  (response) => {
    // If our backend wraps responses in an ApiResponse object
    if (response.data && response.data.success !== undefined) {
      if (!response.data.success) {
        toast.error(response.data.message || 'An error occurred');
        return Promise.reject(new Error(response.data.message));
      }
      // Return the inner data
      return response.data.data;
    }
    return response.data;
  },
  (error) => {
    const status = error.response?.status;
    const message = error.response?.data?.message || error.message;

    if (status === 401) {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
    } else if (status === 403) {
      toast.error('Access denied');
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
    } else if (status === 502) {
      toast.error('AI Generation failed. Please try again.');
    } else if (status >= 400 && status < 500) {
      toast.error(message);
    } else {
      toast.error('Something went wrong on the server');
    }

    return Promise.reject(error);
  }
);
