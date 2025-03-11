import React from 'react';
import { Grid, Paper, Typography, Box } from '@mui/material';
import PeopleIcon from '@mui/icons-material/People';
import BarChartIcon from '@mui/icons-material/BarChart';
import LockIcon from '@mui/icons-material/Lock';
import HistoryIcon from '@mui/icons-material/History';

const StatCard: React.FC<{
  title: string;
  value: string;
  icon: React.ReactNode;
  color: string;
}> = ({ title, value, icon, color }) => {
  return (
    <Paper
      sx={{
        padding: 2,
        display: 'flex',
        overflow: 'auto',
        flexDirection: 'column',
        height: 140,
        backgroundColor: color,
        color: 'white',
        borderRadius: 2,
      }}
    >
      <Box display="flex" alignItems="center">
        <Box mr={2}>{icon}</Box>
        <Typography component="h2" variant="h6" gutterBottom>
          {title}
        </Typography>
      </Box>
      <Typography component="p" variant="h3">
        {value}
      </Typography>
    </Paper>
  );
};

const Dashboard: React.FC = () => {
  return (
    <div>
      <Typography component="h1" variant="h4" gutterBottom>
        Dashboard
      </Typography>
      <Grid container spacing={3}>
        <Grid item xs={12} md={6} lg={3}>
          <StatCard
            title="User Activities"
            value="120"
            icon={<PeopleIcon fontSize="large" />}
            color="#3f51b5"
          />
        </Grid>
        <Grid item xs={12} md={6} lg={3}>
          <StatCard
            title="Analytics Events"
            value="85"
            icon={<BarChartIcon fontSize="large" />}
            color="#f50057"
          />
        </Grid>
        <Grid item xs={12} md={6} lg={3}>
          <StatCard
            title="Active Sessions"
            value="32"
            icon={<LockIcon fontSize="large" />}
            color="#00bcd4"
          />
        </Grid>
        <Grid item xs={12} md={6} lg={3}>
          <StatCard
            title="Audit Logs"
            value="214"
            icon={<HistoryIcon fontSize="large" />}
            color="#4caf50"
          />
        </Grid>
      </Grid>
      
      <Box mt={4}>
        <Typography component="h2" variant="h5" gutterBottom>
          Welcome to the Administration Dashboard
        </Typography>
        <Typography paragraph>
          This dashboard provides you with an overview of your system's activity. Use the menu on the left to navigate to different sections.
        </Typography>
        <Typography paragraph>
          You can manage user activities, view analytics, monitor sessions, check audit logs, and import user data.
        </Typography>
      </Box>
    </div>
  );
};

export default Dashboard; 