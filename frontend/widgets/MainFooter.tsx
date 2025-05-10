import React from "react";
import Link from "next/link";

export const MainFooter: React.FC = () => {
  return (
    <footer className="border-t bg-background">
      <div className="container flex flex-col md:flex-row py-10 md:py-12 gap-8 md:gap-16">
        <div className="flex-1">
          <div className="text-2xl font-bold tracking-tighter text-primary mb-4">
            티켓 예매 시스템
          </div>
          <p className="text-sm text-muted-foreground max-w-sm">
            높은 TPS와 안정성을 갖춘 티켓 예매 시스템입니다. 빠르고 공정한 예매
            경험을 제공합니다.
          </p>
        </div>
        <div className="grid grid-cols-2 md:grid-cols-3 gap-10">
          <div className="space-y-3">
            <h3 className="text-sm font-medium">서비스</h3>
            <ul className="space-y-2">
              <li>
                <Link
                  href="/events"
                  className="text-sm text-muted-foreground hover:text-foreground transition-colors"
                >
                  이벤트 예매
                </Link>
              </li>
              <li>
                <Link
                  href="/how-it-works"
                  className="text-sm text-muted-foreground hover:text-foreground transition-colors"
                >
                  이용 방법
                </Link>
              </li>
              <li>
                <Link
                  href="/faq"
                  className="text-sm text-muted-foreground hover:text-foreground transition-colors"
                >
                  자주 묻는 질문
                </Link>
              </li>
            </ul>
          </div>
          <div className="space-y-3">
            <h3 className="text-sm font-medium">지원</h3>
            <ul className="space-y-2">
              <li>
                <Link
                  href="/contact"
                  className="text-sm text-muted-foreground hover:text-foreground transition-colors"
                >
                  고객센터
                </Link>
              </li>
              <li>
                <Link
                  href="/terms"
                  className="text-sm text-muted-foreground hover:text-foreground transition-colors"
                >
                  이용약관
                </Link>
              </li>
              <li>
                <Link
                  href="/privacy"
                  className="text-sm text-muted-foreground hover:text-foreground transition-colors"
                >
                  개인정보 처리방침
                </Link>
              </li>
            </ul>
          </div>
          <div className="space-y-3 col-span-2 md:col-span-1">
            <h3 className="text-sm font-medium">문의</h3>
            <ul className="space-y-2">
              <li className="text-sm text-muted-foreground">
                이메일:{" "}
                <a
                  href="mailto:support@ticket.example.com"
                  className="hover:text-foreground"
                >
                  support@ticket.example.com
                </a>
              </li>
              <li className="text-sm text-muted-foreground">
                전화:{" "}
                <a href="tel:+8210000000000" className="hover:text-foreground">
                  02-000-0000
                </a>
              </li>
            </ul>
          </div>
        </div>
      </div>
      <div className="border-t py-6">
        <div className="container flex flex-col md:flex-row justify-between items-center gap-4">
          <p className="text-xs text-muted-foreground text-center md:text-left">
            &copy; {new Date().getFullYear()} 티켓 예매 시스템. 모든 권리 보유.
          </p>
          <div className="flex items-center gap-4">
            <Link
              href="https://twitter.com"
              target="_blank"
              rel="noopener noreferrer"
              className="text-muted-foreground hover:text-foreground"
              aria-label="Twitter"
            >
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
                className="h-5 w-5"
              >
                <path d="M22 4s-.7 2.1-2 3.4c1.6 10-9.4 17.3-18 11.6 2.2.1 4.4-.6 6-2C3 15.5.5 9.6 3 5c2.2 2.6 5.6 4.1 9 4-.9-4.2 4-6.6 7-3.8 1.1 0 3-1.2 3-1.2z" />
              </svg>
            </Link>
            <Link
              href="https://facebook.com"
              target="_blank"
              rel="noopener noreferrer"
              className="text-muted-foreground hover:text-foreground"
              aria-label="Facebook"
            >
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
                className="h-5 w-5"
              >
                <path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z" />
              </svg>
            </Link>
            <Link
              href="https://instagram.com"
              target="_blank"
              rel="noopener noreferrer"
              className="text-muted-foreground hover:text-foreground"
              aria-label="Instagram"
            >
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
                className="h-5 w-5"
              >
                <rect x="2" y="2" width="20" height="20" rx="5" ry="5" />
                <path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z" />
                <line x1="17.5" y1="6.5" x2="17.51" y2="6.5" />
              </svg>
            </Link>
          </div>
        </div>
      </div>
    </footer>
  );
};
