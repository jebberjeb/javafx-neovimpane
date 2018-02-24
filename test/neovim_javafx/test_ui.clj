(ns neovim-javafx.test-ui
  "This namespace contains a simple UI to test NeovimPane."
  (:import
    (javafx.application Platform)
    (javafx.scene Scene)
    (javafx.stage Stage))
  (:require [neovim-javafx.util :refer [later]]))

(defn ui
  [result-atom]
  ;; Initialize the JFX runtime.
  (javafx.embed.swing.JFXPanel.)
  ;; Let us close & recreate the test UI without having to restart the REPL.
  (javafx.application.Platform/setImplicitExit false)
  (later
    #(try
       (let [npane (neovim_javafx.NeovimPane.)
             scene (Scene. npane)]

         (doto (Stage.)
           (.setScene scene)
           (.show))

         @(future
            (Thread/sleep 3000)
            (println "Setting text")
            (.setText npane "Foo Bar!"))

         ;; Stash the result (we're on another thread here) so that we can
         ;; inspect it from the REPL.
         (when result-atom (reset! result-atom npane)))
       (catch Throwable t
         (println t)))))
