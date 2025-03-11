import React, { useState } from 'react';
import {
  Typography,
  Paper,
  TextField,
  Button,
  Box,
  CircularProgress,
  Tab,
  Tabs,
} from '@mui/material';
import { DataGrid, GridColDef } from '@mui/x-data-grid';
import { getAuditLogsByUser, createAuditLog } from '../services/api';
import { AuditLog } from '../types/models';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel = (props: TabPanelProps) => {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`audit-tabpanel-${index}`}
      aria-labelledby={`audit-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
};

const AuditLogs: React.FC = () => {
  const [tabValue, setTabValue] = useState(0);
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  
  // For search
  const [searchUserId, setSearchUserId] = useState<string>('');
  
  // For create
  const [userId, setUserId] = useState<string>('');
  const [action, setAction] = useState<string>('');
  const [resourceType, setResourceType] = useState<string>('');
  const [resourceId, setResourceId] = useState<string>('');
  const [changeKey, setChangeKey] = useState<string>('');
  const [changeValue, setChangeValue] = useState<string>('');

  const columns: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 220 },
    { field: 'userId', headerName: 'User ID', width: 150 },
    { field: 'action', headerName: 'Action', width: 150 },
    { field: 'resourceType', headerName: 'Resource Type', width: 150 },
    { field: 'resourceId', headerName: 'Resource ID', width: 150 },
    { 
      field: 'changes', 
      headerName: 'Changes', 
      width: 250,
      valueGetter: (params) => {
        return JSON.stringify(params.value);
      } 
    },
    { field: 'status', headerName: 'Status', width: 120 },
    { field: 'message', headerName: 'Message', width: 200 },
    { field: 'timestamp', headerName: 'Timestamp', width: 200 },
  ];

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleSearch = async () => {
    if (searchUserId) {
      setLoading(true);
      try {
        const data = await getAuditLogsByUser(searchUserId);
        setAuditLogs(data);
      } catch (error) {
        console.error('Error fetching audit logs:', error);
      } finally {
        setLoading(false);
      }
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (userId && action && resourceType && resourceId && changeKey && changeValue) {
      setLoading(true);
      try {
        const changes: Record<string, any> = {};
        changes[changeKey] = changeValue;
        
        await createAuditLog(userId, action, resourceType, resourceId, changes);
        
        setUserId('');
        setAction('');
        setResourceType('');
        setResourceId('');
        setChangeKey('');
        setChangeValue('');
        
        // Refresh the list if we're viewing the same user
        if (searchUserId === userId) {
          await handleSearch();
        }
      } catch (error) {
        console.error('Error creating audit log:', error);
      } finally {
        setLoading(false);
      }
    }
  };

  return (
    <div>
      <Typography component="h1" variant="h4" gutterBottom>
        Audit Logs
      </Typography>

      <Paper sx={{ width: '100%', mb: 4 }}>
        <Tabs value={tabValue} onChange={handleTabChange} aria-label="audit-logs tabs">
          <Tab label="Search Audit Logs" />
          <Tab label="Create Audit Log" />
        </Tabs>

        <TabPanel value={tabValue} index={0}>
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

          <div style={{ height: 400, width: '100%', marginTop: 20 }}>
            <DataGrid
              rows={auditLogs}
              columns={columns}
              loading={loading}
              pageSizeOptions={[5, 10, 25]}
              disableRowSelectionOnClick
            />
          </div>
        </TabPanel>

        <TabPanel value={tabValue} index={1}>
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
              label="Resource Type"
              variant="outlined"
              fullWidth
              margin="normal"
              value={resourceType}
              onChange={(e) => setResourceType(e.target.value)}
              required
            />
            <TextField
              label="Resource ID"
              variant="outlined"
              fullWidth
              margin="normal"
              value={resourceId}
              onChange={(e) => setResourceId(e.target.value)}
              required
            />
            <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
              Changes (Key-Value Pair)
            </Typography>
            <Box display="flex">
              <TextField
                label="Key"
                variant="outlined"
                fullWidth
                margin="normal"
                value={changeKey}
                onChange={(e) => setChangeKey(e.target.value)}
                required
                sx={{ mr: 2 }}
              />
              <TextField
                label="Value"
                variant="outlined"
                fullWidth
                margin="normal"
                value={changeValue}
                onChange={(e) => setChangeValue(e.target.value)}
                required
              />
            </Box>
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
        </TabPanel>
      </Paper>
    </div>
  );
};

export default AuditLogs; 