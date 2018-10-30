function getStyleUse(bundleFilename) {
  return [
    {
      loader: 'file-loader',
      options: {
        name: bundleFilename,
      },
    },
    { loader: 'extract-loader' },
    { loader: 'css-loader' },
    {
      loader: 'sass-loader',
      options: {
        includePaths: ['./node_modules'],
      }
    },
  ];
}

module.exports = [
  {
    entry: './main.scss',
    output: {
      // This is necessary for webpack to compile, but we never reference this js file.
      filename: 'style-bundle-main.js',
    },
    module: {
      rules: [{
        test: /main.scss$/,
        use: getStyleUse('bundle-main.css')
      }]
    },
  },
  {
    entry: "./main.js",
    output: {
      filename: "bundle-main.js"
    },
    module: {
      loaders: [{
        test: /main.js$/,
        loader: 'babel-loader',
        query: {presets: ['env']}
      }]
    },
  },
];
