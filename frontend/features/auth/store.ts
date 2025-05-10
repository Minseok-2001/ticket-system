import { create } from "zustand";
import { persist } from "zustand/middleware";
import { User, LoginRequest, SignupRequest, TokenResponse } from "./types";
import api from "@/lib/api";
import { AxiosError } from "axios";

interface AuthState {
  user: User | null;
  token: TokenResponse | null;
  loading: boolean;
  error: string | null;

  // Actions
  setUser: (user: User | null) => void;
  setToken: (token: TokenResponse | null) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;

  // Auth operations
  login: (credentials: LoginRequest) => Promise<void>;
  signup: (data: SignupRequest) => Promise<void>;
  logout: () => Promise<void>;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      loading: false,
      error: null,

      setUser: (user) => set({ user }),
      setToken: (token) => set({ token }),
      setLoading: (loading) => set({ loading }),
      setError: (error) => set({ error }),

      login: async (credentials) => {
        try {
          set({ loading: true, error: null });
          const response = await api.post("/auth/login", credentials);

          const data = response.data;

          // 로컬 스토리지에 토큰 저장
          localStorage.setItem("token", data.token.accessToken);
          if (data.token.sessionId) {
            localStorage.setItem("refreshToken", data.token.sessionId);
          }

          set({
            user: data.user,
            token: data.token,
            loading: false,
          });
        } catch (error) {
          const axiosError = error as AxiosError<{ message: string }>;
          set({
            loading: false,
            error:
              axiosError.response?.data?.message || "로그인에 실패했습니다.",
          });
        }
      },

      signup: async (data) => {
        try {
          set({ loading: true, error: null });
          const response = await api.post("/auth/signup", data);

          const responseData = response.data;
          set({
            user: responseData.user,
            loading: false,
          });
        } catch (error) {
          const axiosError = error as AxiosError<{ message: string }>;
          set({
            loading: false,
            error:
              axiosError.response?.data?.message || "회원가입에 실패했습니다.",
          });
        }
      },

      logout: async () => {
        try {
          set({ loading: true, error: null });
          await api.post("/auth/logout");

          // 로컬 스토리지에서 토큰 제거
          localStorage.removeItem("token");
          localStorage.removeItem("refreshToken");

          set({ user: null, token: null, loading: false });
        } catch (error) {
          const axiosError = error as AxiosError<{ message: string }>;
          set({
            loading: false,
            error:
              axiosError.response?.data?.message || "로그아웃에 실패했습니다.",
          });
        }
      },
    }),
    {
      name: "auth-storage",
      partialize: (state) => ({ user: state.user, token: state.token }),
    }
  )
);
