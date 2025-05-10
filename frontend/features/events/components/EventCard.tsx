import React from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "../../../components/ui/card";
import { Button } from "../../../components/ui/button";
import { Event } from "../types";
import Link from "next/link";

interface EventCardProps {
  event: Event;
}

export const EventCard: React.FC<EventCardProps> = ({ event }) => {
  // 날짜 포맷팅
  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return new Intl.DateTimeFormat("ko-KR", {
      year: "numeric",
      month: "long",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    }).format(date);
  };

  // 이벤트 상태에 따른 배지 스타일
  const getStatusBadge = () => {
    switch (event.status) {
      case "ACTIVE":
        return (
          <span className="px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">
            진행 중
          </span>
        );
      case "UPCOMING":
        return (
          <span className="px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800">
            예정됨
          </span>
        );
      case "ENDED":
        return (
          <span className="px-2 py-1 text-xs font-semibold rounded-full bg-gray-100 text-gray-800">
            종료됨
          </span>
        );
      case "CANCELLED":
        return (
          <span className="px-2 py-1 text-xs font-semibold rounded-full bg-red-100 text-red-800">
            취소됨
          </span>
        );
      default:
        return null;
    }
  };

  // 예매 가능 여부
  const isBookable =
    event.status === "ACTIVE" && new Date(event.salesEndDate) > new Date();

  // 가장 저렴한 티켓 가격
  const cheapestTicket =
    event.ticketTypes.length > 0
      ? event.ticketTypes.reduce((prev, current) =>
          prev.price < current.price ? prev : current
        )
      : null;

  return (
    <Card className="overflow-hidden transition-all hover:shadow-md">
      <CardHeader className="relative pb-2">
        <div className="flex justify-between items-start">
          <CardTitle className="text-xl">{event.name}</CardTitle>
          <div>{getStatusBadge()}</div>
        </div>
        <CardDescription className="pt-1">
          <span>
            {event.venue} · {formatDate(event.eventDate)}
          </span>
        </CardDescription>
      </CardHeader>
      <CardContent className="pb-2">
        <p className="text-sm text-muted-foreground line-clamp-2">
          {event.content}
        </p>

        {cheapestTicket && (
          <div className="mt-3 text-sm">
            <span className="font-semibold">
              {cheapestTicket.price.toLocaleString()}원
            </span>
            <span className="text-muted-foreground"> 부터</span>
          </div>
        )}
      </CardContent>
      <CardFooter className="pt-2">
        {event.isQueueActive ? (
          <Button asChild className="w-full">
            <Link href={`/queue/${event.id}`}>대기열 참여</Link>
          </Button>
        ) : isBookable ? (
          <Button asChild className="w-full">
            <Link href={`/events/${event.id}`}>예매하기</Link>
          </Button>
        ) : (
          <Button asChild variant="outline" className="w-full">
            <Link href={`/events/${event.id}`}>상세보기</Link>
          </Button>
        )}
      </CardFooter>
    </Card>
  );
};
