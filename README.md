# lein-search

The search plugin allows you to download and query indices from remote
Maven repositories.

## Usage

    $ lein plugin install org.clojars.technomancy/lein-search 1.0.0-SNAPSHOT

    $ lein search TERM

    $ lein search --update # force indices to be refreshed

## Add

Previous versions of lein-search also supported adding dependencies
directly to project.clj. This functionality has been dropped and may
be spun off to a separate plugin in the future.

## License

Copyright © 2009-2011 Heinz N. Gies, Phil Hagelberg

This code is published under the EPL, have fun! See LICENSE.html for the booring stuff.

## Thanks

Big thanks to purcell@github for a bunch of improvements and cleanup in the code.
