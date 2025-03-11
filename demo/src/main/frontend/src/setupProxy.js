const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
  app.use(
    '/api',
    createProxyMiddleware({
      target: 'http://localhost:8080',
      changeOrigin: true,
      pathRewrite: {
        '^/api': '/api'
      },
      onError: (err, req, res) => {
        console.log('Proxy Error:', err);
        res.writeHead(500, {
          'Content-Type': 'text/plain',
        });
        res.end('Something went wrong while connecting to the backend server.');
      }
    })
  );
}; 