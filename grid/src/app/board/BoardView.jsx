/* @flow */

import React, { Component } from 'react';
import ReactDOM             from 'react-dom';
import { FluxComponent }    from 'airflux';
import * as Actions         from './BoardViewActions';
import Store                from './BoardViewStore';
import Board                from './components/Board';

import styles               from './BoardView.scss';


export type BoardViewProps = ReactBaseProps & {};
export type BoardViewState = {
    size: number,
    data: Array< Object >
};

@FluxComponent
export default class BoardView extends Component< void, BoardViewProps, BoardViewState > {


    constructor( props:BoardViewProps ) {
        super( props );
        this.connectStore( Store, 'data' );
    }

    _timerId;

    state:BoardViewState = {
        size: 400
    };

    _resize = () => {
        var sizes = ReactDOM.findDOMNode( this ).getBoundingClientRect();
        console.log( sizes );
    };

    componentDidMount():void {
        this._timerId = setInterval( Actions.load, 1000 );
    }

    componentWillUnmount():void {
        clearInterval( this._timerId );
    }

    render():?ReactElement {
        const { size } = this.state;
        return (
            <div className={ styles.root }>
                <Board width={ size } height={ size } items={ this.state.data }/>
            </div>
        );
    }
}
