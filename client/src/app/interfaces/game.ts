import { GameMessage } from "./game-message";
import { Player } from "./player"

export interface Game {
    drawer: Player;
    "max-rounds": number;
    picture: string;
    players: Player[];
    round: number;
    time: number;
    word: string;
    passers: string;
    announcement: string;
    options: string[];
    messages: GameMessage[];
    secret: string;
    isOver: boolean;
}