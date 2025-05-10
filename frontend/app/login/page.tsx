import React from "react";
import { LoginForm } from "../../features/auth/components/LoginForm";

export const metadata = {
  title: "로그인 | 티켓 예매 시스템",
  description: "티켓 예매 시스템에 로그인하여 서비스를 이용하세요.",
};

export default function LoginPage() {
  return (
    <div className="container max-w-md py-8 md:py-12">
      <LoginForm />
    </div>
  );
}
