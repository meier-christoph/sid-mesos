import CopyWebpackPlugin     from 'copy-webpack-plugin';
import WebpackNotifierPlugin from 'webpack-notifier';
import HtmlWebpackPlugin     from 'html-webpack-plugin';
import util                  from 'util';
import webpack               from 'webpack';
import merge                 from 'webpack-merge';
import pkg                   from '../package.json';
import { PATHS,
    CONFIG,
    CHUNKS,
    SASS_MODULE_NAME,
    STATIC_COPY }            from './base';

const START_CONFIG = {
    devtool:   'eval-source-map',
    devServer: {
        host:               pkg.config.devHost,
        port:               pkg.config.devPort,
        reload:             util.format( 'http://%s:%d', pkg.config.devHost, pkg.config.devPort ),
        noInfo:             true,
        historyApiFallback: true,
        hot:                true,
        inline:             true,
        progress:           true,
        // Display only errors to reduce the amount of output.
        stats:              'errors-only'
    },
    resolve:   {
        root: [ PATHS.src ]
    },
    module:    {
        preLoaders: [
            {
                test:    /\.jsx?$/,
                loaders: [ 'eslint' ],
                include: PATHS.src
            }
        ],
        loaders:    [
            {
                test:    /\.s?css$/,
                loaders: [ 'style', `css?${SASS_MODULE_NAME.dev}`, 'resolve-url', 'sass?sourceMap' ]
            }
        ]
    },
    plugins:   [
        new CopyWebpackPlugin( STATIC_COPY ),
        new WebpackNotifierPlugin( { title: pkg.name } ),
        new HtmlWebpackPlugin( {
            appMountId: 'app',
            mobile:     true,
            template:   PATHS.src + '/index.html',
            title:      pkg.title
        } ),
        new webpack.HotModuleReplacementPlugin(),
        new webpack.optimize.CommonsChunkPlugin( { names: CHUNKS } )
    ]
};

export default merge( CONFIG, START_CONFIG );
