(ns conch.builtins
  (:use [clojure.java.shell :only (sh)]))

(defn ls [cwd args]
  (sh ))