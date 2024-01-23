import { Component, OnInit } from '@angular/core';
import { Util } from 'src/app/classes/util';
import { AppState } from 'src/app/enums/app-state.enum';
import { AppService } from 'src/app/services/app.service';
import { YwhttpService } from 'src/app/services/ywhttp.service';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

    state: AppState = AppState.HOME;
    army = this.randomArmy(8);

    constructor(
        private ywhttpSvc: YwhttpService,
        private appSvc: AppService
    ) { }

    ngOnInit() {
        this.ywhttpSvc.request("ping", {}, async () => {}, 1000)
            .catch(() => window.alert("Error occurred while pinging the server. Plase close/refresh tab."));
        this.appSvc.view$.subscribe(view => this.state = view);
        window.addEventListener("beforeunload", () => {
            this.ywhttpSvc.request("bye", {});
        });
    }

    /**
     * Generates an boolean array where one element is guaranteed to be true while the rest are false.
     * This is used as a helper method to generate the avatar gallery.
     * @param size the number of avatars to account for
     * @returns a boolean array where all elements are false except one
     */
    private randomArmy(size: number): boolean[] {
        const arr = new Array<boolean>(size).fill(false);
        const king = Util.randExHigh(size);
        arr[king] = true;
        return arr;
    }

    private triggered = false;
    runSecret() {
        if (this.triggered) return;
        this.triggered = true;

        this.ywhttpSvc.request("cheese", {}, async (res) => {
            const cheese = res.payload.stdout;
            window.alert(cheese || "Cheese!");
        }, 500).catch(() => {
            window.alert("Oops, you touched something special but it wasn't working.");
        })
    }

}
