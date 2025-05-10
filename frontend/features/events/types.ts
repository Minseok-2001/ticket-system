export type EventStatus = "UPCOMING" | "ACTIVE" | "ENDED" | "CANCELLED";

export interface TicketType {
  id: number;
  name: string;
  price: number;
  quantity: number;
  description?: string;
}

export interface Event {
  id: number;
  name: string;
  content: string;
  venue: string;
  eventDate: string;
  salesStartDate: string;
  salesEndDate: string;
  totalSeats: number;
  status: EventStatus;
  isQueueActive: boolean;
  ticketTypes: TicketType[];
  createdAt: string;
  updatedAt: string;
}

export interface EventsResponse {
  content: Event[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}
