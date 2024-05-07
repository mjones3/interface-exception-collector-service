export interface NoteDto {
  id?: number;
  typeKey: string;
  referenceId: string;
  employeeId?: string;
  comments: string;
  createDate?: Date;
  deleteDate?: Date;
  screenName?: string;
}

export enum NoteType {
  DONOR = 'donor',
  DONOR_CONFIDENTIAL = 'donor_confidential',
  DONATION = 'donation',
  PHYSICAL_EXAM = 'physical_exam',
  DRAW_PREPARATION = 'draw_preparation',
  DRAW_SUMMARY = 'draw_summary',
  COUNSELING_CONFIDENTIAL = 'counseling-confidential.label',
  ANTIGEN_RESULT = 'antigen-result-note.label',
  ANTIGEN_RESULT_ENTRY = 'antigen-result-entry.label',
  DONOR_GENERAL = 'DONOR_GENERAL',
}

export interface NoteDataSource {
  createDate: string;
  staff: string;
  confidential?: string;
  comments: string;
}
