/**
 * Interface for the 'Auth' data
 */
import Keycloak from 'keycloak-js';

export interface AuthEntity {
  id?: string;
  user?: Keycloak.KeycloakProfile;
  accessToken?: string;

  [key: string]: any;
}
