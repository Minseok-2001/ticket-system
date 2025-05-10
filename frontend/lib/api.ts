import axios from "axios";

// axios 인스턴스 생성
const api = axios.create({
  baseURL: "/api",
  headers: {
    "Content-Type": "application/json",
  },
});

// 요청 인터셉터 설정
api.interceptors.request.use(
  (config) => {
    // 로컬 스토리지에서 토큰 가져오기
    const token = localStorage.getItem("token");

    // 토큰이 있으면 헤더에 추가
    if (token) {
      config.headers["Authorization"] = `Bearer ${token}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터 설정
api.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    // 401 에러 (인증 실패) 처리
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        // 리프레시 토큰으로 새 토큰 요청
        const refreshToken = localStorage.getItem("refreshToken");

        if (refreshToken) {
          const response = await axios.post("/api/auth/refresh", {
            refreshToken,
          });

          const { token } = response.data;

          // 새 토큰 저장
          localStorage.setItem("token", token);

          // 헤더 업데이트
          api.defaults.headers.common["Authorization"] = `Bearer ${token}`;
          originalRequest.headers["Authorization"] = `Bearer ${token}`;

          // 원래 요청 재시도
          return api(originalRequest);
        }
      } catch {
        // 리프레시 토큰으로도 실패하면 로그아웃 처리
        localStorage.removeItem("token");
        localStorage.removeItem("refreshToken");

        // 로그인 페이지로 리다이렉트
        window.location.href = "/login";
      }
    }

    return Promise.reject(error);
  }
);

export default api;
