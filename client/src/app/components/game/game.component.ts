import { AfterViewInit, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { BehaviorSubject, fromEvent } from 'rxjs';
import { pairwise, switchMap, takeUntil, tap } from 'rxjs/operators';
import { AppState } from 'src/app/enums/app-state.enum';
import { GameMessage } from 'src/app/interfaces/game-message';
import { Player } from 'src/app/interfaces/player';
import { AppService } from 'src/app/services/app.service';
import { LoggerService } from 'src/app/services/logger.service';
import { YwhttpService } from 'src/app/services/ywhttp.service';

@Component({
    selector: 'app-game',
    templateUrl: './game.component.html',
    styleUrls: ['./game.component.scss']
})
export class GameComponent implements OnInit, AfterViewInit {

    pallete: string[] = ["#FFF", "#C1C1C1", "#EF130B", "#FF7100", "#FFE400", "#00CC00", "#00B2FF", "#231FD3", "#A300BA", "#D37CAA", "#A0522D", "#000", "#4C4C4C", "#740B07", "#C23800", "#E8A200", "#005510", "#00569E", "#0E0865", "#550069", "#A75574", "#63300D"];
    widths: number[] = [6, 16, 30, 44];

    myUid: string = "";
    drawer: Player = { points: 0, user: { name: "", uid: "", avatar: { color: 0, eyes: 0, mouth: 0 } } };
    maxRounds: number = 0;
    picture: string = "";
    players: Player[] = [];
    passers: string = "";
    round: number = 0;
    time: number = 0;
    word: string = "";
    announcement: string = "";
    messages: GameMessage[] = [];
    options: string[] = [];
    secret: string = "";

    @ViewChild("chatbox") chatbox: ElementRef;
    @ViewChild("drawerCanvas") drawerCanvas: ElementRef;
    @ViewChild("outputCanvas") outputCanvas: ElementRef;

    canvasWidth = 800;
    canvasHeight = 600;

    private drawerCtx: CanvasRenderingContext2D;
    drawerColor$ = new BehaviorSubject<string>("#000000");
    private drawerWidth$ = new BehaviorSubject<number>(3);

    private outputCtx: CanvasRenderingContext2D;

    drawerListener: number;

    constructor(
        private appSvc: AppService,
        private ywhttpSvc: YwhttpService,
        private loggerSvc: LoggerService
    ) { }

    ngOnInit(): void {
        this.ywhttpSvc.uid$.subscribe(uid => this.myUid = uid);
        const gameSub = this.appSvc.game$.subscribe(game => {
            if (!game) {
                this.appSvc.goTo(AppState.HOME);
                return gameSub.unsubscribe();
            }

            this.loggerSvc.write(game);
        
            this.drawer = game.drawer;
            this.maxRounds = game["max-rounds"];
            this.picture = game.picture;
            this.players = game.players || [];
            this.round = game.round;
            this.time = game.time;
            this.word = game.word;
            this.passers = game.passers;
            this.messages = game.messages || [];
            this.announcement = game.announcement;
            this.options = game.options || [];
            this.secret = game.secret;
;
            if (this.drawer.user.uid == this.myUid) {
                if (typeof this.drawerListener != "number") {
                    let previous = "";
                    this.clear();
                    this.drawerListener = window.setInterval(() => {
                        const newData = (this.drawerCanvas.nativeElement as HTMLCanvasElement).toDataURL();
                        if (newData == previous) return;
                        previous = newData;
                        this.appSvc.sendDrawing(newData);
                    }, 250);
                }
            } else {
                if (typeof this.drawerListener == "number") {
                    window.clearInterval(this.drawerListener);
                    this.drawerListener = null;
                }
                let img = new Image();
                img.addEventListener("load", () => {
                    this.outputCtx.drawImage(img, 0, 0);
                })
                img.src = this.picture;
            }
        });
    }

    ngAfterViewInit(): void {
        const outputCanvas = this.outputCanvas.nativeElement as HTMLCanvasElement;
        this.outputCtx = outputCanvas.getContext("2d");

        outputCanvas.width = this.canvasWidth;
        outputCanvas.height = this.canvasHeight;
        
        const drawerCanvas = this.drawerCanvas.nativeElement as HTMLCanvasElement;
        this.drawerCtx = drawerCanvas.getContext("2d");

        drawerCanvas.width = this.canvasWidth;
        drawerCanvas.height = this.canvasHeight;

        this.drawerWidth$.subscribe(width => this.drawerCtx.lineWidth = width);
        this.drawerCtx.lineCap = "round";

        this.drawerColor$.subscribe(color => this.drawerCtx.strokeStyle = color);

        this.listenForDrawing(drawerCanvas);
    }

    async submitMessage(msgInput: HTMLInputElement) {
        const value = `${(msgInput.value || "")}`.trim();
        msgInput.value = null;
        if (value.length < 1) return;

        await this.appSvc.sendMessage(value);

        const ele = this.chatbox.nativeElement as HTMLDivElement;
        ele.scrollTop = ele.scrollHeight;
    }

    private listenForDrawing(ele: HTMLCanvasElement): { unsubscribe: () => void } {
        const subscriber = ([event1, event2]: [MouseEvent, MouseEvent]) => {
            const rect = ele.getBoundingClientRect();
            const calcX = (event: MouseEvent) => (event.pageX - rect.left - scrollX)/rect.width*this.canvasWidth;
            const calcY = (event: MouseEvent) => (event.pageY - rect.top - scrollY)/rect.height*this.canvasHeight;
            const calc = (event: MouseEvent) => ({ x: calcX(event), y: calcY(event) });
            this.draw(calc(event1), calc(event2));
        };
        const mouseSub = fromEvent(ele, "mousedown")
            .pipe(
                switchMap(() => fromEvent(ele, "mousemove")
                    .pipe(
                        takeUntil(fromEvent(ele, "mouseup")),
                        takeUntil(fromEvent(ele, "mouseleave")),
                        pairwise()
                    )
                )
            )
            .subscribe(subscriber);
        return {
            unsubscribe() {
                mouseSub.unsubscribe();
                // touchSub.unsubscribe();
            }
        };
    }
    
    private draw(
        prevPos: { x: number, y: number }, 
        currentPos: { x: number, y: number }
    ) {
        if (!this.drawerCtx) return;

        this.drawerCtx.beginPath();
        if (prevPos) {
            this.drawerCtx.moveTo(prevPos.x, prevPos.y);
            this.drawerCtx.lineTo(currentPos.x, currentPos.y);
            this.drawerCtx.stroke();
        }
    }

    clear() {
        this.drawerCtx.fillStyle = "#fff";
        this.drawerCtx.fillRect(0, 0, this.canvasWidth, this.canvasHeight);
        this.drawerCtx.fillStyle = this.drawerColor$.value;
    }

    setColor(color: string) {
        this.drawerColor$.next(color);
    }

    setWidth(size: number) {
        this.drawerWidth$.next(size);
    }

    selectWord(choice: number) {
        this.appSvc.selectWord(choice);
    }

}
