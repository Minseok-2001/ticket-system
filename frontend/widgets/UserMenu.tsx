"use client";

import React, { useState } from "react";
import Link from "next/link";
import { User } from "../features/auth/types";
import { useAuthStore } from "../features/auth/store";

interface UserMenuProps {
  user: User;
}

export const UserMenu: React.FC<UserMenuProps> = ({ user }) => {
  const [isOpen, setIsOpen] = useState(false);
  const { logout } = useAuthStore();

  const toggleMenu = () => setIsOpen(!isOpen);
  const closeMenu = () => setIsOpen(false);

  const handleLogout = async () => {
    closeMenu();
    await logout();
  };

  return (
    <div className="relative">
      <button
        onClick={toggleMenu}
        className="flex items-center gap-2 rounded-full p-1 overflow-hidden focus:outline-none focus:ring-2 focus:ring-ring"
      >
        <div className="bg-primary text-primary-foreground font-medium h-8 w-8 rounded-full flex items-center justify-center">
          {user.name.substring(0, 1).toUpperCase()}
        </div>
      </button>

      {isOpen && (
        <>
          <div className="fixed inset-0 z-10" onClick={closeMenu} />
          <div className="absolute right-0 mt-2 w-56 rounded-md border bg-popover text-popover-foreground shadow-md z-20">
            <div className="p-2">
              <div className="border-b pb-2 mb-2">
                <p className="text-sm font-medium">{user.name}</p>
                <p className="text-xs text-muted-foreground">{user.email}</p>
              </div>
              <nav className="flex flex-col space-y-1">
                <Link
                  href="/my-tickets"
                  className="text-sm px-3 py-1.5 rounded-md hover:bg-accent transition-colors"
                  onClick={closeMenu}
                >
                  내 티켓
                </Link>
                <Link
                  href="/my-notifications"
                  className="text-sm px-3 py-1.5 rounded-md hover:bg-accent transition-colors"
                  onClick={closeMenu}
                >
                  알림
                </Link>
                <Link
                  href="/profile"
                  className="text-sm px-3 py-1.5 rounded-md hover:bg-accent transition-colors"
                  onClick={closeMenu}
                >
                  프로필 설정
                </Link>
                <button
                  onClick={handleLogout}
                  className="text-sm px-3 py-1.5 rounded-md hover:bg-accent text-left text-red-500 transition-colors"
                >
                  로그아웃
                </button>
              </nav>
            </div>
          </div>
        </>
      )}
    </div>
  );
};
