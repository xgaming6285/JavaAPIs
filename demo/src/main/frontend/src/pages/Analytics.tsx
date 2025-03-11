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
import { getAnalyticsByType, logAnalyticsEvent } from '../services/api';
import { AnalyticsData } from '../types/models';

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
      id={`analytics-tabpanel-${index}`}
      aria-labelledby={`analytics-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
};

const Analytics: React.FC = () => {
  const [tabValue, setTabValue] = useState(0);
  const [analyticsData, setAnalyticsData] = useState<AnalyticsData[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  
  // For search
  const [searchEventType, setSearchEventType] = useState<string>('');
  
  // For create
  const [eventType, setEventType] = useState<string>('');
  const [userId, setUserId] = useState<string>('');
  const [metadataKey, setMetadataKey] = useState<string>('');
  const [metadataValue, setMetadataValue] = useState<string>('');

  const columns: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 220 },
    { field: 'eventType', headerName: 'Event Type', width: 150 },
    { field: 'userId', headerName: 'User ID', width: 150 },
    { 
      field: 'metadata', 
      headerName: 'Metadata', 
      width: 250,
      valueGetter: (params) => {
        return JSON.stringify(params.value);
      } 
    },
    { field: 'timestamp', headerName: 'Timestamp', width: 200 },
  ];

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleSearch = async () => {
    if (searchEventType) {
      setLoading(true);
      try {
        const data = await getAnalyticsByType(searchEventType);
        setAnalyticsData(data);
      } catch (error) {
        console.error('Error fetching analytics data:', error);
      } finally {
        setLoading(false);
      }
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (eventType && userId && metadataKey && metadataValue) {
      setLoading(true);
      try {
        const metadata: Record<string, any> = {};
        metadata[metadataKey] = metadataValue;
        
        await logAnalyticsEvent(eventType, userId, metadata);
        
        setEventType('');
        setUserId('');
        setMetadataKey('');
        setMetadataValue('');
        
        // Refresh the list if we're viewing the same event type
        if (searchEventType === eventType) {
          await handleSearch();
        }
      } catch (error) {
        console.error('Error logging analytics event:', error);
      } finally {
        setLoading(false);
      }
    }
  };

  return (
    <div>
      <Typography component="h1" variant="h4" gutterBottom>
        Analytics
      </Typography>

      <Paper sx={{ width: '100%', mb: 4 }}>
        <Tabs value={tabValue} onChange={handleTabChange} aria-label="analytics tabs">
          <Tab label="Search Analytics" />
          <Tab label="Log New Event" />
        </Tabs>

        <TabPanel value={tabValue} index={0}>
          <Box display="flex" alignItems="center">
            <TextField
              label="Event Type"
              variant="outlined"
              fullWidth
              margin="normal"
              value={searchEventType}
              onChange={(e) => setSearchEventType(e.target.value)}
              sx={{ mr: 2 }}
            />
            <Button
              variant="contained"
              color="primary"
              onClick={handleSearch}
              disabled={loading || !searchEventType}
            >
              Search
            </Button>
          </Box>

          <div style={{ height: 400, width: '100%', marginTop: 20 }}>
            <DataGrid
              rows={analyticsData}
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
              label="Event Type"
              variant="outlined"
              fullWidth
              margin="normal"
              value={eventType}
              onChange={(e) => setEventType(e.target.value)}
              required
            />
            <TextField
              label="User ID"
              variant="outlined"
              fullWidth
              margin="normal"
              value={userId}
              onChange={(e) => setUserId(e.target.value)}
              required
            />
            <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
              Metadata (Key-Value Pair)
            </Typography>
            <Box display="flex">
              <TextField
                label="Key"
                variant="outlined"
                fullWidth
                margin="normal"
                value={metadataKey}
                onChange={(e) => setMetadataKey(e.target.value)}
                required
                sx={{ mr: 2 }}
              />
              <TextField
                label="Value"
                variant="outlined"
                fullWidth
                margin="normal"
                value={metadataValue}
                onChange={(e) => setMetadataValue(e.target.value)}
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

export default Analytics; 