import http from 'node:http';
import { request as httpRequest } from 'node:http';

const NEXT_PORT = 3001;
const BACKEND_PORT = 8080;
const PROXY_PORT = 3000;
const STRIP_PREFIX = '/absports/3000';
const RESTORE_PREFIX = '/codeeditor/default';

const server = http.createServer((req, res) => {
  let url = req.url || '/';

  // Strip /absports/3000 prefix that code-server sends
  let stripped = url;
  if (stripped.startsWith(STRIP_PREFIX)) {
    stripped = stripped.slice(STRIP_PREFIX.length) || '/';
  }

  // API requests go directly to backend (no basePath needed)
  const isApi = stripped.startsWith('/api/') || stripped === '/api';
  if (isApi) {
    proxy(req, res, BACKEND_PORT, stripped);
    return;
  }

  // For Next.js: prepend /codeeditor/default to reconstruct the full basePath
  // Next.js expects basePath = /codeeditor/default/absports/3000
  // So the URL sent to Next.js should be: /codeeditor/default + /absports/3000 + path
  const nextUrl = RESTORE_PREFIX + STRIP_PREFIX + stripped;
  proxy(req, res, NEXT_PORT, nextUrl);
});

function proxy(req, res, port, path) {
  const proxyReq = httpRequest(
    {
      hostname: '127.0.0.1',
      port,
      path,
      method: req.method,
      headers: {
        ...req.headers,
        host: `127.0.0.1:${port}`,
      },
    },
    (proxyRes) => {
      // Rewrite Location headers: strip /codeeditor/default, keep /absports/3000/...
      let location = proxyRes.headers['location'];
      if (location && !location.startsWith('http')) {
        if (location.startsWith(RESTORE_PREFIX + STRIP_PREFIX)) {
          location = location.slice(RESTORE_PREFIX.length);
          proxyRes.headers['location'] = location;
        }
      }
      res.writeHead(proxyRes.statusCode, proxyRes.headers);
      proxyRes.pipe(res);
    }
  );

  proxyReq.on('error', (err) => {
    console.error(`Proxy error → :${port}${path}`, err.message);
    res.writeHead(502);
    res.end('Bad Gateway');
  });

  req.pipe(proxyReq);
}

server.listen(PROXY_PORT, '0.0.0.0', () => {
  console.log(`SageMaker proxy listening on :${PROXY_PORT}`);
  console.log(`  Next.js → :${NEXT_PORT} (prepend ${RESTORE_PREFIX})`);
  console.log(`  Backend → :${BACKEND_PORT}`);
});
