import { Event } from "../events/types";

export interface Ticket {
  id: number;
  eventId: number;
  eventName: string;
  ticketTypeId: number;
  ticketTypeName: string;
  price: number;
  purchaseDate: string;
  reservationExpiryDate?: string;
  isConfirmed: boolean;
  isPaid: boolean;
  seatNumber?: string;
}

export interface ReservationRequest {
  eventId: number;
  ticketTypeId: number;
  quantity: number;
}

export interface ReservationResponse {
  reservationId: string;
  tickets: Ticket[];
  totalPrice: number;
  expiryTime: string;
  paymentUrl: string;
}

export interface MyTicketsResponse {
  reservations: {
    reservationId: string;
    eventId: number;
    eventName: string;
    purchaseDate: string;
    totalTickets: number;
    totalPrice: number;
    isPaid: boolean;
  }[];
  tickets: Ticket[];
  event?: Event;
}
