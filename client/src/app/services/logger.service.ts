import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
    providedIn: 'root'
})
export class LoggerService {

    private readonly debug = !environment.production;

    constructor() {
        if (this.debug)
            console.log("LoggerService is in debug mode:", this.debug);
    }

    /**
     * Logs onto the console like console.log but only when debug mode is on.
     * @param data first part of message
     * @param moreData any additional parts of the message
     */
    write(data: any, ...moreData: any[]): void {
        if (!this.debug) return;
        console.log(data, ...moreData);
    }

    /**
     * Logs onto the console like console.table but only when debug mode is on.
     * @param data first part of message
     */
    table(data: any, ...moreData: any[]): void {
        if (!this.debug) return;
        console.table(data, ...moreData);
    }

    /**
     * Logs errors onto the console like console.error but only when debug mode is on.
     * @param data first part of message
     * @param moreData any additonal parts of the message
     */
    error(data: any, ...moreData: any[]): void {
        if (!this.debug) return;
        console.error(data, ...moreData);
    }

}
