import { NextRequest, NextResponse } from "next/server";

// 백엔드 API 기본 URL 설정
const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8082/api";

export async function GET(request: NextRequest) {
  try {
    // URL에서 쿼리 파라미터 추출
    const searchParams = request.nextUrl.searchParams;
    const page = searchParams.get("page") || "0";
    const size = searchParams.get("size") || "10";

    // 백엔드 API 호출
    const response = await fetch(
      `http://localhost:8082/api/events?page=${page}&size=${size}`,
      {
        headers: {
          "Content-Type": "application/json",
        },
        next: { revalidate: 60 }, // 60초마다 재검증
      }
    );

    if (!response.ok) {
      throw new Error(`API error: ${response.status}`);
    }

    const data = await response.json();

    return NextResponse.json(data);
  } catch (error) {
    console.error("Events API error:", error);
    return NextResponse.json(
      { message: "이벤트 목록을 불러오는데 실패했습니다." },
      { status: 500 }
    );
  }
}
