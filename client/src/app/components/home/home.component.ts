import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { LocalStorageService } from 'ngx-webstorage';
import { filter, take } from 'rxjs/operators';
import { AppState } from 'src/app/enums/app-state.enum';
import { AppService } from 'src/app/services/app.service';
import { AvatarComponent } from '../avatar/avatar.component';

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

    @ViewChild("main") avatar: AvatarComponent;
    @ViewChild("name") name: ElementRef;
    disabled = false;
    avatarName = "";

    constructor(
        private activatedRoute: ActivatedRoute,
        private appSvc: AppService,
        private localStorageSvc: LocalStorageService
    ) { }

    ngOnInit(): void {
        this.appSvc.buttonsLocked$.subscribe(state => this.disabled = state);
        this.appSvc.roomId$.subscribe(roomId => {
            if (roomId) this.appSvc.goTo(AppState.ROOM);
        });
        const storagenName = this.localStorageSvc.retrieve("name");
        this.avatarName = `${storagenName ? storagenName : ""}`.trim()
    }

    start(): void {
        const query = this.activatedRoute.snapshot.queryParams;
        const roomId = query.room ? `${query.room}`.trim() : null;
        const name = (this.name.nativeElement as HTMLInputElement).value;
        if (typeof roomId != "string") return;
        this.appSvc.joinRoom(name, this.avatar, roomId);
    }

    create(): void {
        const name = (this.name.nativeElement as HTMLInputElement).value;
        this.appSvc.createRoom(name, this.avatar);
    }

    saveName(): void {
        const val = `${(this.name.nativeElement as HTMLInputElement).value}`.trim();
        this.localStorageSvc.store("name", val);
    }

}
