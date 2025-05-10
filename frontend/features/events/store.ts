import { create } from "zustand";
import { Event, EventsResponse } from "./types";
// 주석 처리: import api from "@/lib/api";
// 주석 처리: import { AxiosError } from "axios";

// 더미 데이터 추가
const mockEvents: Event[] = [
  {
    id: 1,
    name: "2023 여름 페스티벌",
    content: "국내 최고의 아티스트들이 총출동하는 여름 페스티벌입니다.",
    venue: "올림픽 공원",
    eventDate: "2023-07-15T18:00:00",
    salesStartDate: "2023-06-01T10:00:00",
    salesEndDate: "2023-07-14T18:00:00",
    totalSeats: 5000,
    status: "ACTIVE",
    isQueueActive: true,
    ticketTypes: [
      {
        id: 1,
        name: "VIP석",
        price: 150000,
        quantity: 500,
        description: "스페셜 굿즈 포함",
      },
      {
        id: 2,
        name: "일반석",
        price: 88000,
        quantity: 4500,
      },
    ],
    createdAt: "2023-05-01T00:00:00",
    updatedAt: "2023-05-01T00:00:00",
  },
  {
    id: 2,
    name: "클래식 음악회",
    content: "유명 오케스트라와 함께하는 클래식 음악의 밤",
    venue: "예술의전당",
    eventDate: "2023-08-20T19:30:00",
    salesStartDate: "2023-07-01T10:00:00",
    salesEndDate: "2023-08-19T18:00:00",
    totalSeats: 2000,
    status: "UPCOMING",
    isQueueActive: false,
    ticketTypes: [
      {
        id: 3,
        name: "R석",
        price: 120000,
        quantity: 1000,
      },
      {
        id: 4,
        name: "S석",
        price: 80000,
        quantity: 1000,
      },
    ],
    createdAt: "2023-05-10T00:00:00",
    updatedAt: "2023-05-10T00:00:00",
  },
  {
    id: 3,
    name: "연극 '인생'",
    content: "화제의 연극 '인생'이 찾아옵니다. 감동과 웃음이 함께하는 시간!",
    venue: "국립극장",
    eventDate: "2023-09-10T15:00:00",
    salesStartDate: "2023-08-01T10:00:00",
    salesEndDate: "2023-09-09T18:00:00",
    totalSeats: 800,
    status: "UPCOMING",
    isQueueActive: false,
    ticketTypes: [
      {
        id: 5,
        name: "프리미엄",
        price: 70000,
        quantity: 200,
      },
      {
        id: 6,
        name: "일반",
        price: 50000,
        quantity: 600,
      },
    ],
    createdAt: "2023-05-15T00:00:00",
    updatedAt: "2023-05-15T00:00:00",
  },
  {
    id: 4,
    name: "재즈 나이트",
    content: "도심 속 재즈의 향연. 감미로운 음악과 함께하는 특별한 밤",
    venue: "블루노트 서울",
    eventDate: "2023-07-25T20:00:00",
    salesStartDate: "2023-06-25T10:00:00",
    salesEndDate: "2023-07-24T18:00:00",
    totalSeats: 300,
    status: "ACTIVE",
    isQueueActive: false,
    ticketTypes: [
      {
        id: 7,
        name: "테이블석",
        price: 100000,
        quantity: 100,
      },
      {
        id: 8,
        name: "바 석",
        price: 70000,
        quantity: 200,
      },
    ],
    createdAt: "2023-05-20T00:00:00",
    updatedAt: "2023-05-20T00:00:00",
  },
];

// 더미 데이터를 페이지네이션 형식으로 반환하는 함수
const getMockEventsPaginated = (page: number, size: number): EventsResponse => {
  const startIndex = page * size;
  const endIndex = startIndex + size;
  const paginatedEvents = mockEvents.slice(startIndex, endIndex);

  return {
    content: paginatedEvents,
    totalElements: mockEvents.length,
    totalPages: Math.ceil(mockEvents.length / size),
    size: size,
    number: page,
    first: page === 0,
    last: (page + 1) * size >= mockEvents.length,
    numberOfElements: paginatedEvents.length,
    empty: paginatedEvents.length === 0,
  };
};

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

      // 실제 백엔드 API가 아직 없으므로 더미 데이터 사용
      // 실제 API 구현 후 주석 해제
      /*
      const response = await api.get(`/events`, {
        params: { page, size }
      });
      const data: EventsResponse = response.data;
      */

      // 더미 데이터 사용
      const data = getMockEventsPaginated(page, size);

      set({
        events: data.content,
        totalEvents: data.totalElements,
        currentPage: data.number,
        loading: false,
      });
    } catch (_) {
      console.log("백엔드 연결 실패, 더미 데이터를 사용합니다.");
      // 더미 데이터 사용
      const data = getMockEventsPaginated(page, size);
      set({
        loading: false,
        error: null,
        events: data.content,
        totalEvents: data.totalElements,
        currentPage: data.number,
      });
    }
  },

  fetchEventById: async (id) => {
    try {
      set({ loading: true, error: null });

      // 실제 백엔드 API가 아직 없으므로 더미 데이터 사용
      // 실제 API 구현 후 주석 해제
      /*
      const response = await api.get(`/events/${id}`);
      const event: Event = response.data;
      */

      // 더미 데이터에서 이벤트 찾기
      const mockEvent = mockEvents.find((event) => event.id === id);

      if (mockEvent) {
        set({ currentEvent: mockEvent, loading: false });
      } else {
        set({
          loading: false,
          error: "이벤트를 찾을 수 없습니다.",
        });
      }
    } catch {
      console.log("백엔드 연결 실패, 더미 데이터를 사용합니다.");

      // 백엔드 연결 실패 시 더미 데이터 사용
      const mockEvent = mockEvents.find((event) => event.id === id);
      if (mockEvent) {
        set({ currentEvent: mockEvent, loading: false, error: null });
      } else {
        set({
          loading: false,
          error: "이벤트를 찾을 수 없습니다.",
        });
      }
    }
  },
}));
