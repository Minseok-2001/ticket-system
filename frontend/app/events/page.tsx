import React from "react";
import { EventList } from "../../features/events/components/EventList";

export const metadata = {
  title: "이벤트 목록 | 티켓 예매 시스템",
  description:
    "다양한 공연, 콘서트, 스포츠 경기 등의 이벤트 예매를 편리하게 이용하세요.",
};

export default function EventsPage() {
  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">이벤트</h1>
        <p className="text-muted-foreground mt-2">
          다양한 공연, 콘서트, 스포츠 경기 등의 이벤트를 만나보세요.
        </p>
      </div>

      {/* 필터 UI가 여기에 추가될 수 있음 */}

      <EventList />
    </div>
  );
}
