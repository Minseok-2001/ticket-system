"use client";

import React from "react";
import Link from "next/link";
import { useAuthStore } from "../features/auth/store";
import { UserMenu } from "./UserMenu";

export const MainHeader: React.FC = () => {
  const { user } = useAuthStore();

  return (
    <header className="border-b bg-background sticky top-0 z-50">
      <div className="container flex h-16 items-center justify-between">
        <div className="flex items-center gap-6">
          <Link
            href="/"
            className="text-2xl font-bold tracking-tighter text-primary"
          >
            티켓 예매
          </Link>
          <nav className="hidden md:flex gap-6">
            <Link
              href="/"
              className="text-sm font-medium hover:text-primary transition-colors"
            >
              홈
            </Link>
            <Link
              href="/events"
              className="text-sm font-medium hover:text-primary transition-colors"
            >
              이벤트
            </Link>
            {user && (
              <Link
                href="/my-tickets"
                className="text-sm font-medium hover:text-primary transition-colors"
              >
                내 티켓
              </Link>
            )}
          </nav>
        </div>
        <div className="flex items-center gap-4">
          {user ? (
            <UserMenu user={user} />
          ) : (
            <div className="flex gap-4">
              <Link
                href="/login"
                className="inline-flex h-9 items-center justify-center rounded-md border border-input bg-background px-4 py-2 text-sm font-medium shadow-sm hover:bg-accent hover:text-accent-foreground"
              >
                로그인
              </Link>
              <Link
                href="/signup"
                className="inline-flex h-9 items-center justify-center rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground shadow hover:bg-primary/90"
              >
                회원가입
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
