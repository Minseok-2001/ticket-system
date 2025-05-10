import { NextRequest, NextResponse } from "next/server";

// 백엔드 API 기본 URL 설정
const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8082/api";

export async function GET(
  request: NextRequest,
  { params }: { params: { id: string } }
) {
  try {
    const eventId = params.id;

    // 백엔드 API 호출 (공개 API이므로 인증 토큰 필요 없음)
    const response = await fetch(`${API_BASE_URL}/queue/status/${eventId}`, {
      headers: {
        "Content-Type": "application/json",
      },
      next: { revalidate: 10 }, // 10초마다 재검증
    });

    if (!response.ok) {
      const errorData = await response.json();
      return NextResponse.json(
        { message: errorData.message || "대기열 상태 조회에 실패했습니다." },
        { status: response.status }
      );
    }

    const data = await response.json();

    return NextResponse.json(data);
  } catch (error) {
    console.error(`Queue status API error for event ID ${params.id}:`, error);
    return NextResponse.json(
      { message: "서버 오류가 발생했습니다." },
      { status: 500 }
    );
  }
}
