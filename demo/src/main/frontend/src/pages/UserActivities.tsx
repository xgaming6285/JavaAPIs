import React, { useState, useEffect } from 'react';
import {
  Typography,
  Paper,
  TextField,
  Button,
  Box,
  CircularProgress,
} from '@mui/material';
import { DataGrid, GridColDef } from '@mui/x-data-grid';
import { getUserActivities, logUserActivity } from '../services/api';
import { UserActivity } from '../types/models';

const UserActivities: React.FC = () => {
  const [activities, setActivities] = useState<UserActivity[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [userId, setUserId] = useState<string>('');
  const [action, setAction] = useState<string>('');
  const [details, setDetails] = useState<string>('');
  const [searchUserId, setSearchUserId] = useState<string>('');

  const columns: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 220 },
    { field: 'userId', headerName: 'User ID', width: 150 },
    { field: 'action', headerName: 'Action', width: 150 },
    { field: 'details', headerName: 'Details', width: 200 },
    { field: 'ipAddress', headerName: 'IP Address', width: 130 },
    { field: 'timestamp', headerName: 'Timestamp', width: 200 },
  ];

  const handleSearch = async () => {
    if (searchUserId) {
      setLoading(true);
      try {
        const data = await getUserActivities(searchUserId);
        setActivities(data);
      } catch (error) {
        console.error('Error fetching user activities:', error);
      } finally {
        setLoading(false);
      }
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (userId && action && details) {
      setLoading(true);
      try {
        await logUserActivity(userId, action, details);
        setUserId('');
        setAction('');
        setDetails('');
        
        // Refresh the list if we're viewing the same user
        if (searchUserId === userId) {
          await handleSearch();
        }
      } catch (error) {
        console.error('Error logging user activity:', error);
      } finally {
        setLoading(false);
      }
    }
  };

  return (
    <div>
      <Typography component="h1" variant="h4" gutterBottom>
        User Activities
      </Typography>

      <Box mb={4}>
        <Paper sx={{ p: 3 }}>
          <Typography component="h2" variant="h6" gutterBottom>
            Log New Activity
          </Typography>
          <form onSubmit={handleSubmit}>
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
              label="Action"
              variant="outlined"
              fullWidth
              margin="normal"
              value={action}
              onChange={(e) => setAction(e.target.value)}
              required
            />
            <TextField
              label="Details"
              variant="outlined"
              fullWidth
              margin="normal"
              value={details}
              onChange={(e) => setDetails(e.target.value)}
              required
              multiline
              rows={2}
            />
            <Button
              type="submit"
              variant="contained"
              color="primary"
              disabled={loading}
              sx={{ mt: 2 }}
            >
              {loading ? <CircularProgress size={24} /> : 'Submit'}
            </Button>
          </form>
        </Paper>
      </Box>

      <Box>
        <Paper sx={{ p: 3, mb: 3 }}>
          <Typography component="h2" variant="h6" gutterBottom>
            Search Activities by User ID
          </Typography>
          <Box display="flex" alignItems="center">
            <TextField
              label="User ID"
              variant="outlined"
              fullWidth
              margin="normal"
              value={searchUserId}
              onChange={(e) => setSearchUserId(e.target.value)}
              sx={{ mr: 2 }}
            />
            <Button
              variant="contained"
              color="primary"
              onClick={handleSearch}
              disabled={loading || !searchUserId}
            >
              Search
            </Button>
          </Box>
        </Paper>

        <div style={{ height: 400, width: '100%' }}>
          <DataGrid
            rows={activities}
            columns={columns}
            loading={loading}
            pageSizeOptions={[5, 10, 25]}
            disableRowSelectionOnClick
          />
        </div>
      </Box>
    </div>
  );
};

export default UserActivities; 