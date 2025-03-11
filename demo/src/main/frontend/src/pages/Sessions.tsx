import React, { useState } from 'react';
import {
  Typography,
  Paper,
  TextField,
  Button,
  Box,
  CircularProgress,
  IconButton,
  Chip,
} from '@mui/material';
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import RefreshIcon from '@mui/icons-material/Refresh';
import DeleteIcon from '@mui/icons-material/Delete';
import { createSession, updateSessionActivity, invalidateSession } from '../services/api';
import { UserSession } from '../types/models';

const Sessions: React.FC = () => {
  const [sessions, setSessions] = useState<UserSession[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [userId, setUserId] = useState<string>('');
  const [sessionToken, setSessionToken] = useState<string>('');
  const [selectedSession, setSelectedSession] = useState<string | null>(null);

  const columns: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 220 },
    { field: 'userId', headerName: 'User ID', width: 150 },
    { field: 'sessionToken', headerName: 'Session Token', width: 220 },
    { field: 'ipAddress', headerName: 'IP Address', width: 130 },
    { 
      field: 'isValid', 
      headerName: 'Status', 
      width: 120,
      renderCell: (params: GridRenderCellParams) => (
        <Chip 
          label={params.value ? 'Active' : 'Inactive'} 
          color={params.value ? 'success' : 'error'} 
          size="small" 
        />
      )
    },
    { field: 'lastActiveTimestamp', headerName: 'Last Active', width: 180 },
    { field: 'createdTimestamp', headerName: 'Created', width: 180 },
    {
      field: 'actions',
      headerName: 'Actions',
      width: 120,
      renderCell: (params: GridRenderCellParams) => (
        <Box>
          <IconButton 
            size="small" 
            onClick={() => handleUpdateSession(params.row.sessionToken)}
            disabled={!params.row.isValid || loading}
            title="Update Activity"
          >
            <RefreshIcon />
          </IconButton>
          <IconButton 
            size="small" 
            onClick={() => handleInvalidateSession(params.row.sessionToken)}
            disabled={!params.row.isValid || loading}
            title="Invalidate Session"
            color="error"
          >
            <DeleteIcon />
          </IconButton>
        </Box>
      )
    },
  ];

  const handleCreateSession = async (e: React.FormEvent) => {
    e.preventDefault();
    if (userId && sessionToken) {
      setLoading(true);
      try {
        const newSession = await createSession(userId, sessionToken);
        setSessions([...sessions, newSession]);
        setUserId('');
        setSessionToken('');
      } catch (error) {
        console.error('Error creating session:', error);
      } finally {
        setLoading(false);
      }
    }
  };

  const handleUpdateSession = async (token: string) => {
    setSelectedSession(token);
    setLoading(true);
    try {
      await updateSessionActivity(token);
      // Update the local state to reflect changes
      setSessions(
        sessions.map(session => 
          session.sessionToken === token 
            ? { ...session, lastActiveTimestamp: new Date().toISOString() } 
            : session
        )
      );
    } catch (error) {
      console.error('Error updating session:', error);
    } finally {
      setLoading(false);
      setSelectedSession(null);
    }
  };

  const handleInvalidateSession = async (token: string) => {
    setSelectedSession(token);
    setLoading(true);
    try {
      await invalidateSession(token);
      // Update the local state to reflect changes
      setSessions(
        sessions.map(session => 
          session.sessionToken === token 
            ? { ...session, isValid: false } 
            : session
        )
      );
    } catch (error) {
      console.error('Error invalidating session:', error);
    } finally {
      setLoading(false);
      setSelectedSession(null);
    }
  };

  return (
    <div>
      <Typography component="h1" variant="h4" gutterBottom>
        User Sessions
      </Typography>

      <Box mb={4}>
        <Paper sx={{ p: 3 }}>
          <Typography component="h2" variant="h6" gutterBottom>
            Create New Session
          </Typography>
          <form onSubmit={handleCreateSession}>
            <TextField
              label="User ID"
              variant="outlined"
              fullWidth
              margin="normal"
              value={userId}
              onChange={(e) => setUserId(e.target.value)}
              required
            />
            <TextField
              label="Session Token"
              variant="outlined"
              fullWidth
              margin="normal"
              value={sessionToken}
              onChange={(e) => setSessionToken(e.target.value)}
              required
            />
            <Button
              type="submit"
              variant="contained"
              color="primary"
              disabled={loading}
              sx={{ mt: 2 }}
            >
              {loading ? <CircularProgress size={24} /> : 'Create Session'}
            </Button>
          </form>
        </Paper>
      </Box>

      <Paper sx={{ p: 3 }}>
        <Typography component="h2" variant="h6" gutterBottom>
          Session Management
        </Typography>
        <div style={{ height: 400, width: '100%' }}>
          <DataGrid
            rows={sessions}
            columns={columns}
            loading={loading && !selectedSession}
            pageSizeOptions={[5, 10, 25]}
            disableRowSelectionOnClick
          />
        </div>
      </Paper>
    </div>
  );
};

export default Sessions; 