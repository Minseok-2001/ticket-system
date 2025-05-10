import { create } from "zustand";
import { QueuePositionResponse, QueueStatusResponse } from "./types";
import api from "@/lib/api";
import { AxiosError } from "axios";

interface QueueState {
  position: QueuePositionResponse | null;
  status: QueueStatusResponse | null;
  loading: boolean;
  error: string | null;
  pollingInterval: number | null;

  // Actions
  setPosition: (position: QueuePositionResponse | null) => void;
  setStatus: (status: QueueStatusResponse | null) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  setPollingInterval: (interval: number | null) => void;

  // Operations
  enterQueue: (eventId: number) => Promise<QueuePositionResponse | undefined>;
  getPosition: (eventId: number) => Promise<QueuePositionResponse | undefined>;
  getStatus: (eventId: number) => Promise<QueueStatusResponse | undefined>;
  startPolling: (eventId: number, intervalMs?: number) => void;
  stopPolling: () => void;
  reset: () => void;
}

export const useQueueStore = create<QueueState>((set, get) => ({
  position: null,
  status: null,
  loading: false,
  error: null,
  pollingInterval: null,

  setPosition: (position) => set({ position }),
  setStatus: (status) => set({ status }),
  setLoading: (loading) => set({ loading }),
  setError: (error) => set({ error }),
  setPollingInterval: (interval) => set({ pollingInterval: interval }),

  enterQueue: async (eventId) => {
    try {
      set({ loading: true, error: null });
      const response = await api.post("/queue/enter", { eventId });

      const position: QueuePositionResponse = response.data;
      set({ position, loading: false });

      return position;
    } catch (error) {
      const axiosError = error as AxiosError<{ message: string }>;
      set({
        loading: false,
        error:
          axiosError.response?.data?.message || "대기열 입장에 실패했습니다.",
      });
      throw error;
    }
  },

  getPosition: async (eventId) => {
    try {
      set({ loading: true, error: null });
      const response = await api.get(`/queue/position/${eventId}`);

      const position: QueuePositionResponse = response.data;
      set({ position, loading: false });

      return position;
    } catch (error) {
      const axiosError = error as AxiosError<{ message: string }>;
      set({
        loading: false,
        error:
          axiosError.response?.data?.message ||
          "대기열 위치 조회에 실패했습니다.",
      });
    }
  },

  getStatus: async (eventId) => {
    try {
      set({ error: null });
      const response = await api.get(`/queue/status/${eventId}`);

      const status: QueueStatusResponse = response.data;
      set({ status });

      return status;
    } catch (error) {
      const axiosError = error as AxiosError<{ message: string }>;
      set({
        error:
          axiosError.response?.data?.message ||
          "대기열 상태 조회에 실패했습니다.",
      });
    }
  },

  startPolling: (eventId, intervalMs = 5000) => {
    // 이미 폴링 중이라면 중단
    get().stopPolling();

    // 즉시 상태 가져오기
    get().getPosition(eventId);
    get().getStatus(eventId);

    // 폴링 시작
    const interval = window.setInterval(() => {
      get().getPosition(eventId);
      get().getStatus(eventId);
    }, intervalMs);

    set({ pollingInterval: interval });
  },

  stopPolling: () => {
    const { pollingInterval } = get();
    if (pollingInterval !== null) {
      window.clearInterval(pollingInterval);
      set({ pollingInterval: null });
    }
  },

  reset: () => {
    get().stopPolling();
    set({
      position: null,
      status: null,
      loading: false,
      error: null,
      pollingInterval: null,
    });
  },
}));
