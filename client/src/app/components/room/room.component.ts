import { Location } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { AppState } from 'src/app/enums/app-state.enum';
import { User } from 'src/app/interfaces/user';
import { AppService } from 'src/app/services/app.service';
import { YwhttpService } from 'src/app/services/ywhttp.service';

@Component({
    selector: 'app-room',
    templateUrl: './room.component.html',
    styleUrls: ['./room.component.scss']
})
export class RoomComponent implements OnInit {

    myUid: string = "";
    url: string = "";
    owner: User = { name: "", uid: "", avatar: { color: 0, eyes: 0, mouth: 0 } };
    users: User[] = [];
    round: number = 2;
    time: number = 30;

    constructor(
        private appSvc: AppService,
        private ywhttpSvc: YwhttpService,
        private location: Location
    ) { }

    ngOnInit(): void {
        this.ywhttpSvc.uid$.subscribe(uid => this.myUid = uid);
        const roomSub = this.appSvc.room$.subscribe(
            room => {
                if (!room) {
                    this.appSvc.goTo(AppState.HOME);
                    this.appSvc.roomId$.next(null);
                    return roomSub.unsubscribe();
                }  
                
                this.url = `${window.origin + window.location.pathname}?room=${room.id}`;
                this.location.replaceState(`/?room=${room.id}`);
                this.owner = room.owner;
                this.users = room.users || [];
                this.round = room["max-rounds"];
                this.time = room["max-time"];
            }
        );
        const gameSub = this.appSvc.game$.subscribe(game => {
            if (game) {
                this.appSvc.goTo(AppState.GAME);
                return gameSub.unsubscribe();
            }
        })
    }

    copyUrl(input: HTMLInputElement) {
        input.select();
        document.execCommand("copy"); 
    }

    updateSettings(roundEle: HTMLSelectElement, timeEle: HTMLSelectElement) {
        let round = parseInt(roundEle.value);
        if (isNaN(round))
            return window.alert("An invalid round value detected.");

        let time = parseInt(timeEle.value);
        if (isNaN(time))
            return window.alert("An invalid time value detected.");

        this.appSvc.updateRoom(round, time);
    }

    startGame() {
        this.appSvc.startGame();
    }

}
