import React from "react";
import Link from "next/link";
import { Button } from "../../../components/ui/button";
import { Card, CardContent, CardFooter } from "../../../components/ui/card";

interface EventDetailPageProps {
  params: {
    id: string;
  };
}

// 메타데이터 생성
export async function generateMetadata({ params }: EventDetailPageProps) {
  try {
    const eventId = params.id;
    const event = await fetchEvent(eventId);
    return {
      title: `${event.name} | 티켓 예매 시스템`,
      description: event.content.substring(0, 160),
    };
  } catch (error) {
    return {
      title: "이벤트 상세 | 티켓 예매 시스템",
      description: "티켓 예매 시스템에서 이벤트 상세 정보를 확인하세요.",
    };
  }
}

// 서버 컴포넌트에서 이벤트 데이터 가져오기
async function fetchEvent(eventId: string) {
  // 실제 API 호출은 배포 환경에서 구현
  // 지금은 목업 데이터 반환
  return {
    id: Number(eventId),
    name: "2023 여름 K-POP 페스티벌",
    content:
      "국내 최고의 K-POP 아티스트들이 총출동하는 여름 페스티벌! 화려한 무대와 풍성한 이벤트가 준비되어 있습니다.",
    venue: "올림픽 공원 체조경기장",
    eventDate: "2023-07-15T18:00:00",
    salesStartDate: "2023-06-01T10:00:00",
    salesEndDate: "2023-07-14T18:00:00",
    totalSeats: 5000,
    status: "ACTIVE",
    isQueueActive: true,
    ticketTypes: [
      {
        id: 1,
        name: "VIP",
        price: 150000,
        quantity: 500,
        description: "VIP 좌석 (스페셜 굿즈 포함)",
      },
      {
        id: 2,
        name: "R석",
        price: 120000,
        quantity: 1500,
        description: "R석 좌석",
      },
      {
        id: 3,
        name: "S석",
        price: 90000,
        quantity: 3000,
        description: "S석 좌석",
      },
    ],
    createdAt: "2023-05-01T09:00:00",
    updatedAt: "2023-05-15T11:30:00",
  };
}

export default async function EventDetailPage({
  params,
}: EventDetailPageProps) {
  const eventId = params.id;
  const event = await fetchEvent(eventId);

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

  // 판매 중인지 확인
  const isOnSale =
    event.status === "ACTIVE" &&
    new Date() >= new Date(event.salesStartDate) &&
    new Date() <= new Date(event.salesEndDate);

  return (
    <div className="space-y-8">
      {/* 상단 정보 */}
      <div className="space-y-4">
        <div className="flex items-center gap-2">
          <Link
            href="/events"
            className="text-sm text-muted-foreground hover:text-foreground"
          >
            이벤트 목록
          </Link>
          <span className="text-muted-foreground">/</span>
          <span className="text-sm">{event.name}</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="md:col-span-2 space-y-4">
            <h1 className="text-3xl font-bold tracking-tight">{event.name}</h1>

            <div className="flex flex-wrap gap-4 text-sm text-muted-foreground">
              <div className="flex items-center gap-1">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  className="lucide-map-pin"
                >
                  <path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z" />
                  <circle cx="12" cy="10" r="3" />
                </svg>
                <span>{event.venue}</span>
              </div>

              <div className="flex items-center gap-1">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  className="lucide-calendar"
                >
                  <rect width="18" height="18" x="3" y="4" rx="2" ry="2" />
                  <line x1="16" x2="16" y1="2" y2="6" />
                  <line x1="8" x2="8" y1="2" y2="6" />
                  <line x1="3" x2="21" y1="10" y2="10" />
                </svg>
                <span>{formatDate(event.eventDate)}</span>
              </div>

              <div className="flex items-center gap-1">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  className="lucide-ticket"
                >
                  <path d="M2 9a3 3 0 0 1 0 6v2a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-2a3 3 0 0 1 0-6V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2Z" />
                  <path d="M13 5v2" />
                  <path d="M13 17v2" />
                  <path d="M13 11v2" />
                </svg>
                <span>
                  티켓 판매: {formatDate(event.salesStartDate)} ~{" "}
                  {formatDate(event.salesEndDate)}
                </span>
              </div>
            </div>

            <div className="py-4 border-t">
              <h2 className="text-xl font-semibold mb-4">이벤트 소개</h2>
              <p className="whitespace-pre-line">{event.content}</p>
            </div>
          </div>

          {/* 티켓 구매 카드 */}
          <div>
            <Card className="sticky top-20">
              <CardContent className="pt-6">
                <h3 className="text-lg font-semibold mb-4">티켓 구매</h3>

                <div className="space-y-4">
                  {event.ticketTypes.map((ticketType) => (
                    <div
                      key={ticketType.id}
                      className="flex justify-between items-center pb-2 border-b"
                    >
                      <div>
                        <p className="font-medium">{ticketType.name}</p>
                        <p className="text-sm text-muted-foreground">
                          {ticketType.description}
                        </p>
                      </div>
                      <div className="text-right">
                        <p className="font-semibold">
                          {ticketType.price.toLocaleString()}원
                        </p>
                        <p className="text-xs text-muted-foreground">
                          남은 좌석: {ticketType.quantity}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
              <CardFooter className="flex-col gap-4">
                {event.isQueueActive ? (
                  <Button className="w-full" asChild>
                    <Link href={`/queue/${event.id}`}>대기열 참여하기</Link>
                  </Button>
                ) : isOnSale ? (
                  <Button className="w-full" asChild>
                    <Link href={`/reserve/${event.id}`}>예매하기</Link>
                  </Button>
                ) : (
                  <Button className="w-full" disabled>
                    {new Date() < new Date(event.salesStartDate)
                      ? "판매 준비중"
                      : "판매 종료"}
                  </Button>
                )}

                <div className="text-xs text-center text-muted-foreground">
                  {event.isQueueActive ? (
                    <p>
                      현재 대기열이 운영 중입니다. 대기열에 참여하여 순서대로
                      예매하세요.
                    </p>
                  ) : new Date() < new Date(event.salesStartDate) ? (
                    <p>티켓 판매 시작: {formatDate(event.salesStartDate)}</p>
                  ) : new Date() > new Date(event.salesEndDate) ? (
                    <p>티켓 판매가 종료되었습니다.</p>
                  ) : (
                    <p>선착순 예매가 가능합니다.</p>
                  )}
                </div>
              </CardFooter>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}
