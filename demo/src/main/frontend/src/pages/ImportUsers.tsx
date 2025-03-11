import React, { useState } from 'react';
import {
  Typography,
  Paper,
  Button,
  Box,
  CircularProgress,
  Alert,
  List,
  ListItem,
  ListItemText,
  Divider,
} from '@mui/material';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import { importUsers } from '../services/api';

const ImportUsers: React.FC = () => {
  const [file, setFile] = useState<File | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [results, setResults] = useState<string[]>([]);
  const [error, setError] = useState<string>('');
  const [success, setSuccess] = useState<boolean>(false);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      setFile(e.target.files[0]);
      setError('');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!file) {
      setError('Please select a file to upload');
      return;
    }

    if (!file.name.endsWith('.csv')) {
      setError('Please upload a CSV file');
      return;
    }

    setLoading(true);
    setResults([]);
    setSuccess(false);
    setError('');

    try {
      const data = await importUsers(file);
      setResults(data);
      setSuccess(true);
    } catch (err: any) {
      setError(err.response?.data?.[0] || 'An error occurred during import');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <Typography component="h1" variant="h4" gutterBottom>
        Import Users
      </Typography>

      <Paper sx={{ p: 3, mb: 4 }}>
        <Typography component="h2" variant="h6" gutterBottom>
          Upload CSV File
        </Typography>
        <Typography paragraph>
          Upload a CSV file containing user data. The file should have the following columns:
          username, email, firstName, lastName, role.
        </Typography>

        <form onSubmit={handleSubmit}>
          <Box
            sx={{
              border: '2px dashed #ccc',
              borderRadius: 2,
              p: 3,
              mb: 3,
              textAlign: 'center',
            }}
          >
            <input
              accept=".csv"
              style={{ display: 'none' }}
              id="raised-button-file"
              type="file"
              onChange={handleFileChange}
            />
            <label htmlFor="raised-button-file">
              <Button
                variant="contained"
                component="span"
                startIcon={<CloudUploadIcon />}
              >
                Select CSV File
              </Button>
            </label>
            {file && (
              <Typography sx={{ mt: 2 }}>
                Selected file: {file.name}
              </Typography>
            )}
          </Box>

          {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}
          {success && (
            <Alert severity="success" sx={{ mb: 3 }}>
              Users imported successfully!
            </Alert>
          )}

          <Button
            type="submit"
            variant="contained"
            color="primary"
            disabled={loading || !file}
          >
            {loading ? <CircularProgress size={24} /> : 'Import Users'}
          </Button>
        </form>
      </Paper>

      {results.length > 0 && (
        <Paper sx={{ p: 3 }}>
          <Typography component="h2" variant="h6" gutterBottom>
            Import Results
          </Typography>
          <List>
            {results.map((result, index) => (
              <React.Fragment key={index}>
                <ListItem>
                  <ListItemText primary={result} />
                </ListItem>
                {index < results.length - 1 && <Divider />}
              </React.Fragment>
            ))}
          </List>
        </Paper>
      )}
    </div>
  );
};

export default ImportUsers; 