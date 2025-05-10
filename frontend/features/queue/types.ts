export interface EnterQueueRequest {
  eventId: number;
}

export interface QueuePositionResponse {
  eventId: number;
  memberId: number;
  position: number;
  estimatedWaitTimeSeconds: number;
  timestamp: string;
}

export interface QueueStatusResponse {
  eventId: number;
  totalWaiting: number;
  activeUsers: number;
  maxActiveUsers: number;
  isQueueActive: boolean;
  timestamp: string;
}
