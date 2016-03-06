import CopyWebpackPlugin from 'copy-webpack-plugin';
import ExtractTextPlugin from 'extract-text-webpack-plugin';
import HtmlWebpackPlugin from 'html-webpack-plugin';
import webpack           from 'webpack';
import merge             from 'webpack-merge';
import pkg               from '../package.json';
import { PATHS,
    CONFIG,
    CHUNKS,
    SASS_MODULE_NAME,
    STATIC_COPY }        from './base';

const BUILD_CONFIG = {
    output:  {
        path:          PATHS.build,
        filename:      '[name].[chunkhash].js',
        chunkFilename: '[chunkhash].js'
    },
    resolve: {
        root: [ PATHS.src ]
    },
    module:  {
        loaders: [
            {
                test:    /\.s?css$/,
                loader:  ExtractTextPlugin.extract( 'style', [ `css?${SASS_MODULE_NAME.build}`, 'resolve-url', 'sass' ] )
            }
        ]
    },
    plugins: [
        new CopyWebpackPlugin( STATIC_COPY ),
        new ExtractTextPlugin( 'styles.[chunkhash].css' ),
        new HtmlWebpackPlugin( {
            appMountId: 'app',
            mobile:     true,
            template:   PATHS.src + '/index.html',
            title:      pkg.title
        } ),
        new webpack.optimize.CommonsChunkPlugin( { names: CHUNKS } ),
        new webpack.optimize.UglifyJsPlugin( {
            compress: {
                warnings: false
            }
        } )
    ]
};

export default merge( CONFIG, BUILD_CONFIG );