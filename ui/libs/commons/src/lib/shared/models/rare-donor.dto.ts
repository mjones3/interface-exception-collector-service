export interface RareDonorDto {
  id: number;
  donorId: number;
  registryName: string;
  createDate: string;
}

export interface RareDonorWriteDto {
  donorId: number;
  rareRegistryProperties: string[];
}
