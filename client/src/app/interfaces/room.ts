import { Game } from "./game";
import { User } from "./user";

export interface Room {
    id: string;
    owner: User;
    "max-rounds": number;
    "max-time": number;
    users: User[];
    game: Game;
}