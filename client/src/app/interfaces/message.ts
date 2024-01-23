export interface Message {
    id: string; // id of the post
    type: string; // type of message
    timestamp: number; // time message was sent
    payload: { // extra stuff
        [s: string]: any
    };
}