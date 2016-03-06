declare class ByteString {

};

declare class Headers {
    get( name: string ) : string;
};


declare class Response {
    type : string;
    url         : string;
    status      : number;
    ok          : boolean;
    statusText  : string;
    headers     : Headers;

    blob() : Promise< Blob >;
    json() : Promise< Object >;
    text() : Promise< string >;
};

declare function fetch( url: string, options: ?Object ) : Promise< Response >;
