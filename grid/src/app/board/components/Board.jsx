/* @flow */

import React, { Component } from 'react';
import _                    from 'lodash';
import Cell                 from './Cell';


import styles               from './Board.scss';


export type BoardViewDefaultProps = {
    width: number,
    height: number
};
export type BoardViewProps = ReactBaseProps & {
    width: number,
    height: number,
    items: Array< Object >,
    total: number
};
export type BoardViewState = {
    rowHeight: number
};

const grid = _.range( 10 ).map( () => _.range( 10 ) );
const cols = [ 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J' ];

export default class Board extends Component< BoardViewDefaultProps, BoardViewProps, BoardViewState > {

    static defaultProps:BoardViewDefaultProps = {
        width:  300,
        height: 300
    };

    constructor( props:BoardViewProps ) {
        super( props );
        this._updateParameters( props );
    }

    state:BoardViewState = {
        rowHeight: 0
    };

    _updateParameters( props:BoardViewProps ):void {
        this.state.total = _.sum( props.items.map( item => item.nb ) );
        this.state.rowHeight = props.width / grid.length;
    }

    componentWillReceiveProps( props:BoardViewProps ):void {
        this._updateParameters( props );
    }

    renderCell( rowNumber:number, nbCols:number ):Function {
        const { width, items } = this.props;
        const { rowHeight, total } = this.state;
        const rowWidth = width / nbCols;
        return ( dummy:number, colNumber:number ) => {
            const item = _.find( items, item => {
                return item.coord === `${cols[ colNumber ]}${rowNumber + 1}`;
            } );
            return (
                <Cell key={ `${rowNumber}-${colNumber}` }
                      x={ colNumber * rowWidth }
                      y={ rowNumber * rowHeight }
                      width={ rowWidth }
                      height={ rowHeight }
                      qte={ item ? item.nb : 0 }
                      total={ total }/>
            );
        };
    }

    renderRow( row:Array, rowNumber:number ):Array< ReactElement > {
        return row.map( this.renderCell( rowNumber, row.length ) );
    };

    render():?ReactElement {
        const { width, height } = this.props;
        return (
            <svg width={ width } height={ height } className={ styles.root }>
                {
                    grid.map( this.renderRow.bind( this ) )
                }
            </svg>
        );
    }
}
