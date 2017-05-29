import Expo from 'expo';
import React from 'react';
import { StyleSheet, Text, View } from 'react-native';


// Runtime string -> module map for `(js/require ...)`

const moduleMap = {
  'expo': require('expo'),
  'react': require('react'),
  'react-native': require('react-native'),
};
const oldRequire = require;
global.require = (m) => moduleMap[m] || oldRequire(m);


// Remote JavaScript loader for `goog.require(...)` for development

const packagerHost = Expo.Constants.manifest.bundleUrl
                         .match(/^https?:\/\/.*?\//)[0];

let lastLoadTargetAsync = Promise.resolve();
const loadTargetAsync = (path) => (
  lastLoadTargetAsync = Promise.all([
    fetch(`${packagerHost}target/main.out/${path}`)
      .then((response) => response.text()),
    lastLoadTargetAsync,
  ]).then(([text]) => {
    (0, eval)(text);

    // Shim `goog.net.jsloader` to use our loader
    if (path === 'goog/net/jsloader.js') {
      goog.net.jsloader.safeLoad = (path) => {
        const deferred = new goog.async.Deferred();
        (async () => {
          try {
            const unwrapped = goog.html.TrustedResourceUrl.unwrap(path)
                                  .path_;
            // These paths happen to be relative to `target/`
            await loadTargetAsync(`../${unwrapped}`);
            deferred.callback();
          } catch (e) {
            deferred.errback();
          }
        })();
        return deferred;
      }
    }
  })
);


// Load and enter CLJS entrypoint for development

const initCLJSDevAsync = async () => {
  // Load and configure Google Closure Library for Expo context
  await loadTargetAsync('goog/base.js');
  goog.basePath = 'goog/';
  goog.global.CLOSURE_IMPORT_SCRIPT = (src) =>
    (loadTargetAsync(src), true);

  // Normal CLJS startup process
  await loadTargetAsync('cljs_deps.js');
  await loadTargetAsync('goog/deps.js');
  await loadTargetAsync('../main.js');
}


// Displays component set by `(js/setCLJSRootElement ...)`

class App extends React.Component {
  state = {
    cljsRoot: null,
  }

  componentDidMount() {
    global.setCLJSRootElement = (cljsRoot) =>
      this.setState({ cljsRoot });
    initCLJSDevAsync();
  }

  render() {
    return this.state.cljsRoot || (
      <View style={{
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
      }}>
        <Text>waiting for cljs root element...</Text>
      </View>
    );
  }
}

Expo.registerRootComponent(App);
