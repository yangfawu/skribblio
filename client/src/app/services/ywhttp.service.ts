import { Injectable } from '@angular/core';
import { AngularFireAuth } from '@angular/fire/auth';
import { AngularFireDatabase } from '@angular/fire/database';
import { BehaviorSubject, Observable, Subscription } from 'rxjs';
import { concatAll, filter, map, take } from "rxjs/operators";
import { Request } from '../interfaces/request';
import { RequestPayloads } from '../interfaces/request-payloads';
import { Response } from '../interfaces/response';
import { LoggerService } from './logger.service';
import firebase from 'firebase/app';
import "firebase/auth";

@Injectable({
    providedIn: 'root'
})
export class YwhttpService {

    private user = new BehaviorSubject<firebase.User>(null);

    constructor(
        private fbAuth: AngularFireAuth,
        private fbDb: AngularFireDatabase,
        private loggerSvc: LoggerService
    ) {
        this.fbAuth.setPersistence(firebase.auth.Auth.Persistence.SESSION)
            .then(() => this.fbAuth.authState.subscribe(
                user => {
                    this.user.next(user);
                    if (user) this.loggerSvc.write(`Signed in anonymously as ${user.uid}.`, user);
                    else this.fbAuth.signInAnonymously();
                }
            ))
            .catch(err => this.loggerSvc.error(err));
    }

    /**
     * Generates an observable of the client id.
     * @returns an observable of a DEFINED client id
     */
    get uid$(): Observable<string> {
        return this.user
            .pipe(
                filter(user => new Boolean(user).valueOf()),
                map(user => user.uid)
            );
    }

    // /**
    //  * Generates an observable of the client path to use for communicating with the server
    //  * @returns an observable of a DEFINED client path
    //  */
    // get path$(): Observable<string> {
    //     return this.uid$
    //         .pipe(
    //             map(uid => `/ws/${uid}`)
    //         );
    // }

    /**
     * Generates a promise that resolves when a provided asynchronous function executes after a DEFINED uid is detected.
     * If a timeout is provided, and the promise hasn't resolved after the timeout, it will be rejected. 
     * @param instructions an asynchronous function to execute on detection of DEFINED uid
     * @param timeout amount of time in MS before the provided instructions wil be dumped
     * @returns a promise that resolves on instructions execution OR rejects on timeout
     */
    run(instructions: (uid: string) => Promise<void>, timeout?: number): Promise<void> {
        let resolve: () => void,
            reject: () => void;
        const latch = new Promise<void>((pRes, pRej) => {
            resolve = pRes;
            reject = pRej;
        });
        let bomb: number | undefined = typeof timeout !== "undefined" ?
            window.setTimeout(() => reject(), timeout) :
            undefined;
        const sub = this.uid$.subscribe(
            uid => {
                instructions(uid).finally(() => {
                    if (typeof timeout !== "undefined") window.clearTimeout(bomb);
                    resolve();
                    sub.unsubscribe();
                });
            }
        );
        return latch.catch(() => {
            sub.unsubscribe();
            throw new Error();
        });
    }

    /**
     * Sends a request to the server with a customizable payload
     * @param type the type of request to make to the server
     * @param payload an object payload to send along with your request
     * @param responseHandler a handler that deals with the server response to your request (adding this means that you expect a response from the server)
     * @param timeout the amount of time given for the completion each of these two tasks: generating a request, getting a response if needed 
     * @returns a promise that resolves if the request is sent and a response is handled in time OR rejects if an error happens along the way
     */
    request<K extends keyof RequestPayloads>(type: K & keyof RequestPayloads, payload: RequestPayloads[K], responseHandler?: (res: Response) => Promise<void>, timeout?: number): Promise<void> {
        let resolve: () => void,
            reject: (reason: any) => void;
        const latch = new Promise<void>((pRes, pRej) => {
            resolve = pRes;
            reject = pRej;
        });
        this.run(async (uid) => {
            // generate reference of request
            let ref: firebase.database.Reference;
            try {
                ref = await this.fbDb.list<Request[]>(`/ws/toServer`).push(null);
            } catch {
                return reject("Failed to GENERATE request.");
            }
            
            // determine whether or not a response is desired
            const expectResponse = new Boolean(responseHandler).valueOf();

            // form request
            const req: Request = {
                id: ref.key,
                from: uid,
                type: `${type}`.trim().toLowerCase(),
                wantResponse: expectResponse,
                payload: payload,
                timestamp: new Date().getTime()
            };

            // send request
            try {
                await this.fbDb.object<Request>(ref)
                    .set(req);
            } catch {
                return reject("Failed to SEND request.");
            }

            // if no response is needed, resolve
            if (!expectResponse) return resolve();

            // create a bomb if a timeout is provided
            let bomb: number | undefined = undefined;

            // create a subscription to listen for response
            const sub: Subscription = this.fbDb.list<Response>(
                    `/ws/toClient`, 
                    ref => ref.orderByChild("replyTo").equalTo(req.id)
                )
                .valueChanges()
                .pipe(
                    concatAll(),
                    take(1)
                )
                .subscribe(async res => {
                    if (typeof timeout !== "undefined") window.clearTimeout(bomb);
                    try {
                        if (res.confirmResponse)
                            this.request("got-response", { "response-id": res.id });
                        await responseHandler(res);
                        resolve();
                    } catch (err) {
                        reject(`Error occurred while handling response: ${typeof err !== "undefined" ? err : "UNDEFINED-ERROR"}`);
                    }
                    return sub.unsubscribe();
                });
            
            // light up the bomb
            if (typeof timeout !== "undefined")
                bomb = window.setTimeout(() => {
                    reject(`Server could not respond in ${timeout}ms.`);
                    sub.unsubscribe();
                }, timeout);
        }, timeout).catch(() => reject(`Client could not start generating request in ${timeout}ms.`));
        return latch;
    }

}
