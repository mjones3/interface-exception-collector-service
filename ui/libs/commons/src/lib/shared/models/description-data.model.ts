import {DonationDto, DrawDto, InventoryResponseDto} from '../models/index';

export interface DescriptionData {
  inventory: InventoryResponseDto;
  draw: DrawDto;
  donation: DonationDto;
  donor: any;
}
