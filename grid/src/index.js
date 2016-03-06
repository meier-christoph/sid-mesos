import 'babel-polyfill';
import 'whatwg-fetch';
import ReactDOM from 'react-dom';
import app      from './app';

function startApp() {
    ReactDOM.render( app, document.getElementById( 'app' ) );
}

if ( !global.Intl ) {
    // Webpack parses the inside of require.ensure at build time to know that intl
    // should be bundled separately. You could get the same effect by passing
    // ['intl'] as the first argument.
    require.ensure( [], () => {
        // Ensure only makes sure the module has been downloaded and parsed.
        // Now we actually need to run it to install the polyfill.
        require( 'intl' );
        require( 'intl/locale-data/jsonp/en.js' );
        startApp();
    } );
} else {
    startApp();
}
