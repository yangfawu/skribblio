interface Format {
    [type: string]: {
        [key: string]: any
    }
}
export interface RequestPayloads extends Format {
    "got-response": {
        "response-id": string;
    }
    bye: {}
    ping: {}
    greet: {
        uid: string;
        name: string;
        color: number;
        eyes: number;
        mouth: number;
    }
    "create-room": {};
    "join-room": {
        "room-id": string;
    }
    "update-room": {
        "room-id": string;
        "max-rounds": number;
        "max-time": number;
    }
    "start-game": {
        "room-id": string;
    }
    "send-message": {
        "room-id": string;
        message: string;
    }
    "update-drawing": {
        "room-id": string;
        data: string;
    }
    "select-word": {
        "room-id": string;
        choice: number;
    }
    "join-random-room": {};
    [notDefined: string]: {}
}
