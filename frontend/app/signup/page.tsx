import React from "react";
import { SignupForm } from "../../features/auth/components/SignupForm";

export const metadata = {
  title: "회원가입 | 티켓 예매 시스템",
  description:
    "티켓 예매 시스템에 회원가입하여 다양한 이벤트 예매를 이용하세요.",
};

export default function SignupPage() {
  return (
    <div className="container max-w-md py-8 md:py-12">
      <SignupForm />
    </div>
  );
}
