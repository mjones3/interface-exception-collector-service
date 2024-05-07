export interface ControlResultInterpretationRequestDTO {
  reagentId: number;
  manufacturerId: number;
  controlType: string;
  initialRead?: string;
  finalRead?: string;
  checkCell?: string;
}

export interface ControlResultInterpretationResponseDTO {
  qcInterpretation: string;
  status: string;
  messageKey: string;
}

export interface HgbsControlResultInterpretationRequestDTO {
  kitId: number;
  read: string;
  controlType: string;
}

export interface HgbsControlResultInterpretationResponseDTO {
  qcInterpretation: string;
  status: string;
  messageKey: string;
}
