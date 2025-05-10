import type { Metadata } from "next";
import "./globals.css";
import { MainHeader } from "../widgets/MainHeader";
import { MainFooter } from "../widgets/MainFooter";

export const metadata: Metadata = {
  title: "티켓 예매 시스템",
  description: "빠르고 안정적인, 대규모 트래픽을 위한 티켓 예매 시스템",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body>
        <div className="flex flex-col min-h-screen">
          <MainHeader />
          <main className="flex-1 container py-8">{children}</main>
          <MainFooter />
        </div>
      </body>
    </html>
  );
}
