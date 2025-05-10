"use client";

import React from "react";
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "../../../components/ui/card";
import { Button } from "../../../components/ui/button";
import { TicketType } from "../../events/types";
import { useTicketsStore } from "../store";

interface TicketSelectorProps {
  eventId: number;
  ticketTypes: TicketType[];
  onReserve: () => void;
}

export const TicketSelector: React.FC<TicketSelectorProps> = ({
  eventId,
  ticketTypes,
  onReserve,
}) => {
  const { selectedTickets, setSelectedTicket, resetSelection } =
    useTicketsStore();

  // 티켓 수량 증가
  const incrementTicket = (ticketTypeId: number) => {
    const currentQuantity = selectedTickets[ticketTypeId] || 0;
    const ticketType = ticketTypes.find((t) => t.id === ticketTypeId);

    // 남은 수량 체크
    if (ticketType && currentQuantity < ticketType.quantity) {
      setSelectedTicket(ticketTypeId, currentQuantity + 1);
    }
  };

  // 티켓 수량 감소
  const decrementTicket = (ticketTypeId: number) => {
    const currentQuantity = selectedTickets[ticketTypeId] || 0;
    if (currentQuantity > 0) {
      setSelectedTicket(ticketTypeId, currentQuantity - 1);
    }
  };

  // 선택한 티켓 총 가격 계산
  const calculateTotalPrice = () => {
    return ticketTypes.reduce((total, ticket) => {
      const quantity = selectedTickets[ticket.id] || 0;
      return total + ticket.price * quantity;
    }, 0);
  };

  // 선택한 티켓 수량
  const totalTicketsSelected = Object.values(selectedTickets).reduce(
    (sum, quantity) => sum + quantity,
    0
  );

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl">티켓 선택</CardTitle>
      </CardHeader>

      <CardContent className="space-y-4">
        {ticketTypes.map((ticket) => (
          <div
            key={ticket.id}
            className="flex justify-between items-center border-b pb-3"
          >
            <div>
              <h3 className="font-semibold">{ticket.name}</h3>
              <p className="text-sm text-muted-foreground">
                {ticket.description}
              </p>
              <p className="text-base mt-1">
                {ticket.price.toLocaleString()}원
              </p>
              <p className="text-xs text-muted-foreground">
                남은 좌석: {ticket.quantity}석
              </p>
            </div>

            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => decrementTicket(ticket.id)}
                disabled={!selectedTickets[ticket.id]}
                className="h-8 w-8 p-0"
              >
                -
              </Button>

              <span className="w-6 text-center">
                {selectedTickets[ticket.id] || 0}
              </span>

              <Button
                variant="outline"
                size="sm"
                onClick={() => incrementTicket(ticket.id)}
                disabled={ticket.quantity <= (selectedTickets[ticket.id] || 0)}
                className="h-8 w-8 p-0"
              >
                +
              </Button>
            </div>
          </div>
        ))}

        {totalTicketsSelected > 0 && (
          <div className="pt-4">
            <div className="flex justify-between font-semibold text-lg mt-1">
              <span>총 금액</span>
              <span>{calculateTotalPrice().toLocaleString()}원</span>
            </div>
          </div>
        )}
      </CardContent>

      <CardFooter className="flex-col sm:flex-row gap-2">
        <Button
          variant="outline"
          className="w-full sm:w-auto"
          onClick={resetSelection}
          disabled={totalTicketsSelected === 0}
        >
          초기화
        </Button>

        <Button
          className="w-full sm:w-auto ml-auto"
          disabled={totalTicketsSelected === 0}
          onClick={onReserve}
        >
          예매하기
        </Button>
      </CardFooter>
    </Card>
  );
};
