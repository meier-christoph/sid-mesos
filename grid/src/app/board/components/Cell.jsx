/* @flow */

import React, { Component } from 'react';

import styles               from './Cell.scss';

export type CellProps = ReactBaseProps & {
    x: number,
    y: number,
    width: number,
    height: number,
    qte: number,
    total: number
};
export type CellDefaultProps = {};
export type CellState = {};


export default class Cell extends Component< CellDefaultProps, CellProps, CellState > {

    static defaultProps:CellDefaultProps = {};

    constructor( props:CellProps ) {
        super( props );
    }

    state:CellState;

    render():?ReactElement {
        const { x, y, width, height, qte, total } = this.props;

        return (
            <rect x={ x } y={ y } width={ width } height={ height } fill="blue" fillOpacity={ total === 0 ? 0 : qte / total } stroke="black" strokeWidth={ 1 }>
                <text>{ qte }</text>
            </rect>
        );
    }
}
