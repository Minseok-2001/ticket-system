import React from "react";
import Link from "next/link";
import { EventList } from "../features/events/components/EventList";

export default function HomePage() {
  return (
    <div className="space-y-12">
      {/* 히어로 섹션 */}
      <section className="py-16 px-4 rounded-lg bg-gradient-to-r from-primary/10 to-secondary/10 text-center">
        <div className="max-w-3xl mx-auto space-y-4">
          <h1 className="text-4xl font-bold tracking-tight sm:text-5xl">
            티켓 예매 시스템
          </h1>
          <p className="text-lg text-muted-foreground">
            대규모 트래픽을 위한 안정적인 티켓 예매 시스템으로 좋은 자리를
            확보하세요. 실시간 알림과 공정한 대기열로 예매 경험이 더욱
            편리해집니다.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center pt-4">
            <Link
              href="/events"
              className="inline-flex h-11 items-center justify-center rounded-md bg-primary px-8 py-2 text-sm font-medium text-primary-foreground shadow hover:bg-primary/90"
            >
              이벤트 보기
            </Link>
            <Link
              href="/how-it-works"
              className="inline-flex h-11 items-center justify-center rounded-md border border-input bg-background px-8 py-2 text-sm font-medium shadow-sm hover:bg-accent hover:text-accent-foreground"
            >
              이용 방법
            </Link>
          </div>
        </div>
      </section>

      {/* 추천 이벤트 섹션 */}
      <section className="space-y-6">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold tracking-tight">인기 이벤트</h2>
          <Link href="/events" className="text-sm text-primary hover:underline">
            모두 보기 →
          </Link>
        </div>

        <EventList initialPage={0} pageSize={4} />
      </section>

      {/* 특징 섹션 */}
      <section className="py-12 bg-muted/30 rounded-lg">
        <div className="container mx-auto">
          <h2 className="text-2xl font-bold tracking-tight mb-8 text-center">
            서비스 특징
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="bg-card rounded-lg border p-6">
              <div className="rounded-full bg-primary/10 text-primary w-12 h-12 flex items-center justify-center mb-4">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z" />
                  <path d="m9 12 2 2 4-4" />
                </svg>
              </div>
              <h3 className="text-lg font-medium mb-2">안정적인 시스템</h3>
              <p className="text-muted-foreground">
                분산 시스템과 클라우드 인프라를 활용해 대규모 트래픽에도
                안정적인 서비스를 제공합니다.
              </p>
            </div>

            <div className="bg-card rounded-lg border p-6">
              <div className="rounded-full bg-primary/10 text-primary w-12 h-12 flex items-center justify-center mb-4">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <path d="M22 12H3" />
                  <path d="M17 7l5 5-5 5" />
                  <path d="M7 17l-5-5 5-5" />
                </svg>
              </div>
              <h3 className="text-lg font-medium mb-2">공정한 대기열</h3>
              <p className="text-muted-foreground">
                공정한 대기열 시스템으로 모든 사용자에게 동등한 예매 기회를
                제공합니다.
              </p>
            </div>

            <div className="bg-card rounded-lg border p-6">
              <div className="rounded-full bg-primary/10 text-primary w-12 h-12 flex items-center justify-center mb-4">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <path d="M22 9a8 8 0 0 1-16 0" />
                  <path d="M14 9h1" />
                  <path d="M9 9h1" />
                  <path d="m19 7-3 2" />
                  <path d="m5 7 3 2" />
                  <path d="M8 14a6 6 0 0 0 8 0" />
                  <path d="M18 18a10 10 0 0 1-12 0" />
                </svg>
              </div>
              <h3 className="text-lg font-medium mb-2">실시간 알림</h3>
              <p className="text-muted-foreground">
                대기열에서 예매 차례가 오면 실시간 푸시 알림으로 바로 확인할 수
                있습니다.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA 섹션 */}
      <section className="py-12 bg-primary text-primary-foreground rounded-lg text-center">
        <div className="container mx-auto px-4">
          <h2 className="text-2xl font-bold tracking-tight mb-4">
            지금 가입하고 편리한 티켓 예매를 경험하세요
          </h2>
          <p className="text-primary-foreground/80 mb-8 max-w-lg mx-auto">
            회원가입 시 이벤트 알림과 예매 내역 관리 등 다양한 서비스를 이용할
            수 있습니다.
          </p>
          <Link
            href="/signup"
            className="inline-flex h-11 items-center justify-center rounded-md bg-white text-primary px-8 py-2 text-sm font-medium shadow hover:bg-white/90"
          >
            무료로 시작하기
          </Link>
        </div>
      </section>
    </div>
  );
}
