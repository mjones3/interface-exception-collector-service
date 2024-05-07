import { QcReagentDTO } from './qc-reagent';

export interface HgbsKitReagentsDto {
  id?: number;
  kitId: number;
  reagent: QcReagentDTO;
}
