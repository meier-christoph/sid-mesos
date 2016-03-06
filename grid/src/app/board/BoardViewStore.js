/* @flow */

import { Store }    from 'airflux';
import _            from 'lodash';

import * as Actions from './BoardViewActions';

export type BoardControllerStoreState = {
    items: Array< Object >
}

class BoardControllerStore extends Store {

    state:Array< Object > = [];

    constructor() {
        super();
        this.listenTo( Actions.load, this._load );
    }

    _load():void {
        fetch( 'http://localhost:8080/zombies' )
            .then( res => {
                return res.json();
            } )
            .then( this._process )
    }

    _process = ( json:Object ) => {
        this.state = _.map( _.groupBy( json.results, 'coord' ), items => {
            const result = items.shift();
            result.history = items;
            return result;
        } );
        this.publishState();
    }

}

export default new BoardControllerStore();
