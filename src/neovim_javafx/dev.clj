(ns neovim-javafx.dev
  "This namespace contains dev utils.")

(defn generate-classes
  "Generate the bytecode (if we change NeovimPane's interface)."
  []
  ;; Initialize the JFX runtime, since loading these component namespaces to
  ;; compile them seems to need it.
  (javafx.embed.swing.JFXPanel.)
  (binding [*compile-path* "classes"]
    (compile 'neovim-javafx.NeovimPane)))

;; Test code
(comment

  (neovim-javafx.dev/generate-classes)

  ;; Launch the test UI, stash it in an atom so we can inspect it at the REPL.
  (do
    (require '[neovim-javafx.test-ui])
    (def x (atom nil))
    (neovim-javafx.test-ui/ui x)))
