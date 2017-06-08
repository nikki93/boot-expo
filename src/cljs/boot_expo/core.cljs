(ns boot-expo.core
  (:require [goog.object :as gobj]))

(enable-console-print!)
(set! *warn-on-infer* true)

(defonce Expo (js/require "expo"))
(defonce React (js/require "react"))
(defonce ReactNative (js/require "react-native"))

(def createElement (gobj/get React "createElement"))
(def View (gobj/get ReactNative "View"))
(def Text (gobj/get ReactNative "Text"))

(js/setCLJSRootElement
 (createElement
  View
  #js {:flex 1
       :alignItems "center"
       :justifyContent "center"
       :backgroundColor "#a8a8e8"}
  (createElement
   Text
   #js {:style #js {:color "white"
                    :fontWeight "bold"}}
   "welcome to the future :)")))
