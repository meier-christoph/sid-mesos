/* @flow */

import React, { Component, PropTypes } from 'react';
import classNames                      from 'classnames';

import styles                          from './AppView.scss';


type AppViewProps = ReactBaseProps & {};
type AppViewState = {};

export default class AppView extends Component< void, AppViewProps, AppViewState > {

    constructor( props:AppViewProps ) {
        super( props );
    }

    render():?ReactElement {
        return (
            <div className={ styles.root }>
                <div className={ styles.content }>
                    { this.props.children }
                </div>
            </div>
        );
    }
}
