import Expo from 'expo';
import React from 'react';
import { StyleSheet, Text, View } from 'react-native';

const packagerHost = Expo.Constants.manifest.bundleUrl
                         .match(/^https?:\/\/.*?\//)[0];

let lastLoadTargetAsync = Promise.resolve();
const loadTargetAsync = (path) => (
  lastLoadTargetAsync = Promise.all([
    fetch(`${packagerHost}target/main.out/${path}`)
      .then((response) => response.text()),
    lastLoadTargetAsync,
  ]).then(([text]) => (0, eval)(text))
);

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


class App extends React.Component {
  state = {
    initialized: false,
  }

  async componentDidMount() {
    await initCLJSDevAsync();
    this.setState({ initialized: true });
  }

  render() {
    return this.state.initialized ? (
      <View style={styles.container}>
        <Text>hello, world</Text>
      </View>
    ) : (
      <View style={{
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
      }}>
        <Text>initializing cljs dev setup...</Text>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
});

Expo.registerRootComponent(App);
