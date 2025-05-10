"use client";

import React, { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { QueueStatus } from "../../../features/queue/components/QueueStatus";
import Link from "next/link";

interface QueuePageProps {
  params: {
    id: string;
  };
}

export default function QueuePage({ params }: QueuePageProps) {
  const router = useRouter();
  const [eventData, setEventData] = useState<{
    id: number;
    name: string;
  } | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const eventId = parseInt(params.id, 10);

  useEffect(() => {
    // 이벤트 정보 가져오기
    const fetchEventData = async () => {
      try {
        setLoading(true);
        // API 호출 코드 (실제 구현 시에는 실제 API 엔드포인트를 사용해야 함)
        // 현재는 임시 데이터 사용
        const data = {
          id: eventId,
          name: "2023 여름 K-POP 페스티벌",
          venue: "올림픽 공원 체조경기장",
          isQueueActive: true,
        };

        if (!data.isQueueActive) {
          // 대기열이 활성화되지 않은 경우 이벤트 상세 페이지로 리다이렉트
          router.push(`/events/${eventId}`);
          return;
        }

        setEventData(data);
        setError(null);
      } catch (err) {
        setError("이벤트 정보를 불러오는 데 실패했습니다.");
        console.error("이벤트 정보 로딩 오류:", err);
      } finally {
        setLoading(false);
      }
    };

    if (!isNaN(eventId)) {
      fetchEventData();
    } else {
      setError("유효하지 않은 이벤트 ID입니다.");
      setLoading(false);
    }
  }, [eventId, router]);

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[50vh]">
        <div className="text-center">
          <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-current border-r-transparent align-[-0.125em] motion-reduce:animate-[spin_1.5s_linear_infinite]"></div>
          <p className="mt-2 text-sm text-muted-foreground">
            이벤트 정보를 불러오는 중...
          </p>
        </div>
      </div>
    );
  }

  if (error || !eventData) {
    return (
      <div className="text-center py-12">
        <h1 className="text-xl font-bold mb-4">오류가 발생했습니다</h1>
        <p className="text-muted-foreground mb-6">
          {error || "이벤트를 찾을 수 없습니다."}
        </p>
        <Link href="/events" className="text-primary hover:underline">
          이벤트 목록으로 돌아가기
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto">
      <div className="flex flex-col items-center">
        <div className="w-full mb-8">
          <div className="flex items-center gap-2 mb-4">
            <Link
              href="/events"
              className="text-sm text-muted-foreground hover:text-foreground"
            >
              이벤트 목록
            </Link>
            <span className="text-muted-foreground">/</span>
            <Link
              href={`/events/${eventId}`}
              className="text-sm text-muted-foreground hover:text-foreground"
            >
              {eventData.name}
            </Link>
            <span className="text-muted-foreground">/</span>
            <span className="text-sm">대기열</span>
          </div>

          <QueueStatus eventId={eventData.id} eventName={eventData.name} />
        </div>
      </div>
    </div>
  );
}
