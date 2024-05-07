import { HgbsKitReagentsDto } from './hgbs-kits-reagents.dto';

export interface HgbsKitDto {
  id?: number;
  kitNameKey?: string;
  kitReagents?: Array<HgbsKitReagentsDto>;
}
