# room_graphs

Display usage of rooms at a community college in California

## Overview

This program lets you choose a building; it then shows you when each
room in the building is in use by day of week and hour of day (from 0700
to 2300). The data is in a file named `roster_data.cljs`; in the production
version the data comes from a server as JSON data.

You can see it in action [here][http://langintro.com/room_graphs].

## Setup

Most of the following scripts require [rlwrap](http://utopia.knoware.nl/~hlub/uck/rlwrap/) (on OS X installable via brew).

Build your project once in dev mode with the following script and then open `index.html` in your browser.

    ./scripts/build

To auto build your project in dev mode:

    ./scripts/watch

To start an auto-building Node REPL:

    ./scripts/repl

To get source map support in the Node REPL:

    lein npm install

To start a browser REPL:

1. Uncomment the following lines in src/room_graphs/core.cljs:
```clojure
;; (defonce conn
;;   (repl/connect "http://localhost:9000/repl"))
```
2. Run `./scripts/brepl`
3. Browse to `http://localhost:9000` (you should see `Hello world!` in the web console)
4. (back to step 3) you should now see the REPL prompt: `cljs.user=>`
5. You may now evaluate ClojureScript statements in the browser context.

For more info using the browser as a REPL environment, see
[this](https://github.com/clojure/clojurescript/wiki/The-REPL-and-Evaluation-Environments#browser-as-evaluation-environment).

Clean project specific out:

    lein clean

Build a single release artifact with the following script and then change the script
in `index.html` to include `release/room_graphs.js` instead of `out/room_graphs.js`

    ./scripts/release

## License

Copyright © 2016 J David Eisenberg

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
