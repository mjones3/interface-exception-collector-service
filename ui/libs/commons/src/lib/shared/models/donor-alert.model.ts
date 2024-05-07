export interface DonorAlertNotification {
  typeKey?: string;
  statusKey?: string;
  count?: number;
}

export interface DonorAlert {
  donorId: number;
  notifications?: DonorAlertNotification[];
}
