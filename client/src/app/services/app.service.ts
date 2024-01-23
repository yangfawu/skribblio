import { Injectable } from '@angular/core';
import { AngularFireDatabase } from '@angular/fire/database';
import { BehaviorSubject, Observable } from 'rxjs';
import { concatMap, map, take } from 'rxjs/operators';
import { AppState } from '../enums/app-state.enum';
import { Avatar } from '../interfaces/avatar';
import { Game } from '../interfaces/game';
import { Room } from '../interfaces/room';
import { LoggerService } from './logger.service';
import { YwhttpService } from './ywhttp.service';

@Injectable({
    providedIn: 'root'
})
export class AppService {

    view$ = new BehaviorSubject<AppState>(AppState.HOME);
    buttonsLocked$ = new BehaviorSubject<boolean>(false);
    roomId$ = new BehaviorSubject<string>(null);

    constructor(
        private ywhttpSvc: YwhttpService,
        private fbDb: AngularFireDatabase,
        private loggerSvc: LoggerService
    ) { }

    goTo(target: AppState) {
        this.view$.next(target);
    }

    setButtons(state: boolean) {
        this.buttonsLocked$.next(state);
    }

    createRoom(name: string, avatar: Avatar) {
        if (this.buttonsLocked$.value) return;
        this.setButtons(true);
        this.goTo(AppState.LOADING);

        const defaultMsg = "Error occurred while trying to create a private room.";
        const endBad = (msg = defaultMsg) => {
            this.setButtons(false);
            this.goTo(AppState.HOME);
            window.alert(msg);
        }

        this.ywhttpSvc.run(async uid => {
            this.ywhttpSvc.request("greet", {
                uid: uid,
                name: `${name || "skribblio lover"}`.trim(),
                color: avatar.color,
                eyes: avatar.eyes,
                mouth: avatar.mouth
            }, async res => {
                if (!res.isGood)
                    return endBad(res.payload.reason || defaultMsg);
                this.ywhttpSvc.request("create-room", {}, async res2 => {
                    if (!res2.isGood)
                        return endBad(res2.payload.reason || defaultMsg);
                    
                    // user is inside the room
                    const roomId = res2.payload["room-id"];
                    this.roomId$.next(roomId);

                    this.setButtons(false);
                }, 500).catch(() => endBad());
            }, 500).catch(() => endBad());
        }).catch(() => endBad());
    }

    joinRoom(name: string, avatar: Avatar, roomId: string) {  
        if (this.buttonsLocked$.value) return;
        this.setButtons(true);
        this.goTo(AppState.LOADING);

        const defaultMsg = "Error occurred while trying to join room.";
        const endBad = (msg = defaultMsg) => {
            this.setButtons(false);
            this.goTo(AppState.HOME);
            window.alert(msg);
        }

        this.ywhttpSvc.run(async uid => {
            this.ywhttpSvc.request("greet", {
                uid: uid,
                name: `${name || "skribblio lover"}`.trim(),
                color: avatar.color,
                eyes: avatar.eyes,
                mouth: avatar.mouth
            }, async res => {
                if (!res.isGood)
                    return endBad(res.payload.reason || defaultMsg);
                if (typeof roomId == "string")
                    this.ywhttpSvc.request("join-room", {
                        "room-id": roomId
                    }, async res2 => {
                        if (!res2.isGood)
                            return endBad(res2.payload.reason || defaultMsg);

                        this.roomId$.next(roomId);

                        this.setButtons(false);
                    }, 500).catch(() => endBad());
                else
                    this.ywhttpSvc.request("join-random-room", {}, async res2 => {
                        if (!res2.isGood)
                            return endBad(res2.payload.reason || defaultMsg);

                        const roomId = res2.payload["room-id"];
                        this.roomId$.next(roomId);

                        this.setButtons(false);
                    }, 500).catch(() => endBad());
            }, 500).catch(() => endBad());
        }).catch(() => endBad());
    }

    get room$(): Observable<Room> {
        return this.roomId$.pipe(
            concatMap(roomId => roomId ? this.fbDb.object<Room>("/rooms/" + roomId).valueChanges() : null)
        );
    }

    async updateRoom(newRound: number, newTime: number) {
        const roomdId = await this.roomId$.pipe(
            take(1)
        ).toPromise();
        if (!roomdId) return;

        await this.ywhttpSvc.request("update-room", {
            "room-id": roomdId,
            "max-rounds": newRound,
            "max-time": newTime
        }, async () => { }, 500).catch(() => {
            this.loggerSvc.error("Cannot update room settings.");
        });
    }

    async startGame() {
        const roomdId = await this.roomId$.pipe(
            take(1)
        ).toPromise();
        if (!roomdId) return;

        await this.ywhttpSvc.request("start-game", {
            "room-id": roomdId
        }, async () => {}, 500).catch(() => {
            this.loggerSvc.error("Cannot start game.");
        });
    }

    get game$(): Observable<Game> {
        return this.room$.pipe(
            map(room => room?.game)
        );
    }

    async sendMessage(data: string) {
        const room = await this.room$.pipe(
            take(1)
        ).toPromise();
        if (!room || !room.game) return;

        await this.ywhttpSvc.request("send-message", {
            "room-id": room.id,
            message: `${data}`.trim()
        }, async () => { }, 500).catch(() => {
            this.loggerSvc.error("Cannot send message.");
        });
    }

    async sendDrawing(data: string) {
        const room = await this.room$.pipe(
            take(1)
        ).toPromise();
        if (!room || !room.game) return;

        await this.ywhttpSvc.request("update-drawing", {
            "room-id": room.id,
            data: data
        }, async () => { }, 500).catch(() => {
            this.loggerSvc.error("Cannot send drawing.");
        })
    }

    async selectWord(choice: number) {
        const room = await this.room$.pipe(
            take(1)
        ).toPromise();
        if (!room || !room.game) return;

        await this.ywhttpSvc.request("select-word", {
            "room-id": room.id,
            choice: choice
        }, async () => { }, 500).catch(() => {
            this.loggerSvc.error("Cannot choose word.");
        })
    }

}
