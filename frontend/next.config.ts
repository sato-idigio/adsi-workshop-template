import type { NextConfig } from 'next';

const isSageMaker = process.env.SAGEMAKER === '1';
const sageBasePath = '/codeeditor/default/absports/3000';

const nextConfig: NextConfig = {
  skipTrailingSlashRedirect: true,
  ...(isSageMaker
    ? {
        basePath: sageBasePath,
        assetPrefix: sageBasePath,
        async rewrites() {
          return [
            {
              source: '/api/:path*',
              destination: 'http://localhost:8080/api/:path*',
            },
          ];
        },
      }
    : {
        async rewrites() {
          return [
            {
              source: '/api/:path*',
              destination: 'http://localhost:8080/api/:path*',
            },
          ];
        },
      }),
};

export default nextConfig;
