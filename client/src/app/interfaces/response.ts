import { Message } from "./message";

export interface Response extends Message {
    to: string; // the reciever of this response
    replyTo: string; // the id of the request the response is generated for
    confirmResponse: boolean; // whether or not client needs to confirm receipt of response
    isGood: boolean // whether or not this is a good or bad response
}
