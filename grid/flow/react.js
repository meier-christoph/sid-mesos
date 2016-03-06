declare type ReactNode = ( string | ReactElement | Array< ReactElement > );

declare type ValueLink< T > = {
    value           : T;
    requestChange   : ( x: T ) => void;
};

declare type ReactBaseProps = {
    children?: ReactElement | Array< ReactElement >;
    className?: string;
};
