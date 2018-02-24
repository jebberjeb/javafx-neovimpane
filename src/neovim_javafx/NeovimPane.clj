(ns neovim-javafx.NeovimPane
  (:import
    (javafx.scene.layout StackPane Pane)
    (javafx.scene.web WebView WebEngine)
    (javafx.scene.text Text Font))
  (:require
    [clojure.string :as string]
    [neovim-javafx.neovim-client :as neovim-client]
    [neovim-javafx.text-grid :as text-grid]
    [neovim-javafx.util :refer [later key-event-handler get-rows get-cols
                                char-width char-height size-changed-listener]])
  (:gen-class
    :state state
    :post-init post-init
    :init init
    :extends javafx.scene.layout.StackPane
    :methods [[getEngine [] javafx.scene.web.WebEngine]
              [getText [] String]
              [setText [String] void]
              [resetText [] void]]))

;; TODO this ia a pretty naive implementation, resulting in one span
;; per character, which is not efficient and won't scale to larger
;; documents.
(defn char-maps->html
  "Returns an html string."
  [char-maps cursor-pos]
  (string/join
    (map-indexed
      (fn [index {:keys [char highlight]}]
        (format "<span class=\"%s\">%s</span>"
                (cond (= index cursor-pos) "cursor"
                      (get highlight "reverse") "reverse"
                      :else "normal")
                char))
      char-maps)))

(defn render-text-as-html
  [{:keys [grid] :as state}]
  (let [grid (map (fn [row] (conj row {:char "\n" :highlight {}})) grid)
        char-maps (apply concat grid)
        cursor-pos (neovim-client/cursor-position-in-string state)
        ;; TODO generate this, from nvim colormap?
        styles "body {
                font-size: 14px;
                font-family: monospace;
                padding: 0px;
                margin: 0px;
                overflow: hidden;
                }
                span.cursor  { background:black; color:white; }
                span.reverse { background:gray;  color:white; }
                span.normal  { background:white; color:black; }"
        text (char-maps->html char-maps cursor-pos)]
    (format "<html><head><style>%s</style></head><body><pre contenteditable>%s</pre></body></html>"
            styles
            text)))

(defn update-webview
  [webview {:keys [grid cursor] :as state}]
  (later
    #(.loadContent
       (.getEngine webview)
       (render-text-as-html state))))

(defn attach-neovim
  "Modifies a WebView by attaching it to a Neovim instance."
  [^WebView webview conn]
  ;; TODO handle initial height & height changes. Maybe there's a better way
  ;; to do this by hooking in later in the lifecycle.
  (let [height (int (/ (.getPrefHeight webview) (char-height)))
        width (int (/ (.getPrefWidth webview) (char-width)))
        size-listener (size-changed-listener conn webview)]
    (neovim-client/attach-ui conn width height #(update-webview webview %))
    (-> webview .widthProperty (.addListener size-listener))
    (-> webview .heightProperty (.addListener size-listener))
    (.setOnKeyPressed webview (key-event-handler conn (fn [_] nil)))))

(defn -init
  [& args]
  [args (atom {:conn (neovim-client/get-nvim)})])

(defn -post-init
  [this & args]
  (let [webview (WebView.)]
    (.add (.getChildren this) webview)
    (attach-neovim webview (:conn @(.state this)))
    this))

(defn -getEngine
  [this]
  (.getEngine (first (.getChildren this))))

(defn -getText
  [this]
  (let [conn (:conn @(.state this))]
    (neovim-client/get-current-buffer-text conn)))

(defn -setText
  [this ^String code]
  (let [conn (:conn @(.state this))]
    (neovim-client/set-current-buffer-text conn code)))

(defn -resetText
  [this]
  (-setText this ""))

