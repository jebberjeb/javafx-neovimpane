# Neovim JavaFX

A collection of JavaFX components backed by [Neovim](https://neovim.io),
including:

* NeovimPane - A text input component that allows a user to enter multiple
lines of plain text.

All components work seamlessly with JavaFX, and can be manipulated with code,
or included in FXML. Each component is attached to an embedded Neovim process.
[neovim-client](https://github.com/clojure-vim/neovim-client) is used to
interact with the neovim process using Neovim's [RPC
API](https://neovim.io/doc/user/api.html#api).

[![IMAGE NEOVIM PANE](http://img.youtube.com/vi/cNucOrrblB0/0.jpg)](http://www.youtube.com/watch?v=cNucOrrblB0)

## Prerequisites

You'll need the Neovim binary `nvim` on your path to use any of the
components included.

This project uses [Clojure CLI and deps.edn](). Some of the sample code below
assumes you've got the `clj` binary on your path.

## Release and Dependency Information

### deps.edn

```cljoure
{:deps
    {github-jebberjeb/neovim-javafx
        {:git/url "https://github.com/jebberjeb/neovim-javafx" :sha "..."}}}
```

TODO publish to clojars

### Leiningen

```
TODO
```

### Maven

```
TODO
```

## Usage

### Clojure

```clojure
(ns neovim-javafx.test-ui
  "This namespace contains a simple UI to test NeovimPane."
  (:import
    (javafx.application Platform)
    (javafx.scene Scene)
    (javafx.stage Stage))
  (:require [neovim-javafx.util :refer [later]]))

(javafx.embed.swing.JFXPanel.)
(javafx.application.Platform/setImplicitExit false)
(later
  #(doto (Stage.)
    (.setScene (Scene. (neovim_javafx.NeovimPane.)))
    (.show)))
```

### JavaFX FXML

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?language javascript?>

<?import javafx.scene.layout.VBox?>
<?import neovim_javafx.NeovimPane?>

<VBox ...>
    <children>
        <NeovimPane .../>
    </children>
</VBox>

```

### Java

```java
package neovim_javafx;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.embed.swing.JFXPanel;

public class NeovimPaneTest {

    public static void main(String[] args) {

        new JFXPanel();
        Platform.setImplicitExit(false);

        Platform.runLater(
                new Runnable() {
                    public void run() {
                        try {
                            NeovimPane npane = new NeovimPane();
                            Scene scene = new Scene(npane);
                            Stage stage = new Stage();
                            stage.setScene(scene);
                            stage.show();
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                });
    }
}
```

## Samples

### Java

To build the Java sample:

```bash
$> mkdir classes
$> clj -e "(require '[neovim-javafx.dev :as dev]) (dev/generate-classes)"
```

To run the Java sample:

```bash
$> javac -cp $(clj -Spath -C:test) test/neovim_javafx/*.java -d classes
$> java -cp $(clj -Spath -C:test) neovim_javafx.NeovimPaneTest
```

### Clojure

To build and run the Clojure sample:

```bash
$> clj -C:test -e "(require '[neovim-javafx.test-ui :refer [ui]) (ui)"
```

## Copyright and License

The MIT License (MIT)

Copyright (c) 2018 Jeb Beich

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
