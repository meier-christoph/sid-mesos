/* @flow */
// Modules
import React                 from 'react';
import {
    Router,
    Route,
    Redirect,
    useRouterHistory }       from 'react-router';
import { createHashHistory } from 'history';
import AppView               from 'app/AppView';
import BoardView             from 'app/board/BoardView';

import 'app.scss';

// Needed for React Developer Tools
window.React = React;

const appHistory = useRouterHistory( createHashHistory )( { queryKey: false } );


export default (
    <Router history={ appHistory }>
        <Route component={ AppView }>
            <Route path="/" component={ BoardView }/>
        </Route>
    </Router>
);
