import { Message } from "./message";

export interface Request extends Message {
    from: string; // the uid of the sender
    wantResponse: boolean; // whether or not client wants a response
}
