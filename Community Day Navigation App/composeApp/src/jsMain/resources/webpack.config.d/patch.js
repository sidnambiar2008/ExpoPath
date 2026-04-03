// This provides the browser-based versions of Node.js modules
config.resolve.fallback = {
    "zlib": require.resolve("browserify-zlib"),
    "stream": require.resolve("stream-browserify"),
    "buffer": require.resolve("buffer/"),
    "util": require.resolve("util/"),
    "url": require.resolve("url/"),
    "assert": require.resolve("assert/"),
    "crypto": require.resolve("crypto-browserify"),
    "http": require.resolve("stream-http"),
    "https": require.resolve("https-browserify"),
    "os": require.resolve("os-browserify/browser"),
    "path": require.resolve("path-browserify"),
    "process": require.resolve("process/browser")
};