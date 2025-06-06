import { NextRequest, NextResponse } from "next/server";
import { cookies } from "next/headers";

// 백엔드 API 기본 URL 설정
const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8082/api";

export async function GET(
  request: NextRequest,
  { params }: { params: { id: string } }
) {
  try {
    const eventId = params.id;
    const cookieStore = cookies();
    const token = cookieStore.get("token")?.value;

    // 인증 토큰이 없으면 401 에러 반환
    if (!token) {
      return NextResponse.json(
        { message: "로그인이 필요합니다." },
        { status: 401 }
      );
    }

    // 백엔드 API 호출
    const response = await fetch(`${API_BASE_URL}/queue/position/${eventId}`, {
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    });

    if (!response.ok) {
      const errorData = await response.json();
      return NextResponse.json(
        { message: errorData.message || "대기열 위치 조회에 실패했습니다." },
        { status: response.status }
      );
    }

    const data = await response.json();

    return NextResponse.json(data);
  } catch (error) {
    console.error(`Queue position API error for event ID ${params.id}:`, error);
    return NextResponse.json(
      { message: "서버 오류가 발생했습니다." },
      { status: 500 }
    );
  }
}
