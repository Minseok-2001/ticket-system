/** @type {import('next').NextConfig} */
const nextConfig = {
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: "http://localhost:8082/:path*", // 백엔드 서버 주소
      },
    ];
  },
};

module.exports = nextConfig;
