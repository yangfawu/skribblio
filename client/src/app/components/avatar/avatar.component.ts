import { Component, Input, OnInit, Output } from '@angular/core';
import { LocalStorageService } from 'ngx-webstorage';
import { filter } from 'rxjs/operators';
import { Util } from 'src/app/classes/util';
import { Avatar } from 'src/app/interfaces/avatar';

@Component({
    selector: 'app-avatar',
    templateUrl: './avatar.component.html',
    styleUrls: ['./avatar.component.scss']
})
export class AvatarComponent implements Avatar, OnInit {

    @Input() size = 48;
    @Input() isMain = false;

    static MAX: Avatar = {
        color: 18,
        eyes: 31,
        mouth: 24
    };
    static floor = Math.floor;

    @Input() color: number;
    @Input() eyes: number;
    @Input() mouth: number;
    @Input() crown = false;

    constructor(
        private localStorageSvc: LocalStorageService
    ) { }

    ngOnInit() {
        this.loop(key => {
            if (typeof this[key] !== "number")
                this[key] = Util.randExHigh(AvatarComponent.MAX[key]);
        });
        if (!this.isMain) return;
        
        this.loop(key => {
            const val = this.localStorageSvc.retrieve(key);
            if (typeof val === "number")
                this.change(key, val);
            this.localStorageSvc.observe(key)
                .pipe(filter(val1 => typeof val1 === "number"))
                .subscribe(val1 => this.change(key, val1));
        });
    }

    /**
     * Loops through all the keys of an Avatar instance and applies an outside function to those keys
     * @param func a function to execute with a key of an Avatar instance 
     */
    private loop(func: (s: keyof Avatar) => any): void {
        const keys: (keyof Avatar)[] = ["color", "eyes", "mouth"];
        for (const key of keys)
            func(key);
    }

    /**
     * Changes a specific part of the avatar and saves chanegs to local storage if this is the main avatar
     * @param target the part of the avatar to change (needs to be a key of an Avatar instance)
     * @param index the new index or offset to be applied as the change at the specified part of the avatar
     * @param local whether or not the index parameter should be treated as an offset or not
     */
    change(target: keyof Avatar, index: number = this[target], local = false): void {
        const offset = AvatarComponent.MAX[target];
        if (local) {
            this[target]+= index;
            this[target]%= offset;
        } else this[target] = index % offset;
        if (this[target] < 0) this[target]+= offset;

        if (!this.isMain) return;
        this.localStorageSvc.store(target, this[target]);
    }

    /**
     * Randomizes the avatar layout.
     */
    randomize(): void {
        this.loop(key => this.change(key, Util.randExHigh(AvatarComponent.MAX[key])));
    }

    /**
     * Parses a provided index into a CSS styling rule
     * @param index the corresponding index of the sprite image of a specific avatar part
     * @returns the styling value that makes the right sprite image show up
     */
    calc(index: number): string {
        const row = Math.floor(index / 10) * -this.size;
        const col = (index % 10) * -this.size;
        return `${col}px ${row}px`;
    }

    /**
     * A helper method to change different parts of the avatar by a specified increment or decrement
     * @param target the part of the avatar to change
     * @param offset the offset that the part of the avatar will change by
     */
    private swap(target: keyof Avatar, offset: number): void {
        this.change(target, offset, true);
    }

    /**
     * Switches to the index corresponding to the next costume part of the avatar
     * @param target the part of the avatar to change
     */
    next(target: keyof Avatar): void {
        this.swap(target, 1);
    }

    /**
     * Switches to the index corresponding to the previous costume part of the avatar
     * @param target the part of the avatar to change
     */
    back(target: keyof Avatar): void {
        this.swap(target, -1);
    }

}
