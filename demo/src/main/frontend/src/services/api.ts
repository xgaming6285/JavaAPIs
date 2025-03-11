import axios from 'axios';
import { UserActivity, AnalyticsData, UserSession, AuditLog } from '../types/models';

const API_BASE_URL = '/api';

// User Activities
export const getUserActivities = async (userId: string): Promise<UserActivity[]> => {
  const response = await axios.get(`${API_BASE_URL}/logs/activity/user/${userId}`);
  return response.data;
};

export const logUserActivity = async (
  userId: string, 
  action: string, 
  details: string
): Promise<UserActivity> => {
  const response = await axios.post(
    `${API_BASE_URL}/logs/activity?userId=${userId}&action=${action}&details=${details}`
  );
  return response.data;
};

// Analytics
export const getAnalyticsByType = async (eventType: string): Promise<AnalyticsData[]> => {
  const response = await axios.get(`${API_BASE_URL}/logs/analytics/type/${eventType}`);
  return response.data;
};

export const logAnalyticsEvent = async (
  eventType: string, 
  userId: string, 
  metadata: Record<string, any>
): Promise<AnalyticsData> => {
  const response = await axios.post(
    `${API_BASE_URL}/logs/analytics?eventType=${eventType}&userId=${userId}`,
    metadata
  );
  return response.data;
};

// Sessions
export const createSession = async (
  userId: string, 
  sessionToken: string
): Promise<UserSession> => {
  const response = await axios.post(
    `${API_BASE_URL}/logs/sessions?userId=${userId}&sessionToken=${sessionToken}`
  );
  return response.data;
};

export const updateSessionActivity = async (sessionToken: string): Promise<void> => {
  await axios.put(`${API_BASE_URL}/logs/sessions/${sessionToken}`);
};

export const invalidateSession = async (sessionToken: string): Promise<void> => {
  await axios.delete(`${API_BASE_URL}/logs/sessions/${sessionToken}`);
};

// Audit Logs
export const getAuditLogsByUser = async (userId: string): Promise<AuditLog[]> => {
  const response = await axios.get(`${API_BASE_URL}/logs/audit/user/${userId}`);
  return response.data;
};

export const createAuditLog = async (
  userId: string,
  action: string,
  resourceType: string,
  resourceId: string,
  changes: Record<string, any>
): Promise<AuditLog> => {
  const response = await axios.post(
    `${API_BASE_URL}/logs/audit?userId=${userId}&action=${action}&resourceType=${resourceType}&resourceId=${resourceId}`,
    changes
  );
  return response.data;
};

// Import
export const importUsers = async (file: File): Promise<string[]> => {
  const formData = new FormData();
  formData.append('file', file);
  
  const response = await axios.post(`${API_BASE_URL}/v1/import/users`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  
  return response.data;
}; 