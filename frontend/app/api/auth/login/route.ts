import { NextRequest, NextResponse } from "next/server";

// 백엔드 API 기본 URL 설정
const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8082/api";

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();

    // 백엔드 API 호출
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      const errorData = await response.json();
      return NextResponse.json(
        { message: errorData.message || "로그인에 실패했습니다." },
        { status: response.status }
      );
    }

    const tokenData = await response.json();

    // 사용자 정보 가져오기
    const userResponse = await fetch(`${API_BASE_URL}/auth/me`, {
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${tokenData.accessToken}`,
      },
    });

    if (!userResponse.ok) {
      return NextResponse.json(
        { message: "사용자 정보를 가져오는데 실패했습니다." },
        { status: userResponse.status }
      );
    }

    const userData = await userResponse.json();

    // 토큰과 사용자 정보 함께 반환
    return NextResponse.json({
      token: tokenData,
      user: userData,
    });
  } catch (error) {
    console.error("Login API error:", error);
    return NextResponse.json(
      { message: "서버 오류가 발생했습니다." },
      { status: 500 }
    );
  }
}
