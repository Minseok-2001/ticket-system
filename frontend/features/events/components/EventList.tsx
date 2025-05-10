import React, { useEffect } from "react";
import { EventCard } from "./EventCard";
import { useEventsStore } from "../store";

interface EventListProps {
  initialPage?: number;
  pageSize?: number;
}

export const EventList: React.FC<EventListProps> = ({
  initialPage = 0,
  pageSize = 10,
}) => {
  const { events, loading, error, totalEvents, currentPage, fetchEvents } =
    useEventsStore();

  useEffect(() => {
    fetchEvents(initialPage, pageSize);
  }, [fetchEvents, initialPage, pageSize]);

  if (loading && events.length === 0) {
    return (
      <div className="flex justify-center items-center min-h-[200px]">
        <div className="text-center">
          <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-current border-r-transparent align-[-0.125em] motion-reduce:animate-[spin_1.5s_linear_infinite]"></div>
          <p className="mt-2 text-sm text-muted-foreground">
            이벤트 목록을 불러오는 중...
          </p>
        </div>
      </div>
    );
  }

  if (error && events.length === 0) {
    return (
      <div className="rounded-lg border border-destructive p-4 text-center">
        <p className="text-destructive">오류가 발생했습니다: {error}</p>
        <button
          className="mt-2 text-sm underline text-primary"
          onClick={() => fetchEvents(initialPage, pageSize)}
        >
          다시 시도
        </button>
      </div>
    );
  }

  if (events.length === 0) {
    return (
      <div className="rounded-lg border p-4 text-center">
        <p className="text-muted-foreground">
          현재 진행 중인 이벤트가 없습니다.
        </p>
      </div>
    );
  }

  return (
    <div>
      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
        {events.map((event) => (
          <EventCard key={event.id} event={event} />
        ))}
      </div>

      {/* 더 보기 버튼 */}
      {totalEvents > (currentPage + 1) * pageSize && (
        <div className="mt-6 text-center">
          <button
            className="inline-flex items-center justify-center px-4 py-2 text-sm font-medium border rounded-md border-input bg-background hover:bg-accent hover:text-accent-foreground"
            onClick={() => fetchEvents(currentPage + 1, pageSize)}
            disabled={loading}
          >
            {loading ? "로딩 중..." : "더 보기"}
          </button>
        </div>
      )}
    </div>
  );
};
