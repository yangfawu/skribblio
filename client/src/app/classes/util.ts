export class Util {

    static randEx(low: number, high: number): number {
        return Math.floor(Math.random()*(high-low) + low);
    }

    static randExHigh(high: number): number {
        return Util.randEx(0, high);
    }

}