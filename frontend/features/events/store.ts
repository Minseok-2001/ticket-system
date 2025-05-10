import { create } from "zustand";
import { Event, EventsResponse } from "./types";

interface EventsState {
  events: Event[];
  currentEvent: Event | null;
  loading: boolean;
  error: string | null;
  totalEvents: number;
  currentPage: number;

  // Actions
  setEvents: (events: Event[]) => void;
  setCurrentEvent: (event: Event | null) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  setPagination: (total: number, page: number) => void;

  // Fetch actions (to be used with API)
  fetchEvents: (page?: number, size?: number) => Promise<void>;
  fetchEventById: (id: number) => Promise<void>;
}

export const useEventsStore = create<EventsState>((set) => ({
  events: [],
  currentEvent: null,
  loading: false,
  error: null,
  totalEvents: 0,
  currentPage: 0,

  setEvents: (events) => set({ events }),
  setCurrentEvent: (event) => set({ currentEvent: event }),
  setLoading: (loading) => set({ loading }),
  setError: (error) => set({ error }),
  setPagination: (total, page) =>
    set({ totalEvents: total, currentPage: page }),

  fetchEvents: async (page = 0, size = 10) => {
    try {
      set({ loading: true, error: null });
      const response = await fetch(`/api/events?page=${page}&size=${size}`);

      if (!response.ok) {
        throw new Error("이벤트 목록을 불러오는데 실패했습니다.");
      }

      const data: EventsResponse = await response.json();
      set({
        events: data.content,
        totalEvents: data.totalElements,
        currentPage: data.number,
        loading: false,
      });
    } catch (error) {
      set({
        loading: false,
        error:
          error instanceof Error
            ? error.message
            : "알 수 없는 오류가 발생했습니다.",
      });
    }
  },

  fetchEventById: async (id) => {
    try {
      set({ loading: true, error: null });
      const response = await fetch(`/api/events/${id}`);

      if (!response.ok) {
        throw new Error("이벤트 정보를 불러오는데 실패했습니다.");
      }

      const event: Event = await response.json();
      set({ currentEvent: event, loading: false });
    } catch (error) {
      set({
        loading: false,
        error:
          error instanceof Error
            ? error.message
            : "알 수 없는 오류가 발생했습니다.",
      });
    }
  },
}));
