(ns boot-expo.core)

(enable-console-print!)


(defonce Expo (js/require "expo"))
(defonce React (js/require "react"))
(defonce ReactNative (js/require "react-native"))


(js/setCLJSRootElement (React.createElement
                        ReactNative.View
                        #js {:flex 1
                             :alignItems "center"
                             :justifyContent "center"
                             :backgroundColor "#a8a8e8"}
                        (React.createElement
                         ReactNative.Text
                         #js {:style #js {:color "white"
                                          :fontWeight "bold"}}
                         "welcome to the future :)")))
