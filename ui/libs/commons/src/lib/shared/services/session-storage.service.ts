import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SessionStorageService {

  constructor() { }

  setJsonSession(sessionName: string, jsonObject: any){
    sessionStorage.setItem(sessionName, JSON.stringify(jsonObject));
  }

  getJsonSession(sessionName: string): any{
    return JSON.parse(sessionStorage.getItem(sessionName));
  }

  replaceJsonSession(currentSessionName: string, previousSessionName: string, jsonObject:any){
    this.removeSession(previousSessionName);
    this.setJsonSession(currentSessionName, jsonObject)
  }

  addValueToSessionObject(sessionName: string, sessionObject: any){
    const session = JSON.parse(sessionStorage.getItem(sessionName));
    session.push(sessionObject);
    this.setJsonSession(sessionName, session);
  }

  updateJsonObject(itemName: string, key: string, value: any){
    const item = JSON.parse(sessionStorage.getItem(itemName));
    item[key] = value;
    this.setJsonSession(itemName, item);
  }

  getValueFromSessionObject(sessionName: string, key: string){
    const session = JSON.parse(sessionStorage.getItem(sessionName));
    return session ? session[key] : null;
  }

  removeSession(sessionName: string){
    sessionStorage.removeItem(sessionName);
  }
}
