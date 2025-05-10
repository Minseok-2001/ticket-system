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

    // 백엔드 API 호출
    const response = await fetch(`${API_BASE_URL}/events/${eventId}`, {
      headers: {
        "Content-Type": "application/json",
      },
      next: { revalidate: 60 }, // 60초마다 재검증
    });

    if (!response.ok) {
      throw new Error(`API error: ${response.status}`);
    }

    const data = await response.json();

    return NextResponse.json(data);
  } catch (error) {
    console.error(`Event API error for ID ${params.id}:`, error);
    return NextResponse.json(
      { message: "이벤트 정보를 불러오는데 실패했습니다." },
      { status: 500 }
    );
  }
}
