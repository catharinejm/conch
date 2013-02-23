(ns conch.core
  (:use [clojure.java.shell :only (sh with-sh-dir)]
        [clojure.java.io :only (file)]))

(defn valid? [cmd]
  (list? cmd))

(def ^:private -builtins-
  {:cd (fn [current-dir [new-dir]]
         (let [full-path (.getCanonicalFile
                          (if (= (first new-dir) \/)
                            (file new-dir)
                            (file current-dir new-dir)))]
           (if (and (.isDirectory full-path)
                    (.canExecute full-path))
             {:current-dir (str full-path) :exit 0}
             {:current-dir current-dir :exit 1 :err (str "Cannot change to \"" full-path "\"\n")})))
   })

(defn builtin? [cmd]
  (contains? -builtins- (keyword cmd)))

(defn exec-builtin! [current-dir cmd opts]
  (let [builtin-fn (-builtins- (keyword cmd))
        str-opts (map str opts)]
    (builtin-fn current-dir str-opts)))

(defn execute! [current-dir [cmd & opts :as sh-string]]
  (if (builtin? cmd)
    (exec-builtin! current-dir cmd opts)
    (let [sh-results (with-sh-dir current-dir (->> sh-string
                                                   (map str)
                                                   (apply sh)))]
      (assoc sh-results :current-dir current-dir))))


(defn run []
  (loop [{:keys [current-dir out err exit] :as process-map} {:current-dir (System/getProperty "user.dir")
                                                             :exit 0}]
    (print (str out))
    (print (str err))
    (print (str current-dir "$ "))
    (flush)
    (let [cmd (read)]
      (if (valid? cmd)
        (recur (execute! current-dir cmd))
        (recur (assoc process-map :err (str "Invalid statement: " cmd "\n") :exit 127))))))

(defn -main [&]
  (run))
