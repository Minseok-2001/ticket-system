"use client";

import React, { useEffect } from "react";
import { useQueueStore } from "../store";
import { useAuthStore } from "../../auth/store";
import { Button } from "../../../components/ui/button";
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "../../../components/ui/card";
import Link from "next/link";

interface QueueStatusProps {
  eventId: number;
  eventName: string;
}

export const QueueStatus: React.FC<QueueStatusProps> = ({
  eventId,
  eventName,
}) => {
  const {
    position,
    status,
    loading,
    error,
    enterQueue,
    startPolling,
    stopPolling,
  } = useQueueStore();
  const { user } = useAuthStore();

  useEffect(() => {
    // 컴포넌트 마운트 시 폴링 시작
    if (user && position) {
      startPolling(eventId);
    }

    // 컴포넌트 언마운트 시 폴링 중단
    return () => {
      stopPolling();
    };
  }, [eventId, user, position, startPolling, stopPolling]);

  // 시간 포맷팅
  const formatWaitTime = (seconds: number) => {
    if (seconds < 60) {
      return `${seconds}초`;
    } else if (seconds < 3600) {
      const minutes = Math.floor(seconds / 60);
      return `${minutes}분`;
    } else {
      const hours = Math.floor(seconds / 3600);
      const minutes = Math.floor((seconds % 3600) / 60);
      return `${hours}시간 ${minutes}분`;
    }
  };

  const handleEnterQueue = async () => {
    if (!user) {
      return;
    }

    try {
      await enterQueue(eventId);
      startPolling(eventId);
    } catch (error) {
      console.error("대기열 참여 오류:", error);
    }
  };

  // 로딩 상태 표시
  if (loading && !position) {
    return (
      <Card className="w-full max-w-md mx-auto">
        <CardHeader>
          <CardTitle className="text-xl text-center">
            {eventName} 대기열
          </CardTitle>
        </CardHeader>
        <CardContent className="pb-6 text-center">
          <div className="flex flex-col items-center justify-center py-8">
            <div className="h-12 w-12 rounded-full border-4 border-t-primary border-r-primary border-b-primary/30 border-l-primary/30 animate-spin" />
            <p className="mt-4 text-muted-foreground">
              대기열 정보를 불러오는 중...
            </p>
          </div>
        </CardContent>
      </Card>
    );
  }

  // 에러 상태 표시
  if (error && !position) {
    return (
      <Card className="w-full max-w-md mx-auto">
        <CardHeader>
          <CardTitle className="text-xl text-center">
            {eventName} 대기열
          </CardTitle>
        </CardHeader>
        <CardContent className="pb-6 text-center">
          <div className="flex flex-col items-center justify-center py-8">
            <div className="mb-4 text-red-500">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="48"
                height="48"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <circle cx="12" cy="12" r="10" />
                <path d="m15 9-6 6" />
                <path d="m9 9 6 6" />
              </svg>
            </div>
            <p className="text-red-500 font-medium">{error}</p>
            <Button className="mt-4" onClick={handleEnterQueue}>
              다시 시도
            </Button>
          </div>
        </CardContent>
      </Card>
    );
  }

  // 대기열에 참여하지 않은 경우
  if (!position) {
    return (
      <Card className="w-full max-w-md mx-auto">
        <CardHeader>
          <CardTitle className="text-xl text-center">
            {eventName} 대기열
          </CardTitle>
        </CardHeader>
        <CardContent className="pb-6">
          <div className="space-y-4 text-center py-4">
            <p>
              이 이벤트는 현재 대기열로 운영되고 있습니다. 대기열에 참여하여
              순서가 오면 티켓 예매 페이지로 이동할 수 있습니다.
            </p>

            {status && (
              <div className="bg-muted/50 rounded-md p-4 mt-4">
                <p className="text-sm text-muted-foreground">
                  현재{" "}
                  <span className="font-medium text-foreground">
                    {status.totalWaiting.toLocaleString()}
                  </span>
                  명이 대기 중입니다.
                </p>
              </div>
            )}
          </div>
        </CardContent>
        <CardFooter>
          {!user ? (
            <div className="w-full space-y-2">
              <p className="text-sm text-muted-foreground text-center mb-2">
                대기열에 참여하려면 로그인이 필요합니다.
              </p>
              <Button className="w-full" asChild>
                <Link href={`/login?redirect=/queue/${eventId}`}>
                  로그인하고 대기열 참여
                </Link>
              </Button>
            </div>
          ) : (
            <Button
              className="w-full"
              onClick={handleEnterQueue}
              disabled={loading}
            >
              {loading ? "처리 중..." : "대기열 참여하기"}
            </Button>
          )}
        </CardFooter>
      </Card>
    );
  }

  // 대기열에 참여 중인 경우
  return (
    <Card className="w-full max-w-md mx-auto">
      <CardHeader>
        <CardTitle className="text-xl text-center">
          {eventName} 대기열
        </CardTitle>
      </CardHeader>
      <CardContent className="pb-6 text-center">
        <div className="space-y-6 py-4">
          <div className="flex flex-col items-center">
            <div className="text-3xl font-bold mb-2">
              {position.position.toLocaleString()}번
            </div>
            <p className="text-muted-foreground">현재 대기 순번</p>
          </div>

          <div className="space-y-2">
            <div className="w-full bg-secondary rounded-full h-2.5">
              <div
                className="bg-primary h-2.5 rounded-full"
                style={{
                  width: status
                    ? `${Math.min(
                        100,
                        (status.activeUsers / position.position) * 100
                      )}%`
                    : "0%",
                }}
              />
            </div>

            <div className="flex justify-between text-xs text-muted-foreground">
              <span>대기열 시작</span>
              <span>내 위치</span>
              <span>입장</span>
            </div>
          </div>

          <div className="bg-muted/50 rounded-md p-4">
            <p className="text-sm">
              예상 대기 시간:{" "}
              <span className="font-medium">
                {formatWaitTime(position.estimatedWaitTimeSeconds)}
              </span>
            </p>
            {status && (
              <p className="text-xs text-muted-foreground mt-1">
                현재 {status.activeUsers.toLocaleString()}명이 티켓 예매
                중입니다
              </p>
            )}
          </div>

          <div className="text-sm text-muted-foreground">
            <p>
              브라우저를 닫거나 새로고침해도 대기 순번은 유지됩니다. 페이지를
              계속 열어두시면 순번이 되었을 때 자동으로 알림을 드립니다.
            </p>
          </div>
        </div>
      </CardContent>
      <CardFooter className="flex-col gap-2">
        {position.position <= (status?.activeUsers || 0) ? (
          <Button className="w-full" asChild>
            <Link href={`/reserve/${eventId}`}>티켓 예매하기</Link>
          </Button>
        ) : (
          <Button className="w-full" disabled>
            대기 중...
          </Button>
        )}
        <Button variant="outline" className="w-full" asChild>
          <Link href={`/events/${eventId}`}>이벤트 정보 보기</Link>
        </Button>
      </CardFooter>
    </Card>
  );
};
