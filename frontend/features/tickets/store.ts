import { create } from "zustand";
import { Ticket, ReservationRequest, ReservationResponse } from "./types";
import api from "@/lib/api";
import { AxiosError } from "axios";

interface TicketsState {
  tickets: Ticket[];
  currentReservation: ReservationResponse | null;
  selectedTickets: { [ticketTypeId: number]: number }; // 티켓 타입 ID별 수량
  loading: boolean;
  error: string | null;

  // Actions
  setTickets: (tickets: Ticket[]) => void;
  setCurrentReservation: (reservation: ReservationResponse | null) => void;
  setSelectedTicket: (ticketTypeId: number, quantity: number) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  resetSelection: () => void;

  // Operations
  fetchMyTickets: () => Promise<Ticket[]>;
  createReservation: (
    request: ReservationRequest
  ) => Promise<ReservationResponse>;
  getReservation: (reservationId: string) => Promise<ReservationResponse>;
  confirmPayment: (reservationId: string, paymentId: string) => Promise<void>;
}

export const useTicketsStore = create<TicketsState>((set, get) => ({
  tickets: [],
  currentReservation: null,
  selectedTickets: {},
  loading: false,
  error: null,

  setTickets: (tickets) => set({ tickets }),
  setCurrentReservation: (reservation) =>
    set({ currentReservation: reservation }),
  setSelectedTicket: (ticketTypeId, quantity) =>
    set((state) => ({
      selectedTickets: {
        ...state.selectedTickets,
        [ticketTypeId]: quantity,
      },
    })),
  setLoading: (loading) => set({ loading }),
  setError: (error) => set({ error }),
  resetSelection: () => set({ selectedTickets: {} }),

  fetchMyTickets: async () => {
    try {
      set({ loading: true, error: null });
      const response = await api.get("/tickets");

      const data = response.data;
      set({ tickets: data.tickets, loading: false });

      return data.tickets;
    } catch (error) {
      const axiosError = error as AxiosError<{ message: string }>;
      set({
        loading: false,
        error:
          axiosError.response?.data?.message ||
          "티켓 목록을 불러오는데 실패했습니다.",
      });
      return [];
    }
  },

  createReservation: async (request) => {
    try {
      set({ loading: true, error: null });
      const response = await api.post("/tickets/reserve", request);

      const reservation: ReservationResponse = response.data;
      set({ currentReservation: reservation, loading: false });

      return reservation;
    } catch (error) {
      const axiosError = error as AxiosError<{ message: string }>;
      set({
        loading: false,
        error:
          axiosError.response?.data?.message || "티켓 예약에 실패했습니다.",
      });
      throw error;
    }
  },

  getReservation: async (reservationId) => {
    try {
      set({ loading: true, error: null });
      const response = await api.get(`/tickets/reservation/${reservationId}`);

      const reservation: ReservationResponse = response.data;
      set({ currentReservation: reservation, loading: false });

      return reservation;
    } catch (error) {
      const axiosError = error as AxiosError<{ message: string }>;
      set({
        loading: false,
        error:
          axiosError.response?.data?.message ||
          "예약 정보를 불러오는데 실패했습니다.",
      });
      throw error;
    }
  },

  confirmPayment: async (reservationId, paymentId) => {
    try {
      set({ loading: true, error: null });
      await api.post(`/tickets/payment/confirm`, { reservationId, paymentId });

      // 예약 정보 다시 불러오기
      await get().getReservation(reservationId);
      set({ loading: false });
    } catch (error) {
      const axiosError = error as AxiosError<{ message: string }>;
      set({
        loading: false,
        error:
          axiosError.response?.data?.message || "결제 확인에 실패했습니다.",
      });
      throw error;
    }
  },
}));
