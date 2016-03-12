(ns bidi-test.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [bidi-test.core-test]))

(enable-console-print!)

(doo-tests 'bidi-test.core-test)
