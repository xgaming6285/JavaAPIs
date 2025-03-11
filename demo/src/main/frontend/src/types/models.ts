export interface UserActivity {
  id?: string;
  userId: string;
  action: string;
  details: string;
  ipAddress?: string;
  userAgent?: string;
  timestamp?: string;
}

export interface AnalyticsData {
  id?: string;
  eventType: string;
  userId: string;
  metadata: Record<string, any>;
  timestamp?: string;
}

export interface UserSession {
  id?: string;
  userId: string;
  sessionToken: string;
  ipAddress?: string;
  userAgent?: string;
  lastActiveTimestamp?: string;
  createdTimestamp?: string;
  isValid: boolean;
}

export interface AuditLog {
  id?: string;
  userId: string;
  action: string;
  resourceType: string;
  resourceId: string;
  changes: Record<string, any>;
  ipAddress?: string;
  status: string;
  message: string;
  timestamp?: string;
}

export interface User {
  id?: string;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  role?: string;
  createdAt?: string;
  lastLogin?: string;
} 