import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import Box from '@mui/material/Box';

import Dashboard from './pages/Dashboard';
import UserActivities from './pages/UserActivities';
import Analytics from './pages/Analytics';
import Sessions from './pages/Sessions';
import AuditLogs from './pages/AuditLogs';
import ImportUsers from './pages/ImportUsers';
import Header from './components/Header';
import Sidebar from './components/Sidebar';

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
    background: {
      default: '#f5f5f5',
    },
  },
});

const App: React.FC = () => {
  const [open, setOpen] = React.useState(true);
  
  const toggleDrawer = () => {
    setOpen(!open);
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Box sx={{ display: 'flex' }}>
        <Header open={open} toggleDrawer={toggleDrawer} />
        <Sidebar open={open} toggleDrawer={toggleDrawer} />
        <Box
          component="main"
          sx={{
            flexGrow: 1,
            padding: 3,
            marginTop: 8,
            overflow: 'auto',
          }}
        >
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/user-activities" element={<UserActivities />} />
            <Route path="/analytics" element={<Analytics />} />
            <Route path="/sessions" element={<Sessions />} />
            <Route path="/audit-logs" element={<AuditLogs />} />
            <Route path="/import-users" element={<ImportUsers />} />
          </Routes>
        </Box>
      </Box>
    </ThemeProvider>
  );
};

export default App; 